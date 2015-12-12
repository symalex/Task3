package com.symbysoft.task3.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.symbysoft.task3.MainApp;
import com.symbysoft.task3.R;
import com.symbysoft.task3.data.DataProvider;
import com.symbysoft.task3.data.DatabaseHelper;
import com.symbysoft.task3.data.LocalDataBaseTask;
import com.symbysoft.task3.data.LocalDataBaseTask.LocalDataBaseNotification;
import com.symbysoft.task3.ui.activities.MainActivity;

import android.widget.AdapterView.OnItemClickListener;

public class HistoryFragment extends Fragment implements OnItemClickListener, LocalDataBaseNotification
{
	private final String TAG = "HistoryFragment";
	public static final String FTAG = "history_fragment";

	private final String PREF = "history";
	private final String HISTORY_LIST_INDEX = "hist_list_index";

	@Bind(R.id.fragment_history_list_view)
	protected ListView mListView;

	private MenuItem mMenuItemFavorite;
	private MenuItem mMenuItemDelete;

	private DataProvider mDataProvider;
	private int mPosition;
	private SharedPreferences mPref;

	public static Fragment newInstance()
	{
		return new HistoryFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_history, container, false);
		ButterKnife.bind(this, view);

		mDataProvider = ((MainApp) getContext().getApplicationContext()).getDataProvider();
		mDataProvider.getLocalDataBase().addDBNotification(this);
		updateList();

		mListView.setSelector(R.drawable.list_selector);
		mListView.setOnItemClickListener(this);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setHasOptionsMenu(true);

		mPref = getContext().getSharedPreferences(PREF, 0);
		mPosition = mPref.getInt(HISTORY_LIST_INDEX, -1);

		return view;
	}

	private void updateSelection()
	{
		if (mListView != null && mPosition >= 0 && mPosition < mDataProvider.getHistoryList().size())
		{
			//int pos = mListView.getSelectedItemPosition();
			mListView.setSelection(mPosition);
		}
	}

	private void updateList()
	{
		List<String> lines = new ArrayList<>();
		for (ContentValues cv : mDataProvider.getHistoryList())
		{
			lines.add((String) cv.get(DatabaseHelper.HIST_SOURCE));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, lines);
		mListView.setAdapter(adapter);
		updateSelection();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		mPosition = position;
		if (mMenuItemFavorite != null)
		{
			mMenuItemFavorite.setVisible(true);
		}
		if (mMenuItemDelete != null)
		{
			mMenuItemDelete.setVisible(true);
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.history_menu, menu);

		menu.findItem(R.id.action_settings).setVisible(false);
		if (mListView.getSelectedItem() == null)
		{
			mMenuItemFavorite = menu.findItem(R.id.history_menu_action_bookmark);
			mMenuItemFavorite.setVisible(false);
			mMenuItemDelete = menu.findItem(R.id.history_menu_action_delete);
			mMenuItemDelete.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		ContentValues cv;
		switch (item.getItemId())
		{
			case R.id.history_menu_action_go:
				if (mPosition >= 0 && mPosition < mDataProvider.getHistoryList().size() && getActivity() instanceof MainActivity)
				{
					((MainActivity) getActivity()).gotoMainAndSetData(mDataProvider.getHistoryList().get(mPosition));
				}
				return true;

			case R.id.history_menu_action_bookmark:
				if (mPosition >= 0 && mPosition < mDataProvider.getHistoryList().size())
				{
					cv = mDataProvider.getHistoryList().get(mPosition);
					mDataProvider.getLocalDataBase().addToFavorite(cv.getAsLong(DatabaseHelper.KEY_ID));
				}
				return true;

			case R.id.history_menu_action_delete:
				if (mPosition >= 0 && mPosition < mDataProvider.getHistoryList().size())
				{
					mDataProvider.getLocalDataBase().delFromHistory(mDataProvider.getHistoryList().get(mPosition).getAsLong(DatabaseHelper.KEY_ID));
				}
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();

		Log.d(TAG, "onStart()");

		if (mPref != null)
		{
			mPosition = mPref.getInt(HISTORY_LIST_INDEX, -1);
			updateSelection();
		}

		if (mDataProvider != null)
		{
			mDataProvider.getLocalDataBase().addDBNotification(this);
		}
	}

	@Override
	public void onStop()
	{
		Log.d(TAG, "onStop()");

		if (mDataProvider != null)
		{
			mDataProvider.getLocalDataBase().removeDBNotification(this);
		}

		if (mPref != null)
		{
			SharedPreferences.Editor ed = mPref.edit();
			ed.putInt(HISTORY_LIST_INDEX, mPosition);
			ed.commit();
		}

		super.onStop();
	}

	@Override
	public void onDBReadHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBReadFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
	}

	@Override
	public void onDBAddHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBDelHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBAddFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
	}

	@Override
	public void onDBDelFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
	}

}
