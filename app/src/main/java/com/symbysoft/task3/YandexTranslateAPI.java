package com.symbysoft.task3;

// https://tech.yandex.ru/translate/

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class YandexTranslateAPI implements YandexTranslateAPINotification
{
	private String mApiKey;
	private String mTranslateDirection = "ru"; // example: ru, en-ru;
	private String mUiLang = "ru";
	private String mLastSourceText = "";

	private YandexTranslateAPITask mSupportLangsTask;
	private YandexTranslateAPITask mDetectLangTask;
	private YandexTranslateAPITask mTranslateTask;
	private YandexTranslateAPINotification mAPINotification;

	public void setAPINotification(YandexTranslateAPINotification APINotification)
	{
		mAPINotification = APINotification;
	}

	public String getApiKey()
	{
		return mApiKey;
	}

	public void setApiKey(String apiKey)
	{
		mApiKey = apiKey;
	}

	public String getUiLang()
	{
		return mUiLang;
	}

	public String getTranslateDirection()
	{
		return mTranslateDirection;
	}

	public void setTranslateDirection(String translateDirection)
	{
		mTranslateDirection = translateDirection;
	}

	public String getLastSourceText()
	{
		return mLastSourceText;
	}

	private YandexTranslateAPI()
	{
		updateUiLang();
	}

	public static YandexTranslateAPI newInstance()
	{
		return new YandexTranslateAPI();
	}

	public void onDestroy()
	{
		CancelTask(mSupportLangsTask);
		mSupportLangsTask = null;

		CancelTask(mDetectLangTask);
		mDetectLangTask = null;

		CancelTask(mTranslateTask);
		mTranslateTask = null;
	}

	private void CancelTask(YandexTranslateAPITask task)
	{
		if (task != null)
		{
			task.cancel(true);
		}
	}

	@Override
	public void onSupportedLangsUpdate(YandexTranslateAPITask task, Set<String> dirs, Map<String, String> langs)
	{
		if (mAPINotification != null)
		{
			mAPINotification.onSupportedLangsUpdate(task, dirs, langs);
		}
		mSupportLangsTask = null;
	}

	@Override
	public void onDetectedLangUpdate(YandexTranslateAPITask task, String detected_lang)
	{
		if (mAPINotification != null)
		{
			mAPINotification.onDetectedLangUpdate(task, detected_lang);
		}
		mDetectLangTask = null;
	}

	@Override
	public void onTranslationUpdate(YandexTranslateAPITask task, String detected_lang, String detected_dir, String text)
	{
		if (mAPINotification != null)
		{
			mAPINotification.onTranslationUpdate(task, detected_lang, detected_dir, text);
		}
		mTranslateTask = null;
	}

	@Override
	public void onHttpRequestResultError(YandexTranslateAPITask task, int http_status_code)
	{
		if (mAPINotification != null)
		{
			mAPINotification.onHttpRequestResultError(task, http_status_code);
		}
		mTranslateTask = null;
	}

	public boolean isTaskInProgress(YandexTranslateAPITask.YandexTranslateApiAction action)
	{
		boolean in_progress = false;
		switch (action)
		{
			case YANDEX_TRANSLATE_API_ACTION_LANGS:
				in_progress = mSupportLangsTask != null;
				break;
			case YANDEX_TRANSLATE_API_ACTION_DETECT_LANG:
				in_progress = mDetectLangTask != null;
				break;
			case YANDEX_TRANSLATE_API_ACTION_TRANSLATE:
				in_progress = mTranslateTask != null;
				break;
		}
		return in_progress;
	}

	public void updateSupportedLangs()
	{
		CancelTask(mSupportLangsTask);

		updateUiLang();
		mSupportLangsTask = new YandexTranslateAPITask();
		mSupportLangsTask.setApiKey(mApiKey);
		mSupportLangsTask.setUiLang(mUiLang);
		mSupportLangsTask.setAPINotification(this);
		mSupportLangsTask.updateSupportedLangs();
	}

	public void detectLang(String text)
	{
		CancelTask(mDetectLangTask);
		mDetectLangTask = new YandexTranslateAPITask();
		mDetectLangTask.setApiKey(mApiKey);
		mDetectLangTask.setUiLang(mUiLang);
		mDetectLangTask.setAPINotification(this);
		mDetectLangTask.detectLang(text);
	}

	public void translate(String text)
	{
		CancelTask(mTranslateTask);
		mLastSourceText = text;
		mTranslateTask = new YandexTranslateAPITask();
		mTranslateTask.setApiKey(mApiKey);
		mTranslateTask.setUiLang(mUiLang);
		mTranslateTask.setTranslateDirection(mTranslateDirection);
		mTranslateTask.setAPINotification(this);
		mTranslateTask.translate(text);
	}

	private void updateUiLang()
	{
		mUiLang = Locale.getDefault().getLanguage();
	}

	public void update()
	{
		updateSupportedLangs();
	}

}
