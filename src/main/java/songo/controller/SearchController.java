package songo.controller;

import com.google.inject.Inject;
import songo.model.Playlist;
import songo.view.SearchView;
import songo.vk.VkClient;

class SearchController {
	private final SearchView searchView;
	private final VkClient client;
	private final Playlist playlist;

	@Inject
	SearchController(SearchView searchView, VkClient client, Playlist playlist) {
		this.searchView = searchView;
		this.client = client;
		this.playlist = playlist;
		searchView.addSearchListener(new Runnable() {
			@Override
			public void run() {
				search();
			}
		});
		searchView.addAddListener(new Runnable() {
			@Override
			public void run() {
				add();
			}
		});
	}

	private void search() {
		searchView.setResults(client.audioSearch(searchView.getSearchQuery()));
	}

	private void add() {
		playlist.add(searchView.getSelectedTracks());
	}
}
