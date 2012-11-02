package songo.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import songo.model.Player;
import songo.model.Playlist;
import songo.view.PlayerControl;

public class PlayerController {
	private final PlayerControl playerControl;
	private final Player player;
	private final Playlist playlist;

	@Inject
	PlayerController(PlayerControl playerControl, EventBus bus, Player player, Playlist playlist) {
		this.playerControl = playerControl;
		this.player = player;
		this.playlist = playlist;
		playerControl.addPlayPauseListener(new Runnable() {
			@Override
			public void run() {
				playPause();
			}
		});
		playerControl.addStopListener(new Runnable() {
			@Override
			public void run() {
				stop();
			}
		});
		bus.register(this);
	}

	private void stop() {
		if (player.getState() == Player.State.PLAYING || player.getState() == Player.State.PAUSED)
			player.stop();
	}

	private void playPause() {
		if (player.getState() == Player.State.STOPPED) {
			if (playlist.getTracks().size() == 0)
				return;
			if (playlist.getCurrentTrackIndex() == -1)
				playlist.setCurrentTrackIndex(0);
			player.play();
			return;
		}
		if (player.getState() == Player.State.PAUSED) {
			player.unpause();
			return;
		}
		player.pause();
	}

	@Subscribe
	public void updateDuration(Player.UpdateDuration e) {
		playerControl.updateDuration(e.duration);
	}

	@Subscribe
	public void updatePosition(Player.UpdatePosition e) {
		playerControl.updatePosition(e.position);
	}

	@Subscribe
	public void updateDownloadProgress(Player.UpdateDownloadProgress e) {
		playerControl.updateDownloadProgress(e.position);
	}

	@Subscribe
	public void playerStateChanged(Player.StateChanged e) {
		playerControl.setIsPlaying(e.state == Player.State.PLAYING);
	}
}
