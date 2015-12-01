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
	private static final String TABLE_HISTORY = "history";
	private static final String TABLE_FAVORITE = "favorite";
	private static final String KEY_ID = "id";
	private static final String HIST_ID = "hist_id";
	private static final String HIST_SOURCE = "src";
	private static final String HIST_DEST = "dest";

	public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
	{
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "(" +
				KEY_ID + " INTEGER PRIMARY KEY," +
				HIST_SOURCE + " TEXT," +
				HIST_DEST + " TEXT" + ")";
		db.execSQL(CREATE_HISTORY_TABLE);

		String CREATE_FAVORITE_TABLE = "CREATE TABLE " + TABLE_FAVORITE + "(" +
				KEY_ID + " INTEGER PRIMARY KEY," +
				HIST_ID + " INTEGER" + ")";
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

	public void addToHistory(String direction, String src_text, String dest_text)
	{
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(HIST_SOURCE, src_text);
		cv.put(HIST_DEST, dest_text);
		db.insert(TABLE_HISTORY, null, cv);
	}

	public List<String> getHistoryData()
	{
		List<String> list = new ArrayList<>();
		String selectQuery = "SELECT  * FROM " + TABLE_HISTORY;

		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst())
		{
			do
			{
				String item = cursor.getString(1);
				list.add(item);
			}
			while (cursor.moveToNext());
		}

		return list;
	}

}
