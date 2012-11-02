package songo.view;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import songo.TableUtil;
import songo.annotation.SearchTab;
import songo.vk.Audio;

import java.util.List;

public class SearchView extends Composite {
	private Text searchField;
	private Table table;
	private List<Audio> results = ImmutableList.of();

	@Inject
	SearchView(@SearchTab final TabItem searchTab) {
		super(searchTab.getParent(), SWT.NONE);
		searchTab.setControl(this);
		setLayout(new GridLayout());
		searchField = new Text(this, SWT.BORDER);
		searchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		searchTab.getParent().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item == searchTab)
					searchField.setFocus();
			}
		});
		table = new Table(this, SWT.MULTI);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		String[] columns = new String[]{"Artist", "Title"};
		for (String name : columns) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(name);
			column.pack();
		}
	}

	public void addSearchListener(final Runnable listener) {
		searchField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				listener.run();
			}
		});
	}

	public String getSearchQuery() {
		return searchField.getText();
	}

	public void setResults(List<Audio> results) {
		this.results = results;
		table.setRedraw(false);
		table.removeAll();
		for (Audio a : results) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[]{a.artist, a.title});
		}
		TableUtil.packColumns(table);
		table.setTopIndex(0);
		table.setRedraw(true);
	}


	public List<Audio> getSelectedTracks() {
		ImmutableList.Builder<Audio> result = new ImmutableList.Builder<Audio>();
		for (int i : table.getSelectionIndices())
			result.add(results.get(i));
		return result.build();
	}

	public void addAddListener(final Runnable listener) {
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				listener.run();
			}
		});
	}
}
