package com.symbysoft.task3.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.symbysoft.task3.adapters.HistoryRecyclerAdapter;
import com.symbysoft.task3.data.DataProvider;
import com.symbysoft.task3.data.DatabaseHelper;
import com.symbysoft.task3.data.LocalDataBaseTask;
import com.symbysoft.task3.data.LocalDataBaseTask.LocalDataBaseListener;
import com.symbysoft.task3.ui.activities.MainActivity;

import android.widget.AdapterView.OnItemClickListener;

import com.symbysoft.task3.adapters.HistoryRecyclerAdapter.HistoryRecyclerItemClickListener;

//http://developer.android.com/intl/ru/training/material/lists-cards.html
//http://code.tutsplus.com/tutorials/getting-started-with-recyclerview-and-cardview-on-android--cms-23465

public class HistoryFragment extends Fragment implements LocalDataBaseListener, HistoryRecyclerItemClickListener
{
	private final String TAG = "HistoryFragment";
	public static final String FTAG = "history_fragment";

	private final String PREF = "history";
	private final String HISTORY_LIST_INDEX = "hist_list_index";

	@Bind(R.id.fragment_history_list_view)
	protected RecyclerView mRecyclerView;
	private LinearLayoutManager mLayoutManager;
	private HistoryRecyclerAdapter mAdapter;

	private MenuItem mMenuItemFavorite;
	private MenuItem mMenuItemDelete;

	private DataProvider mDataProvider;

	private final ItemTouchHelper.SimpleCallback mItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
	{
		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
		{
			return false;
		}

		@Override
		public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir)
		{
			if (mAdapter != null)
			{
				mAdapter.setSelectedPosition(viewHolder.getAdapterPosition());
			}
			switch (swipeDir)
			{
				case ItemTouchHelper.LEFT:
					Log.d(TAG, "add to faworite");
					startAction(R.id.history_menu_action_bookmark);
					break;

				case ItemTouchHelper.RIGHT:
					Log.d(TAG, "Delete item");
					startAction(R.id.history_menu_action_delete);
					break;
			}
		}
	};
	private final ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(mItemTouchCallback);

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
		mDataProvider.getLocalDataBase().addListener(this);

		// use this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mRecyclerView.setHasFixedSize(true);

		// use a linear layout manager
		mLayoutManager = new LinearLayoutManager(getContext());
		mRecyclerView.setLayoutManager(mLayoutManager);

		// specify an adapter (see also next example)
		mAdapter = new HistoryRecyclerAdapter(mDataProvider.getHistoryList());
		mAdapter.setOnItemClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
		mItemTouchHelper.attachToRecyclerView(mRecyclerView);

		mAdapter.setSelectedPosition(mDataProvider.getHistorySelectedItemPosition());

		setHasOptionsMenu(true);

		return view;
	}

	@Override
	public void onItemClick(HistoryRecyclerAdapter adapter, View view, int position, long id)
	{
		switch (view.getId())
		{
			case R.id.item_history_card_view_layout:
				if (mDataProvider != null)
				{
					mDataProvider.setHistorySelectedItemPosition(position);
				}
				if (mMenuItemFavorite != null)
				{
					mMenuItemFavorite.setVisible(true);
				}
				if (mMenuItemDelete != null)
				{
					mMenuItemDelete.setVisible(true);
				}
				break;

			case R.id.item_history_btn_favorite:
				if (mDataProvider != null && position >= 0 && position < mDataProvider.getHistoryList().size())
				{
					ContentValues cv = mDataProvider.getHistoryList().get(position);
					long in_fav_id = cv.getAsLong(DatabaseHelper.IN_FAVORITE_ID);
					if (in_fav_id == 0)
					{
						startAction(R.id.history_menu_action_bookmark);
					}
					else
					{
						// remove from favorites
						mDataProvider.getLocalDataBase().delFromFavorite(in_fav_id);
					}
				}
				break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.history_menu, menu);

		menu.findItem(R.id.action_settings).setVisible(false);

		if (mAdapter != null && mAdapter.getSelectedPosition() == -1)
		{
			mMenuItemFavorite = menu.findItem(R.id.history_menu_action_bookmark);
			mMenuItemFavorite.setVisible(false);
			mMenuItemDelete = menu.findItem(R.id.history_menu_action_delete);
			mMenuItemDelete.setVisible(false);
		}
	}

	private void startAction(int action_id)
	{
		ContentValues cv;
		int pos = mAdapter != null ? mAdapter.getSelectedPosition() : -1;
		switch (action_id)
		{
			case R.id.history_menu_action_go:
				if (pos >= 0 && pos < mDataProvider.getHistoryList().size() && getActivity() instanceof MainActivity)
				{
					((MainActivity) getActivity()).gotoMainAndSetData(mDataProvider.getHistoryList().get(pos));
				}
				break;

			case R.id.history_menu_action_bookmark:
				if (pos >= 0 && pos < mDataProvider.getHistoryList().size())
				{
					cv = mDataProvider.getHistoryList().get(pos);
					mDataProvider.getLocalDataBase().addToFavorite(cv.getAsLong(DatabaseHelper.KEY_ID));
				}
				break;

			case R.id.history_menu_action_delete:
				if (pos >= 0 && pos < mDataProvider.getHistoryList().size())
				{
					mDataProvider.getLocalDataBase().delFromHistory(mDataProvider.getHistoryList().get(pos).getAsLong(DatabaseHelper.KEY_ID));
				}
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.history_menu_action_go:
			case R.id.history_menu_action_bookmark:
			case R.id.history_menu_action_delete:
				startAction(item.getItemId());
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

		if (mDataProvider != null)
		{
			mDataProvider.getLocalDataBase().addListener(this);
		}
	}

	@Override
	public void onStop()
	{
		Log.d(TAG, "onStop()");

		if (mDataProvider != null)
		{
			mDataProvider.getLocalDataBase().removeListener(this);
		}

		super.onStop();
	}

	private void updateList()
	{
		if (mAdapter != null)
		{
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onDBReadHistoryComplete(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBReadFavoriteComplete(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBAddHistoryComplete(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBDelHistoryComplete(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBAddFavoriteComplete(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBDelFavoriteComplete(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

}
