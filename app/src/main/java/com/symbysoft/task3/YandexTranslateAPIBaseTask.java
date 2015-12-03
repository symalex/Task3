package com.symbysoft.task3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YandexTranslateAPIBaseTask extends AsyncTask<Void, Void, String>
{
	private static final String TAG = "YandexTranslateAPIBaseTask";

	protected String mUrl;
	protected String mApiKey;
	private int mHttpCode;

	public String getUrl()
	{
		return mUrl;
	}

	protected void setUrl(String url)
	{
		mUrl = url;
	}

	public String getApiKey()
	{
		return mApiKey;
	}

	public int getHttpCode()
	{
		return mHttpCode;
	}

	public void setApiKey(String apiKey)
	{
		mApiKey = apiKey;
	}

	@Override
	protected String doInBackground(Void... params)
	{
		mHttpCode = 0;
		String resultJson;

		try
		{
			URL url = new URL(mUrl);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			mHttpCode = urlConnection.getResponseCode();

			if (mHttpCode == 200)
			{
				InputStream inputStream = urlConnection.getInputStream();
				StringBuilder buffer = new StringBuilder();

				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

				String line;
				while ((line = reader.readLine()) != null)
				{
					buffer.append(line);
				}

				resultJson = buffer.toString().trim();
				if (resultJson.length() > 7)
				{
					if (resultJson.charAt(0) == '{' && !resultJson.substring(1, 7).equals("\"code\""))
					{
						resultJson = String.format("{\"code\":%d,", mHttpCode) + resultJson.substring(1);
					}
				}
				else
				{
					// no response found
					resultJson = String.format("{\"code\":%d}", 444);
				}
			}
			else
			{
				resultJson = String.format("{\"code\":%d}", mHttpCode);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			resultJson = String.format("{\"code\":%d}", mHttpCode);
		}
		return resultJson;
	}

}
