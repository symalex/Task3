package com.symbysoft.task3.common;

import android.util.Log;

public class helper
{
	public static String getMethodName(Object o, final int depth, String ... args)
	{
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		String ret = o.getClass().getSimpleName() + '.' + ste[depth + 3].getMethodName();
		if (args.length > 0)
		{
			ret += ": ";
		}
		for( String  s : args)
		{
			ret += s;
		}
		return ret;
	}
}
