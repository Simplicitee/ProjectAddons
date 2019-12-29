package me.simplicitee.project.addons.ability.fire;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LightningAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.simplicitee.project.addons.ProjectAddons;

public class ArcSpark extends LightningAbility implements AddonAbility {
	
	private int speed, length;
	private double damage;
	private long duration, cooldown, charge, chargedTill;
	private boolean shoot, charged;
	private List<String> attractive;

	public ArcSpark(Player player) {
		super(player);
		
		this.speed = ProjectAddons.instance.getConfig().getInt("Abilities.ArcSpark.Speed");
		this.length = ProjectAddons.instance.getConfig().getInt("Abilities.ArcSpark.Length");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.ArcSpark.Damage");
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.ArcSpark.Cooldown");
		this.duration = ProjectAddons.instance.getConfig().getLong("Abilities.ArcSpark.Duration");
		this.charge = ProjectAddons.instance.getConfig().getLong("Abilities.ArcSpark.ChargeTime");
		this.attractive = ProjectAddons.instance.getConfig().getStringList("Properties.MetallicBlocks");
		this.charged = false;
		this.shoot = false;
		this.chargedTill = System.currentTimeMillis();
		
		start();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline() || !player.isSneaking() || !bPlayer.getBoundAbilityName().equalsIgnoreCase("ArcSpark")) {
			remove();
			return;
		}
		
		if (!charged) {
			long checkTime = getStartTime() + charge - System.currentTimeMillis();
			if (checkTime <= 0) {
				charged = true;
			} else {
				ProjectAddons.instance.getMethods().playLightningParticles(player.getLocation().add(0, 1, 0), 2, 0.36, 0.21, 0.36);
			}
			
			chargedTill = System.currentTimeMillis();
			player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.05f, 0.5f);
		} else if (charged && !shoot) {
			ProjectAddons.instance.getMethods().playLightningParticles(GeneralMethods.getMainHandLocation(player), 1, 0.001, 0.001, 0.001);
			chargedTill = System.currentTimeMillis();
			player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.05f, 0.5f);
		} else if (charged && shoot) {
			if (chargedTill + duration < System.currentTimeMillis()) {
				remove();
				return;
			}
			
			Location hand = GeneralMethods.getMainHandLocation(player);
			hand.setDirection(player.getEyeLocation().getDirection());
			Arc arc = new Arc(hand);	
			
			for (int i = 0; i < speed*length; i++) {
				arc.run(this);
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		if (charged && shoot) {
			bPlayer.addCooldown(this);
		}
	}
	
	public void shoot() {
		if (charged) {
			shoot = true;
		}
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
		return "ArcSpark";
	}

	@Override
	public Location getLocation() {
		return null;
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
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.ArcSpark.Enabled");
	}

	class Arc {
		
		private boolean progressing = true;
		private Location loc;
		
		public Arc(Location loc) {
			this.loc = loc;
		}
		
		public void run(ArcSpark a) {
			if (!progressing) {
				return;
			}
			
			double shortest = Double.MAX_VALUE;
			Entity closest = null;
			Location to = null;
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 3)) {
				if (!(e instanceof LivingEntity) || e.getEntityId() == player.getEntityId()) {
					continue;
				}
				
				double dist = loc.distance(e.getLocation().clone().add(0, 1, 0));
				
				if (dist <= 1) {
					DamageHandler.damageEntity(e, damage, a);
					progressing = false;
					return;
				} else {
					if (dist < shortest || closest == null) {
						shortest = dist;
						closest = e;
						to = e.getLocation().clone().add(0, 1, 0);
					}
				}
			}
			
			if (closest == null) {
				for (Block b : GeneralMethods.getBlocksAroundPoint(loc, 2)) {
					if (b.isPassable() || !attractive.contains(b.getType().toString())) {
						continue;
					}
					
					Location center = b.getLocation().add(0.5, 0.5, 0.5);
					double dist = loc.distance(center);
					
					if (dist < shortest) {
						shortest = dist;
						to = center;
					}
				}
			}
			
			Vector movement = null;
			
			if (to != null) {
				movement = GeneralMethods.getDirection(loc, to);
			} else {
				movement = new Vector(Math.random() / 5 - 0.1, Math.random() / 5 - 0.1, Math.random() / 5 - 0.1);
			}
			
			double angle = movement.angle(loc.getDirection());
			if (angle < 60 && angle > -60) {
				loc.setDirection(loc.getDirection().add(movement));
			}
			
			loc.getDirection().normalize();
			loc.add(loc.getDirection().multiply(0.3));

			if (loc.getBlock().getType() == Material.WATER || attractive.contains(loc.getBlock().getType().toString())) {
				if (Math.random() > 0.55) {
					new Electrify(player, loc.getBlock(), false);
				}
				progressing = false;
				return;
			} else if (!loc.getBlock().isPassable()) {
				progressing = false;
				return;
			}
			
			ProjectAddons.instance.getMethods().playLightningParticles(loc, 1, 0, 0, 0);
			if (Math.random() < 0.15) {
				playLightningbendingSound(loc);
			}
		}
	}
	
	
	
	@Override
	public String getInstructions() {
		return "Hold sneak to charge up and click when charged to shoot, but keep holding sneak!";
	}
	
	@Override
	public String getDescription() {
		return "Shoots many arcs of electricity in the direction you are looking, and the arcs are attracted to some blocks and entities! Hitting a metallic block or water will cause it to become electrified!";
	}
}
