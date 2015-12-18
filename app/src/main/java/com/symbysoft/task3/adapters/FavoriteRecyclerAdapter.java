package com.symbysoft.task3.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.symbysoft.task3.R;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.symbysoft.task3.data.FavoriteRow;
import com.symbysoft.task3.data.HistoryRow;

public class FavoriteRecyclerAdapter extends RecyclerView.Adapter<FavoriteRecyclerAdapter.ViewHolder>
{
	private static final String TAG = "FavoriteRecyclerAdapter";

	private boolean isLongClick = false;
	private int mSelectedPosition = -1;
	private Activity mActivity;
	private ArrayList<FavoriteRow> mList;
	private FavoriteRecyclerItemClickListener mOnItemClickListener;

	public int getSelectedPosition()
	{
		return mSelectedPosition;
	}

	public ArrayList<FavoriteRow> getList()
	{
		return mList;
	}

	public void setList(ArrayList<FavoriteRow> list)
	{
		mList = list;
	}

	public void setSelectedPosition(int selectedPosition)
	{
		mSelectedPosition = selectedPosition;
	}

	public boolean isLongClick()
	{
		return isLongClick;
	}

	public interface FavoriteRecyclerItemClickListener
	{
		void onItemClick(FavoriteRecyclerAdapter adapter, View view, int position, long id, boolean is_long_click);
	}

	public void setOnItemClickListener(FavoriteRecyclerItemClickListener listener)
	{
		mOnItemClickListener = listener;
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
	{
		private LinearLayout mLayout;

		@Bind(R.id.item_history_card_view)
		protected CardView mCardView;
		@Bind(R.id.item_history_card_view_layout)
		protected RelativeLayout mRelativeLayout;
		@Bind(R.id.item_history_btn_favorite)
		protected Button mBtnFavorite;
		@Bind(R.id.item_history_top_text)
		protected TextView mSrcTextView;
		@Bind(R.id.item_history_bottom_text)
		protected TextView mDestTextView;

		public ViewHolder(LinearLayout view)
		{
			super(view);
			mLayout = view;
			ButterKnife.bind(this, view);
			mBtnFavorite.setClickable(false);
			mBtnFavorite.setFocusableInTouchMode(false);
			mBtnFavorite.setFocusable(false);
			mRelativeLayout.setOnClickListener(this);
			mRelativeLayout.setOnLongClickListener(this);
		}

		@Override
		public boolean onLongClick(View v)
		{
			isLongClick = true;
			return false;
		}

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				case R.id.item_history_card_view_layout:
				case R.id.item_history_btn_favorite:
					if (isLongClick)
					{
						mSelectedPosition = getAdapterPosition();
						notifyDataSetChanged();
						Log.d(TAG, "LongClick: " + String.valueOf(getAdapterPosition()));
					}
					else
					{
						mSelectedPosition = -1;
						Log.d(TAG, "Click: " + String.valueOf(getAdapterPosition()));
					}
					if (mOnItemClickListener != null)
					{
						mOnItemClickListener.onItemClick(FavoriteRecyclerAdapter.this, v, mSelectedPosition, getItemId(), isLongClick);
					}
					break;
			}

			isLongClick = false;
		}

	}

	public FavoriteRecyclerAdapter(Activity activity, ArrayList<FavoriteRow> list)
	{
		mActivity = activity;
		mList = list;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		Log.d(TAG, "Draw mSelectedPosition=" + String.valueOf(mSelectedPosition));
		holder.mRelativeLayout.setPressed(mSelectedPosition == position);
		//if (mSelectedPosition == position)
		//{
			//holder.mCardView.setBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
		//}

		FavoriteRow frow = mList.get(position);
		if (frow.getHistory() != null)
		{
			holder.mSrcTextView.setText(frow.getHistory().getSource());
			holder.mDestTextView.setText(frow.getHistory().getDestination());
			holder.mBtnFavorite.setText(frow.getHistory().getDirection());
		}
		holder.mBtnFavorite.setPressed(true);
	}

	@Override
	public int getItemCount()
	{
		return mList.size();
	}

}
