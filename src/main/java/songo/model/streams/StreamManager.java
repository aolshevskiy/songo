package songo.model.streams;

import com.google.inject.Singleton;
import songo.vk.Audio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class StreamManager {
	private ConcurrentMap<Audio, RemoteStream> streams = new ConcurrentHashMap<Audio, RemoteStream>();

	void add(RemoteStream stream) {
		streams.put(stream.getTrack(), stream);
	}

	void remove(RemoteStream stream) {
		streams.remove(stream.getTrack(), stream);
	}

	RemoteStream get(Audio track) {
		RemoteStream stream = streams.get(track);
		if (stream != null)
			stream.seek(0);
		return stream;
	}

	public void close() {
		for (Map.Entry<Audio, RemoteStream> e : streams.entrySet()) {
			e.getValue().close();
			e.getValue().getTrackFile().delete();
		}
	}
}
