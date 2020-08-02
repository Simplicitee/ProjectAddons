package me.simplicitee.project.addons.ability.earth;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.project.addons.ProjectAddons;

public class Accretion extends EarthAbility implements AddonAbility {
	
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("Blocks")
	private int blocks;
	@Attribute(Attribute.SELECT_RANGE)
	private int selectRange;
	@Attribute("RevertTime")
	private long revertTime;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SPEED)
	private double throwSpeed;
	
	private Set<FallingBlock> tracker;
	private Set<TempBlock> temps;
	private boolean shot;

	public Accretion(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (!isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			return;
		}
		
		if (hasAbility(player, Accretion.class)) {
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			return;
		}
		
		this.shot = false;
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.Accretion.Damage");
		this.blocks = ProjectAddons.instance.getConfig().getInt("Abilities.Earth.Accretion.Blocks");
		this.selectRange = ProjectAddons.instance.getConfig().getInt("Abilities.Earth.Accretion.SelectRange");
		this.revertTime = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.Accretion.RevertTime");
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.Accretion.Cooldown");
		this.throwSpeed = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.Accretion.ThrowSpeed");
		
		this.tracker = new HashSet<>();
		this.temps = new HashSet<>();
		
		List<Location> list = GeneralMethods.getCircle(player.getLocation(), selectRange, 1, false, false, 0);
		
		for (int i = 0; i < list.size(); i++) {
			Block b = GeneralMethods.getTopBlock(list.get(new Random().nextInt(list.size())), 2);
			
			if (!isAir(b.getRelative(BlockFace.UP).getType())) {
				continue;
			}
			
			if (TempBlock.isTempBlock(b)) {
				continue;
			}
			
			if (!isEarthbendable(b.getType(), true, true, false)) {
				continue;
			}
			
			Material type = b.getType();
			temps.add(new TempBlock(b, Material.AIR));
			FallingBlock fb = GeneralMethods.spawnFallingBlock(b.getLocation().add(0.5, 0.5, 0.5), type);
			
			fb.setVelocity(new Vector(0, 0.8, 0));
			fb.setMetadata("accretion", new FixedMetadataValue(ProjectAddons.instance, this));
			fb.setHurtEntities(false);
			fb.setDropItem(false);
			
			tracker.add(fb);
			
			if (temps.size() == blocks) {
				break;
			}
		}
		
		playEarthbendingSound(player.getLocation());
		
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		Iterator<FallingBlock> iter = tracker.iterator();
		loop: while (iter.hasNext()) {
			FallingBlock fb = iter.next();
			ParticleEffect.BLOCK_CRACK.display(fb.getLocation(), 1, 0.1, 0.1, 0.1, fb.getBlockData());
			
			if (shot) {
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 1)) {
					if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
						entityCollision(fb, (LivingEntity) e);
						iter.remove();
						continue loop;
					}
				}
			}
		}
		
		if (tracker.isEmpty()) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() - getStartTime() >= 6000) {
			for (FallingBlock fb : tracker) {
				fb.remove();
			}
			remove();
			return;
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		tracker.clear();
		
		for (TempBlock tb : temps) {
			if (tb.getBlockData().getMaterial() == Material.AIR) {
				tb.revertBlock();
			}
		}
		
		temps.clear();
	}
	
	public void entityCollision(FallingBlock fb, LivingEntity entity) {
		int duration = 20;
		int amp = 1;
		if (entity.hasPotionEffect(PotionEffectType.SLOW)) {
			PotionEffect effect = entity.getPotionEffect(PotionEffectType.SLOW);
			
			duration += effect.getDuration();
			amp += effect.getAmplifier();
			
			entity.removePotionEffect(PotionEffectType.SLOW);
		}
		
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, amp, true, false));
		DamageHandler.damageEntity(entity, damage, this);
		fb.remove();
	}
	
	public void blockCollision(FallingBlock fb, Block block) {
		TempBlock tb;
		if (TempBlock.isTempBlock(block)) {
			tb = TempBlock.get(block);
			tb.setType(fb.getBlockData());
		} else {
			tb = new TempBlock(block, fb.getBlockData().getMaterial());
		}
		
		tb.setRevertTime(revertTime);
		
		temps.add(tb);
		tracker.remove(fb);
		fb.remove();
	}
	
	public void shoot() {
		if (shot) {
			return;
		}
		
		if (tracker.isEmpty()) {
			remove();
			return;
		}
		
		
		
		for (FallingBlock fb : tracker) {
			Location target = null;
			Entity e = GeneralMethods.getTargetedEntity(player, 30);
			
			if (e != null) {
				target = e.getLocation();
			} else {
				target = GeneralMethods.getTargetedLocation(player, 30);
			}
			
			fb.setVelocity(GeneralMethods.getDirection(fb.getLocation(), target).add(new Vector(0, 0.185, 0)).normalize().multiply(throwSpeed));
			playEarthbendingSound(fb.getLocation());
		}
		
		bPlayer.addCooldown(this);
		shot = true;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
		return "Accretion";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public void load() {
	}

	@Override
	public void stop() {
	}

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
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Earth.Accretion.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Slam the earth to send blocks into the air, then shoot them all towards a single point! They will build up on an enemy, damaging and slowing them down! Each block that hits adds 1 second and level of slowness.";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak to rise blocks, Left Click before they land to shoot!";
	}
}
