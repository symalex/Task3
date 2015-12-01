package com.symbysoft.task3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YandexTranslateAPITask extends YandexTranslateAPIBaseTask
{
	private static final String TAG = "YandexTranslateAPITask";

	public enum YandexTranslateApiAction
	{
		YANDEX_TRANSLATE_API_ACTION_NONE,
		YANDEX_TRANSLATE_API_ACTION_LANGS,
		YANDEX_TRANSLATE_API_ACTION_DETECT_LANG,
		YANDEX_TRANSLATE_API_ACTION_TRANSLATE
	}

	private String mUiLang;
	private String mTranslateDirection;
	private YandexTranslateApiAction mAction = YandexTranslateApiAction.YANDEX_TRANSLATE_API_ACTION_NONE;
	private int mHttpJsonResultCode;

	private YandexTranslateAPINotification mAPINotification;

	public void setAPINotification(YandexTranslateAPINotification APINotification)
	{
		mAPINotification = APINotification;
	}

	public String getTranslateDirection()
	{
		return mTranslateDirection;
	}

	public String getUiLang()
	{
		return mUiLang;
	}

	public void setUiLang(String uiLang)
	{
		mUiLang = uiLang;
	}

	public void setTranslateDirection(String translateDirection)
	{
		mTranslateDirection = translateDirection;
	}

	YandexTranslateAPITask()
	{
	}

	public void updateSupportedLangs()
	{
		mAction = YandexTranslateApiAction.YANDEX_TRANSLATE_API_ACTION_LANGS;
		setUrl(String.format("https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=%s&ui=%s", mApiKey, mUiLang));
		execute();
	}

	public void detectLang(String text)
	{
		mAction = YandexTranslateApiAction.YANDEX_TRANSLATE_API_ACTION_DETECT_LANG;
		setUrl(String.format("https://translate.yandex.net/api/v1.5/tr.json/detect?key=%s&text=%s", mApiKey, text));
		execute();
	}

	public void translate(String text)
	{
		mAction = YandexTranslateApiAction.YANDEX_TRANSLATE_API_ACTION_TRANSLATE;

		/*
		lang Может задаваться одним из следующих способов:
		В виде пары кодов языков («с какого»-«на какой»), разделенных дефисом. Например, en-ru обозначает перевод с английского на русский.
		В виде кода конечного языка (например ru). В этом случае сервис пытается определить исходный язык автоматически.*/

		setUrl(String.format("https://translate.yandex.net/api/v1.5/tr.json/translate?key=%s&text=%s&lang=%s&format=plain&options=1", mApiKey, text, mTranslateDirection));
		execute();
	}

	@Override
	protected void onPostExecute(String strJson)
	{
		super.onPostExecute(strJson);

		// show json string
		Log.d(TAG, strJson);

		JSONObject dataJsonObj;

		try
		{
			dataJsonObj = new JSONObject(strJson);
			mHttpJsonResultCode = dataJsonObj.getInt("code");

			String detected_lang;

			switch (mAction)
			{
				case YANDEX_TRANSLATE_API_ACTION_LANGS:
					Set<String> rdirs = new HashSet<>();
					Map<String, String> rlangs = new HashMap<>();

					JSONArray dirs = dataJsonObj.getJSONArray("dirs");
					for (int i = 0; i < dirs.length(); i++)
					{
						Log.d(TAG, "dir: " + dirs.get(i));
						rdirs.add(dirs.get(i).toString());
					}

					JSONObject langs_obj = dataJsonObj.getJSONObject("langs");
					JSONArray lang_keys = langs_obj.names();
					for (int i = 0; i < lang_keys.length(); i++)
					{
						Log.d(TAG, "langs: " + lang_keys.get(i).toString() + ":" + langs_obj.getString(lang_keys.get(i).toString()));
						rlangs.put(lang_keys.get(i).toString(), langs_obj.getString(lang_keys.get(i).toString()));
					}

					if (mAPINotification != null)
					{
						mAPINotification.onSupportedLangsUpdate(this, rdirs, rlangs);
					}
					break;

				case YANDEX_TRANSLATE_API_ACTION_DETECT_LANG:
					detected_lang = dataJsonObj.getString("lang");

					if (mAPINotification != null)
					{
						mAPINotification.onDetectedLangUpdate(this, detected_lang);
					}
					break;

				case YANDEX_TRANSLATE_API_ACTION_TRANSLATE:
					//{"code":200,"detected":{"lang":"en"},"lang":"en-ru","text":["Яндекс тестирование текста"]}
					JSONObject detected = dataJsonObj.getJSONObject("detected");
					detected_lang = detected.getString("lang");
					String detected_dir = dataJsonObj.getString("lang");
					String text = dataJsonObj.getString("text").trim();
					if (text.length() > 1 && text.charAt(0) == '[' && text.charAt(text.length() - 1) == ']')
					{
						text = text.substring(1, text.length() - 1);
						if (text.length() > 1 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"')
						{
							text = text.substring(1, text.length() - 1);
						}
					}

					if (mAPINotification != null)
					{
						mAPINotification.onTranslationUpdate(this, detected_lang, detected_dir, text);
					}
					break;

			}

		}
		catch (JSONException e)
		{
			e.printStackTrace();
			if (mAPINotification != null)
			{
				mAPINotification.onHttpRequestResultError(this, mHttpJsonResultCode);
			}
		}
		finally
		{
			mAction = YandexTranslateApiAction.YANDEX_TRANSLATE_API_ACTION_NONE;
		}

	}

}
