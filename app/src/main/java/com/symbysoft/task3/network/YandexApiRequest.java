package com.symbysoft.task3.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;
import com.symbysoft.task3.network.YandexTranslateAPIData.YandexApiResult;
import com.symbysoft.task3.network.YandexTranslateAPIData.LangsData;
import com.symbysoft.task3.network.YandexTranslateAPIData.LangDetectData;
import com.symbysoft.task3.network.YandexTranslateAPIData.TranslateData;

public interface YandexApiRequest
{
	//https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=%s&ui=%s
	@GET("/api/v1.5/tr.json/getLangs")
	Call<LangsData> listLangs(
			@Query("key") String key,
			@Query("ui") String ui
	);

	//https://translate.yandex.net/api/v1.5/tr.json/detect?key=%s&text=%s
	@GET("/api/v1.5/tr.json/detect")
	Call<LangDetectData> detectLang(
			@Query("key") String key,
			@Query("text") String text
	);

	//https://translate.yandex.net/api/v1.5/tr.json/translate?key=%s&text=%s&lang=%s&format=plain&options=1
	@GET("/api/v1.5/tr.json/translate")
	Call<TranslateData> translate(
			@Query("key") String key,
			@Query("text") String text,
			@Query("lang") String direction,
			@Query("format") String format,
			@Query("options") int options
	);

}
