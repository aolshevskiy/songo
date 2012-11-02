package songo.model;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import songo.vk.Audio;

import java.util.List;

@SessionScoped
public class Playlist {
	private List<Audio> tracks = ImmutableList.of();
	private int currentTrackIndex = -1;
	private final EventBus bus;

	public List<Audio> getTracks() {
		return tracks;
	}

	public Audio getCurrentTrack() {
		return tracks.get(currentTrackIndex);
	}

	public int getCurrentTrackIndex() {
		return currentTrackIndex;
	}

	public void setCurrentTrackIndex(int currentTrackIndex) {
		this.currentTrackIndex = currentTrackIndex;
		bus.post(new Changed());
	}

	@Inject
	Playlist(EventBus bus) {
		this.bus = bus;
	}

	public void add(List<Audio> newTracks) {
		tracks = new ImmutableList.Builder<Audio>().addAll(tracks).addAll(newTracks).build();
		bus.post(new Changed());
	}

	public static class Changed {
	}
}
