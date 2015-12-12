package com.symbysoft.task3.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;

import com.symbysoft.task3.ui.activities.MainActivity;
import com.symbysoft.task3.network.InternetReceiver;
import com.symbysoft.task3.network.YandexTranslateAPI;

public class DataProvider implements LocalDataBaseTask.LocalDataBaseNotification
{
	private MainActivity.FragmentPage mActivePage = MainActivity.FragmentPage.MAIN_FRAGMENT;
	private final Context mCtx;
	private boolean mReceiverRegistered = false;
	private boolean mDataLoaded = false;
	private int mProgress = 0;
	private int mProgressMax = 5;
	private SettingsProvider mSettings;
	private InternetReceiver mInternetReceiver;
	private YandexTranslateAPI mTranslateAPI;
	private LocalDataBase mLocalDataBase;
	private final LinkedHashSet<DataProviderListener> mListeners;

	public interface DataProviderListener
	{
		void onLoadDataComplette();
	}

	private ArrayList<ContentValues> mHistoryList;
	private ArrayList<ContentValues> mFavoriteList;

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

	public void addDataProviderNotification(DataProviderListener listener)
	{
		mListeners.add(listener);
	}

	public void removeDataProviderNotification(DataProviderListener listener)
	{
		mListeners.remove(listener);
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
		mListeners = new LinkedHashSet<>();
		mHistoryList = new ArrayList<>();
		mFavoriteList = new ArrayList<>();
	}

	public static DataProvider getInstance(Context ctx)
	{
		DataProvider data = new DataProvider(ctx);
		data.onCreate();
		return data;
	}

	private void onCreate()
	{
		// application settings
		mSettings = SettingsProvider.getInstance(mCtx);

		// handling internet connection state
		mInternetReceiver = new InternetReceiver();
		mInternetReceiver.setContext(mCtx);
		registerInternetReceiver();
		mInternetReceiver.updateConnectionState();

		// translate API
		mTranslateAPI = YandexTranslateAPI.newInstance(mCtx);
		mLocalDataBase = LocalDataBase.newInstance(mCtx);
		mLocalDataBase.addDBNotification(this);
	}

	private void registerInternetReceiver()
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

	public void onDestroy()
	{
		if (mTranslateAPI != null)
		{
			mTranslateAPI.onDestroy();
		}

		if (mLocalDataBase != null)
		{
			mLocalDataBase.removeDBNotification(this);
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

		for (DataProviderListener listener : mListeners)
		{
			if (listener != null)
			{
				listener.onLoadDataComplette();
			}
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

	private int findElement(ArrayList<ContentValues> result, List<ContentValues> list, String key, boolean is_long)
	{
		int index;
		if (list.size() > 0)
		{
			long id = -1;
			String str = "";
			if (is_long)
			{
				id = list.get(0).getAsLong(key);
			}
			else
			{
				str = list.get(0).getAsString(key);
			}

			index = 0;
			for (ContentValues cv : result)
			{
				if (is_long)
				{
					if (cv.getAsLong(key) == id)
					{
						break;
					}
				}
				else
				{
					if (str.equals(cv.getAsString(key)))
					{
						break;
					}
				}
				index++;
			}
			if (index >= result.size())
			{
				index = -1;
			}
		}
		else
		{
			index = -1;
		}
		return index;
	}

	private void addIfNotFound(ArrayList<ContentValues> result, List<ContentValues> list, String key)
	{
		if (findElement(result, list, key, false) == -1)
		{
			result.add(list.get(0));
		}
	}

	private void removeIfFound(ArrayList<ContentValues> result, List<ContentValues> list, String key)
	{
		int index = findElement(result, list, key, true);
		if (index >= 0)
		{
			result.remove(index);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onDBReadFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		mProgress += 1;
		mFavoriteList = (ArrayList<ContentValues>) ((ArrayList<ContentValues>) list).clone();
	}

	@Override
	public void onDBAddHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		addIfNotFound(mHistoryList, list, DatabaseHelper.HIST_SOURCE);
	}

	@Override
	public void onDBDelHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		removeIfFound(mHistoryList, list, DatabaseHelper.KEY_ID);
	}

	@Override
	public void onDBAddFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		addIfNotFound(mFavoriteList, list, DatabaseHelper.HIST_SOURCE);
	}

	@Override
	public void onDBDelFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		removeIfFound(mFavoriteList, list, DatabaseHelper.KEY_ID);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onDBReadHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		mProgress = mProgressMax; //+=1+mLocalDataBase.getDBHelper().getProgress()
		mHistoryList = (ArrayList<ContentValues>) ((ArrayList<ContentValues>) list).clone();
	}

}
