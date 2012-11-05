package songo.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.SessionScoped;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import songo.SongoService;
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
	private static final String LOGIN_PREFIX = "https://login.vk.com/?act=login";

	private static Map<String, String> parseQueryString(String queryString) {
		ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
		for (String rawPair : queryString.split("&")) {
			String[] pair = rawPair.split("=");
			result.put(pair[0], pair[1]);
		}
		return result.build();
	}

	@Inject
	AuthController(final AuthDialog dialog, @VkAuthUrl final String authUrl, @VkRegisterUrl String registerUrl, final Configuration conf, SongoService service, Provider<MainController> mainController) {
		this.dialog = dialog;
		this.authUrl = authUrl;
		this.registerUrl = registerUrl;
		this.conf = conf;
		this.service = service;
		this.mainController = mainController;
		auth();
		dialog.addRegistrationListener(new Runnable() {
			@Override
			public void run() {
				register();
			}
		});
		dialog.addLocationListener(new LocationAdapter() {
			private void listen(LocationEvent event) {
				if (event.location.startsWith(PROFILE_PREFIX) || event.location.startsWith(ERROR_PREFIX)) {
					event.doit = false;
					auth();
				}
				if (event.location.startsWith(TOKEN_PREFIX)) {
					event.doit = false;
					acquireToken(event.location);
				}
			}

			@Override
			public void changing(LocationEvent event) {
				listen(event);
			}

			@Override
			public void changed(LocationEvent event) {
				listen(event);
			}
		});
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

	private String getElementValue(String name) {
		return (String) dialog.getBrowser().evaluate("return document.getElementById('login_submit').elements['" + name + "'].value");
	}

	private void auth() {
		dialog.openUrl(authUrl);
	}

	private void register() {
		dialog.openUrl(registerUrl);
	}

	@Override
	public View getView() {
		return dialog;
	}
}
