package com.symbysoft.task3;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;

public class DataProvider implements LocalDataBaseNotification
{
	private MainActivity.FragmentPage mActivePage = MainActivity.FragmentPage.MAIN_FRAGMENT;
	private Context mCtx;
	private boolean mReceiverRegistered = false;
	private boolean mDataLoaded = false;
	private int mProgress = 0;
	private int mProgressMax = 5;
	private SettingsProvider mSettings;
	private InternetReceiver mInternetReceiver;
	private YandexTranslateAPI mTranslateAPI;
	private LocalDataBase mLocalDataBase;
	private DataProviderNotification mDataProviderNotification;

	ArrayList<ContentValues> mHistoryList;
	ArrayList<ContentValues> mFavoriteList;

	public ArrayList<ContentValues> getHistoryList()
	{
		return mHistoryList;
	}

	public ArrayList<ContentValues> getFavoriteList()
	{
		return mFavoriteList;
	}

	public SettingsProvider getSettings()
	{
		return mSettings;
	}

	public InternetReceiver getInternetReceiver()
	{
		return mInternetReceiver;
	}

	public YandexTranslateAPI getTranslateAPI()
	{
		return mTranslateAPI;
	}

	public DataProviderNotification getDataProviderNotification()
	{
		return mDataProviderNotification;
	}

	public void setDataProviderNotification(DataProviderNotification dataProviderNotification)
	{
		mDataProviderNotification = dataProviderNotification;
	}

	public MainActivity.FragmentPage getActivePage()
	{
		return mActivePage;
	}

	public void setActivePage(MainActivity.FragmentPage activePage)
	{
		mActivePage = activePage;
	}

	public LocalDataBase getLocalDataBase()
	{
		return mLocalDataBase;
	}

	private DataProvider(Context ctx)
	{
		mCtx = ctx;
		mHistoryList = new ArrayList<>();
		mFavoriteList = new ArrayList<>();
	}

	static DataProvider getInstance(Context ctx)
	{
		DataProvider data = new DataProvider(ctx);
		data.onCreate();
		return data;
	}

	protected void onCreate()
	{
		// application settings
		mSettings = SettingsProvider.getInstance(mCtx);

		// handling internet connection state
		mInternetReceiver = new InternetReceiver();
		mInternetReceiver.setContext(mCtx);
		registerInternetReceiver();
		mInternetReceiver.updateConnectionState();

		// translate API
		mTranslateAPI = YandexTranslateAPI.newInstance();
		mLocalDataBase = LocalDataBase.newInstance(mCtx);
	}

	protected void registerInternetReceiver()
	{
		if (!mReceiverRegistered && mCtx != null && mInternetReceiver != null)
		{
			IntentFilter filter = new IntentFilter(InternetReceiver.FILTER_ACTION);
			mCtx.registerReceiver(mInternetReceiver, filter);
			mReceiverRegistered = true;
		}
	}

	protected void unregisterInternetReceiver()
	{
		if (mReceiverRegistered && mCtx != null && mInternetReceiver != null)
		{
			mCtx.unregisterReceiver(mInternetReceiver);
			mReceiverRegistered = false;
		}
	}

	void onDestroy()
	{
		if (mTranslateAPI != null)
		{
			mTranslateAPI.onDestroy();
		}

		if (mLocalDataBase != null)
		{
			mLocalDataBase.close();
		}
	}

	public boolean isDataLoaded()
	{
		return mDataLoaded;
	}

	public int getProgress()
	{
		int v = mProgress + mLocalDataBase.getDBHelper().getProgress();
		if (v >= mProgressMax)
		{
			mDataLoaded = true;
		}
		return v;
	}

	public int getProgressMax()
	{
		return mProgressMax;
	}

	public void loadData()
	{
		mProgress = 0;
		mSettings.ReadSettings();
		mTranslateAPI.setApiKey(mSettings.getTranslateAPIData().getApiKey());
		mTranslateAPI.setTranslateDirection(mSettings.getTranslateAPIData().getTranslateDirection());

		if (mDataProviderNotification != null)
		{
			mDataProviderNotification.onLoadDataComplette();
		}
	}

	public void saveData()
	{
		mSettings.WriteSettings();
	}

	public void openDatabase()
	{
		mLocalDataBase.open();
		mProgressMax = 5 + mLocalDataBase.getDBHelper().getHistoryRecordCount() + mLocalDataBase.getDBHelper().getFavoriteRecordCount();
		mProgress += 1;
	}

	public void downloadApiData()
	{
		if (mInternetReceiver != null && mInternetReceiver.isConnectionOk())
		{
			mProgress += 1;
			mTranslateAPI.update();
			//mTranslateAPI.detectLang("yandex testing text");
			//mTranslateAPI.translate("yandex testing text");
		}
	}

	public void readFavoriteAndHistoryData()
	{
		mProgress += 1;
		mLocalDataBase.getFavoriteAndHistoryData();
	}

	public void readHistoryData()
	{
		mLocalDataBase.readHistory();
		//mLocalDataBase.addToHistory(mSettings.getTranslateAPIData().getTranslateDirection(),"test source", "test result");
	}

	public void readFavoriteData()
	{
		mLocalDataBase.readFavorite();
	}

	@Override
	public void onDBReadFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		mProgress += 1;
		mFavoriteList = (ArrayList<ContentValues>) ((ArrayList<ContentValues>) list).clone();
	}

	@Override
	public void onDBAddHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		ContentValues cv = list.size() > 0 ? list.get(0) : new ContentValues();
		mHistoryList.add(cv);
	}

	@Override
	public void onDBDelHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		long id = -1;
		if (list.size() > 0)
		{
			id = list.get(0).getAsLong(DatabaseHelper.KEY_ID);
		}
		int index = 0;
		if (id > 0)
		{
			for (ContentValues cv : mHistoryList)
			{
				if (cv.getAsLong(DatabaseHelper.KEY_ID) == id)
				{
					break;
				}
				index++;
			}
		}

		if (index >= 0 && index < mHistoryList.size())
		{
			mHistoryList.remove(index);
		}
	}

	@Override
	public void onDBAddFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{

	}

	@Override
	public void onDBDelFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{

	}

	@Override
	public void onDBReadHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		mProgress += 1;
		mHistoryList = (ArrayList<ContentValues>) ((ArrayList<ContentValues>) list).clone();
	}

}
