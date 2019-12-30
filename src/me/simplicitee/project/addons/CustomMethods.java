package me.simplicitee.project.addons;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.util.HexColor;

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
            int d = (int) Math.floor(player.getEyeLocation().distance(loc) * 2);
            int i = r.nextInt(blueFire.length);
            
            String hexVal = blueFire[d < i ? d : i];
            HexColor hex = new HexColor(hexVal);
            Color color = Color.fromRGB(hex.toRGB()[0], hex.toRGB()[1], hex.toRGB()[2]);
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, amount, xOff, yOff, zOff, 0.02, new DustOptions(color, (float) ( 1 + (i / blueFire.length))));
        } else {
        	ParticleEffect.FLAME.display(loc, amount, xOff, yOff, zOff, 0.02);
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
