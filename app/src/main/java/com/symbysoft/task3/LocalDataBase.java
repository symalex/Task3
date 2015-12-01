package com.symbysoft.task3;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

// http://startandroid.ru/ru/uroki/vse-uroki-spiskom/112-urok-53-simplecursortreeadapter-primer-ispolzovanija.html

public class LocalDataBase implements LocalDataBaseNotification
{
	// Database Name
	public static final String DATABASE_NAME = "history.db";

	// Database Version
	public static final int DATABASE_VERSION = 1;

	private Context mCtx;
	private DatabaseHelper mDBHelper;
	private SQLiteDatabase mDB;

	private LocalDataBaseTask mReadHistoryTask;
	private LocalDataBaseTask mAddHistoryTask;

	private LocalDataBase(Context ctx)
	{
		mCtx = ctx;
	}

	public static LocalDataBase newInstance(Context ctx)
	{
		return new LocalDataBase(ctx);
	}

	public void open()
	{
		mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
		mDB = mDBHelper.getWritableDatabase();
	}

	public void close()
	{
		if (mDBHelper != null)
			mDBHelper.close();
	}

	private void CancelTask(LocalDataBaseTask task)
	{
		if (task != null)
		{
			task.cancel(true);
		}
	}

	public void addToHistory(String direction, String src_text, String dest_text)
	{
		CancelTask(mAddHistoryTask);
		mAddHistoryTask = new LocalDataBaseTask(mDBHelper);
		mAddHistoryTask.setDBNotification(this);
		mAddHistoryTask.addToHistory(direction, src_text, dest_text);
	}

	public void loadHistory()
	{
		CancelTask(mReadHistoryTask);
		mReadHistoryTask = new LocalDataBaseTask(mDBHelper);
		mReadHistoryTask.setDBNotification(this);
		mReadHistoryTask.getHistoryData();
	}

	@Override
	public void onDBReadHistoryComplette(LocalDataBaseTask task, List<String> list)
	{
		list.clear();
	}
}
