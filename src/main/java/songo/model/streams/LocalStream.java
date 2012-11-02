package songo.model.streams;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import songo.vk.Audio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LocalStream implements Stream {
	private final RandomAccessFile file;

	@Inject
	LocalStream(StreamUtil util, @Assisted Audio track) {
		try {
			file = new RandomAccessFile(util.getTrackFile(track), "r");
		} catch (FileNotFoundException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public int read(byte[] buffer) {
		try {
			return file.read(buffer);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public boolean seek(long position) {
		try {
			file.seek(position);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		return true;
	}

	@Override
	public long getLength() {
		try {
			return file.length();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setDoneSeekingListener(Runnable listener) {

	}

	@Override
	public void open() {

	}

	@Override
	public void close() {
		try {
			file.close();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void closeIfCompleted() {
		close();
	}

	@Override
	public long getPosition() {
		try {
			return file.getFilePointer();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public long getLimit() {
		return getLength();
	}
}
