package com.symbysoft.task3;

import android.content.Context;

public class DataProvider
{
	private Context mCtx;
	private boolean mDataLoaded = false;
	private int mProgress = 0;
	private int mProgressMax = 100;
	private SettingsProvider mSettings;

	private DataProvider(Context ctx)
	{
		mCtx = ctx;
	}

	static DataProvider getInstance(Context ctx)
	{
		DataProvider data = new DataProvider(ctx);
		data.onCreate();
		return data;
	}

	protected void onCreate()
	{
		mSettings = SettingsProvider.getInstance(mCtx);
	}

	public boolean isDataLoaded()
	{
		return mDataLoaded;
	}

	public int getProgress()
	{
		if (mProgress >= mProgressMax)
		{
			mDataLoaded = true;
		}
		mProgress += 10;
		return mProgress;
	}

	public int getProgressMax()
	{
		return mProgressMax;
	}

	public void loadData()
	{
		mSettings.ReadSettings();
	}

	public void saveData()
	{
		mSettings.WriteSettings();
	}
}
