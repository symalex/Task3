package com.symbysoft.task3;

// http://stackoverflow.com/questions/9290394/sqlitedatabase-getwritabledatabase-not-working

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
	// Contacts table name
	private final String TABLE_HISTORY = "history";
	private final String TABLE_FAVORITE = "favorite";
	public static final String KEY_ID = "id";
	public static final String DATE_TIME = "dt";
	public static final String HIST_ID = "hist_id";
	public static final String HIST_SOURCE = "src";
	public static final String HIST_DEST = "dest";
	public static final String DIRECTION = "dir";

	public static final String COUNT = "count";
	public static final String DOUBLE = "double";

	private int mHistoryCounter;
	private int mFavoriteCounter;

	public int getHistoryCounter()
	{
		return mHistoryCounter;
	}

	public int getFavoriteCounter()
	{
		return mFavoriteCounter;
	}

	public int getProgress()
	{
		return mFavoriteCounter + mHistoryCounter;
	}

	public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
	{
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "(" +
				KEY_ID + " INTEGER PRIMARY KEY," +                             // 0
				DATE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," + // 1
				DIRECTION + " VARCHAR(5)," +                                   // 2
				HIST_SOURCE + " TEXT," +                                       // 3
				HIST_DEST + " TEXT" + ")";                                     // 4
		db.execSQL(CREATE_HISTORY_TABLE);

		String CREATE_FAVORITE_TABLE = "CREATE TABLE " + TABLE_FAVORITE + "(" +
				KEY_ID + " INTEGER PRIMARY KEY," +                                                            // 0
				HIST_ID + " INTEGER" + " REFERENCES " + TABLE_HISTORY + "(" + KEY_ID + ") ON DELETE CASCADE" + ")"; // 1
		db.execSQL(CREATE_FAVORITE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE);

		// Create tables again
		onCreate(db);
	}

	public int getHistoryRecordCount()
	{
		SQLiteDatabase db = getReadableDatabase();
		String selectQuery = "SELECT COUNT(*) FROM " + TABLE_HISTORY;
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		return count;
	}

	public ContentValues addToHistory(String direction, String src_text, String dest_text)
	{
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DIRECTION, direction);
		cv.put(HIST_SOURCE, src_text);
		cv.put(HIST_DEST, dest_text);
		long id = db.insert(TABLE_HISTORY, null, cv);
		cv.put(KEY_ID, id);
		return cv;
	}

	public ContentValues delFromHistory(long id)
	{
		SQLiteDatabase db = getWritableDatabase();
		int count = db.delete(TABLE_HISTORY, "id=?", new String[]{String.valueOf(id)});
		ContentValues cv = new ContentValues();
		cv.put(KEY_ID, id);
		cv.put(COUNT, count);
		return cv;
	}

	public ContentValues addToFavorite(long hist_id)
	{
		SQLiteDatabase db = getWritableDatabase();
		String selectQuery;
		Cursor cursor;
		boolean found;

		long id = -1;
		selectQuery = String.format("SELECT  * FROM %s WHERE %s=%d", TABLE_FAVORITE, HIST_ID, hist_id);
		cursor = db.rawQuery(selectQuery, null);
		found = cursor.moveToFirst();
		if (found)
		{
			id = cursor.getLong(0);
		}
		cursor.close();

		ContentValues cv = new ContentValues();
		cv.put(HIST_ID, hist_id);
		if (found)
		{
			cv.put(DOUBLE, true);
		}
		else
		{
			id = db.insert(TABLE_FAVORITE, null, cv);
		}
		cv.put(KEY_ID, id);

		selectQuery = String.format("SELECT  * FROM %s WHERE %s=%d", TABLE_HISTORY, KEY_ID, hist_id);
		cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		cv.put(DIRECTION, cursor.getString(2));
		cv.put(HIST_SOURCE, cursor.getString(3));
		cv.put(HIST_DEST, cursor.getString(4));
		cursor.close();

		return cv;
	}

	public ContentValues delFromFavorite(long id)
	{
		SQLiteDatabase db = getWritableDatabase();
		int count = db.delete(TABLE_FAVORITE, "id=?", new String[]{String.valueOf(id)});
		ContentValues cv = new ContentValues();
		cv.put(KEY_ID, id);
		cv.put(COUNT, count);
		return cv;
	}

	public List<ContentValues> getHistoryData()
	{
		List<ContentValues> list = new ArrayList<>();
		String selectQuery = "SELECT  * FROM " + TABLE_HISTORY;

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		mHistoryCounter = 0;

		if (cursor.moveToFirst())
		{
			do
			{
				ContentValues cv = new ContentValues();
				cv.put(KEY_ID, cursor.getLong(0));
				cv.put(DATE_TIME, cursor.getString(1));
				cv.put(DIRECTION, cursor.getString(2));
				cv.put(HIST_SOURCE, cursor.getString(3));
				cv.put(HIST_DEST, cursor.getString(4));
				list.add(cv);
				mHistoryCounter += 1;
			}
			while (cursor.moveToNext());
		}
		cursor.close();

		return list;
	}

	public int getFavoriteRecordCount()
	{
		SQLiteDatabase db = getReadableDatabase();
		String selectQuery = "SELECT COUNT(*) FROM " + TABLE_FAVORITE;
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		return count;
	}

	public List<ContentValues> getFavoriteData()
	{
		List<ContentValues> list = new ArrayList<>();
		String selectQuery = "SELECT * FROM " + TABLE_FAVORITE +
				" f JOIN " + TABLE_HISTORY + " h ON " +
				"f." + HIST_ID + "=h." + KEY_ID;

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		mFavoriteCounter = 0;

		if (cursor.moveToFirst())
		{
			do
			{
				ContentValues cv = new ContentValues();
				cv.put(KEY_ID, cursor.getLong(0));
				cv.put(HIST_ID, cursor.getLong(1));
				//cv.put(KEY_ID, cursor.getLong(2));
				cv.put(DATE_TIME, cursor.getString(3));
				cv.put(DIRECTION, cursor.getString(4));
				cv.put(HIST_SOURCE, cursor.getString(5));
				cv.put(HIST_DEST, cursor.getString(6));
				list.add(cv);
				mFavoriteCounter += 1;
			}
			while (cursor.moveToNext());
		}
		cursor.close();

		return list;
	}

}
