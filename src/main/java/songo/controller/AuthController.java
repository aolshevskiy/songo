package songo.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.SessionScoped;
import org.eclipse.swt.browser.LocationEvent;
import songo.SongoService;
import songo.annotation.SessionBus;
import songo.model.Configuration;
import songo.view.AuthDialog;
import songo.view.View;
import songo.vk.VkAuthUrl;
import songo.vk.VkRegisterUrl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@SessionScoped
public class AuthController implements Controller {
	private final AuthDialog dialog;
	private final String authUrl;
	private final String registerUrl;
	private final Configuration conf;
	private final SongoService service;
	private final Provider<MainController> mainController;
	private static final String ERROR_PREFIX = "https://oauth.vk.com/error";
	private static final String TOKEN_PREFIX = "https://oauth.vk.com/blank.html#";
	private static final String PROFILE_PREFIX = "http://vk.com/al_profile.php";

	private static Map<String, String> parseQueryString(String queryString) {
		ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
		for(String rawPair : queryString.split("&")) {
			String[] pair = rawPair.split("=");
			result.put(pair[0], pair[1]);
		}
		return result.build();
	}

	@Inject
	AuthController(final AuthDialog dialog, @VkAuthUrl final String authUrl, @VkRegisterUrl String registerUrl,
		final Configuration conf, SongoService service, Provider<MainController> mainController, @SessionBus EventBus bus) {
		this.dialog = dialog;
		this.authUrl = authUrl;
		this.registerUrl = registerUrl;
		this.conf = conf;
		this.service = service;
		this.mainController = mainController;
		auth();
		bus.register(this);
	}

	@Subscribe
	public void register(AuthDialog.Register e) {
		dialog.openUrl(registerUrl);
	}

	@Subscribe
	public void location(LocationEvent event) {
		if(event.location.startsWith(PROFILE_PREFIX) || event.location.startsWith(ERROR_PREFIX)) {
			event.doit = false;
			auth();
		}
		if(event.location.startsWith(TOKEN_PREFIX)) {
			event.doit = false;
			acquireToken(event.location);
		}
	}

	private void acquireToken(String location) {
		try {
			Map<String, String> loginResult = parseQueryString(new URL(location).getRef());
			conf.setAccessToken(loginResult.get("access_token"));
			service.setController(mainController);
		} catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
	}

	private void auth() {
		dialog.openUrl(authUrl);
	}


	@Override
	public View getView() {
		return dialog;
	}
}
