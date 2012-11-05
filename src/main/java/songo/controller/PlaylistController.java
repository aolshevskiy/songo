package songo.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.eclipse.swt.widgets.Display;
import songo.model.Player;
import songo.model.Playlist;
import songo.view.PlayerControl;
import songo.view.PlaylistView;
import songo.vk.Audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistController {
	private final PlaylistView view;
	private final Player player;
	private final Playlist playlist;
	private final Display display;

	@Inject
	PlaylistController(PlaylistView view, EventBus bus, Player player, Playlist playlist, Display display, PlayerControl playerControl) {
		this.view = view;
		this.player = player;
		this.playlist = playlist;
		this.display = display;
		view.addPlayListener(new Runnable() {
			@Override
			public void run() {
				play();
			}
		});
		playerControl.addPrevListener(new Runnable() {
			@Override
			public void run() {
				prev();
			}
		});
		playerControl.addNextListener(new Runnable() {
			@Override
			public void run() {
				next();
			}
		});
		bus.register(this);
	}

	@Subscribe
	public void playlistChanged(Playlist.Changed e) {
		view.updateTable();
	}

	@Subscribe
	public void seek(PlayerControl.Seek e) {
		player.seek(e.position);
	}

	@Subscribe
	public void donePlaying(Player.DonePlaying e) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				next();
			}
		});
	}

	public void prev() {
		int prev = playlist.getCurrentTrackIndex() - 1;
		if (prev < 0)
			prev = playlist.getTracks().size() - 1;
		playlist.setCurrentTrackIndex(prev);
		player.play();
	}

	public void next() {
		int next = playlist.getCurrentTrackIndex() + 1;
		if (next == playlist.getTracks().size())
			next = 0;
		playlist.setCurrentTrackIndex(next);
		player.play();
	}

	public void play() {
		if (view.getSelectedIndices().length == 0)
			return;
		playlist.setCurrentTrackIndex(view.getSelectedIndices()[0]);
		player.play();
	}

	@Subscribe
	public void insertBefore(PlaylistView.InsertBefore e) {
		List<Audio> view = new ArrayList<Audio>();
		view.addAll(playlist.getTracks());
		Audio target = view.get(e.target);
		List<Integer> sourceIds = new ArrayList<Integer>();
		for(int i: e.source)
			sourceIds.add(i);
		Collections.reverse(sourceIds);
		List<Audio> source = new ArrayList<Audio>();
		for(int i: sourceIds)
			source.add(view.remove(i));
		Collections.reverse(source);
		int i = 0;
		for(Audio track: view) {
			if(track == target)
				break;
			i++;
		}
		view.addAll(i, source);
		playlist.setTracks(ImmutableList.copyOf(view));
	}
}
