package songo.view;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import songo.SWTUtil;

@SessionScoped
public class MainView implements View {
	private Shell shell;
	private TabItem searchTab;
	private TabItem playlistTab;

	public TabItem getSearchTab() {
		return searchTab;
	}

	public TabItem getPlaylistTab() {
		return playlistTab;
	}

	@Inject
	MainView(Shell shell, SWTUtil util) {
		this.shell = shell;
		util.exitOnClose(shell);
		shell.setText("Songo");
		shell.setLayout(new GridLayout());
		TabFolder tabs = new TabFolder(shell, SWT.NONE);
		tabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		playlistTab = new TabItem(tabs, SWT.NONE);
		playlistTab.setText("Playlist");
		searchTab = new TabItem(tabs, SWT.NONE);
		searchTab.setText("Search");
		tabs.setSelection(1);
	}

	@Override
	public Shell getShell() {
		return shell;
	}
}
