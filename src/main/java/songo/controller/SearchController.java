package songo.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.eclipse.swt.widgets.Display;
import songo.annotation.BackgroundExecutor;
import songo.annotation.SessionBus;
import songo.model.Player;
import songo.model.Playlist;
import songo.view.SearchView;
import songo.vk.Audio;
import songo.vk.VkClient;

import java.util.List;
import java.util.concurrent.ExecutorService;

class SearchController {
	private final SearchView searchView;
	private final VkClient client;
	private final Playlist playlist;
	private final Player player;
	private final ExecutorService executor;
	private final Display display;

	@Inject
	SearchController(SearchView searchView, VkClient client, Playlist playlist, Player player,
		@BackgroundExecutor ExecutorService executor, Display display, @SessionBus EventBus bus) {
		this.searchView = searchView;
		this.client = client;
		this.playlist = playlist;
		this.player = player;
		this.executor = executor;
		this.display = display;
		bus.register(this);
	}

	@Subscribe
	public void search(SearchView.Search e) {
		final String query = searchView.getSearchQuery();
		executor.submit(new Runnable() {
			@Override
			public void run() {
				final List<Audio> results = client.audioSearch(query);
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						searchView.setResults(results);
					}
				});
			}
		});
	}

	@Subscribe
	public void play(SearchView.Play e) {
		add();
		playlist.setCurrentTrackIndex(playlist.getTracks().size() - 1);
		player.play();
	}

	@Subscribe
	public void add(SearchView.Add e) {
		add();
	}

	@Subscribe
	public void replace(SearchView.Replace e) {
		playlist.replace(searchView.getSelectedTracks());
	}

	private void add() {
		playlist.add(searchView.getSelectedTracks());
	}
}
