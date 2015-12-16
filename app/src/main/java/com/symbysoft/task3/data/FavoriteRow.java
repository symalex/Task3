package com.symbysoft.task3.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = FavoriteRow.TABLE_NAME)
public class FavoriteRow
{
	public static final String TABLE_NAME = "favorite";

	public static final String KEY_ID = "id";
	public static final String HIST_ID = "hist_id";

	@DatabaseField(columnName = KEY_ID, generatedId = true)
	private long mId;

	@DatabaseField(columnName = HIST_ID, foreignColumnName = HistoryRow.KEY_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true,
			columnDefinition = "INTEGER REFERENCES " + HistoryRow.TABLE_NAME + "(" + HistoryRow.KEY_ID + ") ON DELETE CASCADE")
	private HistoryRow mHistory;

	public long getId()
	{
		return mId;
	}

	public void setId(long id)
	{
		mId = id;
	}

	public HistoryRow getHistory()
	{
		return mHistory;
	}

	public void setHistory(HistoryRow history)
	{
		mHistory = history;
	}

	public long getHistId()
	{
		return mHistory != null ? mHistory.getId() : 0;
	}

	public void setHistId(long histId)
	{
		if (mHistory != null)
		{
			mHistory.setId(histId);
		}
	}

	@Override
	public String toString()
	{
		return String.format("{\"%s\"=\"%d\", \"%s\"=%s}", KEY_ID, mId, HIST_ID, mHistory != null ? mHistory.toString() : "null");
	}

}
