package songo.vk;

import com.google.common.base.Objects;

public class Audio {
	public final String artist;
	public final String title;
	public final String url;
	public final int duration;

	Audio(String artist, String title, String url, int duration) {
		this.artist = artist;
		this.title = title;
		this.url = url;
		this.duration = duration;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(artist, title);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(this == obj)
			return true;
		if(!(obj instanceof Audio))
			return false;
		Audio a = (Audio) obj;
		return artist.equals(a.artist) && title.equals(a.title);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("artist", artist)
			.add("title", title)
			.add("url", url)
			.toString();
	}
}
