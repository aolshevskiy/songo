package songo;

import com.google.common.eventbus.EventBus;
import com.google.inject.*;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.servlet.SessionScoped;
import com.ning.http.client.AsyncHttpClient;
import com.sun.istack.internal.Nullable;
import com.sun.jna.Native;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import songo.annotation.ConfigurationFile;
import songo.annotation.PlaylistTab;
import songo.annotation.SearchTab;
import songo.annotation.SessionScope;
import songo.model.Configuration;
import songo.model.Playlist;
import songo.model.streams.*;
import songo.mpg123.Mpg123Native;
import songo.view.MainView;
import songo.vk.*;

import java.io.File;

public class MainModule extends AbstractModule {
	@Override
	protected void configure() {
		SimpleScope sessionScope = new SimpleScope();
		bindScope(SessionScoped.class, sessionScope);
		bind(SimpleScope.class).annotatedWith(SessionScope.class).toInstance(sessionScope);
		bind(EventBus.class).in(Singleton.class);
		bind(File.class).annotatedWith(ConfigurationFile.class).toInstance(new File("configuration.properties"));
		bind(String.class).annotatedWith(VkAppId.class).toInstance("3192319");
		bind(String.class).annotatedWith(VkRegisterUrl.class).toInstance("http://vk.com/join");
		bind(AsyncHttpClient.class).in(Singleton.class);
		bind(Stream.class).toProvider(StreamProvider.class);
		install(
			new FactoryModuleBuilder()
				.implement(RemoteStream.class, RemoteStream.class)
				.build(RemoteStreamFactory.class));
		install(
			new FactoryModuleBuilder()
				.implement(LocalStream.class, LocalStream.class)
				.build(LocalStreamFactory.class));
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
		return (Mpg123Native) Native.loadLibrary("mpg123", Mpg123Native.class);
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

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(Stage.PRODUCTION, new MainModule());
		final SongoService service = injector.getInstance(SongoService.class);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				service.stopAndWait();
			}
		});
		service.startAndWait();
	}
}
