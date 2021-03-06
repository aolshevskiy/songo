package songo.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.eclipse.swt.widgets.Display;
import songo.annotation.GlobalBus;
import songo.annotation.SessionBus;
import songo.model.Player;
import songo.model.Playlist;
import songo.view.PlayerControl;
import songo.view.PlaylistView;
import songo.vk.Audio;

import java.util.Collections;
import java.util.List;

public class PlaylistController {
	private final PlaylistView view;
	private final Player player;
	private final Playlist playlist;
	private final Display display;

	@Inject
	PlaylistController(PlaylistView view, @GlobalBus EventBus globalBus, @SessionBus EventBus sessionBus, Player player,
		Playlist playlist, Display display,
		PlayerControl playerControl) {
		this.view = view;
		this.player = player;
		this.playlist = playlist;
		this.display = display;
		globalBus.register(this);
		sessionBus.register(this);
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

	@Subscribe
	public void prev(PlayerControl.Prev e) {
		int prev = playlist.getCurrentTrackIndex() - 1;
		if(prev < 0)
			prev = playlist.getTracks().size() - 1;
		prevNextUpdateState(prev);
	}

	@Subscribe
	public void next(PlayerControl.Next e) {
		next();
	}

	private void next() {
		int next = playlist.getCurrentTrackIndex() + 1;
		if(next == playlist.getTracks().size())
			next = 0;
		prevNextUpdateState(next);
	}

	private void prevNextUpdateState(int trackIndex) {
		playlist.setCurrentTrackIndex(trackIndex);
		if(player.getState() == Player.State.PLAYING)
			player.play();
		else if(player.getState() == Player.State.PAUSED)
			player.stop();
	}

	@Subscribe
	public void play(PlaylistView.Play e) {
		if(view.getSelectedIndices().length == 0)
			return;
		playlist.setCurrentTrackIndex(view.getSelectedIndices()[0]);
		player.play();
	}

	@Subscribe
	public void insertBefore(PlaylistView.InsertBefore e) {
		List<Audio> view = Lists.newArrayList(playlist.getTracks());
		Audio target = view.get(e.target);
		List<Integer> sourceIds = Lists.newArrayList();
		for(int i : e.source)
			sourceIds.add(i);
		Collections.reverse(sourceIds);
		List<Audio> source = Lists.newArrayList();
		for(int i : sourceIds)
			source.add(view.remove(i));
		Collections.reverse(source);
		int i = 0;
		for(Audio track : view) {
			if(track == target)
				break;
			i++;
		}
		view.addAll(i, source);
		playlist.setTracks(ImmutableList.copyOf(view));
	}

	@Subscribe
	public void delete(PlaylistView.Delete e) {
		playlist.remove(view.getSelectedIndices());
	}
}
