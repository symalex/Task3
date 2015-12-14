package com.symbysoft.task3.common;

import java.sql.SQLException;
import java.sql.Timestamp;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.TimeStampType;

public class LocalTimestampType extends TimeStampType
{
	private static final LocalTimestampType singleton = new LocalTimestampType();
	private String defaultStr;

	public LocalTimestampType()
	{
		super(SqlType.DATE, new Class<?>[]{Timestamp.class});
	}

	public static LocalTimestampType getSingleton()
	{
		return singleton;
	}

	@Override
	public boolean isEscapedDefaultValue()
	{
		if ("CURRENT_TIMESTAMP()".equals(defaultStr))
		{
			return false;
		}
		else
		{
			return super.isEscapedDefaultValue();
		}
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException
	{
		this.defaultStr = defaultStr;
		if ("CURRENT_TIMESTAMP()".equals(defaultStr))
		{
			return defaultStr;
		}
		else
		{
			return super.parseDefaultString(fieldType, defaultStr);
		}
	}

}
