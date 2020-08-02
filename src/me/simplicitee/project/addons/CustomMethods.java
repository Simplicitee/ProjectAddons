package me.simplicitee.project.addons;

import org.bukkit.Location;

import com.projectkorra.projectkorra.GeneralMethods;

public class CustomMethods {

	private ProjectAddons plugin;
	private String[] lightning = {"e6efef", "03d2d2", "33e6ff", "03d2d2", "03d2d2", "33e6ff", "03d2d2", "33e6ff", "33e6ff"};
	
	public CustomMethods(ProjectAddons plugin) {
		this.plugin = plugin;
	}
	
	public ProjectAddons getPlugin() {
		return plugin;
	}
	
	public void playLightningParticles(Location loc, int amount, double xOff, double yOff, double zOff) {
		int i = (int) Math.floor(Math.random() * (lightning.length - 1));
		GeneralMethods.displayColoredParticle(lightning[i], loc, amount, xOff, yOff, zOff);
	}
	
	public double clamp(double min, double max, double value) {
		if (min > value) {
			return min;
		} else if (max < value) {
			return max;
		} else {
			return value;
		}
	}
}
