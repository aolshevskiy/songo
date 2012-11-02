package songo.model.streams;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import songo.vk.Audio;


@Singleton
public class StreamProvider implements Provider<Stream> {
	private final Provider<Audio> audio;
	private final StreamManager manager;
	private final RemoteStreamFactory remoteStreamFactory;
	private final LocalStreamFactory localStreamFactory;
	private final StreamUtil util;

	@Inject
	StreamProvider(Provider<Audio> audio, StreamManager manager, RemoteStreamFactory remoteStreamFactory, LocalStreamFactory localStreamFactory, StreamUtil util) {
		this.audio = audio;
		this.manager = manager;
		this.remoteStreamFactory = remoteStreamFactory;
		this.localStreamFactory = localStreamFactory;
		this.util = util;
	}

	@Override
	public Stream get() {
		Audio track = audio.get();
		RemoteStream existing = manager.get(track);
		if (existing != null)
			return existing;
		if (util.getTrackFile(track).exists())
			return localStreamFactory.create(track);
		return remoteStreamFactory.create(track);
	}
}
