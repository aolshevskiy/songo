package songo.model;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.slf4j.Logger;
import songo.logging.InjectLogger;
import songo.model.streams.RemoteStream;
import songo.model.streams.Stream;
import songo.mpg123.Mpg123;
import songo.mpg123.Mpg123Exception;
import songo.vk.Audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

class Decoder implements Runnable {
	private final int duration;
	private int position;
	private final Stream stream;
	private final Mpg123 mpg;
	private boolean sizeSet;
	private boolean paused;
	private final EventBus bus;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private byte[] buffer = new byte[1024];
	private byte[] outbuffer = new byte[8192];
	private SourceDataLine line;
	@InjectLogger Logger logger;

	@Inject
	Decoder(Audio track, Stream stream, Mpg123 mpg, EventBus bus) {
		this.duration = track.duration;
		this.stream = stream;
		this.mpg = mpg;
		this.bus = bus;
		stream.setDoneSeekingListener(new Runnable() {
			@Override
			public void run() {
				doneSeeking();
			}
		});
		if (stream instanceof RemoteStream)
			((RemoteStream) stream).setDownloadProgressListener(new Runnable() {
				@Override
				public void run() {
					updateProgress();
				}
			});
		updateProgress();
	}

	void open() {
		try {
			mpg.openFeed();
		} catch (Mpg123Exception e) {
			throw Throwables.propagate(e);
		}
		executor.submit(this);
	}

	void close() {
		executor.shutdownNow();
		if(line != null) {
			line.stop();
			line.close();
		}
		try {
			mpg.close();
		} catch (Mpg123Exception e) {
			//ignore mpg handle closed exception here
		}
		if (stream instanceof RemoteStream)
			((RemoteStream) stream).setDownloadProgressListener(null);
		stream.closeIfCompleted();
	}

	@Override
	public void run() {
		try {
			if (paused) {
				line.flush();
				return;
			}
			int read = stream.read(buffer);
			if (read == -1 && stream.getLimit() != stream.getLength()) {
				stream.seek(stream.getPosition());
				return;
			}
			int outsize = outbuffer.length;
			if (line == null)
				outsize = 0;
			int decoded = mpg.decode(buffer, read == -1 ? 0 : read, outbuffer, outsize);
			if (read == -1 && decoded == 0) {
				bus.post(new Player.DonePlaying());
				return;
			}
			if (line == null && mpg.getLastError() == Mpg123.NEW_FORMAT)
				initLine();
			if (line != null)
				line.write(outbuffer, 0, decoded);
			postEvents();
			try {
				executor.execute(this);
			} catch(RejectedExecutionException e) {
				//ignore rejected execution exception
			}
		} catch (Exception e) {
			logger.error("Exception occured in decoder main loop", e);
		}
	}

	private void postEvents() {
		try {
			if (!sizeSet && stream.getLength() != -1) {
				sizeSet = true;
				mpg.setFileSize((int) stream.getLength());
			}
			int newPosition = (int) ((float) duration * mpg.getSamplePosition() / mpg.getLength());
			if (newPosition != position) {
				position = newPosition;
				bus.post(new Player.UpdatePosition(position));
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	private void initLine() {
		try {
			line = AudioSystem.getSourceDataLine(mpg.getFormat());
			line.open();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		line.start();
	}

	private void doneSeeking() {
		try {
			executor.submit(this);
		} catch(RejectedExecutionException e) {
			//Ignore rejected here
		}
	}

	private void updateProgress() {
		int progress;
		if (stream.getLength() == -1)
			progress = 0;
		else
			progress = (int) ((float) stream.getLimit() / stream.getLength() * 100);
		bus.post(new Player.UpdateDownloadProgress(progress));
	}

	void seek(float position) {
		executor.execute(new Seek(position));
	}

	private class Seek implements Runnable {
		private final float position;

		private Seek(float position) {
			this.position = position;
		}

		@Override
		public void run() {
			try {
				line.flush();
				int sampleOffset = (int) (mpg.getLength() * position);
				stream.seek(mpg.seek(sampleOffset));
			} catch(Exception e) {
				logger.error("Exception occured while seeking", e);
			}
		}
	}

	void pause() {
		paused = true;
	}

	void unpause() {
		paused = false;
		executor.execute(this);
	}
}
