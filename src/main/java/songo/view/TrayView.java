package songo.view;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import songo.ResourceUtil;
import songo.annotation.SessionBus;

public class TrayView {
	private final TrayItem item;
	private final EventBus bus;

	@Inject
	TrayView(Display display, ResourceUtil resourceUtil, @SessionBus final EventBus bus) {
		this.bus = bus;
		item = new TrayItem(display.getSystemTray(), SWT.NONE);
		item.setImage(new Image(display, resourceUtil.iconStream("tray")));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				bus.post(new Restore());
			}
		});
	}

	void setShell(Shell shell) {
		final Menu menu = new Menu(shell, SWT.POP_UP);
		MenuItem exit = new MenuItem(menu, SWT.NONE);
		exit.setText("Exit");
		exit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				bus.post(new Exit());
			}
		});
		item.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(MenuDetectEvent menuDetectEvent) {
				menu.setVisible(true);
			}
		});
	}

	public static class Exit {
		private Exit() { }
	}

	public class Restore {
		private Restore() { }
	}
}
