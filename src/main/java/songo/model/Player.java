package songo.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import songo.annotation.GlobalBus;
import songo.vk.Audio;

@Singleton
public class Player {
	private final EventBus bus;
	private State state = State.STOPPED;
	private Provider<Audio> audio;
	private final Provider<Decoder> decoderProvider;
	private Decoder decoder;

	public State getState() {
		return state;
	}

	private void setState(State state) {
		this.state = state;
		bus.post(new StateChanged(state));
	}

	@Inject
	Player(@GlobalBus EventBus bus, Provider<Audio> audio, Provider<Decoder> decoderProvider) {
		this.bus = bus;
		this.audio = audio;
		this.decoderProvider = decoderProvider;
	}

	public void play() {
		stop();
		decoder = decoderProvider.get();
		decoder.open();
		bus.post(new UpdateDuration(audio.get().duration));
		setState(State.PLAYING);
	}

	public void pause() {
		decoder.pause();
		setState(State.PAUSED);
	}

	public void unpause() {
		decoder.unpause();
		setState(State.PLAYING);
	}

	public void stop() {
		if(decoder != null) {
			decoder.close();
			decoder = null;
		}
		bus.post(new UpdatePosition(0));
		setState(State.STOPPED);
	}

	public void seek(float position) {
		decoder.seek(position);
	}

	public static class UpdateDuration {
		public final int duration;

		private UpdateDuration(int duration) {
			this.duration = duration;
		}
	}

	public static class UpdatePosition {
		public final float position;

		public UpdatePosition(float position) {
			this.position = position;
		}
	}

	public static class UpdateDownloadProgress {
		public final int position;

		public UpdateDownloadProgress(int position) {
			this.position = position;
		}
	}

	public static class DonePlaying {
	}

	public enum State {
		STOPPED,
		PLAYING,
		PAUSED
	}

	public static class StateChanged {
		public final State state;

		private StateChanged(State state) {
			this.state = state;
		}
	}
}
