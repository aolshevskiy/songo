package songo.model.streams;

import songo.vk.Audio;

public interface StreamFactory {
	public LocalStream createLocal(Audio track);

	public RemoteStream createRemote(Audio track);
}
