package com.symbysoft.task3.ui.activities;

import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.symbysoft.task3.data.HistoryRow;
import com.symbysoft.task3.ui.fragments.FavoriteFragment;
import com.symbysoft.task3.ui.fragments.HistoryFragment;
import com.symbysoft.task3.MainApp;
import com.symbysoft.task3.ui.fragments.MainFragment;
import com.symbysoft.task3.R;
import com.symbysoft.task3.data.DataProvider;
import com.symbysoft.task3.data.DatabaseHelper;
import com.symbysoft.task3.ui.fragments.SettingsFragment;
import com.symbysoft.task3.network.InternetReceiver;
import com.symbysoft.task3.network.InternetReceiver.InternetReceiverListener;
import com.symbysoft.task3.data.DataProvider.DataProviderListener;
import com.symbysoft.task3.network.YandexTranslateAPI;
import com.symbysoft.task3.network.YandexTranslateAPI.YandexTranslateApiListener;

import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener, InternetReceiverListener, YandexTranslateApiListener, DataProviderListener
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
	protected Toolbar mToolbar;
	@Bind(R.id.drawer_layout)
	protected DrawerLayout mDrawer;
	@Bind(R.id.nav_view)
	protected NavigationView mNavigationView;
	@Bind(R.id.app_bar_main_btn_translate)
	protected FloatingActionButton mBtnTranslate;

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

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mDrawer.setDrawerListener(toggle);
		toggle.syncState();

		mNavigationView.setNavigationItemSelectedListener(this);

		mDataProvider = ((MainApp) getApplication()).getDataProvider();
		mDataProvider.addDataProviderListener(this);
		mDataProvider.getInternetReceiver().addInternetReceiverListener(this);
		mDataProvider.getTranslateAPI().addApiListener(this);

		// create fragment page
		navigateFragment(mDataProvider.getActivePage());

		if (!mDataProvider.isDataLoaded())
		{
			intent = new Intent(this, SplashActivity.class);
			startActivity(intent);
		}
	}

	@OnClick(R.id.app_bar_main_btn_translate)
	public void onButtonClickTranslate(View view)
	{
		if (mFragment != null && mFragment instanceof MainFragment)
		{
			((MainFragment) mFragment).onButtonClickTranslate(view);
		}
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
			mDataProvider.getInternetReceiver().removeInternetReceiverListener(this);
			mDataProvider.removeDataProviderListener(this);
			mDataProvider.getTranslateAPI().removeApiListener(this);
			mDataProvider.onDestroy();
		}
		super.onDestroy();
	}

	@Override
	public void onInternetConnectionChange(InternetReceiver receiver)
	{
		if (mDataProvider != null && receiver.isConnectionOk() &&
				mDataProvider.getSettings().getTranslateAPIData().isApiDataInitialized() &&
				!mDataProvider.getSettings().getTranslateAPIData().isLanguageDataReady())
		{
			mDataProvider.getTranslateAPI().update();
		}
		mBtnTranslate.setVisibility(receiver.isConnectionOk() ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onSupportedLangsUpdate(YandexTranslateAPI api, Set<String> dirs, Map<String, String> langs)
	{
	}

	@Override
	public void onDetectedLangUpdate(YandexTranslateAPI api, String detected_lang)
	{
	}

	@Override
	public void onTranslationUpdate(YandexTranslateAPI api, String detected_lang, String detected_dir, String text)
	{
	}

	@Override
	public void onHttpRequestResultError(YandexTranslateAPI api, int http_status_code, String message)
	{
		if (http_status_code == 403)
		{
			String msg = "Incorrect API key!";
			Snackbar.make(mDrawer, msg, Snackbar.LENGTH_LONG).show();
		}
		else
		{
			String msg;
			if (http_status_code != 0)
			{
				msg = String.format("HTTP error: %d", http_status_code);
			}
			else
			{
				msg = String.format("HTTP error: %s", message);
			}
			Snackbar.make(mDrawer, msg, Snackbar.LENGTH_LONG).show();
		}

		if (mFragment != null && mFragment instanceof YandexTranslateApiListener)
		{
			((YandexTranslateApiListener) mFragment).onHttpRequestResultError(api, http_status_code, message);
		}
	}

	@Override
	public void onLoadDataComplete()
	{
		if (mFragment != null && mFragment instanceof DataProviderListener)
		{
			((DataProviderListener) mFragment).onLoadDataComplete();
		}
	}

	private void saveData()
	{
		mDataProvider.saveData();
	}

	public void gotoMainAndSetData(HistoryRow row)
	{
		mDataProvider.getSettings().getTranslateAPIData().setTranslateDirection(row.getDirection());
		mDataProvider.getSettings().getTranslateAPIData().setSrcText(row.getSource());
		mDataProvider.getSettings().getTranslateAPIData().setDestText(row.getDestination());
		navigateFragment(MainActivity.FragmentPage.MAIN_FRAGMENT);
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
					if (mDataProvider != null && mDataProvider.getInternetReceiver().isConnectionOk())
					{
						mBtnTranslate.setVisibility(View.VISIBLE);
					}
					((MainFragment) mFragment).setBtnTranslate(mBtnTranslate);
					break;

				case HISTORY_FRAGMENT:
					mBtnTranslate.setVisibility(View.GONE);
					mFragment = setFragment(R.id.app_bar_main_frame_main_container_id, HistoryFragment.newInstance(), HistoryFragment.FTAG);
					mNavigationView.setCheckedItem(R.id.nav_history);
					break;

				case FAVORITES_FRAGMENT:
					mBtnTranslate.setVisibility(View.GONE);
					mFragment = setFragment(R.id.app_bar_main_frame_main_container_id, FavoriteFragment.newInstance(), FavoriteFragment.FTAG);
					mNavigationView.setCheckedItem(R.id.nav_favorites);
					break;

				case SETTING_FRAGMENT:
					mBtnTranslate.setVisibility(View.GONE);
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
		else if( mFragment instanceof SettingsFragment || mFragment instanceof HistoryFragment || mFragment instanceof FavoriteFragment )
		{
			navigateFragment(FragmentPage.MAIN_FRAGMENT);
		}
		else
		{
			// in MainFragment
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
		switch (item.getItemId())
		{
			case R.id.nav_home:
				doMenuHome();
				break;

			case R.id.nav_history:
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
