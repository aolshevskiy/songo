package songo;

import java.io.InputStream;

public class ResourceUtil {
	private ResourceUtil() {

	}
	public static InputStream iconStream(String name) {
		return ResourceUtil.class.getResourceAsStream("/songo/icons/dark/" + name + ".png");
	}
}
