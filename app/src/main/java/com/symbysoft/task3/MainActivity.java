package com.symbysoft.task3;

import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, InternetReceiverNotification, YandexTranslateAPINotification, DataProviderNotification, LocalDataBaseNotification
{
	private static final String TAG = "MainActivity";

	public enum FragmentPage
	{
		MAIN_FRAGMENT,
		HISTORY_FRAGMENT,
		FAVORITES_FRAGMENT,
		SETTING_FRAGMENT
	}

	@Bind(R.id.toolbar)
	Toolbar mToolbar;
	@Bind(R.id.drawer_layout)
	DrawerLayout mDrawer;
	@Bind(R.id.nav_view)
	NavigationView mNavigationView;

	private boolean mExitFlag = false;
	private DataProvider mDataProvider;
	private FragmentPage mCurPage = FragmentPage.MAIN_FRAGMENT;
	private Fragment mFragment;

	public FragmentPage getCurPage()
	{
		return mCurPage;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent.hasExtra("exit"))
		{
			finish();
			return;
		}

		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		setSupportActionBar(mToolbar);

		/*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});*/

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mDrawer.setDrawerListener(toggle);
		toggle.syncState();

		mNavigationView.setNavigationItemSelectedListener(this);

		mDataProvider = ((MainApp) getApplication()).getDataProvider();
		mDataProvider.setDataProviderNotification(this);
		mDataProvider.getInternetReceiver().setInternetReceiverNotification(this);
		mDataProvider.getTranslateAPI().setAPINotification(this);
		mDataProvider.getLocalDataBase().setDBNotification(this);

		// create fragment page
		navigateFragment(mDataProvider.getActivePage());

		if (!mDataProvider.isDataLoaded())
		{
			intent = new Intent(this, SplashActivity.class);
			startActivity(intent);
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		saveData();
	}

	@Override
	protected void onDestroy()
	{
		if (mDataProvider != null && mExitFlag)
		{
			mDataProvider.onDestroy();
		}
		super.onDestroy();
	}

	@Override
	public void onInternetConnectionChange(InternetReceiver receiver)
	{
		if (mFragment != null && mFragment instanceof InternetReceiverNotification && mDataProvider != null)
		{
			if (receiver.isConnectionOk() && !mDataProvider.getSettings().getTranslateAPIData().isLanguageDataReady())
			{
				mDataProvider.getTranslateAPI().update();
			}
			((MainFragment) mFragment).onInternetConnectionChange(receiver);
		}
	}

	@Override
	public void onSupportedLangsUpdate(YandexTranslateAPITask task, Set<String> dirs, Map<String, String> langs)
	{
		// save dirs & langs
		if (mDataProvider != null)
		{
			mDataProvider.getSettings().onSupportedLangsUpdate(task, dirs, langs);
		}

		// fragment notification
		if (mFragment != null && mFragment instanceof YandexTranslateAPINotification)
		{
			((YandexTranslateAPINotification) mFragment).onSupportedLangsUpdate(task, dirs, langs);
		}
	}

	@Override
	public void onDetectedLangUpdate(YandexTranslateAPITask task, String detected_lang)
	{
		// save dirs & langs
		if (mDataProvider != null)
		{
			mDataProvider.getSettings().onDetectedLangUpdate(task, detected_lang);
		}

		if (mFragment != null && mFragment instanceof YandexTranslateAPINotification)
		{
			((YandexTranslateAPINotification) mFragment).onDetectedLangUpdate(task, detected_lang);
		}
	}

	@Override
	public void onTranslationUpdate(YandexTranslateAPITask task, String detected_lang, String detected_dir, String text)
	{
		// save dirs & langs
		if (mDataProvider != null)
		{
			mDataProvider.getSettings().onTranslationUpdate(task, detected_lang, detected_dir, text);
		}

		if (mFragment != null && mFragment instanceof YandexTranslateAPINotification)
		{
			((YandexTranslateAPINotification) mFragment).onTranslationUpdate(task, detected_lang, detected_dir, text);
		}
	}

	@Override
	public void onHttpRequestResultError(YandexTranslateAPITask task, int http_status_code)
	{
		if (http_status_code == 403)
		{
			String msg = "Incorrect API key!";
			Snackbar.make(mDrawer, msg, Snackbar.LENGTH_LONG).show();
		}
		else
		{
			String msg = String.format("HTTP error: %d", http_status_code);
			Snackbar.make(mDrawer, msg, Snackbar.LENGTH_LONG).show();
		}

		if (mFragment != null && mFragment instanceof YandexTranslateAPINotification)
		{
			((YandexTranslateAPINotification) mFragment).onHttpRequestResultError(task, http_status_code);
		}
	}

	@Override
	public void onLoadDataComplette()
	{
		if (mFragment != null && mFragment instanceof DataProviderNotification)
		{
			((DataProviderNotification) mFragment).onLoadDataComplette();
		}
	}

	@Override
	public void onDBReadHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		if (mDataProvider != null)
		{
			mDataProvider.onDBReadHistoryComplette(task, list);
		}

		if (mFragment != null && mFragment instanceof LocalDataBaseNotification)
		{
			((LocalDataBaseNotification) mFragment).onDBReadHistoryComplette(task, list);
		}
	}

	@Override
	public void onDBReadFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		if (mDataProvider != null)
		{
			mDataProvider.onDBReadFavoriteComplette(task, list);
		}

		if (mFragment != null && mFragment instanceof LocalDataBaseNotification)
		{
			((LocalDataBaseNotification) mFragment).onDBReadFavoriteComplette(task, list);
		}
	}

	@Override
	public void onDBAddHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		if (mDataProvider != null)
		{
			mDataProvider.onDBAddHistoryComplette(task, list);
		}

		if (mFragment != null && mFragment instanceof LocalDataBaseNotification)
		{
			((LocalDataBaseNotification) mFragment).onDBAddHistoryComplette(task, list);
		}
	}

	@Override
	public void onDBDelHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		if (mDataProvider != null)
		{
			mDataProvider.onDBDelHistoryComplette(task, list);
		}

		if (mFragment != null && mFragment instanceof LocalDataBaseNotification)
		{
			((LocalDataBaseNotification) mFragment).onDBDelHistoryComplette(task, list);
		}
	}

	@Override
	public void onDBAddFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		if (mDataProvider != null)
		{
			mDataProvider.onDBAddFavoriteComplette(task, list);
		}

		if (mFragment != null && mFragment instanceof LocalDataBaseNotification)
		{
			((LocalDataBaseNotification) mFragment).onDBAddFavoriteComplette(task, list);
		}
	}

	@Override
	public void onDBDelFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		if (mDataProvider != null)
		{
			mDataProvider.onDBDelFavoriteComplette(task, list);
		}

		if (mFragment != null && mFragment instanceof LocalDataBaseNotification)
		{
			((LocalDataBaseNotification) mFragment).onDBDelFavoriteComplette(task, list);
		}
	}

	protected void saveData()
	{
		mDataProvider.saveData();
	}

	public void navigateFragment(FragmentPage page)
	{
		if (mCurPage != page || mFragment == null)
		{
			switch (page)
			{
				case MAIN_FRAGMENT:
					mFragment = setFragment(R.id.app_bar_main_frame_main_container_id, MainFragment.newInstance(), MainFragment.FTAG);
					mNavigationView.setCheckedItem(R.id.nav_home);
					break;

				case HISTORY_FRAGMENT:
					mFragment = setFragment(R.id.app_bar_main_frame_main_container_id, HistoryFragment.newInstance(), HistoryFragment.FTAG);
					mNavigationView.setCheckedItem(R.id.nav_histrory);
					break;

				case FAVORITES_FRAGMENT:
					mFragment = setFragment(R.id.app_bar_main_frame_main_container_id, FavoriteFragment.newInstance(), FavoriteFragment.FTAG);
					mNavigationView.setCheckedItem(R.id.nav_favorites);
					break;

				case SETTING_FRAGMENT:
					if (mDataProvider != null)
					{
						mFragment = setFragment(R.id.app_bar_main_frame_main_container_id, SettingsFragment.newInstance(), SettingsFragment.FTAG);
						mNavigationView.setCheckedItem(R.id.nav_settings);
					}
					else
					{
						Snackbar.make(mDrawer, "Language data not ready", Snackbar.LENGTH_LONG).show();
					}
					break;
			}
			mCurPage = page;
			mDataProvider.setActivePage(mCurPage);
		}
	}

	public void setSourceTextInfo(String src_info)
	{
		if (mFragment != null && mFragment instanceof MainFragment)
		{
			((MainFragment) mFragment).setSourceTextInfo(src_info);
		}
	}

	public void setDestinationTextInfo(String dst_info)
	{
		if (mFragment != null && mFragment instanceof MainFragment)
		{
			((MainFragment) mFragment).setDestinationTextInfo(dst_info);
		}
	}

	private Fragment setFragment(int id, Fragment fragment, String tag)
	{
		FragmentManager fm = getSupportFragmentManager();
		int nfragments = fm.getFragments() != null ? fm.getFragments().size() : 0;

		Fragment fr = fm.findFragmentByTag(tag);
		if (fr == null)
		{
			Log.d(TAG, this + ": Existing fragment not found. ");

			FragmentTransaction ft = fm.beginTransaction();
			if (nfragments > 0)
			{
				ft.replace(id, fragment, tag);
			}
			else
			{
				ft.add(id, fragment, tag);
			}
			ft.commit();
		}
		else
		{
			Log.d(TAG, this + ": Existing fragment found.");
			fragment = fr;
		}

		return fragment;
	}

	@Override
	public void onBackPressed()
	{
		if (mDrawer != null && mDrawer.isDrawerOpen(GravityCompat.START))
		{
			mDrawer.closeDrawer(GravityCompat.START);
		}
		else
		{
			mExitFlag = true;
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			doMenuSettings();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item)
	{
		// Handle navigation view item clicks here.
		switch (item.getItemId())
		{
			case R.id.nav_home:
				doMenuHome();
				break;

			case R.id.nav_histrory:
				doMenuHistory();
				break;

			case R.id.nav_favorites:
				doMenuFavorites();
				break;

			case R.id.nav_settings:
				doMenuSettings();
				break;

			case R.id.nav_exit:
				doMenuExit();
				break;
		}

		mDrawer.closeDrawer(GravityCompat.START);
		return true;
	}

	private void doMenuHome()
	{
		navigateFragment(FragmentPage.MAIN_FRAGMENT);
	}

	private void doMenuHistory()
	{
		navigateFragment(FragmentPage.HISTORY_FRAGMENT);
	}

	private void doMenuFavorites()
	{
		navigateFragment(FragmentPage.FAVORITES_FRAGMENT);
	}

	private void doMenuSettings()
	{
		navigateFragment(FragmentPage.SETTING_FRAGMENT);
	}

	private void doMenuExit()
	{
		mExitFlag = true;
		mNavigationView.setCheckedItem(R.id.nav_exit);
		finish();
	}
}
