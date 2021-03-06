package songo.view;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import songo.ResourceUtil;
import songo.annotation.GlobalBus;
import songo.annotation.SessionBus;

import static songo.ResourceUtil.iconStream;

@SessionScoped
public class PlayerControl extends Composite {
	private final EventBus globalBus;
	private final ProgressBar progress;
	private int duration = 0;
	private ProgressBar downloadProgress;
	private ToolItem playButton;
	private Image playIcon;
	private Image pauseIcon;

	@Inject
	public PlayerControl(MainView mainView, @GlobalBus EventBus globalBus, @SessionBus final EventBus sessionBus) {
		super(mainView.getShell(), SWT.NONE);
		this.globalBus = globalBus;
		moveAbove(mainView.getShell().getChildren()[0]);
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		GridLayout thisLayout = new GridLayout(2, false);
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
		prevButton.setImage(new Image(getDisplay(), iconStream("prev")));
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
		playIcon = new Image(getDisplay(), iconStream("play"));
		pauseIcon = new Image(getDisplay(), iconStream("pause"));
		playButton.setImage(playIcon);
		ToolItem stopButton = new ToolItem(toolbar, SWT.FLAT);
		stopButton.setImage(new Image(getDisplay(), iconStream("stop")));
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sessionBus.post(new Stop());
			}
		});
		ToolItem nextButton = new ToolItem(toolbar, SWT.FLAT);
		nextButton.setImage(new Image(getDisplay(), iconStream("next")));
		nextButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sessionBus.post(new Next());
			}
		});
		progress = new ProgressBar(this, SWT.NONE);
		progress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		progress.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				String string =
					formatProgressText(progress.getSelection());
				Point point = progress.getSize();
				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width =
					fontMetrics.getAverageCharWidth() * string.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground
					(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
				e.gc.drawString
					(string, (point.x-width)/2 , (point.y-height)/2, true);
			}
		});
		progress.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				seek(e);
			}
		});
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

	public void updateDuration(final int duration) {
		this.duration = duration;
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				progress.setMaximum(duration);
				progress.setSelection(0);
			}
		});
	}

	public void updatePosition(final int position) {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				progress.setSelection(position);
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
