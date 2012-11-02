package songo;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class TableUtil {
	public static void packColumns(Table table) {
		for (TableColumn column : table.getColumns())
			column.pack();
	}
}
