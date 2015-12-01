package com.symbysoft.task3;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class LocalDataBaseTask extends AsyncTask<Void, Void, List<String>>
{
	private static final String TAG = "LocalDataBaseTask";

	public enum LocalDataBaseAction
	{
		DB_ACTION_NONE,
		DB_ACTION_READ_HISTORY_DATA,
		DB_ACTION_WRITE_HISTORY_DATA,
		DB_ACTION_ADD_HISTORY,
		DB_ACTION_DEL_HISTORY
	}

	private LocalDataBaseAction mAction = LocalDataBaseAction.DB_ACTION_NONE;
	private DatabaseHelper mDbHelper;
	private String mDirection;
	private String mSrcText;
	private String mDestText;

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
	}

	public void getHistoryData()
	{
		mAction = LocalDataBaseAction.DB_ACTION_READ_HISTORY_DATA;
		execute();
	}

	public void addToHistory(String direction, String src_text, String dest_text)
	{
		mAction = LocalDataBaseAction.DB_ACTION_ADD_HISTORY;
		mDirection = direction;
		mSrcText = src_text;
		mDestText = dest_text;
		execute();
	}

	@Override
	protected List<String> doInBackground(Void... params)
	{
		List<String> list = new ArrayList<>();
		switch (mAction)
		{
			case DB_ACTION_READ_HISTORY_DATA:
				list = mDbHelper.getHistoryData();
				break;

			case DB_ACTION_ADD_HISTORY:
				mDbHelper.addToHistory(mDirection, mSrcText, mDestText);
				break;

		}
		return list;
	}

	@Override
	protected void onPostExecute(List<String> list)
	{
		super.onPostExecute(list);

		switch (mAction)
		{
			case DB_ACTION_READ_HISTORY_DATA:
				if (mDBNotification != null)
				{
					mDBNotification.onDBReadHistoryComplette(this, list);
				}
				break;

			case DB_ACTION_ADD_HISTORY:
				break;

		}

	}

}
