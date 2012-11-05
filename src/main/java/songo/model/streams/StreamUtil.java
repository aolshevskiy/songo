package songo.model.streams;

import com.google.inject.Inject;
import songo.annotation.RunningWindows;
import songo.vk.Audio;

import java.io.File;

public class StreamUtil {
	private final File cacheDir = new File("cache");
	private final boolean runningWindows;

	@Inject
	StreamUtil(@RunningWindows Boolean runningWindows) {
		this.runningWindows = runningWindows;
	}

	private static String escapeForWindows(String input) {
		return input.replace("\"", "");
	}

	private String fixFilename(String input) {
		if(runningWindows)
			return escapeForWindows(input);
		return input;
	}

	public File getTrackFile(Audio track) {
		File artistDir = new File(cacheDir, fixFilename(track.artist));
		if (!artistDir.exists())
			artistDir.mkdirs();
		return new File(artistDir, fixFilename(track.title) + ".mp3");
	}
}
