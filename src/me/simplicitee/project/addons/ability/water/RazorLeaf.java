package me.simplicitee.project.addons.ability.water;

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
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.project.addons.ProjectAddons;
import me.simplicitee.project.addons.Util;

public class RazorLeaf extends PlantAbility implements AddonAbility {
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.RANGE)
	private double range;
	
	private Location center;
	private int particles, uses = 0, maxUses;
	private TempBlock source;
	private Vector direction;
	private boolean counted = true;

	public RazorLeaf(Player player, boolean sourced) {
		super(player);
		
		if (hasAbility(player, RazorLeaf.class)) {
			return;
		}
		
		if (sourced) {
			Block source = player.getTargetBlock(getTransparentMaterialSet(), 7);
			if (!isPlantbendable(source.getType())) {
				return;
			}
			
			this.source = new TempBlock(source, Material.AIR);
			this.center = source.getLocation().add(0.5, 0.5, 0.5);
		} else {
			this.source = null;
			this.center = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.5));
		}
		
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Water.RazorLeaf.Cooldown");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.RazorLeaf.Damage");
		this.radius = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.RazorLeaf.Radius");
		this.range = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.RazorLeaf.Range");
		this.particles = ProjectAddons.instance.getConfig().getInt("Abilities.Water.RazorLeaf.Particles");
		this.maxUses = ProjectAddons.instance.getConfig().getInt("Abilities.Water.RazorLeaf.MaxRecalls");
		
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
		
		if (player.isSneaking() && uses < maxUses) {
			counted = true;
			direction = GeneralMethods.getDirection(center, player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(radius + 0.5)));
		} else {
			if (counted) {
				counted = false;
				++uses;
			}
			
			Location target = null;
			Entity e = GeneralMethods.getTargetedEntity(player, range);
			
			if (e == null || !(e instanceof LivingEntity)) {
				target = GeneralMethods.getTargetedLocation(player, range).add(player.getEyeLocation().getDirection());
			} else {
				target = e.getLocation().add(0, 1, 0);
			}
			
			direction = GeneralMethods.getDirection(center, target);
		}
		
		if (direction.length() > 1) {
			center = center.add(direction.normalize().multiply(0.75));
		} else {
			center = center.add(direction);
		}
		
		if (!center.getBlock().isPassable()) {
			remove();
			return;
		}
		
		if (Math.random() < 0.13) {
			playPlantbendingSound(center);
		}
		
		for (int n = 0; n < particles; ++n) {
			double phi = n * 137.5;
			double r = 0.075 * Math.sqrt(n);
			
			if (r > radius) {
				break;
			}
			
			GeneralMethods.displayColoredParticle(Util.LEAF_COLOR, center.clone().add(r * Math.cos(Math.toRadians(phi)), 0, r * Math.sin(Math.toRadians(phi))));
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
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Water.RazorLeaf.Enabled");
	}
}
