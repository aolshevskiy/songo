package songo.model.streams;

import songo.vk.Audio;

public interface StreamFactory<T extends Stream> {
	public T create(Audio track);
}
