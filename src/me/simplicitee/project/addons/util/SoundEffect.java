package me.simplicitee.project.addons.util;

import org.bukkit.Location;
import org.bukkit.Sound;

public class SoundEffect {

	private Sound sound;
	private float volume, pitch;
	private int counter = -1, waitTime;
	
	/**
	 * Create a {@link SoundEffect} using the given sound with defaults
	 * <code>volume = 0.4, pitch = 1, and waitTime = 20</code>
	 * @param sound What {@link Sound} to use
	 */
	public SoundEffect(Sound sound) {
		this(sound, 0.4f, 1f, 20);
	}
	
	/**
	 * Create a {@link SoundEffect} using the given sound, volume, and pitch
	 * with default <code>waitTime = 20</code>
	 * @param sound What {@link Sound} to use
	 * @param volume How loud to play the sound
	 * @param pitch The sound's pitch
	 */
	public SoundEffect(Sound sound, float volume, float pitch) {
		this(sound, volume, pitch, 20);
	}
	
	/**
	 * Create a {@link SoundEffect} using the given sound, volume, pitch, and tickRate
	 * @param sound What {@link Sound} to use
	 * @param volume How loud to play the sound
	 * @param pitch The sound's pitch
	 * @param waitTime how many ticks to wait before the sound can be played using {@link SoundEffect#play(Location)}
	 */
	public SoundEffect(Sound sound, float volume, float pitch, int waitTime) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.waitTime = waitTime;
	}
	
	public Sound getSound() {
		return sound;
	}
	
	public float getVolume() {
		return volume;
	}
	
	public float getPitch() {
		return pitch;
	}
	
	public void setVolume(float volume) {
		this.volume = volume;
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	
	public void play(Location loc) {
		if (++counter % waitTime == 0) {
			loc.getWorld().playSound(loc, sound, volume, pitch);
		}
	}
}
