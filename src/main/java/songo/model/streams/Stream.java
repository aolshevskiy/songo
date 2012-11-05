package songo.model.streams;

public interface Stream {
	int read(byte[] buffer);

	boolean seek(long position);

	long getLength();

	void setDoneSeekingListener(Runnable listener);

	void close();

	void closeIfCompleted();

	long getPosition();

	long getLimit();
}
