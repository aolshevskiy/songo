package songo;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.ning.http.client.AsyncHttpClient;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import songo.annotation.BackgroundExecutor;
import songo.annotation.SessionScope;
import songo.controller.AuthController;
import songo.controller.Controller;
import songo.controller.MainController;
import songo.logging.InjectLogger;
import songo.model.Configuration;
import songo.model.Player;
import songo.model.streams.StreamManager;
import songo.mpg123.Mpg123Native;

import java.util.concurrent.ExecutorService;

import static songo.Constants.HOME_DIR;

@Singleton
public class SongoService extends AbstractService {
	private final AsyncHttpClient asyncHttpClient;
	private final Mpg123Native mpg123Native;
	private final Player player;
	private final SimpleScope scope;
	private final Configuration conf;
	private final Provider<AuthController> authController;
	private final Provider<MainController> mainController;
	private final StreamManager streamManager;
	private final ExecutorService backgroundExecutor;
	private Display display;
	private Provider<? extends Controller> controller;
	private Shell shell;
	@InjectLogger
	Logger logger;

	Display getDisplay() {
		return display;
	}

	@Inject
	SongoService(AsyncHttpClient asyncHttpClient, Mpg123Native mpg123Native, Player player,
		@SessionScope SimpleScope scope, Configuration conf, Provider<AuthController> authController,
		Provider<MainController> mainController, StreamManager streamManager,
		@BackgroundExecutor ExecutorService backgroundExecutor) {
		this.asyncHttpClient = asyncHttpClient;
		this.mpg123Native = mpg123Native;
		this.player = player;
		this.scope = scope;
		this.conf = conf;
		this.authController = authController;
		this.mainController = mainController;
		this.streamManager = streamManager;
		this.backgroundExecutor = backgroundExecutor;
	}

	private void run() {
		while(isRunning()) {
			scope.enter();
			shell = controller.get().getView().getShell();
			shell.open();
			shell.setFocus();
			while(!shell.isDisposed())
				if(!display.readAndDispatch())
					display.sleep();
			scope.exit();
		}
	}

	public void setController(Provider<? extends Controller> controller) {
		this.controller = controller;
		shell.dispose();
	}

	@Override
	protected void doStart() {
		try {
			if(!HOME_DIR.exists())
				HOME_DIR.mkdir();
			display = new Display();
			if(!conf.isAuthorized())
				controller = authController;
			else
				controller = mainController;
			notifyStarted();
		} catch (Exception e) {
			notifyFailed(e);
			logger.error("Exception occured in doStart", e);
			return;
		}
		try {
			run();
		} catch (Exception e) {
			notifyFailed(e);
			logger.error("Exception occured in run", e);
		}
	}

	@Override
	protected void doStop() {
		try {
			backgroundExecutor.shutdownNow();
			player.stop();
			asyncHttpClient.close();
			mpg123Native.mpg123_exit();
			streamManager.close();
			display.dispose();
			notifyStopped();
		} catch (Exception e) {
			notifyFailed(e);
			logger.error("Exception occured in stop", e);
		}
	}
}
