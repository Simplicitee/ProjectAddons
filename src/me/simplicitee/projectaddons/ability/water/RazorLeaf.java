package me.simplicitee.projectaddons.ability.water;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.PlantAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.projectaddons.ProjectAddons;

public class RazorLeaf extends PlantAbility implements AddonAbility {
	
	private Location center;
	private long cooldown;
	private double damage;
	private double radius;
	private double range;
	private TempBlock source;
	private Vector direction;

	public RazorLeaf(Player player) {
		super(player);
		
		if (hasAbility(player, RazorLeaf.class)) {
			return;
		}
		
		Block source = player.getTargetBlock(getTransparentMaterialSet(), 7);
		if (!isPlantbendable(source.getType())) {
			return;
		}
		
		this.source = new TempBlock(source, Material.AIR);
		this.center = source.getLocation().clone().add(0.5, 0.5, 0.5);
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.RazorLeaf.Cooldown");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.RazorLeaf.Damage");
		this.radius = ProjectAddons.instance.getConfig().getDouble("Abilities.RazorLeaf.Radius");
		this.range = ProjectAddons.instance.getConfig().getDouble("Abilities.RazorLeaf.Range");
		
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (!player.getWorld().equals(center.getWorld())) {
			remove();
			return;
		}
		
		if (center.distance(player.getEyeLocation()) >= range) {
			remove();
			return;
		}
		
		if (player.isSneaking()) {
			Location holding = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().clone().normalize().multiply(1.5));
			direction = GeneralMethods.getDirection(center, holding);
		} else {
			Location target = null;
			Entity e = GeneralMethods.getTargetedEntity(player, range);
			
			if (e == null || !(e instanceof LivingEntity)) {
				target = GeneralMethods.getTargetedLocation(player, range);
			} else {
				target = e.getLocation().clone().add(0, 1, 0);
			}
			
			direction = GeneralMethods.getDirection(center, target);
		}
		
		if (direction.length() > 1) {
			center = center.add(direction.normalize().multiply(0.75));
		} else {
			center = center.add(direction);
		}
		
		if (!isAir(center.getBlock().getType()) && !isWater(center.getBlock().getType())) {
			remove();
			return;
		}
		
		playPlantbendingSound(center);
		
		for (int n = 0; n < 200; n++) {
			Location current, start = center.clone();
			double c = 0.075;
			double phi = n * 137.5;
			double r = c * Math.sqrt(n);
			double x = r * Math.cos(Math.toRadians(phi));
			double z = r * Math.sin(Math.toRadians(phi));
			current = start.clone().add(x, 0, z);
			
			if (current.distance(start) > radius) {
				break;
			}
			
			GeneralMethods.displayColoredParticle("3D9970", current);
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(center, radius + 1)) {
			if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
				DamageHandler.damageEntity(e, damage, this);
				remove();
				return;
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		
		if (source != null) {
			if (source.getBlock().getType() == Material.AIR) {
				source.revertBlock();
			}
		}
		
		bPlayer.addCooldown(this);
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "RazorLeaf";
	}

	@Override
	public Location getLocation() {
		return center;
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
	public String getDescription() {
		return "Spin leaves around really fast and make them razor sharp!";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak at plants to begin, hold to aim, and release to shoot it! Sneaking again will pull it back towards you!";
	}

	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.RazorLeaf.Enabled");
	}
}
