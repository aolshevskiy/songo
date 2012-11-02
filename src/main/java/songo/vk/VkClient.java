package songo.vk;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class VkClient {
	private final Provider<String> accessToken;
	private final Gson gson;

	@Inject
	VkClient(@VkAccessToken Provider<String> accessToken, Gson gson) {
		this.accessToken = accessToken;
		this.gson = gson;
	}

	private static final String ENDPOINT = "https://api.vk.com/method/";

	private static String urlEncode(String input) {
		try {
			return URLEncoder.encode(input, Charsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw Throwables.propagate(e);
		}
	}

	private URL buildRequestUrl(String method, Map<String, String> params) {
		StringBuilder url = new StringBuilder();
		url.append(ENDPOINT).append(method).append("?access_token=").append(accessToken.get());
		for (Map.Entry<String, String> e : params.entrySet())
			url.append("&").append(urlEncode(e.getKey())).append("=").append(urlEncode(e.getValue()));
		try {
			return new URL(url.toString());
		} catch (MalformedURLException e) {
			throw Throwables.propagate(e);
		}
	}

	private <T> T call(String method, Map<String, String> params, TypeToken<T> type) {
		URL url = buildRequestUrl(method, params);
		TypeToken<VkResponse<T>> token = new TypeToken<VkResponse<T>>() {
		}.where(new TypeParameter<T>() {
		}, type);
		try {
			VkResponse<T> response = gson.fromJson(new InputStreamReader(url.openStream()), token.getType());
			return response.getResponse();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	private static String fixHtmlEntities(String input) {
		return input.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
	}

	@SuppressWarnings("unchecked")
	public List<Audio> audioSearch(String query, int count) {
		List<Object> audios = call("audio.search", ImmutableMap.of("q", query, "count", Integer.toString(count)), new TypeToken<List<Object>>() {
		});
		audios = audios.subList(1, audios.size());
		ImmutableList.Builder<Audio> result = new ImmutableList.Builder<Audio>();
		for (Object o : audios) {
			Map<String, Object> i = (Map<String, Object>) o;
			result.add(new Audio(fixHtmlEntities((String) i.get("artist")), fixHtmlEntities((String) i.get("title")), (String) i.get("url"), ((Double) i.get("duration")).intValue()));
		}
		return result.build();
	}

	public List<Audio> audioSearch(String query) {
		return audioSearch(query, 200);
	}
}
