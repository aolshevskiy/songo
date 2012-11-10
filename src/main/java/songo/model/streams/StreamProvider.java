package songo.model.streams;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import songo.vk.Audio;


@Singleton
public class StreamProvider implements Provider<Stream> {
	private final Provider<Audio> audio;
	private final StreamManager manager;
	private final StreamFactory streamFactory;
	private final StreamUtil util;

	@Inject
	StreamProvider(Provider<Audio> audio, StreamManager manager, StreamFactory streamFactory, StreamUtil util) {
		this.audio = audio;
		this.manager = manager;
		this.streamFactory = streamFactory;
		this.util = util;
	}

	@Override
	public Stream get() {
		Audio track = audio.get();
		RemoteStream existing = manager.get(track);
		if (existing != null)
			return existing;
		if (util.getTrackFile(track).exists())
			return streamFactory.createLocal(track);
		return streamFactory.createRemote(track);
	}
}
