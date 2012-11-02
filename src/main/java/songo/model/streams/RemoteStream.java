package songo.model.streams;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.ning.http.client.*;
import songo.vk.Audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executors;

public class RemoteStream implements Stream {
	private final AsyncHttpClient client;
	private final LocalStreamFactory factory;
	private final StreamManager manager;
	private final Audio track;
	private final File trackFile;
	private final RandomAccessFile file;
	private final FileChannel channel;
	private volatile long limit;
	private long length = -1;
	private LocalStream delegate;
	private volatile long expectedSeekPosition = -1;
	private volatile Runnable listener;
	private volatile Runnable progressListener;

	@Inject
	RemoteStream(AsyncHttpClient client, StreamUtil util, LocalStreamFactory factory, StreamManager manager, @Assisted Audio track) {
		this.client = client;
		this.factory = factory;
		this.manager = manager;
		this.track = track;
		trackFile = util.getTrackFile(track);
		try {
			file = new RandomAccessFile(trackFile, "rw");
		} catch (FileNotFoundException e) {
			throw Throwables.propagate(e);
		}
		channel = file.getChannel();
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			@Override
			public void run() {
				open();
			}
		});
	}

	public File getTrackFile() {
		return trackFile;
	}

	public Audio getTrack() {
		return track;
	}

	@Override
	public int read(byte[] buffer) {
		if (delegate != null)
			return delegate.read(buffer);
		try {
			return channel.read(ByteBuffer.wrap(buffer));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public boolean seek(final long position) {
		if (delegate != null)
			return delegate.seek(position);
		try {
			if (limit > position) {
				channel.position(position);
				return true;
			}
			expectedSeekPosition = position;
			return false;
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public long getLength() {
		if (delegate != null)
			return delegate.getLength();
		return length;
	}

	@Override
	public void setDoneSeekingListener(Runnable listener) {
		this.listener = listener;
	}

	public void setDownloadProgressListener(Runnable progressListener) {
		this.progressListener = progressListener;
	}

	@Override
	public void open() {
		try {
			client.prepareGet(track.url).execute(new HttpHandler());
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		manager.add(this);
	}

	@Override
	public void close() {
		if (delegate != null) {
			delegate.close();
			return;
		}
		closeThis();
	}

	private void closeThis() {
		try {
			file.close();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		manager.remove(this);
	}

	@Override
	public synchronized void closeIfCompleted() {
		if (delegate != null)
			close();
	}

	@Override
	public long getPosition() {
		try {
			return channel.position();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public long getLimit() {
		if (delegate != null)
			return delegate.getLimit();
		return limit;
	}

	private class HttpHandler implements AsyncHandler<Object> {
		@Override
		public void onThrowable(Throwable t) {
			closeThis();
			trackFile.delete();
		}

		@Override
		public STATE onBodyPartReceived(final HttpResponseBodyPart bodyPart) throws Exception {
			ByteBuffer buffer = bodyPart.getBodyByteBuffer();
			int bufferLength = buffer.limit() - buffer.position();
			while (buffer.limit() != buffer.position())
				channel.write(buffer, limit);
			limit += bufferLength;
			if (progressListener != null)
				progressListener.run();
			if (expectedSeekPosition != -1 && listener != null && expectedSeekPosition <= limit) {
				channel.position(expectedSeekPosition);
				expectedSeekPosition = -1;
				listener.run();
			}
			return STATE.CONTINUE;
		}

		@Override
		public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
			if (responseStatus.getStatusCode() != 200)
				return STATE.ABORT;
			return STATE.CONTINUE;
		}

		@Override
		public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
			long l = Long.valueOf(headers.getHeaders().get("Content-Length").get(0));
			synchronized (this) {
				length = l;
			}
			return STATE.CONTINUE;
		}

		@Override
		public Object onCompleted() throws Exception {
			delegate = factory.create(track);
			delegate.seek(channel.position());
			closeThis();
			return null;
		}
	}
}
