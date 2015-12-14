package com.symbysoft.task3.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

public class LocalDataBaseTask extends AsyncTask<Void, Void, Object>
{
	private static final String TAG = "LocalDataBaseTask";

	public interface LocalDataBaseListener
	{
		void onDBReadHistoryComplete(LocalDataBaseTask task, List<HistoryRow> list);

		void onDBReadFavoriteComplete(LocalDataBaseTask task, List<FavoriteRow> list);

		void onDBAddHistoryComplete(LocalDataBaseTask task, List<ContentValues> list);

		void onDBDelHistoryComplete(LocalDataBaseTask task, List<ContentValues> list);

		void onDBAddFavoriteComplete(LocalDataBaseTask task, List<ContentValues> list);

		void onDBDelFavoriteComplete(LocalDataBaseTask task, List<ContentValues> list);
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
	private final DatabaseHelper mDBHelper;
	private final DataBaseHelper mDbHelper;
	private final ContentValues mValues;

	private LocalDataBaseListener mListener;

	public LocalDataBaseAction getAction()
	{
		return mAction;
	}

	public LocalDataBaseListener getListener()
	{
		return mListener;
	}

	public void setListener(LocalDataBaseListener listener)
	{
		mListener = listener;
	}

	LocalDataBaseTask(DatabaseHelper db_helper, DataBaseHelper helper)
	{
		mDBHelper = db_helper;
		mDbHelper = helper;
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
	protected Object doInBackground(Void... params)
	{
		Object ret = null;
		List<ContentValues> list = new ArrayList<>();
		switch (mAction)
		{
			case DB_ACTION_READ_FAVORITE_DATA:
				list = mDBHelper.getFavoriteData();
				try
				{
					/*
					List<HistoryRow> hr = mDbHelper.getHistoryDAO().getAll(mDbHelper.getFavoriteDAO().queryBuilder(), mDbHelper.getFavoriteDAO().getTableInfo());
					FavoriteRow r = new FavoriteRow();
					r.setHistory(hr.get(2));
					mDbHelper.getFavoriteDAO().create(r);*/
					List<FavoriteRow> rows = mDbHelper.getFavoriteDAO().getAll();
					for (FavoriteRow row : rows)
					{
						Log.d(TAG, row.toString());
					}
					ret = rows;
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				break;

			case DB_ACTION_READ_HISTORY_DATA:
				ret = list = mDBHelper.getHistoryData();
				try
				{
					/*
					HistoryRow r = new HistoryRow();
					r.setDirection("en-ru");
					r.setSource("source");
					r.setDestination("destination");
					r.now();
					mDbHelper.getHistoryDAO().create(r);*/
					List<HistoryRow> rows = mDbHelper.getHistoryDAO().getAll();
					for (HistoryRow row : rows)
					{
						Log.d(TAG, row.toString());
					}
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				break;

			case DB_ACTION_ADD_HISTORY:
				ret = list;
				list.add(mDBHelper.addToHistory((String) mValues.get(DatabaseHelper.DIRECTION), (String) mValues.get(DatabaseHelper.HIST_SOURCE), (String) mValues.get(DatabaseHelper.HIST_DEST)));
				break;

			case DB_ACTION_DEL_HISTORY:
				ret = list;
				list.add(mDBHelper.delFromHistory(mValues.getAsLong(DatabaseHelper.KEY_ID)));
				break;

			case DB_ACTION_ADD_FAVORITE:
				ret = list;
				list.add(mDBHelper.addToFavorite(mValues.getAsLong(DatabaseHelper.HIST_ID)));
				break;

			case DB_ACTION_DEL_FAVORITE:
				ret = list;
				list.add(mDBHelper.delFromFavorite(mValues.getAsLong(DatabaseHelper.KEY_ID)));
				break;

		}
		return ret;
	}

	@Override
	protected void onPostExecute(Object obj)
	{
		super.onPostExecute(obj);
		List<ContentValues> list;
		switch (mAction)
		{
			case DB_ACTION_READ_FAVORITE_DATA:
				if (mListener != null)
				{
					mListener.onDBReadFavoriteComplete(this, (List<FavoriteRow>) obj);
				}
				break;

			case DB_ACTION_READ_HISTORY_DATA:
				if (mListener != null)
				{
					mListener.onDBReadHistoryComplete(this, (List<HistoryRow>) obj);
				}
				break;

			case DB_ACTION_ADD_HISTORY:
				if (mListener != null)
				{
					list = (List<ContentValues>) obj;
					mListener.onDBAddHistoryComplete(this, list);
				}
				break;

			case DB_ACTION_DEL_HISTORY:
				if (mListener != null)
				{
					list = (List<ContentValues>) obj;
					mListener.onDBDelHistoryComplete(this, list);
				}
				break;

			case DB_ACTION_ADD_FAVORITE:
				if (mListener != null)
				{
					list = (List<ContentValues>) obj;
					mListener.onDBAddFavoriteComplete(this, list);
				}
				break;

			case DB_ACTION_DEL_FAVORITE:
				if (mListener != null)
				{
					list = (List<ContentValues>) obj;
					mListener.onDBDelFavoriteComplete(this, list);
				}
				break;

		}

	}

}
