package com.symbysoft.task3.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

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
		@Bind(R.id.item_history_status_area)
		protected LinearLayout mStatusArea;
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
			mStatusArea.setVisibility(View.GONE);
			mBtnFavorite.setBackgroundResource(android.R.drawable.btn_star_big_on);
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
						Log.d(TAG, "LongClick: " + String.valueOf(getAdapterPosition()));
					}
					else
					{
						mSelectedPosition = -1;
						Log.d(TAG, "Click: " + String.valueOf(getAdapterPosition()));
					}
					notifyDataSetChanged();
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
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		Log.d(TAG, "Draw mSelectedPosition=" + String.valueOf(mSelectedPosition));

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

		FavoriteRow frow = mList.get(position);
		if (frow.getHistory() != null)
		{
			holder.mSrcTextView.setText(frow.getHistory().getSource());
			holder.mDestTextView.setText(frow.getHistory().getDestination());
			holder.mBtnFavorite.setText(frow.getHistory().getDirection());
			SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
			holder.mDateTimeTextView.setText(dtf.format(frow.getHistory().getDt()));
		}
	}

	@Override
	public int getItemCount()
	{
		return mList.size();
	}

}
