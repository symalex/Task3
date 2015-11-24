package com.symbysoft.task3;

import android.app.Application;

public class MainApp extends Application
{
	private DataProvider mDataProvider;

	public DataProvider getDataProvider()
	{
		return mDataProvider;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		mDataProvider = DataProvider.getInstance(this);
	}
}
