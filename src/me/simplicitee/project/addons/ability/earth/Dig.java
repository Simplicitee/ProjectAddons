package me.simplicitee.project.addons.ability.earth;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.project.addons.ProjectAddons;

public class Dig extends EarthAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("RevertTime")
	private long revertTime;
	@Attribute(Attribute.SPEED)
	private double speed;
	
	public Dig(Player player) {
		super(player);
		
		if (!isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			return;
		}
		
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.Dig.Cooldown");
		this.duration = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.Dig.Duration");
		this.revertTime = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.Dig.RevertTime");
		this.speed = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.Dig.Speed");
		
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead() || !player.isSneaking()) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		Block b = player.getTargetBlock(getTransparentMaterialSet(), 4);
		if (!isEarthbendable(b)) {
			player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(0.9));
			remove();
			return;
		}
		
		for (Block block : GeneralMethods.getBlocksAroundPoint(player.getEyeLocation(), 2.4)) {
			if (isEarthbendable(block)) {
				ParticleEffect.BLOCK_CRACK.display(block.getLocation().add(0.5, 0.5, 0.5), 5, 0.25, 0.25, 0.25, block.getBlockData());
				new TempBlock(block, Material.AIR).setRevertTime(revertTime);
			}
		}
		
		ParticleEffect.CRIT.display(player.getEyeLocation(), 7, 0.6, 0.6, 0.6);
		
		player.setGliding(true);
		player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(speed));
		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 5, 1));
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
		return "Dig";
	}

	@Override
	public Location getLocation() {
		return player.getEyeLocation();
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
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Earth.Dig.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Swim through the earth, digging a path with your earthbending. Inspired by toph's learning from the badgermoles! You must also be looking at an earthbendable block for the ability to work!";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak while on an earthbendable block";
	}
}
