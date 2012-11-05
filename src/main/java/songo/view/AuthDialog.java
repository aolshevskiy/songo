package songo.view;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import songo.SWTUtil;
import songo.annotation.BrowserStyle;
import songo.model.Configuration;

@SessionScoped
public class AuthDialog implements View {
	private final Shell shell;
	private final Browser browser;
	private final Button registerButton;

	public Browser getBrowser() {
		return browser;
	}

	private static GridLayout gridLayout() {
		return gridLayout(1);
	}

	private static GridLayout gridLayout(int cols) {
		return new GridLayout(cols, false);
	}

	private static GridData horizontalFill() {
		return new GridData(SWT.FILL, SWT.CENTER, true, false);
	}

	@Inject
	AuthDialog(Shell shell, SWTUtil util, @BrowserStyle Integer browserStyle) {
		this.shell = shell;
		util.exitOnClose(shell);
		shell.setText("Songo - VKontakte Authorization");
		shell.setSize(1000, 600);
		shell.setLayout(gridLayout());
		Composite topPanel = new Composite(shell, SWT.NONE);
		topPanel.setLayout(gridLayout(2));
		topPanel.setLayoutData(horizontalFill());
		Label authLabel = new Label(topPanel, SWT.CENTER);
		authLabel.setLayoutData(horizontalFill());
		authLabel.setText("VKontakte authorization is absolutely required");
		final ProgressBar progressBar = new ProgressBar(topPanel, SWT.NONE);
		browser = new Browser(shell, browserStyle);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void changed(ProgressEvent event) {
				progressBar.setSelection(event.current);
			}

			@Override
			public void completed(ProgressEvent event) {
				if ((Boolean) browser.evaluate("return document.getElementById('login_submit') != null;")) {
					browser.setFocus();
					browser.execute("document.getElementById('login_submit').elements['email'].focus();");
				}
			}
		});
		Composite bottomPanel = new Composite(shell, SWT.NONE);
		bottomPanel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		bottomPanel.setLayout(gridLayout(2));
		Label dontHaveLabel = new Label(bottomPanel, SWT.RIGHT);
		dontHaveLabel.setText("Don't have a VKontakte account?");
		registerButton = new Button(bottomPanel, SWT.PUSH);
		registerButton.setText("Register");
	}

	public void addRegistrationListener(final Runnable listener) {
		registerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				listener.run();
			}
		});
	}

	public void openUrl(String url) {
		browser.setUrl(url);
	}

	public void addLocationListener(LocationListener listener) {
		browser.addLocationListener(listener);
	}

	@Override
	public Shell getShell() {
		return shell;
	}
}
