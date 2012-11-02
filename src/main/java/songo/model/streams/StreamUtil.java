package songo.model.streams;

import songo.vk.Audio;

import java.io.File;

public class StreamUtil {
	private final File cacheDir = new File("cache");

	public File getTrackFile(Audio track) {
		File artistDir = new File(cacheDir, track.artist);
		if (!artistDir.exists())
			artistDir.mkdirs();
		return new File(artistDir, track.title + ".mp3");
	}
}
