package me.simplicitee.projectaddons.ability.air;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.projectaddons.ProjectAddons;

public class Zephyr extends AirAbility implements AddonAbility {

	private double radius;
	private long cooldown;
	private int angle;
	
	public Zephyr(Player player) {
		super(player);
		
		this.angle = new Random().nextInt(360);
		this.radius = ProjectAddons.instance.getConfig().getDouble("Abilities.Zephyr.Radius");
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Zephyr.Cooldown");
		
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead() || !player.isSneaking() || player.getLocation().getBlock().isLiquid()) {
			remove();
			return;
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), radius)) {
			if (e instanceof LivingEntity && ((LivingEntity) e).getEyeLocation().getY() >= player.getLocation().getY()) {
				((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 2), true);
				ParticleEffect.CLOUD.display(e.getLocation(), 2, 0.3, 0.15, 0.3);
			}
		}
		
		Location anim = player.getLocation().clone();
		
		for (int i = 0; i < 6; i++) {
			Vector ortho = GeneralMethods.getOrthogonalVector(new Vector(0, 1, 0), 60 * i + angle, radius + 0.5);
			anim.add(ortho);
			
			playAirbendingParticles(anim, 1, 0, 0, 0);
			
			anim.subtract(ortho);
		}
		
		angle += 4;
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "Zephyr";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public void load() {}

	@Override
	public void stop() {}

	@Override
	public String getAuthor() {
		return "Simplicitee";
	}

	@Override
	public String getVersion() {
		return ProjectAddons.instance.version();
	}

	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Zephyr.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Use many gental air currents to slow the fall of entities around you, including yourself!";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak";
	}
}
