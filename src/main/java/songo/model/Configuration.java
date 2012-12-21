package songo.model;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static songo.Constants.HOME_DIR;

@Singleton
public class Configuration {
	private static final File CONFIGURATION_FILE = new File(HOME_DIR, "configuration.properties");
	private Properties props;

	@Inject
	Configuration() {
		load();
	}

	private void load() {
		props = new Properties();
		if(!CONFIGURATION_FILE.exists())
			return;
		try {
			props.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private void save() {
		try {
			props.store(new FileOutputStream(CONFIGURATION_FILE), "");
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	public boolean isAuthorized() {
		return props.getProperty("vk.accessToken") != null;
	}

	public void setAccessToken(String token) {
		props.setProperty("vk.accessToken", token);
		save();
	}

	public String getAccessToken() {
		return props.getProperty("vk.accessToken");
	}
}
