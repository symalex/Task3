package com.symbysoft.task3.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symbysoft.task3.R;
import com.symbysoft.task3.data.HistoryRow;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder>
{
	private static final String TAG = "HistoryRecyclerAdapter";

	private boolean isLongClick = false;
	private int mSelectedPosition = -1;
	private Activity mActivity;
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
		void onItemClick(HistoryRecyclerAdapter adapter, View view, int position, long id, boolean is_long_click);
	}

	public void setOnItemClickListener(HistoryRecyclerItemClickListener listener)
	{
		mOnItemClickListener = listener;
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
	{
		private LinearLayout mLayout;

		@Bind(R.id.item_history_card_view)
		protected CardView mCardView;
		@Bind(R.id.item_history_textview_favorite)
		protected TextView mBtnFavorite;
		@Bind(R.id.item_history_top_text)
		protected TextView mSrcTextView;
		@Bind(R.id.item_history_bottom_text)
		protected TextView mDestTextView;
		@Bind(R.id.item_history_date_time)
		protected TextView mDateTimeTextView;

		public ViewHolder(View view)
		{
			super(view);
			ButterKnife.bind(this, view);
			mBtnFavorite.setOnClickListener(this);
			mCardView.setOnClickListener(this);
			mCardView.setOnLongClickListener(this);
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
				case R.id.item_history_card_view:
					if (isLongClick)
					{
						mSelectedPosition = getAdapterPosition();
						Log.d(TAG, "Click: " + String.valueOf(getAdapterPosition()));
					}
					else
					{
						mSelectedPosition = -1;
						Log.d(TAG, "Click: " + String.valueOf(getAdapterPosition()));
					}
					notifyDataSetChanged();
					if (mOnItemClickListener != null)
					{
						mOnItemClickListener.onItemClick(HistoryRecyclerAdapter.this, v, mSelectedPosition, getItemId(), isLongClick);
					}
					break;

				case R.id.item_history_textview_favorite:
					if (mOnItemClickListener != null)
					{
						mOnItemClickListener.onItemClick(HistoryRecyclerAdapter.this, v, getAdapterPosition(), getItemId(), isLongClick);
					}
					notifyDataSetChanged();
					break;
			}

			isLongClick = false;
		}

	}

	public HistoryRecyclerAdapter(Activity activity, ArrayList<HistoryRow> list)
	{
		mActivity = activity;
		mList = list;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		Log.d(TAG, "Draw on Click: " + String.valueOf(mSelectedPosition));

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
		{

		}
		else
		{
			if (mSelectedPosition == position)
			{
				holder.mCardView.setCardBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
			}
			else
			{
				holder.mCardView.setCardBackgroundColor(mActivity.getResources().getColor(R.color.cardview_light_background));
			}
		}

		HistoryRow hist_row = mList.get(position);
		holder.mSrcTextView.setText(hist_row.getSource());
		holder.mDestTextView.setText(hist_row.getDestination());
		holder.mBtnFavorite.setText(hist_row.getDirection());
		SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		holder.mDateTimeTextView.setText(dtf.format(hist_row.getDt()));
		if (hist_row.getFavId() != 0)
		{
			holder.mBtnFavorite.setBackgroundResource(android.R.drawable.btn_star_big_on);
		}
		else
		{
			holder.mBtnFavorite.setBackgroundResource(android.R.drawable.btn_star_big_off);
		}
	}

	@Override
	public int getItemCount()
	{
		return mList.size();
	}

}
