package songo.view;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import songo.SWTUtil;
import songo.annotation.SessionBus;

@SessionScoped
public class MainView implements View {
	private final Shell shell;
	private final SWTUtil util;
	private final TabItem searchTab;
	private final TabItem playlistTab;
	private final ShellAdapter hideOnClose;

	public TabItem getSearchTab() {
		return searchTab;
	}

	public TabItem getPlaylistTab() {
		return playlistTab;
	}

	@Inject
	MainView(final Shell shell, SWTUtil util, TrayView trayView, @SessionBus EventBus bus) {
		this.shell = shell;
		this.util = util;
		bus.register(this);
		hideOnClose = new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent shellEvent) {
				shell.setVisible(false);
				shellEvent.doit = false;
			}
		};
		shell.addShellListener(hideOnClose);
		shell.setText("Songo");
		shell.setLayout(new GridLayout());
		TabFolder tabs = new TabFolder(shell, SWT.NONE);
		tabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		playlistTab = new TabItem(tabs, SWT.NONE);
		playlistTab.setText("Playlist");
		searchTab = new TabItem(tabs, SWT.NONE);
		searchTab.setText("Search");
		tabs.setSelection(1);
		trayView.setShell(shell);
	}

	@Subscribe
	public void restore(TrayView.Restore e) {
		shell.setVisible(!shell.getVisible());
	}

	@Subscribe
	public void close(TrayView.Exit e) {
		shell.removeShellListener(hideOnClose);
		util.exitOnClose(shell);
		shell.close();
	}

	@Override
	public Shell getShell() {
		return shell;
	}
}
