package songo;

import com.google.inject.Inject;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Shell;

public class SWTUtil {
	private final SongoService service;

	@Inject
	SWTUtil(SongoService service) {
		this.service = service;
	}

	public void exitOnClose(Shell shell) {
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				service.stop();
			}
		});
	}
}
