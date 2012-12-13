package songo.view;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import songo.ResourceUtil;
import songo.annotation.GlobalBus;
import songo.annotation.SessionBus;

@SessionScoped
public class PlayerControl extends Composite {
	private final EventBus globalBus;
	private final ProgressBar progress;
	private Label progressText;
	private int duration = 0;
	private ProgressBar downloadProgress;
	private ToolItem playButton;
	private Image playIcon;
	private Image pauseIcon;

	@Inject
	public PlayerControl(MainView mainView, @GlobalBus EventBus globalBus, @SessionBus final EventBus sessionBus,
		ResourceUtil resourceUtil) {
		super(mainView.getShell(), SWT.NONE);
		this.globalBus = globalBus;
		moveAbove(mainView.getShell().getChildren()[0]);
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		GridLayout thisLayout = new GridLayout(3, false);
		thisLayout.marginWidth = 0;
		thisLayout.marginHeight = 0;
		thisLayout.horizontalSpacing = 0;
		thisLayout.verticalSpacing = 0;
		setLayout(thisLayout);
		ToolBar toolbar = new ToolBar(this, SWT.FLAT);
		GridData toolbarData = new GridData(SWT.CENTER, SWT.FILL, false, true);
		toolbarData.verticalSpan = 2;
		toolbar.setLayoutData(toolbarData);
		ToolItem prevButton = new ToolItem(toolbar, SWT.FLAT);
		prevButton.setImage(new Image(getDisplay(), resourceUtil.iconStream("prev")));
		prevButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sessionBus.post(new Prev());
			}
		});
		playButton = new ToolItem(toolbar, SWT.FLAT);
		playButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sessionBus.post(new PlayPause());
			}
		});
		playIcon = new Image(getDisplay(), resourceUtil.iconStream("play"));
		pauseIcon = new Image(getDisplay(), resourceUtil.iconStream("pause"));
		playButton.setImage(playIcon);
		ToolItem stopButton = new ToolItem(toolbar, SWT.FLAT);
		stopButton.setImage(new Image(getDisplay(), resourceUtil.iconStream("stop")));
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sessionBus.post(new Stop());
			}
		});
		ToolItem nextButton = new ToolItem(toolbar, SWT.FLAT);
		nextButton.setImage(new Image(getDisplay(), resourceUtil.iconStream("next")));
		nextButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sessionBus.post(new Next());
			}
		});
		progressText = new Label(this, SWT.NONE);
		updateProgressText(0);
		progressText.setAlignment(SWT.RIGHT);
		progress = new ProgressBar(this, SWT.NONE);
		progress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		progress.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				seek(e);
			}
		});
		Label placeholder = new Label(this, SWT.NONE);
		placeholder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		downloadProgress = new ProgressBar(this, SWT.NONE);
		GridData downloadData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		downloadData.heightHint = 10;
		downloadProgress.setLayoutData(downloadData);
	}

	public void seek(MouseEvent e) {
		globalBus.post(new Seek((float) e.x / progress.getSize().x));
	}

	private String formatProgressText(int position) {
		return String.format("%02d:%02d/%02d:%02d", position / 60, position % 60, duration / 60, duration % 60);
	}

	private void updateProgressText(int position) {
		progressText.setText(formatProgressText(position));
	}

	public void updateDuration(final int duration) {
		this.duration = duration;
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				progress.setMaximum(duration);
				progress.setSelection(0);
				updateProgressText(0);
			}
		});
	}

	public void updatePosition(final int position) {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				progress.setSelection(position);
				updateProgressText(position);
			}
		});
	}

	public void updateDownloadProgress(final int position) {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				downloadProgress.setSelection(position);
				downloadProgress.setVisible(position != 100);
			}
		});
	}

	public static class Seek {
		public final float position;

		private Seek(float position) {
			this.position = position;
		}
	}

	public void setIsPlaying(boolean isPlaying) {
		playButton.setImage(isPlaying ? pauseIcon : playIcon);
	}

	public static class Stop {
		private Stop() { }
	}

	public static class PlayPause {
		private PlayPause() { }
	}

	public static class Prev {
		private Prev() { }
	}

	public static class Next {
		private Next() { }
	}
}
