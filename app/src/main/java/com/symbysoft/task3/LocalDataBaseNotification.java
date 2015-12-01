package com.symbysoft.task3;

import java.util.List;

public interface LocalDataBaseNotification
{
	void onDBReadHistoryComplette(LocalDataBaseTask task, List<String> list);
}
