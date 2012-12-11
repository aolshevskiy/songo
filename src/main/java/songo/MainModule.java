package songo;

import com.google.common.eventbus.EventBus;
import com.google.inject.*;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.SessionScoped;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.sun.jna.Native;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import songo.annotation.*;
import songo.logging.LoggingTypeListener;
import songo.model.Configuration;
import songo.model.Playlist;
import songo.model.streams.*;
import songo.mpg123.Mpg123Native;
import songo.view.MainView;
import songo.vk.*;

import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainModule extends AbstractModule {
	private static int getNativeBrowserStyle() {
		if(System.getProperty("os.name").equals("Linux"))
			return SWT.WEBKIT;
		return SWT.NONE;
	}

	private static boolean isRunningWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	@Override
	protected void configure() {
		bind(Boolean.class).annotatedWith(RunningWindows.class).toInstance(isRunningWindows());
		SimpleScope sessionScope = new SimpleScope();
		bindScope(SessionScoped.class, sessionScope);
		bind(SimpleScope.class).annotatedWith(SessionScope.class).toInstance(sessionScope);
		bind(EventBus.class).in(Singleton.class);
		bind(File.class).annotatedWith(ConfigurationFile.class).toInstance(new File("configuration.properties"));
		bind(String.class).annotatedWith(VkAppId.class).toInstance("3192319");
		bind(String.class).annotatedWith(VkRegisterUrl.class).toInstance("http://vk.com/join");
		bind(Integer.class).annotatedWith(BrowserStyle.class).toInstance(getNativeBrowserStyle());
		bind(Stream.class).toProvider(StreamProvider.class);
		install(
			new FactoryModuleBuilder()
				.implement(RemoteStream.class, RemoteStream.class)
				.implement(LocalStream.class, LocalStream.class)
				.build(StreamFactory.class));
		bindListener(Matchers.any(), new LoggingTypeListener());
	}

	@Provides
	@VkAuthUrl
	@Singleton
	String provideAuthUrl(@VkAppId String appId) {
		return "https://oauth.vk.com/authorize?client_id=" + appId + "&scope=audio,offline&redirect_url=http://oauth.vk.com/blank.html&display=popup&response_type=token";
	}

	@Provides
	Display provideDisplay(SongoService service) {
		return service.getDisplay();
	}

	@Provides
	Shell provideShell(Display display) {
		return new Shell(display);
	}

	@Provides
	@VkAccessToken
	String provideAccessToken(Configuration configuration) {
		return configuration.getAccessToken();
	}

	@Provides
	@Singleton
	Mpg123Native provideMpg123Native() {
		Mpg123Native result = (Mpg123Native) Native.loadLibrary("mpg123", Mpg123Native.class);
		result.mpg123_init();
		return result;
	}

	@Provides
	@SessionScoped
	@SearchTab
	TabItem provideSearchTab(MainView mainView) {
		return mainView.getSearchTab();
	}

	@Provides
	@SessionScoped
	@PlaylistTab
	TabItem providePlaylistTab(MainView mainView) {
		return mainView.getPlaylistTab();
	}

	@Provides
	@Nullable
	Audio provideCurrentTrack(Playlist playlist) {
		return playlist.getCurrentTrack();
	}

	@Provides
	@Singleton @BackgroundExecutor
	ExecutorService provideBackgroundExecutor() {
		return Executors.newSingleThreadExecutor();
	}

	@Provides @Singleton
	AsyncHttpClient provideAsyncHttpClient() {
		AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
			.setRequestTimeoutInMs(300000)
			.build();
		return new AsyncHttpClient(config);
	}

	public static void main(Stage stage) {
		Injector injector = Guice.createInjector(stage, new MainModule());
		final SongoService service = injector.getInstance(SongoService.class);
		service.startAndWait();
	}

	public static void main(String[] args) {
		main(Stage.PRODUCTION);
	}
}
