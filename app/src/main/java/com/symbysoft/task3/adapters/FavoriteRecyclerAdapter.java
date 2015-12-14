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

import com.symbysoft.task3.data.FavoriteRow;

public class FavoriteRecyclerAdapter extends RecyclerView.Adapter<FavoriteRecyclerAdapter.ViewHolder>
{
	private int mSelectedPosition = -1;
	private ArrayList<FavoriteRow> mList;
	private FavoriteRecyclerItemClickListener mOnItemClickListener;

	public int getSelectedPosition()
	{
		return mSelectedPosition;
	}

	public void setSelectedPosition(int selectedPosition)
	{
		mSelectedPosition = selectedPosition;
	}

	public interface FavoriteRecyclerItemClickListener
	{
		void onItemClick(FavoriteRecyclerAdapter adapter, View view, int position, long id);
	}

	public void setOnItemClickListener(FavoriteRecyclerItemClickListener listener)
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
				mOnItemClickListener.onItemClick(FavoriteRecyclerAdapter.this, v, mSelectedPosition, getItemId());
			}
		}

	}

	public FavoriteRecyclerAdapter(ArrayList<FavoriteRow> list)
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
