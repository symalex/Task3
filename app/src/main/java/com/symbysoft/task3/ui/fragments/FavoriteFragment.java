package com.symbysoft.task3.ui.fragments;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import com.symbysoft.task3.MainApp;
import com.symbysoft.task3.R;
import com.symbysoft.task3.adapters.FavoriteRecyclerAdapter;
import com.symbysoft.task3.common.helper;
import com.symbysoft.task3.data.DataProvider;
import com.symbysoft.task3.data.FavoriteRow;
import com.symbysoft.task3.data.HistoryRow;
import com.symbysoft.task3.data.LocalDataBaseTask;
import com.symbysoft.task3.data.LocalDataBaseTask.LocalDataBaseListener;
import com.symbysoft.task3.ui.activities.MainActivity;

import com.symbysoft.task3.adapters.FavoriteRecyclerAdapter.FavoriteRecyclerItemClickListener;

public class FavoriteFragment extends Fragment implements LocalDataBaseListener, FavoriteRecyclerItemClickListener
{
	public static final String TAG = "FavoriteFragment";
	public static final String FTAG = "favorite_fragment";

	@Bind(R.id.fragment_history_list_view)
	protected RecyclerView mRecyclerView;
	private RecyclerView.LayoutManager mLayoutManager;
	private FavoriteRecyclerAdapter mAdapter;
	private Menu mMenu;

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

		//RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
		RecyclerView.ItemAnimator itemAnimator = new SlideInUpAnimator();
		mRecyclerView.setItemAnimator(itemAnimator);

		// specify an adapter (see also next example)
		mAdapter = new FavoriteRecyclerAdapter(getActivity(), mDataProvider.getFavoriteList());
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
			mAdapter.setList(mDataProvider.getFavoriteList());
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(FavoriteRecyclerAdapter adapter, View view, int position, long id, boolean is_long_click)
	{
		switch (view.getId())
		{
			case R.id.item_history_card_view_layout:

				if (mDataProvider != null)
				{
					mDataProvider.setFavoriteSelectedItemPosition(position);
				}
				UpdateMenu();
				break;
		}
	}

	private void UpdateMenu()
	{
		if (mMenu != null)
		{
			mMenu.findItem(R.id.action_settings).setVisible(false);
			if (mAdapter != null)
			{
				mMenu.findItem(R.id.favorite_menu_action_go).setVisible(mAdapter.getSelectedPosition() != -1);
				mMenu.findItem(R.id.favorite_menu_action_delete).setVisible(mAdapter.getSelectedPosition() != -1);
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		mMenu = menu;

		inflater.inflate(R.menu.favorite_menu, menu);
		UpdateMenu();
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
					if (mAdapter != null)
					{
						mAdapter.notifyItemRemoved(pos);
					}
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
		Log.d(TAG, helper.getMethodName(this, 0));
	}

	@Override
	public void onDBReadFavoriteComplete(LocalDataBaseTask task, List<FavoriteRow> list)
	{
		updateList();
		Log.d(TAG, helper.getMethodName(this, 0));
	}

	@Override
	public void onDBAddHistoryComplete(LocalDataBaseTask task, HistoryRow row)
	{
		Log.d(TAG, helper.getMethodName(this, 0));
	}

	@Override
	public void onDBDelHistoryComplete(LocalDataBaseTask task, int result)
	{
		Log.d(TAG, helper.getMethodName(this, 0));
	}

	@Override
	public void onDBAddFavoriteComplete(LocalDataBaseTask task, FavoriteRow row)
	{
		Log.d(TAG, helper.getMethodName(this, 0));
	}

	@Override
	public void onDBDelFavoriteComplete(LocalDataBaseTask task, int result)
	{
		Log.d(TAG, helper.getMethodName(this, 0));
	}
}
