package me.simplicitee.project.addons.ability.avatar;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.ProjectAddons;

public class EnergyBeam extends AvatarAbility implements AddonAbility{
	
	public static Map<UUID, EnergyColor> colors = new HashMap<>();
	
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private int range;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("EasterEgg")
	private boolean effects;
	
	private Map<Location, Vector> map;
	private Map<Location, Integer> ranges;
	private EnergyColor color;
	
	public static enum EnergyColor {
		WHITE("ffffff"), 
		BLACK("000000"), 
		RED("ff5252"), 
		BLUE("0111ff"), 
		GREEN("229965"), 
		PURPLE("c606ff"), 
		YELLOW("c6c600"), 
		ORANGE("ffa500"),
		INDIGO("0a0082"),
		BROWN("d2691e"),
		PINK("e77aea"),
		AQUA("00FFFF"),
		GRAY("647687"),
		RAINBOW("abcdef");
		
		private String hex;
		private EnergyColor(String hex) {
			this.hex = hex;
		}
		
		public String getHex() {
			return hex;
		}
	}

	public EnergyBeam(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		setFields();
		start();
	}
	
	public void setFields() {
		map = new ConcurrentHashMap<>();
		ranges = new HashMap<>();
		damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Avatar.EnergyBeam.Damage");
		range = ProjectAddons.instance.getConfig().getInt("Abilities.Avatar.EnergyBeam.Range");
		cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Avatar.EnergyBeam.Cooldown");
		duration = ProjectAddons.instance.getConfig().getLong("Abilities.Avatar.EnergyBeam.Duration");
		effects = ProjectAddons.instance.getConfig().getBoolean("Abilities.Avatar.EnergyBeam.EasterEgg");
		color = colors.containsKey(player.getUniqueId()) ? colors.get(player.getUniqueId()) : EnergyColor.BLUE;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getEyeLocation().clone();
	}

	@Override
	public String getName() {
		return "EnergyBeam";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		if (getStartTime() + duration < System.currentTimeMillis()) {
			remove();
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		Vector direction = player.getLocation().getDirection().clone();
		Location center = player.getLocation().clone().add(0, 1, 0);
		map.put(center, direction);
		ranges.put(center, 0);
		
		for (Location loc : map.keySet()) {
			collide(loc);
			Vector next = map.get(loc);
			
			if (ranges.get(loc) == null) {
				continue;
			}
			
			int range = ranges.get(loc);
			
			if (ranges.get(loc) > this.range) {
				ranges.remove(loc);
				map.remove(loc);
				continue;
			}
			
			if ((new Random()).nextInt(2) == 0) {
				displayParticles(loc);
				
				if ((new Random()).nextInt(4) == 0) {
					player.getWorld().playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 0.2f, 0.7f);
				}
			}
			
			if (damageEntities(loc)) {
				ranges.remove(loc);
				map.remove(loc);
				continue;
			}
			
			Location nextLoc = loc.add(next);
			
			if (GeneralMethods.isSolid(nextLoc.getBlock()) || GeneralMethods.isRegionProtectedFromBuild(player, nextLoc)) {
				ParticleEffect.EXPLOSION_LARGE.display(nextLoc, 0, 0, 0, 0.01f, 1);
				map.remove(loc);
				ranges.remove(loc);
			} else {
				map.remove(loc);
				map.put(nextLoc, next);
				ranges.remove(loc);
				ranges.put(nextLoc, range + 1);
			}
		}
	}
	
	public void collide(Location loc) {
		for (EnergyBeam abil : getAbilities(EnergyBeam.class)) {
			if (abil == this) {
				continue;
			}
			for (Location check : abil.map.keySet()) {
				if (loc.distance(check) < 1.5) {
					remove();
					abil.remove();
					Location center = new Location(loc.getWorld(), (loc.getX()+check.getX())/2, (loc.getY()+check.getY())/2, (loc.getZ()+check.getZ())/2);
					createExplosion(center);
					return;
				}
			}
		}
	}
	
	private void createExplosion(Location center) {
		ParticleEffect.EXPLOSION_HUGE.display(center, 3, 4, 4, 4, 0.012);
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(center, 4)) {
			if (e instanceof LivingEntity) {
				Vector direction = GeneralMethods.getDirection(center, e.getLocation().clone().add(0, 0.75, 0));
				ParticleEffect.SWEEP_ATTACK.display(e.getLocation(), 1);
				e.setVelocity(direction.clone().multiply(3.5));
				DamageHandler.damageEntity(e, damage, this);
			}
		}
	}
	
	public void displayParticles(Location loc) {
		EnergyColor use = color;
		if (color == EnergyColor.RAINBOW) {
			int r = new Random().nextInt(EnergyColor.values().length - 1);
			use = EnergyColor.values()[r];
		}
		

		GeneralMethods.displayColoredParticle(use.getHex(), loc, 4, 0.3, 0.3, 0.3);
	}
	
	public boolean damageEntities(Location loc) {
		boolean damaged = false;
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
			if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
				LivingEntity le = (LivingEntity) e;
				if (effects) {
					if (color == EnergyColor.BLUE) { //Normal
						DamageHandler.damageEntity(e, player, damage, this);
						damaged = true;
					} else if (color == EnergyColor.GREEN) { //Healing
						if (!le.hasPotionEffect(PotionEffectType.HEAL)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 5, 1));
						}
					} else if (color == EnergyColor.ORANGE) { //Burning
						DamageHandler.damageEntity(e, player, damage, this);
						le.setFireTicks(90);
						damaged = true;
					} else if (color == EnergyColor.BLACK) { //Vampirism
						DamageHandler.damageEntity(e, player, damage, this);
						if (!player.hasPotionEffect(PotionEffectType.HEAL)) {
							player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 40, Math.round((float)damage)-1));
						}
						damaged = true;
					} else if (color == EnergyColor.RED) { //Stronger
						DamageHandler.damageEntity(e, player, damage, this);
						le.setNoDamageTicks(0);
						damaged = true;
					} else if (color == EnergyColor.PURPLE) { //Witchcraft
						if (!le.hasPotionEffect(PotionEffectType.SLOW)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 1));
						}
						
						if (!le.hasPotionEffect(PotionEffectType.CONFUSION)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 30, 1));
						}
						
						if (!le.hasPotionEffect(PotionEffectType.GLOWING)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 80, 1));
						}
					} else if (color == EnergyColor.YELLOW) { //Floater
						if (!le.hasPotionEffect(PotionEffectType.LEVITATION)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 80, 9));
						}
					} else if (color == EnergyColor.WHITE) { //Telekinesis
						e.setVelocity(GeneralMethods.getDirection(e.getLocation(), loc).multiply(0.5));
						damaged = true;
					} else if (color == EnergyColor.INDIGO) { //Blinding
						if (!le.hasPotionEffect(PotionEffectType.BLINDNESS)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 6));
						}
						
						if (!le.hasPotionEffect(PotionEffectType.WITHER)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 2));
						}
					} else if (color == EnergyColor.PINK) {
						ParticleEffect.HEART.display(le.getEyeLocation(), 8, 0.4, 0.4, 0.4, 0.012);
					} else if (color == EnergyColor.AQUA) {
						le.setRemainingAir(le.getRemainingAir() - 1);
					} else if (color == EnergyColor.GRAY) {
						le.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,  30, 2));
					} else if (color == EnergyColor.RAINBOW) {
						DamageHandler.damageEntity(e, player, damage, this);
						le.setFireTicks(90);
						le.setNoDamageTicks(0);
						e.setVelocity(map.get(loc).clone().multiply(3));
						if (!le.hasPotionEffect(PotionEffectType.SLOW)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 1));
						}
						if (!le.hasPotionEffect(PotionEffectType.CONFUSION)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 30, 1));
						}
						if (!le.hasPotionEffect(PotionEffectType.GLOWING)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 80, 1));
						}
						if (!le.hasPotionEffect(PotionEffectType.LEVITATION)) {
							le.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 80, 9));
						}
						if (!player.hasPotionEffect(PotionEffectType.HEAL)) {
							player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 40, Math.round((float)damage)-1));
						}
						damaged = true;
					}
				} else {
					DamageHandler.damageEntity(e, player, damage, this);
					damaged = true;
				}
			}
		}
		if (effects) {
			if (color == EnergyColor.BROWN) {
				Location next = loc.clone().add(map.get(loc));
				if (GeneralMethods.isSolid(next.getBlock())) {
					player.setVelocity(GeneralMethods.getDirection(player.getLocation(), loc).multiply(0.2));
				}
			}
		}
		return damaged;
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
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
	public void load() {}

	@Override
	public void stop() {}
	
	@Override
	public String getDescription() {
		return "A powerful avatar ability that allows them to focus their energy into a destructive beam";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak and enjoy";
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Avatar.EnergyBeam.Enabled");
	}
}
