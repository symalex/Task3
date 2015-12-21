package com.symbysoft.task3.ui.fragments;

import java.util.List;

import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
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

import com.symbysoft.task3.MainApp;
import com.symbysoft.task3.R;
import com.symbysoft.task3.adapters.FavoriteRecyclerAdapter;
import com.symbysoft.task3.adapters.HistoryRecyclerAdapter;
import com.symbysoft.task3.adapters.HistoryRecyclerAdapter.HistoryRecyclerItemClickListener;
import com.symbysoft.task3.common.helper;
import com.symbysoft.task3.data.DataProvider;
import com.symbysoft.task3.data.FavoriteRow;
import com.symbysoft.task3.data.HistoryRow;
import com.symbysoft.task3.data.LocalDataBaseTask;
import com.symbysoft.task3.data.LocalDataBaseTask.LocalDataBaseListener;
import com.symbysoft.task3.ui.activities.MainActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

//http://developer.android.com/intl/ru/training/material/lists-cards.html
//http://code.tutsplus.com/tutorials/getting-started-with-recyclerview-and-cardview-on-android--cms-23465

public class HistoryFragment extends Fragment implements LocalDataBaseListener, HistoryRecyclerItemClickListener, HistoryRecyclerAdapter.HistoryRecyclerItemActionListener
{
	private final String TAG = "HistoryFragment";
	public static final String FTAG = "history_fragment";

	private final String PREF = "history";
	private final String HISTORY_LIST_INDEX = "hist_list_index";

	@Bind(R.id.fragment_history_list_view)
	protected RecyclerView mRecyclerView;
	private RecyclerView.LayoutManager mLayoutManager;
	private HistoryRecyclerAdapter mAdapter;
	private Menu mMenu;
	private Snackbar mSnackbar;

	private MenuItem mMenuItemFavorite;
	private MenuItem mMenuItemDelete;

	private DataProvider mDataProvider;

	private final ItemTouchHelper.SimpleCallback mItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
	{
		private CardView mRequestCardView;
		private boolean mIsElevated;

		@Override
		public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
		{
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

			if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && isCurrentlyActive && !mIsElevated)
			{
				//final float newElevation = 5f + ViewCompat.getElevation(viewHolder.itemView);
				//ViewCompat.setElevation(viewHolder.itemView, newElevation);
				mIsElevated = true;
			}
			else
			{
				if (viewHolder instanceof FavoriteRecyclerAdapter.ViewHolder)
				{
					FavoriteRecyclerAdapter.ViewHolder holder = (FavoriteRecyclerAdapter.ViewHolder) viewHolder;
					CardView card = (CardView) holder.itemView;
				}
			}
		}

		@Override
		public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
		{
			super.clearView(recyclerView, viewHolder);

			if (viewHolder instanceof FavoriteRecyclerAdapter.ViewHolder)
			{
				FavoriteRecyclerAdapter.ViewHolder holder = (FavoriteRecyclerAdapter.ViewHolder) viewHolder;
				CardView card = (CardView) holder.itemView;
			}

			mIsElevated = false;
		}

		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target)
		{
			if (source.getItemViewType() != target.getItemViewType())
			{
				return false;
			}

			mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
			return true;
		}

		@Override
		public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir)
		{
			switch (swipeDir)
			{
				case ItemTouchHelper.LEFT:
				case ItemTouchHelper.RIGHT:
					requestDeleteItems(viewHolder.getAdapterPosition());
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
		RecyclerView.ItemAnimator itemAnimator = new SlideInUpAnimator();
		mRecyclerView.setItemAnimator(itemAnimator);

		// specify an adapter (see also next example)
		mAdapter = new HistoryRecyclerAdapter(getActivity(), mRecyclerView, mDataProvider.getHistoryList());
		mAdapter.setOnItemClickListener(this);
		mAdapter.setOnItemActionListener(this);
		mRecyclerView.setAdapter(mAdapter);
		mItemTouchHelper.attachToRecyclerView(mRecyclerView);

		//mAdapter.setSelectedPosition(mDataProvider.getHistorySelectedItemPosition());

		setHasOptionsMenu(true);

		return view;
	}

	private void requestDeleteItems(int position)
	{
		Log.d(TAG, "Request delete item: " + position);
		if (mAdapter != null)
		{
			if (mAdapter.isEmptySelections() || mAdapter.isGoSelection())
			{
				mAdapter.requestDelete(position);
			}
			else
			{
				if (!mAdapter.getSelections().contains(position))
				{
					mAdapter.invertSelection(position);
				}
				mAdapter.notifyDataSetChanged();

				mSnackbar = Snackbar.make(getActivity().findViewById(R.id.fragment_favorite_list_view), "Remove selected items?", Snackbar.LENGTH_INDEFINITE)
						.setCallback(new Snackbar.Callback()
						{
							@Override
							public void onDismissed(Snackbar snackbar, int event)
							{
								switch (event)
								{
									case Snackbar.Callback.DISMISS_EVENT_SWIPE:
									case Snackbar.Callback.DISMISS_EVENT_ACTION:
									case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
										mSnackbar = null;
										if (mAdapter != null)
										{
											mAdapter.notifyDataSetChanged();
										}
										updateMenu();
										break;
								}
							}
						}).setAction("Remove", new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								mSnackbar = null;
								startAction(R.id.history_menu_action_delete);
								updateMenu();
							}
						});
				mSnackbar.show();
			}
		}
	}

	@Override
	public void onItemClick(HistoryRecyclerAdapter adapter, View view, int position, long id, boolean is_long_click)
	{
		switch (view.getId())
		{
			case R.id.item_history_card_view:
				break;

			case R.id.item_history_textview_favorite:
				if (mDataProvider != null && position >= 0 && position < mDataProvider.getHistoryList().size())
				{
					inverseBookmark(position);

					/*
					HistoryRow hist_row = mDataProvider.getHistoryList().get(position);
					long in_fav_id = hist_row.getFavId();
					if (in_fav_id == 0)
					{
						startAction(R.id.history_menu_action_bookmark, position);
					}
					else
					{
						// remove from favorites
						mDataProvider.getLocalDataBase().delFromFavorite(in_fav_id);
					}*/
				}
				break;
		}
		updateMenu();
	}

	@Override
	public void onDoneDelete(HistoryRecyclerAdapter adapter, View view, int position)
	{
		startAction(R.id.history_menu_action_delete);
		updateMenu();
	}

	@Override
	public void onCancelDelete(HistoryRecyclerAdapter adapter, View view, int position)
	{
		updateMenu();
	}

	private void updateMenu()
	{
		if (mMenu != null)
		{
			mMenu.findItem(R.id.action_settings).setVisible(false);
			if (mAdapter != null)
			{
				mMenu.findItem(R.id.history_menu_action_go).setVisible(mAdapter.isGoSelection());
				mMenu.findItem(R.id.history_menu_action_bookmark).setVisible(!mAdapter.isEmptySelections());
				mMenu.findItem(R.id.history_menu_action_clear_selection).setVisible(!mAdapter.isEmptySelections());
				mMenu.findItem(R.id.history_menu_action_delete).setVisible(!mAdapter.isEmptySelections());
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		mMenu = menu;

		inflater.inflate(R.menu.history_menu, menu);
		updateMenu();
	}

	private void inverseBookmark(int position, boolean... force)
	{
		if (position >= 0 && position < mDataProvider.getHistoryList().size())
		{
			HistoryRow row = mDataProvider.getHistoryList().get(position);
			if (row.getFavId() == 0)
			{
				mDataProvider.getLocalDataBase().addToFavorite(row.getId());
			}
			else
			{
				if (force.length == 0)
				{
					mDataProvider.getLocalDataBase().delFromFavorite(row.getFavId());
				}
			}
			if (mAdapter != null)
			{
				mAdapter.notifyItemChanged(position);
			}
		}
	}

	private void startAction(int action_id, int... args)
	{
		HistoryRow hist_row;
		int pos = args.length > 0 ? args[0] : -1;
		if (mAdapter != null)
		{
			if (!mAdapter.isEmptySelections())
			{
				Object[] arr = mAdapter.getSelections().toArray();
				pos = (int) arr[0];
			}
		}
		switch (action_id)
		{
			case R.id.history_menu_action_go:
				if (pos >= 0 && pos < mDataProvider.getHistoryList().size() && getActivity() instanceof MainActivity)
				{
					((MainActivity) getActivity()).gotoMainAndSetData(mDataProvider.getHistoryList().get(pos));
				}
				break;

			case R.id.history_menu_action_bookmark:
				if (mAdapter != null)
				{
					if (mAdapter.isEmptySelections())
					{
						inverseBookmark(pos);
					}
					else
					{
						// multiple selections -> favorite
						for (int p1 : mAdapter.getSelections())
						{
							inverseBookmark(p1);
						}
					}
				}
				break;

			case R.id.history_menu_action_delete:
				if (mAdapter != null)
				{
					pos = mAdapter.getRequestDeletePosition();
					if (pos >= 0 && pos < mDataProvider.getHistoryList().size())
					{
						hist_row = mDataProvider.getHistoryList().get(pos);
						mDataProvider.getLocalDataBase().delFromHistory(hist_row.getId());
						mAdapter.cancelDelete(true);
					}
					else
					{
						if (!mAdapter.isEmptySelections())
						{
							// delete multiple selections
							for (int p : mAdapter.getSelections())
							{
								hist_row = mDataProvider.getHistoryList().get(p);
								mDataProvider.getLocalDataBase().delFromHistory(hist_row.getId());
							}
							mAdapter.cancelDelete(true);
							mAdapter.clearSelections();
							updateMenu();
						}
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
			case R.id.history_menu_action_go:
			case R.id.history_menu_action_bookmark:
				startAction(item.getItemId());
				return true;

			case R.id.history_menu_action_delete:
				if (mAdapter != null)
				{
					requestDeleteItems(mAdapter.getLastClickedPosition());
				}
				return true;

			case R.id.history_menu_action_clear_selection:
				if (mAdapter != null)
				{
					mAdapter.clearSelections();
					mAdapter.notifyDataSetChanged();
					updateMenu();

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

		if (mSnackbar != null)
		{
			mSnackbar.dismiss();
			mSnackbar = null;
		}

		super.onStop();
	}

	private void updateList()
	{
		if (mAdapter != null)
		{
			mAdapter.setList(mDataProvider.getHistoryList());
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onDBReadHistoryComplete(LocalDataBaseTask task, List<HistoryRow> list)
	{
		updateList();
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
