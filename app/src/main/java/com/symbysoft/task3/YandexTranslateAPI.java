package com.symbysoft.task3;

// https://tech.yandex.ru/translate/

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.symbysoft.task3.YandexTranslateAPITask.YandexTranslateAPINotification;

public class YandexTranslateAPI implements YandexTranslateAPINotification
{
	private final Context mCtx;
	private String mApiKey;
	private String mTranslateDirection = "ru"; // example: ru, en-ru;
	private String mUiLang = "ru";
	private String mLastSourceText = "";

	private YandexTranslateAPITask mSupportLangsTask;
	private YandexTranslateAPITask mDetectLangTask;
	private YandexTranslateAPITask mTranslateTask;
	private final LinkedHashSet<YandexTranslateAPINotification> mAPINotifications;

	public void addAPINotification(YandexTranslateAPINotification APINotification)
	{
		mAPINotifications.add(APINotification);
	}

	public void removeAPINotification(YandexTranslateAPINotification APINotification)
	{
		mAPINotifications.remove(APINotification);
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

	private YandexTranslateAPI(Context ctx)
	{
		mCtx = ctx;
		mAPINotifications = new LinkedHashSet<>();
		updateUiLang();
	}

	public static YandexTranslateAPI newInstance(Context ctx)
	{
		return new YandexTranslateAPI(ctx);
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
		for (YandexTranslateAPINotification notify : mAPINotifications)
		{
			if (notify != null)
			{
				notify.onSupportedLangsUpdate(task, dirs, langs);
			}
		}
		mSupportLangsTask = null;
	}

	@Override
	public void onDetectedLangUpdate(YandexTranslateAPITask task, String detected_lang)
	{
		for (YandexTranslateAPINotification notify : mAPINotifications)
		{
			if (notify != null)
			{
				notify.onDetectedLangUpdate(task, detected_lang);
			}
		}
		mDetectLangTask = null;
	}

	@Override
	public void onTranslationUpdate(YandexTranslateAPITask task, String detected_lang, String detected_dir, String text)
	{
		for (YandexTranslateAPINotification notify : mAPINotifications)
		{
			if (notify != null)
			{
				notify.onTranslationUpdate(task, detected_lang, detected_dir, text);
			}
		}
		mTranslateTask = null;
	}

	@Override
	public void onHttpRequestResultError(YandexTranslateAPITask task, int http_status_code)
	{
		for (YandexTranslateAPINotification notify : mAPINotifications)
		{
			if (notify != null)
			{
				notify.onHttpRequestResultError(task, http_status_code);
			}
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

	private void updateSupportedLangs()
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
