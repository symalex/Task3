package com.symbysoft.task3;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class SettingsProvider
{
	private Context mCtx;
	private SharedPreferences mPref;

	public static final String PREFS_NAME = "settings";

	final String SAVED_TEXT = "saved_text";

	private SettingsProvider(Context ctx)
	{
		mCtx = ctx;
		mPref = mCtx.getSharedPreferences(PREFS_NAME, 0);
	}

	public static SettingsProvider getInstance(Context ctx)
	{
		return new SettingsProvider(ctx);
	}

	void ReadSettings()
	{
		String savedText = mPref.getString(SAVED_TEXT, "");
	}

	void WriteSettings()
	{
		SharedPreferences.Editor ed = mPref.edit();
		ed.putString(SAVED_TEXT, "text");
		ed.commit();
	}
}
