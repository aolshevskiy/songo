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
		RemoteStream prev = streams.putIfAbsent(stream.getTrack(), stream);
		assert prev == null;
	}

	void remove(RemoteStream stream) {
		boolean result = streams.remove(stream.getTrack(), stream);
		assert result;
	}

	RemoteStream get(Audio track) {
		RemoteStream stream = streams.get(track);
		if (stream != null)
			stream.seek(0);
		return stream;
	}

	public void close() {
		for (Map.Entry<Audio, RemoteStream> e : streams.entrySet())
			e.getValue().closeAndDelete();
	}
}
