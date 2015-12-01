package com.symbysoft.task3;

import java.util.Map;
import java.util.Set;

public interface YandexTranslateAPINotification
{
	void onSupportedLangsUpdate(YandexTranslateAPITask task, Set<String> dirs, Map<String, String> langs);

	void onDetectedLangUpdate(YandexTranslateAPITask task, String detected_lang);

	void onTranslationUpdate(YandexTranslateAPITask task, String detected_lang, String detected_dir, String text);

	void onHttpRequestResultError(YandexTranslateAPITask task, int http_status_code);
}
