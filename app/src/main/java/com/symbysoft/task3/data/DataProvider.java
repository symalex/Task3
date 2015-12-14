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
import com.symbysoft.task3.data.LocalDataBaseTask.LocalDataBaseListener;

public class DataProvider implements LocalDataBaseListener
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
	private int mHistorySelectedItemPosition = -1;
	private int mFavoriteSelectedItemPosition = -1;
	private boolean mForceTextTranslateFlag = false;

	public interface DataProviderListener
	{
		void onLoadDataComplete();
	}

	private ArrayList<HistoryRow> mHistoryList;
	private ArrayList<FavoriteRow> mFavoriteList;

	public ArrayList<HistoryRow> getHistoryList()
	{
		return mHistoryList;
	}

	public ArrayList<FavoriteRow> getFavoriteList()
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

	public void addDataProviderListener(DataProviderListener listener)
	{
		mListeners.add(listener);
	}

	public void removeDataProviderListener(DataProviderListener listener)
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

	public int getHistorySelectedItemPosition()
	{
		return mHistorySelectedItemPosition;
	}

	public void setHistorySelectedItemPosition(int historySelectedItemPosition)
	{
		mHistorySelectedItemPosition = historySelectedItemPosition;
	}

	public int getFavoriteSelectedItemPosition()
	{
		return mFavoriteSelectedItemPosition;
	}

	public void setFavoriteSelectedItemPosition(int favoriteSelectedItemPosition)
	{
		mFavoriteSelectedItemPosition = favoriteSelectedItemPosition;
	}

	public boolean isForceTextTranslateFlag()
	{
		return mForceTextTranslateFlag;
	}

	public void setForceTextTranslateFlag(boolean forceTextTranslateFlag)
	{
		mForceTextTranslateFlag = forceTextTranslateFlag;
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
		mLocalDataBase.addListener(this);
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
			mLocalDataBase.removeListener(this);
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
				listener.onLoadDataComplete();
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

	public int findElement(ArrayList<ContentValues> result, String key, long value)
	{
		int index;
		if (result.size() > 0)
		{
			index = 0;
			for (ContentValues cv : result)
			{
				if (cv.getAsLong(key) == value)
				{
					break;
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

	public FavoriteRow findById(ArrayList<FavoriteRow> rows, long id)
	{
		FavoriteRow res = null;
		for (FavoriteRow row : rows)
		{
			if (row.getId() == id)
			{
				res = row;
				break;
			}
		}
		return res;
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
		if (index != -1)
		{
			result.remove(index);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onDBReadFavoriteComplete(LocalDataBaseTask task, List<FavoriteRow> list)
	{
		mProgress += 1;
		mFavoriteList = (ArrayList<FavoriteRow>) ((ArrayList<FavoriteRow>) list).clone();
	}

	@Override
	public void onDBAddHistoryComplete(LocalDataBaseTask task, HistoryRow row)
	{
		//addIfNotFound(mHistoryList, list, DatabaseHelper.HIST_SOURCE);
	}

	@Override
	public void onDBDelHistoryComplete(LocalDataBaseTask task, int result)
	{
		//if (list.size() > 0)
		//{
			/*
			long in_fav_id = 0;
			int index = findElement(mHistoryList, list, DatabaseHelper.KEY_ID, true);
			if (index != -1)
			{
				in_fav_id = mHistoryList.get(index).getAsLong(DatabaseHelper.IN_FAVORITE_ID);

				//removeIfFound(mHistoryList, list, DatabaseHelper.KEY_ID);
				mHistoryList.remove(index);
			}

			// delete related favorite id
			FavoriteRow row = findById(mFavoriteList, in_fav_id);
			mFavoriteList.remove(row);*/
		//}
	}

	@Override
	public void onDBAddFavoriteComplete(LocalDataBaseTask task, FavoriteRow row)
	{
		//if (list.size() > 0)
		//{
			/*
			addIfNotFound(mFavoriteList, list, DatabaseHelper.HIST_SOURCE);
			ContentValues cv = list.get(0);
			long in_fav_id = cv.getAsLong(DatabaseHelper.KEY_ID);
			int i = findElement(mHistoryList, DatabaseHelper.KEY_ID, cv.getAsLong(DatabaseHelper.HIST_ID));
			if (i != -1)
			{
				cv = mHistoryList.get(i);
				cv.put(DatabaseHelper.IN_FAVORITE_ID, in_fav_id);
			}*/
		//}
	}

	@Override
	public void onDBDelFavoriteComplete(LocalDataBaseTask task, int result)
	{
		//if (list.size() > 0)
		//{
			/*
			removeIfFound(mFavoriteList, list, DatabaseHelper.KEY_ID);

			//[IN_FAVORITE_ID] = 0
			int i = findElement(mHistoryList, DatabaseHelper.IN_FAVORITE_ID, list.get(0).getAsLong(DatabaseHelper.KEY_ID));
			if (i != -1)
			{
				mHistoryList.get(i).put(DatabaseHelper.IN_FAVORITE_ID, 0);
			}*/
		//}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onDBReadHistoryComplete(LocalDataBaseTask task, List<HistoryRow> list)
	{
		mProgress = mProgressMax; //+=1+mLocalDataBase.getDBHelper().getProgress()
		mHistoryList = (ArrayList<HistoryRow>) ((ArrayList<HistoryRow>) list).clone();
	}

}
