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
import com.symbysoft.task3.adapters.FavoriteRecyclerAdapter.FavoriteRecyclerItemClickListener;
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

public class FavoriteFragment extends Fragment implements LocalDataBaseListener, FavoriteRecyclerItemClickListener, FavoriteRecyclerAdapter.FavoriteRecyclerItemActionListener
{
	public static final String TAG = "FavoriteFragment";
	public static final String FTAG = "favorite_fragment";

	@Bind(R.id.fragment_favorite_list_view)
	protected RecyclerView mRecyclerView;
	private RecyclerView.LayoutManager mLayoutManager;
	private FavoriteRecyclerAdapter mAdapter;
	private Menu mMenu;
	private Snackbar mSnackbar;

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

		// specify an adapter
		mAdapter = new FavoriteRecyclerAdapter(getActivity(), mRecyclerView, mDataProvider.getFavoriteList());
		mAdapter.setOnItemClickListener(this);
		mAdapter.setOnItemActionListener(this);
		mRecyclerView.setAdapter(mAdapter);
		mItemTouchHelper.attachToRecyclerView(mRecyclerView);
		//mRecyclerView.addOnItemTouchListener(this);

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
								startAction(R.id.favorite_menu_action_delete);
								updateMenu();
							}
						});
				mSnackbar.show();
			}
		}
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
			case R.id.item_history_card_view:
				updateMenu();
				break;
		}
	}

	@Override
	public void onDoneDelete(FavoriteRecyclerAdapter adapter, View view, int position)
	{
		startAction(R.id.favorite_menu_action_delete);
		updateMenu();
	}

	@Override
	public void onCancelDelete(FavoriteRecyclerAdapter adapter, View view, int position)
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
				mMenu.findItem(R.id.favorite_menu_action_go).setVisible(mAdapter.isGoSelection());
				mMenu.findItem(R.id.favorite_menu_action_clear_selection).setVisible(!mAdapter.isEmptySelections());
				mMenu.findItem(R.id.favorite_menu_action_delete).setVisible(!mAdapter.isEmptySelections());
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		mMenu = menu;

		inflater.inflate(R.menu.favorite_menu, menu);
		updateMenu();
	}

	private void startAction(int action_id)
	{
		FavoriteRow fav_row;
		int pos = -1;
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
			case R.id.favorite_menu_action_go:
				if (pos >= 0 && pos < mDataProvider.getFavoriteList().size() && getActivity() instanceof MainActivity)
				{
					fav_row = mDataProvider.getFavoriteList().get(pos);
					((MainActivity) getActivity()).gotoMainAndSetData(fav_row.getHistory());
				}
				break;

			case R.id.favorite_menu_action_delete:
				if (mAdapter != null)
				{
					pos = mAdapter.getRequestDeletePosition();
					if (pos >= 0 && pos < mDataProvider.getFavoriteList().size())
					{
						fav_row = mDataProvider.getFavoriteList().get(pos);
						mDataProvider.getLocalDataBase().delFromFavorite(fav_row.getId());
						mAdapter.cancelDelete(true);
					}
					else
					{
						if (!mAdapter.isEmptySelections())
						{
							// delete multiple selections
							for (int p : mAdapter.getSelections())
							{
								fav_row = mDataProvider.getFavoriteList().get(p);
								mDataProvider.getLocalDataBase().delFromFavorite(fav_row.getId());
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
			case R.id.favorite_menu_action_go:
				startAction(item.getItemId());
				return true;

			case R.id.favorite_menu_action_delete:
				if (mAdapter != null)
				{
					requestDeleteItems(mAdapter.getLastClickedPosition());
				}
				return true;

			case R.id.favorite_menu_action_clear_selection:
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
		if (mSnackbar != null)
		{
			mSnackbar.dismiss();
			mSnackbar = null;
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
