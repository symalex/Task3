package com.symbysoft.task3;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.os.AsyncTask;

public class LocalDataBaseTask extends AsyncTask<Void, Void, List<ContentValues>>
{
	private static final String TAG = "LocalDataBaseTask";

	public interface LocalDataBaseNotification
	{
		void onDBReadHistoryComplette(LocalDataBaseTask task, List<ContentValues> list);

		void onDBReadFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list);

		void onDBAddHistoryComplette(LocalDataBaseTask task, List<ContentValues> list);

		void onDBDelHistoryComplette(LocalDataBaseTask task, List<ContentValues> list);

		void onDBAddFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list);

		void onDBDelFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list);
	}

	public enum LocalDataBaseAction
	{
		DB_ACTION_NONE,
		DB_ACTION_READ_FAVORITE_DATA,
		DB_ACTION_WRITE_FAVORITE_DATA,
		DB_ACTION_READ_HISTORY_DATA,
		DB_ACTION_WRITE_HISTORY_DATA,
		DB_ACTION_ADD_HISTORY,
		DB_ACTION_DEL_HISTORY,
		DB_ACTION_ADD_FAVORITE,
		DB_ACTION_DEL_FAVORITE
	}

	private LocalDataBaseAction mAction = LocalDataBaseAction.DB_ACTION_NONE;
	private final DatabaseHelper mDbHelper;
	private final ContentValues mValues;

	private LocalDataBaseNotification mDBNotification;

	public LocalDataBaseAction getAction()
	{
		return mAction;
	}

	public LocalDataBaseNotification getDBNotification()
	{
		return mDBNotification;
	}

	public void setDBNotification(LocalDataBaseNotification DBNotification)
	{
		mDBNotification = DBNotification;
	}

	LocalDataBaseTask(DatabaseHelper db_helper)
	{
		mDbHelper = db_helper;
		mValues = new ContentValues();
	}

	public void getFavoriteData()
	{
		mAction = LocalDataBaseAction.DB_ACTION_READ_FAVORITE_DATA;
		execute();
	}

	public void getHistoryData()
	{
		mAction = LocalDataBaseAction.DB_ACTION_READ_HISTORY_DATA;
		execute();
	}

	public void addToHistory(String direction, String src_text, String dest_text)
	{
		mAction = LocalDataBaseAction.DB_ACTION_ADD_HISTORY;
		mValues.clear();
		mValues.put(DatabaseHelper.DIRECTION, direction);
		mValues.put(DatabaseHelper.HIST_SOURCE, src_text);
		mValues.put(DatabaseHelper.HIST_DEST, dest_text);
		execute();
	}

	public void delFromHistory(long id)
	{
		mAction = LocalDataBaseAction.DB_ACTION_DEL_HISTORY;
		mValues.clear();
		mValues.put(DatabaseHelper.KEY_ID, id);
		execute();
	}

	public void addToFavorite(long hist_id)
	{
		mAction = LocalDataBaseAction.DB_ACTION_ADD_FAVORITE;
		mValues.clear();
		mValues.put(DatabaseHelper.HIST_ID, hist_id);
		execute();
	}

	public void delFromFavorite(long id)
	{
		mAction = LocalDataBaseAction.DB_ACTION_DEL_FAVORITE;
		mValues.clear();
		mValues.put(DatabaseHelper.KEY_ID, id);
		execute();
	}

	@Override
	protected List<ContentValues> doInBackground(Void... params)
	{
		List<ContentValues> list = new ArrayList<>();
		switch (mAction)
		{
			case DB_ACTION_READ_FAVORITE_DATA:
				list = mDbHelper.getFavoriteData();
				break;

			case DB_ACTION_READ_HISTORY_DATA:
				list = mDbHelper.getHistoryData();
				break;

			case DB_ACTION_ADD_HISTORY:
				list.add(mDbHelper.addToHistory((String) mValues.get(DatabaseHelper.DIRECTION), (String) mValues.get(DatabaseHelper.HIST_SOURCE), (String) mValues.get(DatabaseHelper.HIST_DEST)));
				break;

			case DB_ACTION_DEL_HISTORY:
				list.add(mDbHelper.delFromHistory(mValues.getAsLong(DatabaseHelper.KEY_ID)));
				break;

			case DB_ACTION_ADD_FAVORITE:
				list.add(mDbHelper.addToFavorite(mValues.getAsLong(DatabaseHelper.HIST_ID)));
				break;

			case DB_ACTION_DEL_FAVORITE:
				list.add(mDbHelper.delFromFavorite(mValues.getAsLong(DatabaseHelper.KEY_ID)));
				break;

		}
		return list;
	}

	@Override
	protected void onPostExecute(List<ContentValues> list)
	{
		super.onPostExecute(list);

		switch (mAction)
		{
			case DB_ACTION_READ_FAVORITE_DATA:
				if (mDBNotification != null)
				{
					mDBNotification.onDBReadFavoriteComplette(this, list);
				}
				break;

			case DB_ACTION_READ_HISTORY_DATA:
				if (mDBNotification != null)
				{
					mDBNotification.onDBReadHistoryComplette(this, list);
				}
				break;

			case DB_ACTION_ADD_HISTORY:
				if (mDBNotification != null)
				{
					mDBNotification.onDBAddHistoryComplette(this, list);
				}
				break;

			case DB_ACTION_DEL_HISTORY:
				if (mDBNotification != null)
				{
					mDBNotification.onDBDelHistoryComplette(this, list);
				}
				break;

			case DB_ACTION_ADD_FAVORITE:
				if (mDBNotification != null)
				{
					mDBNotification.onDBAddFavoriteComplette(this, list);
				}
				break;

			case DB_ACTION_DEL_FAVORITE:
				if (mDBNotification != null)
				{
					mDBNotification.onDBDelFavoriteComplette(this, list);
				}
				break;

		}

	}

}
