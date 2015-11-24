package com.symbysoft.task3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity
{
	private DataProvider mDataProvider;
	private boolean mAppExitFlag = false;
	private Toast mToast;
	private Handler mHandler = new Handler();
	@Bind(R.id.activity_splash_progressbar)
	protected ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().hide();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		setContentView(R.layout.activity_splash);

		ButterKnife.bind(this);

		mDataProvider = ((MainApp) getApplication()).getDataProvider();

		// start resource loading
		mDataProvider.loadData();
		updateProgressLoop(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				if (!mAppExitFlag)
				{
					Context context = getApplicationContext();
					CharSequence text = "Back key pressed";
					int duration = Toast.LENGTH_LONG;

					mToast = Toast.makeText(context, text, duration);
					mToast.show();
					mAppExitFlag = true;

					mHandler.postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							mToast.cancel();
							mAppExitFlag = false;
						}
					}, 3000);
				}
				else
				{
					// exit from application
					mToast.cancel();
					Intent intent = new Intent(this, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("exit", "true");
					startActivity(intent);
					finish();
				}

				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void updateProgressLoop(boolean init)
	{
		if (init)
		{
			mProgressBar.setProgress(0);
			mProgressBar.setMax(mDataProvider.getProgressMax());
		}
		mHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (!mDataProvider.isDataLoaded())
				{
					mProgressBar.setProgress(mDataProvider.getProgress());
					updateProgressLoop(false);
				}
				else
				{
					finish();
				}
			}
		}, 100);
	}
}
