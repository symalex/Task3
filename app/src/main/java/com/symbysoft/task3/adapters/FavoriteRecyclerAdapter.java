package com.symbysoft.task3.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symbysoft.task3.R;
import com.symbysoft.task3.common.helper;
import com.symbysoft.task3.data.FavoriteRow;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FavoriteRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private static final String TAG = "FavoriteRecyclerAdapter";

	private boolean isLongClick = false;
	private int mLastClickedPosition = -1;
	private int mLastLongClickedPosition = -1;
	private int mRequestDeletePosition = -1;
	private int mRequestSavedDeletePosition = -1;
	private Set<Integer> mSelections;
	private Activity mActivity;
	private RecyclerView mRecyclerView;
	private ArrayList<FavoriteRow> mList;
	private FavoriteRecyclerItemClickListener mOnItemClickListener;
	private FavoriteRecyclerItemActionListener mOnItemActionListener;

	public ArrayList<FavoriteRow> getList()
	{
		return mList;
	}

	public void setList(ArrayList<FavoriteRow> list)
	{
		mList = list;
	}

	public boolean isLongClick()
	{
		return isLongClick;
	}

	public interface FavoriteRecyclerItemClickListener
	{
		void onItemClick(FavoriteRecyclerAdapter adapter, View view, int position, long id, boolean is_long_click);
	}

	public interface FavoriteRecyclerItemActionListener
	{
		void onDoneDelete(FavoriteRecyclerAdapter adapter, View view, int position);

		void onCancelDelete(FavoriteRecyclerAdapter adapter, View view, int position);
	}

	public int getRequestDeletePosition()
	{
		return mRequestDeletePosition;
	}

	public int getLastClickedPosition()
	{
		return mLastClickedPosition;
	}

	public void setOnItemClickListener(FavoriteRecyclerItemClickListener listener)
	{
		mOnItemClickListener = listener;
	}

	public void setOnItemActionListener(FavoriteRecyclerItemActionListener onItemActionListener)
	{
		mOnItemActionListener = onItemActionListener;
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener
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
			mCardView.setOnLongClickListener(this);
		}

		@Override
		public boolean onLongClick(View v)
		{
			isLongClick = true;
			mLastLongClickedPosition = mLastClickedPosition = getAdapterPosition();
			invertSelection(mLastLongClickedPosition);
			Log.d(TAG, "LongClick: " + mLastLongClickedPosition);
			if (mOnItemClickListener != null)
			{
				mOnItemClickListener.onItemClick(FavoriteRecyclerAdapter.this, v, getAdapterPosition(), getItemId(), isLongClick);
			}
			return false;
		}

		@OnClick(R.id.item_history_card_view)
		public void onClick(View v)
		{
			mLastClickedPosition = getAdapterPosition();
			Log.d(TAG, "Click: " + String.valueOf(mLastClickedPosition));

			if (!isEmptySelections())
			{
				Log.d(TAG, "Click (invertSelection): " + mLastLongClickedPosition);
				invertSelection(mLastClickedPosition);
			}
			//notifyDataSetChanged();

			if (mOnItemClickListener != null)
			{
				mOnItemClickListener.onItemClick(FavoriteRecyclerAdapter.this, v, mLastClickedPosition, getItemId(), isLongClick);
			}

			isLongClick = false;
			mLastLongClickedPosition = -1;
		}

	}

	public class RequestViewHolder extends RecyclerView.ViewHolder
	{
		@Bind(R.id.item_request_done)
		protected Button mBtnDone;
		@Bind(R.id.item_request_undo)
		protected Button mBtnUndo;

		public RequestViewHolder(View view)
		{
			super(view);
			ButterKnife.bind(this, view);
		}

		@OnClick(R.id.item_request_done)
		public void onDoneClick(View v)
		{
			clearSelections();
			if (mOnItemActionListener != null)
			{
				mOnItemActionListener.onDoneDelete(FavoriteRecyclerAdapter.this, v, mRequestSavedDeletePosition);
			}
		}

		@OnClick(R.id.item_request_undo)
		public void onUndoClick(View v)
		{
			cancelDelete();
			clearSelections();
			if (mOnItemActionListener != null)
			{
				mOnItemActionListener.onCancelDelete(FavoriteRecyclerAdapter.this, v, mRequestSavedDeletePosition);
			}
		}
	}

	public FavoriteRecyclerAdapter(Activity activity, RecyclerView recyclerView, ArrayList<FavoriteRow> list)
	{
		mActivity = activity;
		mRecyclerView = recyclerView;
		mList = list;
		mSelections = new HashSet<>();
	}

	public void invertSelection(int position)
	{
		if (mSelections.contains(position))
		{
			mSelections.remove(position);
		}
		else
		{
			mSelections.add(position);
		}
		//notifyItemChanged(position);
		notifyDataSetChanged();
	}

	public void clearSelections()
	{
		mSelections.clear();
		notifyDataSetChanged();
	}

	public boolean isEmptySelections()
	{
		return mSelections.size() == 0;
	}

	public boolean isGoSelection()
	{
		return mSelections.size() == 1;
	}

	public boolean isMultipleSelections()
	{
		return mSelections.size() > 1;
	}

	public Set<Integer> getSelections()
	{
		return mSelections;
	}

	public void requestDelete(int position)
	{
		if (position == -1)
		{
			position = mLastClickedPosition;
		}
		mRequestSavedDeletePosition = mRequestDeletePosition = position;
		notifyItemChanged(position);
	}

	public void cancelDelete(boolean... removed)
	{
		if (removed.length > 0)
		{
			notifyItemRemoved(mRequestSavedDeletePosition);
		}
		else
		{
			notifyItemChanged(mRequestSavedDeletePosition);
		}
		mRequestDeletePosition = -1;
	}

	public void onItemMove(int source, int target)
	{
		mList.add(target, mList.remove(source));
	}

	@Override
	public int getItemViewType(int position)
	{
		return mRequestDeletePosition == position ? 1 : 0;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		RecyclerView.ViewHolder holder = null;
		switch (viewType)
		{
			case 0:
				holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false));
				break;

			case 1:
				holder = new RequestViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_request, parent, false));
				break;
		}

		return holder;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
	{
		ViewHolder vh = null;
		if (holder instanceof ViewHolder)
		{
			vh = (ViewHolder) holder;
		}
		/*else if (holder instanceof RequestViewHolder)
		{

		}*/

		if (vh != null)
		{
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
			{
				if (mSelections.contains(position))
				{
					vh.mCardView.setCardBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
				}
				else
				{
					vh.mCardView.setCardBackgroundColor(mActivity.getResources().getColor(R.color.cardview_light_background));
				}
			}
			else
			{
				if (mSelections.contains(position))
				{
					vh.mCardView.setCardBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
				}
				else
				{
					vh.mCardView.setCardBackgroundColor(mActivity.getResources().getColor(R.color.cardview_light_background));
				}
			}

			FavoriteRow frow = mList.get(position);
			if (frow.getHistory() != null)
			{
				String src_str, dst_str;
				if (mLastClickedPosition != position)
				{
					src_str = helper.getOneLineText(frow.getHistory().getSource(), 35);
					dst_str = helper.getOneLineText(frow.getHistory().getDestination(), 35);
				}
				else
				{
					src_str = frow.getHistory().getSource();
					dst_str = frow.getHistory().getDestination();
				}
				vh.mSrcTextView.setText(src_str);
				vh.mDestTextView.setText(dst_str);
				vh.mBtnFavorite.setText(frow.getHistory().getDirection());
				SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
				vh.mDateTimeTextView.setText(dtf.format(frow.getHistory().getDt()));
			}
		}
	}

	@Override
	public int getItemCount()
	{
		return mList.size();
	}

}
