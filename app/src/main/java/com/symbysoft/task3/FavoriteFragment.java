package com.symbysoft.task3;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.symbysoft.task3.LocalDataBaseTask.LocalDataBaseNotification;

public class FavoriteFragment extends Fragment implements AdapterView.OnItemClickListener, LocalDataBaseNotification
{
	public static final String FTAG = "favorite_fragment";

	@Bind(R.id.fragment_favorite_list_view)
	protected ListView mListView;

	private DataProvider mDataProvider;
	private int mPosition;

	private MenuItem mMenuItemDelete;

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
		mDataProvider.getLocalDataBase().addDBNotification(this);

		mListView.setSelector(R.drawable.list_selector);
		mListView.setOnItemClickListener(this);
		updateList();

		setHasOptionsMenu(true);

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
		for (ContentValues cv : mDataProvider.getFavoriteList())
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
		if (mMenuItemDelete != null)
		{
			mMenuItemDelete.setVisible(true);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.favorite_menu, menu);

		menu.findItem(R.id.action_settings).setVisible(false);
		if (mListView.getSelectedItem() == null)
		{
			mMenuItemDelete = menu.findItem(R.id.favorite_menu_action_delete);
			mMenuItemDelete.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.favorite_menu_action_go:
				if (mPosition >= 0 && mPosition < mDataProvider.getFavoriteList().size() && getActivity() instanceof MainActivity)
				{
					((MainActivity) getActivity()).gotoMainAndSetData(mDataProvider.getFavoriteList().get(mPosition));
				}
				return true;

			case R.id.favorite_menu_action_delete:
				if (mPosition >= 0 && mPosition < mDataProvider.getFavoriteList().size())
				{
					mDataProvider.getLocalDataBase().delFromFavorite(mDataProvider.getFavoriteList().get(mPosition).getAsLong(DatabaseHelper.KEY_ID));
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
			mDataProvider.getLocalDataBase().addDBNotification(this);
		}
	}

	@Override
	public void onStop()
	{
		if (mDataProvider != null)
		{
			mDataProvider.getLocalDataBase().removeDBNotification(this);
		}

		super.onStop();
	}

	@Override
	public void onDBReadHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
	}

	@Override
	public void onDBReadFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBAddHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{

	}

	@Override
	public void onDBDelHistoryComplette(LocalDataBaseTask task, List<ContentValues> list)
	{

	}

	@Override
	public void onDBAddFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

	@Override
	public void onDBDelFavoriteComplette(LocalDataBaseTask task, List<ContentValues> list)
	{
		updateList();
	}

}
