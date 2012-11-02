package songo.mpg123;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface Mpg123Native extends Library {
	int mpg123_init();

	void mpg123_exit();

	Pointer mpg123_new(String decoder, IntByReference err);

	int mpg123_close(Pointer handle);

	int mpg123_delete(Pointer handle);

	int mpg123_open_feed(Pointer handle);

	int mpg123_decode(Pointer handle, byte[] inmem, int inmemsize, byte[] outmem, int outmemsize, IntByReference done);

	int mpg123_getformat(Pointer handle, IntByReference rate, IntByReference channels, IntByReference encoding);

	int mpg123_set_filesize(Pointer handle, int size);

	int mpg123_length(Pointer handle);

	int mpg123_tell(Pointer handle);

	int mpg123_feedseek(Pointer handle, int sampleoff, int whence, IntByReference input_offset);

	int mpg123_tell_stream(Pointer handle);
}
