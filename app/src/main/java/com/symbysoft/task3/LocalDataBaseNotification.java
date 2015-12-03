package com.symbysoft.task3;

import java.util.List;

import android.content.ContentValues;

public interface LocalDataBaseNotification
{
	void onDBReadHistoryComplette(LocalDataBaseTask task, List<ContentValues> list);

	void onDBReadFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list);

	void onDBAddHistoryComplette(LocalDataBaseTask task, List<ContentValues> list);

	void onDBDelHistoryComplette(LocalDataBaseTask task, List<ContentValues> list);

	void onDBAddFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list);

	void onDBDelFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list);
}
