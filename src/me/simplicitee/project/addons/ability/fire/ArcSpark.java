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
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.simplicitee.project.addons.ProjectAddons;
import me.simplicitee.project.addons.Util;
import me.simplicitee.project.addons.util.SoundEffect;

public class ArcSpark extends LightningAbility implements AddonAbility {
	
	@Attribute(Attribute.SPEED)
	private int speed;
	@Attribute("Length")
	private int length;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	
	private long charge, chargedTill;
	private boolean shoot, charged, left;
	private List<String> attractive;
	
	private SoundEffect charging;

	public ArcSpark(Player player) {
		super(player);
		
		this.speed = ProjectAddons.instance.getConfig().getInt("Abilities.Fire.ArcSpark.Speed");
		this.length = ProjectAddons.instance.getConfig().getInt("Abilities.Fire.ArcSpark.Length");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.ArcSpark.Damage");
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Fire.ArcSpark.Cooldown");
		this.duration = ProjectAddons.instance.getConfig().getLong("Abilities.Fire.ArcSpark.Duration");
		this.charge = ProjectAddons.instance.getConfig().getLong("Abilities.Fire.ArcSpark.ChargeTime");
		this.attractive = ProjectAddons.instance.getConfig().getStringList("Properties.MetallicBlocks");
		this.charged = false;
		this.shoot = false;
		this.chargedTill = System.currentTimeMillis();
		
		this.charging = new SoundEffect(Sound.ENTITY_CREEPER_PRIMED, 0.3f, 0.6f, 30);
		
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
				Util.playLightningParticles(player.getLocation().add(0, 1, 0), 2, 0.36, 0.21, 0.36);
			}
			
			chargedTill = System.currentTimeMillis();
			charging.play(player.getEyeLocation());
		} else if (charged && !shoot) {
			Util.playLightningParticles(player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.3)), 1, 0.001, 0.001, 0.001);
			chargedTill = System.currentTimeMillis();
			charging.play(player.getEyeLocation());
		} else if (charged && shoot) {
			if (chargedTill + duration < System.currentTimeMillis()) {
				remove();
				return;
			}
			
			Location hand = left ? GeneralMethods.getLeftSide(player.getLocation(), 0.55).add(0, 1.2, 0) : GeneralMethods.getRightSide(player.getLocation(), 0.55).add(0, 1.2, 0);
			left = !left;
			hand.setDirection(player.getEyeLocation().getDirection());
			
			boolean persist = true;
			for (int i = 0; i < speed*length && persist; ++i) {
				persist = arc(hand);
			}
		}
	}
	
	private boolean arc(Location loc) {
		double shortest = Double.MAX_VALUE;
		Entity closest = null;
		Location to = null;
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 3)) {
			if (!(e instanceof LivingEntity) || e.getEntityId() == player.getEntityId()) {
				continue;
			}
			
			double dist = loc.distance(e.getLocation().clone().add(0, 1, 0));
			
			if (dist <= 1) {
				DamageHandler.damageEntity(e, damage, this);
				return false;
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
			return false;
		} else if (!loc.getBlock().isPassable()) {
			return false;
		}
		
		Util.playLightningParticles(loc, 1, 0, 0, 0);
		if (Math.random() < 0.01) {
			playLightningbendingSound(loc);
		}
		
		return true;
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
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Fire.ArcSpark.Enabled");
	}
	
	@Override
	public String getInstructions() {
		return "Hold sneak to charge up and click when charged to shoot, but keep holding sneak!";
	}
	
	@Override
	public String getDescription() {
		return "Shoots arcs of electricity in the direction you are looking, and the arcs are attracted to some blocks and entities! Hitting a metallic block or water will cause it to become electrified!";
	}
}
