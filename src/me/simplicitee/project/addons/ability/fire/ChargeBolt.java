package me.simplicitee.project.addons.ability.fire;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LightningAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.simplicitee.project.addons.ProjectAddons;
import me.simplicitee.project.addons.Util;

public class ChargeBolt extends LightningAbility implements AddonAbility {
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double boltRange;
	@Attribute(Attribute.RADIUS)
	private double blastRadius;
	@Attribute(Attribute.SPEED)
	private int speed;
	@Attribute("DischargeBoltCount")
	private int dischargeBolts;
	
	private Set<Bolt> bolts;

	public ChargeBolt(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		chargeTime = ProjectAddons.instance.getConfig().getLong("Abilities.Fire.ChargeBolt.ChargeTime");
		damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.ChargeBolt.Damage");
		boltRange = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.ChargeBolt.BoltRange");
		blastRadius = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.ChargeBolt.BlastRadius");
		speed = ProjectAddons.instance.getConfig().getInt("Abilities.Fire.ChargeBolt.Speed");
		cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Fire.ChargeBolt.Cooldown");
		dischargeBolts = ProjectAddons.instance.getConfig().getInt("Abilities.Fire.ChargeBolt.DischargeBoltCount");
		bolts = new HashSet<>(dischargeBolts);
		
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (player.isSneaking() && System.currentTimeMillis() > getStartTime() + chargeTime) {
			Util.playLightningParticles(GeneralMethods.getMainHandLocation(player), 2, 0.1, 0.1, 0.1);
			if (Math.random() < 0.3) {
				playLightningbendingSound(player.getEyeLocation());
			}
		} else if (!player.isSneaking() && System.currentTimeMillis() > getStartTime() + chargeTime) {
			discharge();
		} else if (!player.isSneaking() && System.currentTimeMillis() < getStartTime() + chargeTime) {
			remove();
			return;
		} else if (System.currentTimeMillis() < getStartTime() + chargeTime) {
			return;
		}
		
		Set<Bolt> remove = new HashSet<>();
		
		loop: for (Bolt bolt : bolts) {
			for (int i = 0; i < speed; i++) {
				if (!bolt.advance()) {
					remove.add(bolt);
					continue loop;
				}
			}
		}
		
		bolts.removeAll(remove);
		
		if (bolts.isEmpty() && dischargeBolts < 1) {
			remove();
			return;
		}
	}
	
	private void discharge() {
		bPlayer.addCooldown(this);
		
		Location loc = player.getLocation().add(0, 1, 0);
		Random rand = new Random();
		
		for (int i = 0; i < dischargeBolts; i++) {
			float yaw = player.getEyeLocation().getYaw() + rand.nextInt(60) - 30;
			float pitch = player.getEyeLocation().getPitch() + rand.nextInt(46) - 23;
			
			loc.setYaw(yaw);
			loc.setPitch(pitch);
			
			bolts.add(new Bolt(loc, blastRadius, speed));
		}
		
		dischargeBolts = 0;
	}
	
	public void bolt() {
		if (dischargeBolts < 1) {
			return;
		} else if (System.currentTimeMillis() < getStartTime() + chargeTime) {
			return;
		}
		
		Location loc = GeneralMethods.getMainHandLocation(player);
		loc.setDirection(player.getEyeLocation().getDirection());
		
		bolts.add(new Bolt(loc, boltRange, speed));
		dischargeBolts--;
		
		if (dischargeBolts < 1) {
			bPlayer.addCooldown(this);
		} else {
			ActionBar.sendActionBar(ChatColor.DARK_RED + "" + dischargeBolts + " bolts remaining", player);
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
		return "ChargeBolt";
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
	public String getDescription() {
		return "Charge up your electricity and shoot bolts of lightning! They can be shot one at a time or discharged around you all at once.";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak to charge, click to shoot one at a time or release sneak to discharge.";
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Fire.ChargeBolt.Enabled");
	}
	
	private class Bolt {
		private Location loc, start;
		private double range;
		private Random rand;
		
		private Bolt(Location start, double range, double speed) {
			this.start = start.clone();
			this.loc = start.clone();
			this.loc.getDirection().normalize();
			this.range = range * range;
			this.rand = new Random();
		}
		
		private boolean advance() {
			loc.setYaw(start.getYaw() + (rand.nextInt(16) - 8));
			loc.setPitch(start.getPitch() + (rand.nextInt(16) - 8));
			loc.add(loc.getDirection());
			
			if (loc.distanceSquared(start) >= range) {
				return false;
			}
			
			if (!loc.getBlock().isPassable()) {
				return false;
			}
			
			Util.playLightningParticles(loc, 1, 0.1, 0.1, 0.1);
			if (Math.random() > 0.01) {
				playLightningbendingSound(loc);
			}
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 0.62)) {
				if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
					DamageHandler.damageEntity(e, damage, ChargeBolt.this);
					((LivingEntity) e).setNoDamageTicks(0);
					return false;
				}
			}
			
			return true;
		}
	}
}
