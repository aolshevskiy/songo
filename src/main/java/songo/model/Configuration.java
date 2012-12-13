package songo.model;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import songo.annotation.ConfigurationFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Singleton
public class Configuration {
	private Properties props;
	private final File file;

	@Inject
	Configuration(@ConfigurationFile File file) {
		this.file = file;
		load();
	}

	private void load() {
		props = new Properties();
		if(!file.exists())
			return;
		try {
			props.load(new FileInputStream(file));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private void save() {
		try {
			props.store(new FileOutputStream(file), "");
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
