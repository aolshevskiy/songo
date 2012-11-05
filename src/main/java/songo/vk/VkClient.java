package songo.vk;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ning.http.client.*;
import org.slf4j.Logger;
import songo.logging.InjectLogger;

import java.io.*;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;

public class VkClient {
	private final Provider<String> accessToken;
	private final Gson gson;
	private final AsyncHttpClient client;
	@InjectLogger Logger logger;

	@Inject
	VkClient(@VkAccessToken Provider<String> accessToken, Gson gson, AsyncHttpClient client) {
		this.accessToken = accessToken;
		this.gson = gson;
		this.client = client;
	}

	private static final String ENDPOINT = "https://api.vk.com/method/";

	private String urlEncode(String input) {
		try {
			return URLEncoder.encode(input, Charsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw Throwables.propagate(e);
		}
	}

	private AsyncHttpClient.BoundRequestBuilder buildRequest(String method, Map<String, String> params) {
		StringBuilder url = new StringBuilder();
		url.append(ENDPOINT).append(method).append("?access_token=").append(accessToken.get());
		for (Map.Entry<String, String> e : params.entrySet())
			url.append("&").append(urlEncode(e.getKey())).append("=").append(urlEncode(e.getValue()));
		return client.prepareGet(url.toString());
	}

	private InputStream openStream(AsyncHttpClient.BoundRequestBuilder request) throws IOException {
		final PipedOutputStream out = new PipedOutputStream();
		final PipedInputStream in = new PipedInputStream(out, 8192);
		request.execute(new AsyncHandler<Object>() {
			private void close() {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					logger.warn("IO Exception occured in vk api request", e);
				}
			}

			@Override
			public void onThrowable(Throwable t) {
				logger.warn("Exception occured in vk api request", t);
				close();
			}

			@Override
			public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
				bodyPart.writeTo(out);
				return STATE.CONTINUE;
			}

			@Override
			public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
				if(responseStatus.getStatusCode() != 200)
					return STATE.ABORT;
				return STATE.CONTINUE;
			}

			@Override
			public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
				return STATE.CONTINUE;
			}

			@Override
			public Object onCompleted() throws Exception {
				close();
				return null;
			}
		});
		return in;
	}

	private <T> T call(String method, Map<String, String> params, TypeToken<T> type) {
		AsyncHttpClient.BoundRequestBuilder request = buildRequest(method, params);
		try {
			InputStream in = openStream(request);
			TypeToken<VkResponse<T>> token = new TypeToken<VkResponse<T>>() {}
				.where(new TypeParameter<T>() {
				}, type);
			VkResponse<T> response = gson.fromJson(new InputStreamReader(in), token.getType());
			in.close();
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
