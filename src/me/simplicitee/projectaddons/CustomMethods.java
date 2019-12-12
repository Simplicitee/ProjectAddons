package me.simplicitee.projectaddons;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class CustomMethods {

	private ProjectAddons plugin;
	private String[] blueFire = {"e6ffff", "33e6ff", "03d2d2", "33b8ff", "3371ff", "3349ff", "300cfe", "300cfe", "300cfe"};
	private String[] lightning = {"e6efef", "03d2d2", "33e6ff", "03d2d2", "03d2d2", "33e6ff", "03d2d2", "33e6ff", "33e6ff"};
	
	public CustomMethods(ProjectAddons plugin) {
		this.plugin = plugin;
	}
	
	public ProjectAddons getPlugin() {
		return plugin;
	}
	
	public void playDynamicFireParticles(Player player, Location loc, int amount, double xOff, double yOff, double zOff) {
        Random r = new Random();
		if (player.hasPermission("bending.fire.bluefire")) {
            int d = (int) Math.floor(player.getEyeLocation().distance(loc));
            int i = r.nextInt(blueFire.length);
            
            String hexVal = blueFire[d < i ? d : i];
            GeneralMethods.displayColoredParticle(hexVal, loc, amount, xOff, yOff, zOff);
        } else {
        	ParticleEffect.FLAME.display(loc, amount, xOff, yOff, zOff);
        }
		
		if (r.nextInt(100) < 20) {
			ParticleEffect.SMOKE_NORMAL.display(loc, (int) 1, xOff, yOff, zOff);
		}
	}
	
	public void playLightningParticles(Location loc, int amount, double xOff, double yOff, double zOff) {
		int i = (int) Math.floor(Math.random() * (lightning.length - 1));
		GeneralMethods.displayColoredParticle(lightning[i], loc, amount, xOff, yOff, zOff);
	}
}
