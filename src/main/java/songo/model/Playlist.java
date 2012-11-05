package songo.model;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import songo.vk.Audio;

import java.util.ArrayList;
import java.util.List;

@SessionScoped
public class Playlist {
	private List<Audio> tracks = ImmutableList.of();
	private int currentTrackIndex = -1;
	private final EventBus bus;

	public List<Audio> getTracks() {
		return tracks;
	}

	public void setTracks(List<Audio> tracks) {
		Audio track = getCurrentTrack();
		this.tracks = tracks;
		fixCurrentTrack(track);
		changed();
	}

	private void fixCurrentTrack(Audio track) {
		currentTrackIndex = -1;
		int i = 0;
		for(Audio t: tracks) {
			if(t == track) {
				currentTrackIndex = i;
				return;
			}
			i++;
		}
	}

	public Audio getCurrentTrack() {
		if(currentTrackIndex == -1)
			return null;
		return tracks.get(currentTrackIndex);
	}

	public int getCurrentTrackIndex() {
		return currentTrackIndex;
	}

	public void setCurrentTrackIndex(int currentTrackIndex) {
		this.currentTrackIndex = currentTrackIndex;
		currentTrackChanged();
	}

	@Inject
	Playlist(EventBus bus) {
		this.bus = bus;
	}

	public void add(List<Audio> newTracks) {
		tracks = new ImmutableList.Builder<Audio>().addAll(tracks).addAll(newTracks).build();
		changed();
	}

	public void replace(List<Audio> selectedTracks) {
		tracks = selectedTracks;
		currentTrackIndex = -1;
		changed();
	}

	public void remove(int[] indices) {
		Audio current = getCurrentTrack();
		ArrayList<Audio> view = new ArrayList<Audio>();
		view.addAll(tracks);
		for(int i = indices.length - 1; i >= 0; i--)
			view.remove(indices[i]);
		tracks = ImmutableList.copyOf(view);
		fixCurrentTrack(current);
		changed();
	}

	private void changed() {
		bus.post(new Changed());
	}

	private void currentTrackChanged() {
		bus.post(new CurrentTrackChanged());
	}

	public static class Changed {
		private Changed(){}
	}

	public static class CurrentTrackChanged {
		private CurrentTrackChanged(){}
	}
}
