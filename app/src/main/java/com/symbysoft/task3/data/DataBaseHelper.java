package com.symbysoft.task3.data;

import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableInfo;
import com.j256.ormlite.table.TableUtils;

public class DataBaseHelper extends OrmLiteSqliteOpenHelper
{
	private static final String TAG = "DataBaseHelper";

	// Database Name
	public static final String DATABASE_NAME = "history.sqlite";

	// Database Version
	public static final int DATABASE_VERSION = 1;

	private HistoryDAO mHistoryDAO;
	private FavoriteDAO mFavoriteDAO;

	public static class UnknownColumnIgnoringGenericRowMapper<T, ID, T2> implements RawRowMapper<T>
	{
		private final TableInfo<T, ID> tableInfo;
		private final TableInfo<T2, ID> tableInfo2;

		public UnknownColumnIgnoringGenericRowMapper(TableInfo<T, ID> tableInfo, TableInfo<T2, ID> tableInfo2)
		{
			this.tableInfo = tableInfo;
			this.tableInfo2 = tableInfo2;
		}

		public T mapRow(String[] columnNames, String[] resultColumns) throws SQLException
		{
			// create our object
			T rowObj = tableInfo.createObject();
			for (int i = 0; i < columnNames.length; i++)
			{
				// sanity check, prolly will never happen but let's be careful out there
				if (i >= resultColumns.length)
				{
					continue;
				}
				try
				{
					// run through and convert each field
					FieldType fieldType;
					if (i < tableInfo.getFieldTypes().length)
					{
						fieldType = tableInfo.getFieldTypeByColumnName(columnNames[i]);
						Object fieldObj = fieldType.convertStringToJavaField(resultColumns[i], i);
						fieldType.assignField(rowObj, fieldObj, false, null);
					}
					else
					{
						int j = tableInfo2.getFieldTypes().length - 1 - (i - tableInfo.getFieldTypes().length);
						fieldType = tableInfo2.getFieldTypes()[j];
						Object fieldObj = fieldType.convertStringToJavaField(resultColumns[i], i);
						fieldType.assignField(rowObj, fieldObj, false, null);
					}
				}
				catch (IllegalArgumentException e)
				{
					// log this or do whatever you want
				}
			}
			return rowObj;
		}
	}

	public static class HistoryDAO extends BaseDaoImpl<HistoryRow, Long>
	{
		protected HistoryDAO(ConnectionSource connectionSource, Class<HistoryRow> dataClass) throws SQLException
		{
			super(connectionSource, dataClass);
		}

		public List<HistoryRow> getAll(QueryBuilder<FavoriteRow, Long> fbuilder, TableInfo<FavoriteRow, Long> tableInfo) throws SQLException
		{
			QueryBuilder<HistoryRow, Long> builder = this.queryBuilder();
			String query = builder
					.selectRaw(String.format("`%s`.*, `%s`.`%s` as `%s`", HistoryRow.TABLE_NAME, FavoriteRow.TABLE_NAME, FavoriteRow.KEY_ID, HistoryRow.FAV_ID))
					.leftJoin(fbuilder)
					.prepareStatementString();

			UnknownColumnIgnoringGenericRowMapper<HistoryRow, Long, FavoriteRow> mapper = new UnknownColumnIgnoringGenericRowMapper<HistoryRow, Long, FavoriteRow>(
					getTableInfo(),
					tableInfo
			);
			RawRowMapper<HistoryRow> mapper2 = new RawRowMapper<HistoryRow>()
			{
				public HistoryRow mapRow(String[] columnNames, String[] resultColumns)
				{
					HistoryRow h = new HistoryRow();
					int i = 0;
					for (String name : columnNames)
					{
						String value = resultColumns[i];
						switch (name)
						{
							case HistoryRow.KEY_ID:
								h.setId(Long.parseLong(value));
								break;

							case HistoryRow.DATE_TIME:
								h.setDt(new Date(Long.parseLong(value)));
								break;

							case HistoryRow.DIRECTION:
								h.setDirection(value);
								break;

							case HistoryRow.SOURCE:
								h.setSource(value);
								break;

							case HistoryRow.DEST:
								h.setDestination(value);
								break;

							case HistoryRow.FAV_ID:
								if (value != null)
								{
									h.setFavId(Long.parseLong(value));
								}
								break;
						}
						i++;
					}
					return h;
				}
			};
			GenericRawResults<HistoryRow> raw_res = this.queryRaw(query, mapper2); //getRawRowMapper()
			List<HistoryRow> list = raw_res.getResults();
			return this.queryForAll();
		}

		public List<HistoryRow> getAll() throws SQLException
		{
			return this.queryForAll();
		}
	}

	public static class FavoriteDAO extends BaseDaoImpl<FavoriteRow, Long>
	{
		protected FavoriteDAO(ConnectionSource connectionSource, Class<FavoriteRow> dataClass) throws SQLException
		{
			super(connectionSource, dataClass);
		}

		public List<FavoriteRow> getAll() throws SQLException
		{
			return this.queryForAll();
		}
	}

	public DataBaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource)
	{
		try
		{
			TableUtils.createTable(connectionSource, HistoryRow.class);
			TableUtils.createTable(connectionSource, FavoriteRow.class);
		}
		catch (SQLException e)
		{
			Log.e(TAG, "error creating DB " + DATABASE_NAME);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion)
	{
		try
		{
			switch (oldVersion)
			{
				case 0:
					// never execute
					TableUtils.dropTable(connectionSource, FavoriteRow.class, true);
					TableUtils.dropTable(connectionSource, HistoryRow.class, true);
					onCreate(db, connectionSource);

				case 1:
					// upgrade version 1 -> 2

				case 2:
					// upgrade version 2 -> 3

				case 3:

					break;
			}

		}
		catch (SQLException e)
		{
			Log.e(TAG, "error upgrading db " + DATABASE_NAME + "from ver " + oldVersion);
			throw new RuntimeException(e);
		}
	}

	public HistoryDAO getHistoryDAO() throws SQLException
	{
		if (mHistoryDAO == null)
		{
			mHistoryDAO = new HistoryDAO(getConnectionSource(), HistoryRow.class);
		}
		return mHistoryDAO;
	}

	public FavoriteDAO getFavoriteDAO() throws SQLException
	{
		if (mFavoriteDAO == null)
		{
			mFavoriteDAO = new FavoriteDAO(getConnectionSource(), FavoriteRow.class);
		}
		return mFavoriteDAO;
	}

	@Override
	public void close()
	{
		super.close();

		mFavoriteDAO = null;
		mHistoryDAO = null;
	}

}
