package com.symbysoft.task3.data;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;

// http://startandroid.ru/ru/uroki/vse-uroki-spiskom/112-urok-53-simplecursortreeadapter-primer-ispolzovanija.html

public class LocalDataBase implements LocalDataBaseTask.LocalDataBaseNotification
{
	// Database Name
	private static final String DATABASE_NAME = "history.db";

	// Database Version
	private static final int DATABASE_VERSION = 1;

	private static final String ASYNC_ACTION = "action";
	/*
	public static final String ASYNC_FIELD_DIRECTION = "dir";
	public static final String ASYNC_FIELD_SOURCE_TEXT = "src";
	public static final String ASYNC_FIELD_DESTINATION_TEXT = "dest";*/

	private enum RuningAction
	{
		RA_READ_FAVORITE,
		RA_READ_HISTORY,
		RA_ADD_HISTORY,
		RA_DEL_HISTORY,
		RA_ADD_FAVORITE,
		RA_DEL_FAVORITE
	}

	private final Context mCtx;
	private DatabaseHelper mDBHelper;
	private final LinkedHashSet<LocalDataBaseTask.LocalDataBaseNotification> mDBNotifications;

	private LocalDataBaseTask mTask;
	private final LinkedList<ContentValues> mActions;

	public DatabaseHelper getDBHelper()
	{
		return mDBHelper;
	}

	public boolean isDBNotificationSet(LocalDataBaseTask.LocalDataBaseNotification DBNotification)
	{
		return mDBNotifications.contains(DBNotification);
	}

	public void addDBNotification(LocalDataBaseTask.LocalDataBaseNotification DBNotification)
	{
		mDBNotifications.add(DBNotification);
	}

	public void removeDBNotification(LocalDataBaseTask.LocalDataBaseNotification DBNotification)
	{
		mDBNotifications.remove(DBNotification);
	}

	private LocalDataBase(Context ctx)
	{
		mCtx = ctx;
		mActions = new LinkedList<>();
		mDBNotifications = new LinkedHashSet<>();
	}

	public static LocalDataBase newInstance(Context ctx)
	{
		return new LocalDataBase(ctx);
	}

	public void open()
	{
		mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void close()
	{
		if (mDBHelper != null)
			mDBHelper.close();
	}

	private void cancelTask(LocalDataBaseTask task)
	{
		if (task != null)
		{
			task.cancel(true);
		}
	}

	private void startNextAction(ContentValues action)
	{
		if (action != null)
		{
			mActions.addLast(action);
		}

		if (mActions.size() > 0)
		{
			if (mTask == null)
			{
				action = mActions.removeFirst();
				run(action);
			}
		}
		else
		{
			// run action
			run(action);
		}
	}

	private void run(ContentValues cv)
	{
		if (mTask == null && cv != null)
		{
			// get action
			switch (RuningAction.values()[(int) cv.get(ASYNC_ACTION)])
			{
				case RA_READ_FAVORITE:
					cancelTask(mTask);
					mTask = new LocalDataBaseTask(mDBHelper);
					mTask.setDBNotification(this);
					mTask.getFavoriteData();
					break;

				case RA_READ_HISTORY:
					cancelTask(mTask);
					mTask = new LocalDataBaseTask(mDBHelper);
					mTask.setDBNotification(this);
					mTask.getHistoryData();
					break;

				case RA_ADD_HISTORY:
					cancelTask(mTask);
					mTask = new LocalDataBaseTask(mDBHelper);
					mTask.setDBNotification(this);
					mTask.addToHistory((String) cv.get(DatabaseHelper.DIRECTION), (String) cv.get(DatabaseHelper.HIST_SOURCE), (String) cv.get(DatabaseHelper.HIST_DEST));
					break;

				case RA_DEL_HISTORY:
					cancelTask(mTask);
					mTask = new LocalDataBaseTask(mDBHelper);
					mTask.setDBNotification(this);
					mTask.delFromHistory(cv.getAsLong(DatabaseHelper.KEY_ID));
					break;

				case RA_ADD_FAVORITE:
					cancelTask(mTask);
					mTask = new LocalDataBaseTask(mDBHelper);
					mTask.setDBNotification(this);
					mTask.addToFavorite(cv.getAsLong(DatabaseHelper.HIST_ID));
					break;

				case RA_DEL_FAVORITE:
					cancelTask(mTask);
					mTask = new LocalDataBaseTask(mDBHelper);
					mTask.setDBNotification(this);
					mTask.delFromFavorite(cv.getAsLong(DatabaseHelper.KEY_ID));
					break;
			}
		}
	}

	public void addToFavorite(long hist_id)
	{
		ContentValues cv = new ContentValues();
		cv.put(ASYNC_ACTION, RuningAction.RA_ADD_FAVORITE.ordinal());
		cv.put(DatabaseHelper.HIST_ID, hist_id);
		startNextAction(cv);
	}

	public void delFromFavorite(long id)
	{
		ContentValues cv = new ContentValues();
		cv.put(ASYNC_ACTION, RuningAction.RA_DEL_FAVORITE.ordinal());
		cv.put(DatabaseHelper.KEY_ID, id);
		startNextAction(cv);
	}

	public void addToHistory(String direction, String src_text, String dest_text)
	{
		ContentValues cv = new ContentValues();
		cv.put(ASYNC_ACTION, RuningAction.RA_ADD_HISTORY.ordinal());
		cv.put(DatabaseHelper.DIRECTION, direction);
		cv.put(DatabaseHelper.HIST_SOURCE, src_text);
		cv.put(DatabaseHelper.HIST_DEST, dest_text);
		startNextAction(cv);
	}

	public void delFromHistory(long id)
	{
		ContentValues cv = new ContentValues();
		cv.put(ASYNC_ACTION, RuningAction.RA_DEL_HISTORY.ordinal());
		cv.put(DatabaseHelper.KEY_ID, id);
		startNextAction(cv);
	}

	public void getFavoriteAndHistoryData()
	{
		readFavorite();
		readHistory();
	}

	public void readFavorite()
	{
		ContentValues cv = new ContentValues();
		cv.put(ASYNC_ACTION, RuningAction.RA_READ_FAVORITE.ordinal());
		startNextAction(cv);
	}

	public void readHistory()
	{
		ContentValues cv = new ContentValues();
		cv.put(ASYNC_ACTION, RuningAction.RA_READ_HISTORY.ordinal());
		startNextAction(cv);
	}

	private void notifyAll(RuningAction action, LocalDataBaseTask task, List<ContentValues> list)
	{
		mTask = null;
		for (LocalDataBaseTask.LocalDataBaseNotification notify : mDBNotifications)
		{
			if (notify != null)
			{
				switch (action)
				{
					case RA_READ_FAVORITE:
						notify.onDBReadFavoriteComplette(task, list);
						break;

					case RA_READ_HISTORY:
						notify.onDBReadHistoryComplette(task, list);
						break;

					case RA_ADD_HISTORY:
						notify.onDBAddHistoryComplette(task, list);
						break;

					case RA_DEL_HISTORY:
						notify.onDBDelHistoryComplette(task, list);
						break;

					case RA_ADD_FAVORITE:
						notify.onDBAddFavoriteComplette(task, list);
						break;

					case RA_DEL_FAVORITE:
						notify.onDBDelFavoriteComplette(task, list);
						break;
				}
			}
		}
		startNextAction(null);
	}

	@Override
	public void onDBReadFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		notifyAll(RuningAction.RA_READ_FAVORITE, task, list);
	}

	@Override
	public void onDBReadHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		notifyAll(RuningAction.RA_READ_HISTORY, task, list);
	}

	@Override
	public void onDBAddHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		notifyAll(RuningAction.RA_ADD_HISTORY, task, list);
	}

	@Override
	public void onDBDelHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		notifyAll(RuningAction.RA_DEL_HISTORY, task, list);
	}

	@Override
	public void onDBAddFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		notifyAll(RuningAction.RA_ADD_FAVORITE, task, list);
	}

	@Override
	public void onDBDelFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		notifyAll(RuningAction.RA_DEL_FAVORITE, task, list);
	}

}
