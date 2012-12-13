package songo.controller;

import com.google.inject.Inject;
import songo.view.MainView;
import songo.view.View;

public class MainController implements Controller {
	private final MainView view;

	@Inject
	MainController(MainView view, SearchController searchController, PlaylistController playlistController,
		PlayerController playerController) {
		this.view = view;
	}

	@Override
	public View getView() {
		return view;
	}
}
