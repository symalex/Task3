package com.symbysoft.task3;

import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symbysoft.task3.InternetReceiver.InternetReceiverNotification;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.symbysoft.task3.DataProvider.DataProviderNotification;
import com.symbysoft.task3.YandexTranslateAPITask.YandexTranslateAPINotification;

public class MainFragment extends Fragment implements InternetReceiverNotification, YandexTranslateAPINotification, DataProviderNotification
{
	private static final String TAG = "MainFragment";
	public static final String FTAG = "main_fragment";

	@Bind(R.id.content_main_layout_translate)
	protected LinearLayout mLayoutTranslate;
	@Bind(R.id.content_main_error_state_text)
	protected TextView mErrorText;
	@Bind(R.id.fragment_main_top_text_info)
	protected TextView mTextViewTopTextInfo;
	@Bind(R.id.content_main_edit_text_top)
	protected EditText mEditTextTop;
	@Bind(R.id.fragment_main_bottom_text_info)
	protected TextView mTextViewBottomTextInfo;
	@Bind(R.id.content_main_edit_text_bottom)
	protected EditText mEditTextBottom;
	//@Bind(R.id.fragment_main_btn_translate)
	//protected FloatingActionButton mBtnTranslate;

	private int mWordsCount = 0;
	private String mTopTextComparatorStr = "";

	private InternetReceiver mReceiver;
	private DataProvider mDataProvider;
	private YandexTranslateAPIData mAPIData;
	private final Handler mHandler = new Handler();

	public static Fragment newInstance()
	{
		return new MainFragment();
	}

	private final Runnable mRunnableTextTop = new Runnable()
	{
		@Override
		public void run()
		{
			// run text tranlate
			doTranslateText();
		}
	};

	private final TextWatcher mTextWatcherTop = new TextWatcher()
	{
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
			Log.d(TAG, "beforeTextChanged");
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			Log.d(TAG, "onTextChanged");
		}

		@Override
		public void afterTextChanged(Editable s)
		{
			//Log.d(TAG, "afterTextChanged");
			mHandler.removeCallbacks(mRunnableTextTop);
			mHandler.postDelayed(mRunnableTextTop, 10000);
		}
	};

	private String getTextComparator(String text)
	{
		return text.replaceAll(" ", "").replaceAll("\n", "");
	}

	private int wordsCount(String s)
	{
		int cnt = 0;
		s = s.trim();
		if (s.length() > 0)
		{
			cnt = s.split("\\w+").length;
			if (cnt == 0)
			{
				cnt = 1;
			}
		}

		return cnt;
	}

	private void updateTextComparators()
	{
		mWordsCount = wordsCount(mEditTextTop.getText().toString());
		mTopTextComparatorStr = getTextComparator(mEditTextTop.getText().toString());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		ButterKnife.bind(this, view);

		mDataProvider = ((MainApp) getContext().getApplicationContext()).getDataProvider();
		mAPIData = mDataProvider.getSettings().getTranslateAPIData();
		mReceiver = mDataProvider.getInternetReceiver();
		mDataProvider.addDataProviderNotification(this);

		updateViewData();
		mEditTextTop.addTextChangedListener(mTextWatcherTop);

		onInternetConnectionChange(mReceiver);

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (mDataProvider != null)
		{
			mDataProvider.addDataProviderNotification(this);
			mDataProvider.getInternetReceiver().addInternetReceiverNotification(this);
			mDataProvider.getTranslateAPI().addAPINotification(this);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		updateViewData();
		updateInfoTexts();
	}

	@Override
	public void onPause()
	{
		super.onPause();

		mAPIData.setSrcText(mEditTextTop.getText().toString());
		mAPIData.setDestText(mEditTextBottom.getText().toString());
	}

	@Override
	public void onStop()
	{
		if (mDataProvider != null)
		{
			mDataProvider.getTranslateAPI().removeAPINotification(this);
			mDataProvider.getInternetReceiver().removeInternetReceiverNotification(this);
			mDataProvider.removeDataProviderNotification(this);
		}

		super.onStop();
	}

	@Override
	public void onLoadDataComplette()
	{
		updateInfoTexts();
	}

	private void updateViewData()
	{
		mEditTextTop.setText(mAPIData.getSrcText());
		mEditTextBottom.setText(mAPIData.getDestText());
		updateTextComparators();
	}

	private void updateInfoTexts()
	{
		if (mTextViewTopTextInfo != null && mTextViewBottomTextInfo != null)
		{
			String direction = mAPIData.getTranslateDirection();
			setSourceTextInfo(mAPIData.decode(YandexTranslateAPIData.src(direction), false));
			setDestinationTextInfo(mAPIData.decode(YandexTranslateAPIData.dest(direction), true));
		}
		if (mReceiver != null)
		{
			onInternetConnectionChange(mReceiver);
		}
	}

	private boolean isTopTextChanged()
	{
		return mWordsCount != wordsCount(mEditTextTop.getText().toString().trim()) || !mTopTextComparatorStr.equals(getTextComparator(mEditTextTop.getText().toString()));
	}

	private void doTranslateText()
	{
		if (isTopTextChanged())
		{
			updateTextComparators();
			mDataProvider.getTranslateAPI().translate(mEditTextTop.getText().toString().trim());
		}
	}

	private void doSaveHistory(String src_text, String dest_text)
	{
		if (src_text.length() > 0 && dest_text.length() > 0)
		{
			// save history data
			mDataProvider.getLocalDataBase().addToHistory(mDataProvider.getTranslateAPI().getTranslateDirection(), src_text, dest_text);
		}
	}

	private String change_text(String s)
	{
		String ui_lang = mDataProvider.getTranslateAPI().getUiLang();
		if (ui_lang.equals("ru"))
		{
			String ret = s.substring(0, s.length() - 2);
			return ret + "ом";
		}
		return s;
	}

	public void setSourceTextInfo(String lang_name)
	{
		if (mTextViewTopTextInfo != null && lang_name != null)
		{
			mTextViewTopTextInfo.setText(String.format("Текст на %s языке", change_text(lang_name)));
		}
	}

	public void setDestinationTextInfo(String lang_name)
	{
		if (mTextViewBottomTextInfo != null && lang_name != null)
		{
			mTextViewBottomTextInfo.setText(String.format("Перевод на %s языке", change_text(lang_name)));
		}
	}

	private void setEnabledUiElements(boolean enabled)
	{
		mLayoutTranslate.setEnabled(enabled);
		mTextViewTopTextInfo.setEnabled(enabled);
		mTextViewBottomTextInfo.setEnabled(enabled);
		mEditTextTop.setEnabled(enabled);
		mEditTextBottom.setEnabled(enabled);
	}

	@Override
	public void onInternetConnectionChange(InternetReceiver receiver)
	{
		if (mLayoutTranslate != null && mErrorText != null && mDataProvider != null)
		{
			setEnabledUiElements(receiver.isConnectionOk());
			mErrorText.setVisibility(View.GONE);
			mLayoutTranslate.setVisibility(View.VISIBLE);
			if (!receiver.isConnectionOk())
			{
				if (mDataProvider.getHistoryList().size() == 0)
				{
					mErrorText.setVisibility(View.VISIBLE);
					mLayoutTranslate.setVisibility(View.GONE);
				}
			}
		}
	}

	//@OnClick(R.id.fragment_main_btn_translate)
	public void onButtonClickTranslate(View view)
	{
		mHandler.removeCallbacks(mRunnableTextTop);
		doTranslateText();
	}

	@Override
	public void onSupportedLangsUpdate(YandexTranslateAPITask task, Set<String> dirs, Map<String, String> langs)
	{

	}

	@Override
	public void onDetectedLangUpdate(YandexTranslateAPITask task, String detected_lang)
	{

	}

	@Override
	public void onTranslationUpdate(YandexTranslateAPITask task, String detected_lang, String detected_dir, String text)
	{
		if (mEditTextBottom != null)
		{
			mEditTextBottom.setText(text);
			doSaveHistory(mDataProvider.getTranslateAPI().getLastSourceText(), text);
		}
	}

	@Override
	public void onHttpRequestResultError(YandexTranslateAPITask task, int http_status_code)
	{

	}

}
