package com.symbysoft.task3.adapters;

import java.util.ArrayList;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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

import com.symbysoft.task3.data.HistoryRow;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder>
{
	private int mSelectedPosition = -1;
	private ArrayList<HistoryRow> mList;
	private HistoryRecyclerItemClickListener mOnItemClickListener;

	public int getSelectedPosition()
	{
		return mSelectedPosition;
	}

	public ArrayList<HistoryRow> getList()
	{
		return mList;
	}

	public void setList(ArrayList<HistoryRow> list)
	{
		mList = list;
	}

	public void setSelectedPosition(int selectedPosition)
	{
		mSelectedPosition = selectedPosition;
	}

	public interface HistoryRecyclerItemClickListener
	{
		void onItemClick(HistoryRecyclerAdapter adapter, View view, int position, long id);
	}

	public void setOnItemClickListener(HistoryRecyclerItemClickListener listener)
	{
		mOnItemClickListener = listener;
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
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
			mBtnFavorite.setOnClickListener(this);
			mRelativeLayout.setOnClickListener(this);
		}

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				case R.id.item_history_card_view_layout:
				case R.id.item_history_btn_favorite:
					mSelectedPosition = getAdapterPosition();
					break;
			}
			notifyDataSetChanged();
			if (mOnItemClickListener != null)
			{
				mOnItemClickListener.onItemClick(HistoryRecyclerAdapter.this, v, mSelectedPosition, getItemId());
			}
		}

	}

	public HistoryRecyclerAdapter(ArrayList<HistoryRow> list)
	{
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
		if (mSelectedPosition == position)
		{
			holder.mRelativeLayout.setBackgroundColor(Color.parseColor("#00FF00"));
		}
		else
		{
			holder.mRelativeLayout.setBackgroundColor(Color.parseColor("#FEFFFF"));
		}
		holder.mRelativeLayout.setSelected(mSelectedPosition == position);
		HistoryRow hist_row = mList.get(position);
		holder.mSrcTextView.setText(hist_row.getSource());
		holder.mDestTextView.setText(hist_row.getDestination());
		holder.mBtnFavorite.setText(hist_row.getDirection());
		holder.mBtnFavorite.setPressed(hist_row.getFavId() != 0);
	}

	@Override
	public int getItemCount()
	{
		return mList.size();
	}

}
