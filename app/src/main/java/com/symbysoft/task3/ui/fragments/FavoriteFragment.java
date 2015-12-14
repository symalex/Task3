package com.symbysoft.task3.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
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
import com.symbysoft.task3.adapters.FavoriteRecyclerAdapter;
import com.symbysoft.task3.adapters.HistoryRecyclerAdapter;
import com.symbysoft.task3.data.DataProvider;
import com.symbysoft.task3.data.DatabaseHelper;
import com.symbysoft.task3.data.FavoriteRow;
import com.symbysoft.task3.data.HistoryRow;
import com.symbysoft.task3.data.LocalDataBaseTask;
import com.symbysoft.task3.data.LocalDataBaseTask.LocalDataBaseListener;
import com.symbysoft.task3.ui.activities.MainActivity;

import android.widget.AdapterView.OnItemClickListener;

import com.symbysoft.task3.adapters.FavoriteRecyclerAdapter.FavoriteRecyclerItemClickListener;

public class FavoriteFragment extends Fragment implements LocalDataBaseListener, FavoriteRecyclerItemClickListener
{
	public static final String TAG = "FavoriteFragment";
	public static final String FTAG = "favorite_fragment";

	@Bind(R.id.fragment_history_list_view)
	protected RecyclerView mRecyclerView;
	private RecyclerView.LayoutManager mLayoutManager;
	private FavoriteRecyclerAdapter mAdapter;

	private DataProvider mDataProvider;

	private MenuItem mMenuItemDelete;

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
					break;

				case ItemTouchHelper.RIGHT:
					Log.d(TAG, "Delete item");
					startAction(R.id.favorite_menu_action_delete);
					break;
			}
		}
	};
	private final ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(mItemTouchCallback);

	public static Fragment newInstance()
	{
		return new FavoriteFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_favorite, container, false);
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
		mAdapter = new FavoriteRecyclerAdapter(mDataProvider.getFavoriteList());
		mAdapter.setOnItemClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
		mItemTouchHelper.attachToRecyclerView(mRecyclerView);

		mAdapter.setSelectedPosition(mDataProvider.getFavoriteSelectedItemPosition());

		setHasOptionsMenu(true);

		return view;
	}

	private void updateList()
	{
		if (mAdapter != null)
		{
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(FavoriteRecyclerAdapter adapter, View view, int position, long id)
	{
		switch (view.getId())
		{
			case R.id.item_history_card_view_layout:

				if (mDataProvider != null)
				{
					mDataProvider.setFavoriteSelectedItemPosition(position);
				}
				if (mMenuItemDelete != null)
				{
					mMenuItemDelete.setVisible(true);
				}
				break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.favorite_menu, menu);

		menu.findItem(R.id.action_settings).setVisible(false);
		if (mAdapter != null && mAdapter.getSelectedPosition() == -1)
		{
			mMenuItemDelete = menu.findItem(R.id.favorite_menu_action_delete);
			mMenuItemDelete.setVisible(false);
		}
	}

	private void startAction(int action_id)
	{
		FavoriteRow fav_row;
		int pos = mAdapter != null ? mAdapter.getSelectedPosition() : -1;
		switch (action_id)
		{
			case R.id.favorite_menu_action_go:
				if (pos >= 0 && pos < mDataProvider.getFavoriteList().size() && getActivity() instanceof MainActivity)
				{
					fav_row = mDataProvider.getFavoriteList().get(pos);
					((MainActivity) getActivity()).gotoMainAndSetData(fav_row.getHistory());
				}
				break;

			case R.id.favorite_menu_action_delete:
				if (pos >= 0 && pos < mDataProvider.getFavoriteList().size())
				{
					fav_row = mDataProvider.getFavoriteList().get(pos);
					mDataProvider.getLocalDataBase().delFromFavorite(fav_row.getId());
				}
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.favorite_menu_action_go:
			case R.id.favorite_menu_action_delete:
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

		if (mDataProvider != null)
		{
			mDataProvider.getLocalDataBase().addListener(this);
		}
	}

	@Override
	public void onStop()
	{
		if (mDataProvider != null)
		{
			mDataProvider.getLocalDataBase().removeListener(this);
		}

		super.onStop();
	}

	@Override
	public void onDBReadHistoryComplete(LocalDataBaseTask task, List<HistoryRow> list)
	{
	}

	@Override
	public void onDBReadFavoriteComplete(LocalDataBaseTask task, List<FavoriteRow> list)
	{
		updateList();
	}

	@Override
	public void onDBAddHistoryComplete(LocalDataBaseTask task, HistoryRow row)
	{
	}

	@Override
	public void onDBDelHistoryComplete(LocalDataBaseTask task, int result)
	{
	}

	@Override
	public void onDBAddFavoriteComplete(LocalDataBaseTask task, FavoriteRow row)
	{
		updateList();
	}

	@Override
	public void onDBDelFavoriteComplete(LocalDataBaseTask task, int result)
	{
		updateList();
	}

}
