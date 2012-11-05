package songo.mpg123;

import com.google.inject.Inject;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteOrder;

public class Mpg123 {
	private final Mpg123Native nat;
	private final Pointer handle;
	private boolean sizeSet;
	private int lastError = 0;
	private boolean closed;

	public int getLastError() {
		return lastError;
	}

	public static final int
		NEW_FORMAT = -11, DONE = -12, NEED_MORE = -10,
		MPG123_ENC_8 = 0x00f, MPG123_ENC_16 = 0x040,
		MPG123_ENC_24 = 0x4000, MPG123_ENC_32 = 0x100,
		MPG123_ENC_SIGNED = 0x080, MPG123_ENC_FLOAT = 0xe00,
		MPG123_ENC_SIGNED_16 = (MPG123_ENC_16 | MPG123_ENC_SIGNED | 0x10), MPG123_ENC_UNSIGNED_16 = (MPG123_ENC_16 | 0x20),
		MPG123_ENC_UNSIGNED_8 = 0x01, MPG123_ENC_SIGNED_8 = (MPG123_ENC_SIGNED | 0x02),
		MPG123_ENC_ULAW_8 = 0x04, MPG123_ENC_ALAW_8 = 0x08,
		MPG123_ENC_SIGNED_32 = MPG123_ENC_32 | MPG123_ENC_SIGNED | 0x1000, MPG123_ENC_UNSIGNED_32 = MPG123_ENC_32 | 0x2000,
		MPG123_ENC_SIGNED_24 = MPG123_ENC_24 | MPG123_ENC_SIGNED | 0x1000, MPG123_ENC_UNSIGNED_24 = MPG123_ENC_24 | 0x2000,
		MPG123_ENC_FLOAT_32 = 0x200, MPG123_ENC_FLOAT_64 = 0x400,
		SEEK_SET = 0, SEEK_CUR = 0, SEEK_END = 0;

	private void checkStatus() throws Mpg123Exception {
		if(closed)
			throw new Mpg123Exception("Handle closed");
	}

	@Inject
	Mpg123(Mpg123Native nat) throws Mpg123Exception {
		this.nat = nat;
		synchronized (this) {
			checkStatus();
			handle = nat.mpg123_new(null, null);
		}
	}

	public void openFeed() throws Mpg123Exception {
		synchronized (this) {
			checkStatus();
			nat.mpg123_open_feed(handle);
		}
	}

	public int decode(byte[] in, int insize, byte[] out, int outsize) throws Mpg123Exception {
		IntByReference result = new IntByReference();
		int retval;
		synchronized (this) {
			checkStatus();
			retval = nat.mpg123_decode(handle, in, insize, out, outsize, result);
		}
		lastError = retval;
		return result.getValue();
	}

	public AudioFormat getFormat() throws Mpg123Exception {
		IntByReference rate = new IntByReference(),
			channels = new IntByReference(), encRef = new IntByReference();
		synchronized (this) {
			checkStatus();
			nat.mpg123_getformat(handle, rate, channels, encRef);
		}
		int enc = encRef.getValue();
		int sampleSizeInBits = -1;
		if ((enc & MPG123_ENC_8) != 0)
			sampleSizeInBits = 8;
		if ((enc & MPG123_ENC_16) != 0)
			sampleSizeInBits = 16;
		if ((enc & MPG123_ENC_24) != 0)
			sampleSizeInBits = 24;
		if ((enc & MPG123_ENC_32) != 0)
			sampleSizeInBits = 32;
		boolean signed = false;
		if ((enc & MPG123_ENC_SIGNED) != 0)
			signed = true;
		return new AudioFormat(rate.getValue(), sampleSizeInBits, channels.getValue(), signed, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
	}

	public synchronized void close() throws Mpg123Exception {
		checkStatus();
		closed = true;
		nat.mpg123_close(handle);
		nat.mpg123_delete(handle);
	}

	public void setFileSize(int size) throws Mpg123Exception {
		if (!sizeSet) {
			sizeSet = true;
			synchronized (this) {
				checkStatus();
				nat.mpg123_set_filesize(handle, size);
			}
		}
	}

	public synchronized int getLength() throws Mpg123Exception {
		checkStatus();
		return nat.mpg123_length(handle);
	}

	public synchronized int getSamplePosition() throws Mpg123Exception {
		checkStatus();
		return nat.mpg123_tell(handle);
	}

	public int seek(int sampleOffset) throws Mpg123Exception {
		IntByReference result = new IntByReference();
		synchronized (this) {
			checkStatus();
			nat.mpg123_feedseek(handle, sampleOffset, SEEK_SET, result);
		}
		return result.getValue();
	}

	public synchronized int streamOffset() throws Mpg123Exception {
		checkStatus();
		return nat.mpg123_tell_stream(handle);
	}
}
