package com.symbysoft.task3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import com.symbysoft.task3.YandexTranslateAPITask.YandexTranslateAPINotification;

public class SettingsProvider implements YandexTranslateAPINotification
{
	private final Context mCtx;
	private final SharedPreferences mPref;

	private final YandexTranslateAPIData mTranslateAPIData;

	private static final String PREFS_NAME = "settings";

	private final String EDIT_TEXT_SOURCE = "edit_text_source";
	private final String EDIT_TEXT_DESTINATION = "edit_text_destination";
	private final String YANDEX_API_KEY_TEXT = "yandex_translate_key";
	private final String YANDEX_API_TRANSLATE_DIRECTION = "yandex_translate_direction";
	private final String YANDEX_API_SUPPORTED_DIRECTIONS = "yandex_translate_directions";
	private final String YANDEX_API_SUPPORTED_LANGS = "yandex_translate_langs";

	public YandexTranslateAPIData getTranslateAPIData()
	{
		return mTranslateAPIData;
	}

	private SettingsProvider(Context ctx)
	{
		mCtx = ctx;
		mPref = mCtx.getSharedPreferences(PREFS_NAME, 0);
		mTranslateAPIData = new YandexTranslateAPIData();
	}

	public static SettingsProvider getInstance(Context ctx)
	{
		return new SettingsProvider(ctx);
	}

	void ReadSettings()
	{
		((MainApp) mCtx).getDataProvider().getTranslateAPI().addAPINotification(this);

		mTranslateAPIData.setApiKey(mPref.getString(YANDEX_API_KEY_TEXT, YandexTranslateAPIData.DEFAULT_API_KEY));
		mTranslateAPIData.setTranslateDirection(mPref.getString(YANDEX_API_TRANSLATE_DIRECTION, YandexTranslateAPIData.DEFAULT_DEST_VALUE));

		// read directions
		mTranslateAPIData.setDirs(mPref.getStringSet(YANDEX_API_SUPPORTED_DIRECTIONS, new HashSet<String>()));

		mTranslateAPIData.getLangs().clear();
		try
		{
			// read supported langs
			JSONObject json = new JSONObject(mPref.getString(YANDEX_API_SUPPORTED_LANGS, (new JSONObject()).toString()));
			Iterator<String> key_iter = json.keys();
			while (key_iter.hasNext())
			{
				String key = key_iter.next();
				String value = (String) json.get(key);
				mTranslateAPIData.getLangs().put(key, value);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		mTranslateAPIData.setSrcText(mPref.getString(EDIT_TEXT_SOURCE, ""));
		mTranslateAPIData.setDestText(mPref.getString(EDIT_TEXT_DESTINATION, ""));
	}

	void WriteSettings()
	{
		SharedPreferences.Editor ed = mPref.edit();

		ed.putString(YANDEX_API_KEY_TEXT, mTranslateAPIData.getApiKey());
		ed.putString(YANDEX_API_TRANSLATE_DIRECTION, mTranslateAPIData.getTranslateDirection());
		ed.putStringSet(YANDEX_API_SUPPORTED_DIRECTIONS, mTranslateAPIData.getDirs());

		ed.putString(YANDEX_API_SUPPORTED_LANGS, (new JSONObject(mTranslateAPIData.getLangs())).toString());

		ed.putString(EDIT_TEXT_SOURCE, mTranslateAPIData.getSrcText());
		ed.putString(EDIT_TEXT_DESTINATION, mTranslateAPIData.getDestText());

		ed.commit();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onSupportedLangsUpdate(YandexTranslateAPITask task, Set<String> dirs, Map<String, String> langs)
	{
		mTranslateAPIData.setDirs((HashSet) ((HashSet) dirs).clone());
		mTranslateAPIData.setLangs((HashMap) ((HashMap) langs).clone());
	}

	@Override
	public void onDetectedLangUpdate(YandexTranslateAPITask task, String detected_lang)
	{

	}

	@Override
	public void onTranslationUpdate(YandexTranslateAPITask task, String detected_lang, String detected_dir, String text)
	{

	}

	@Override
	public void onHttpRequestResultError(YandexTranslateAPITask task, int http_status_code)
	{

	}

}
