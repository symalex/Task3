package com.symbysoft.task3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class YandexTranslateAPIData
{
	public static final String DEFAULT_API_KEY = "trnsl.1.1.20151126T092912Z.366a7389b1bb3869.ffe45722eda47f45e459a47a3a8048d68c844baa";
	public static final String DEFAULT_DIRECTION = "en-ru";
	public static final String DEFAULT_SRC_VALUE = "en";
	public static final String DEFAULT_DEST_VALUE = "ru";

	private String mApiKey;
	private String mTranslateDirection; // example: ru, en-ru;

	private Set<String> mDirs;
	private Map<String, String> mLangs;

	private String mSrcText;
	private String mDestText;

	public static String src(String direction)
	{
		return direction.length() >= 2 ? direction.substring(0, 2) : YandexTranslateAPIData.DEFAULT_SRC_VALUE;
	}

	public static String dest(String direction)
	{
		return direction.length() >= 4 ? direction.substring(3, 5) : (direction.length() >= 2 ? direction.substring(0, 2) : YandexTranslateAPIData.DEFAULT_DEST_VALUE);
	}

	public static String direction(String src, String dest)
	{
		return src + "-" + dest;
	}

	public String getApiKey()
	{
		return mApiKey;
	}

	public void setApiKey(String apiKey)
	{
		mApiKey = apiKey;
	}

	public String getTranslateDirection()
	{
		return mTranslateDirection;
	}

	public void setTranslateDirection(String translateDirection)
	{
		mTranslateDirection = translateDirection;
	}

	public Set<String> getDirs()
	{
		return mDirs;
	}

	public void setDirs(Set<String> dirs)
	{
		mDirs = dirs;
	}

	public Map<String, String> getLangs()
	{
		return mLangs;
	}

	public void setLangs(Map<String, String> langs)
	{
		mLangs = langs;
	}

	public String getSrcText()
	{
		return mSrcText;
	}

	public void setSrcText(String srcText)
	{
		mSrcText = srcText;
	}

	public String getDestText()
	{
		return mDestText;
	}

	public void setDestText(String destText)
	{
		mDestText = destText;
	}

	YandexTranslateAPIData()
	{
		mApiKey = DEFAULT_API_KEY;
		mDirs = new HashSet<>();
		mLangs = new HashMap<>();
		mTranslateDirection = DEFAULT_DIRECTION;
	}

	public boolean isLanguageDataReady()
	{
		return mDirs.size() > 0 && mLangs.size() > 0;
	}

	public String decode(String lang_key, boolean dest)
	{
		String key;
		if (mLangs.containsKey(lang_key))
		{
			key = lang_key;
		}
		else
		{
			key = dest ? DEFAULT_DEST_VALUE : DEFAULT_SRC_VALUE;
		}
		return mLangs.get(key);
	}

	public String encode(String lang_name, boolean dest)
	{
		String lang_key = dest ? DEFAULT_DEST_VALUE : DEFAULT_SRC_VALUE;

		for (Map.Entry<String, String> item : mLangs.entrySet())
		{
			if (item.getValue().equals(lang_name))
			{
				lang_key = item.getKey();
				break;
			}
		}

		return lang_key;
	}

	public String encode(String src_lang_name, String dest_lang_name)
	{
		return direction(encode(src_lang_name, false), encode(dest_lang_name, true));
	}
}
