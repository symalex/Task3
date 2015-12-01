package com.symbysoft.task3;

import java.util.ArrayList;
import java.util.List;

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

public class FavoriteFragment extends Fragment implements AdapterView.OnItemClickListener
{
	public static final String FTAG = "favorite_fragment";

	@Bind(R.id.fragment_favorite_list_view)
	ListView mListView;

	MenuItem mMenuItemDelete;

	public static Fragment newInstance()
	{
		return new FavoriteFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_favorite, container, false);
		ButterKnife.bind(this, view);

		List<String> lines = new ArrayList<>();
		lines.add("Favorite A");
		lines.add("Favorite B");
		lines.add("Favorite C");
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
				android.R.layout.simple_list_item_1, lines);
		mListView.setAdapter(adapter);
		mListView.setSelector(R.drawable.list_selector);
		mListView.setOnItemClickListener(this);
		setHasOptionsMenu(true);

		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		view.setSelected(true);

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
		// handle item selection
		switch (item.getItemId())
		{
			case R.id.favorite_menu_action_delete:

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
