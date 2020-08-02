package me.simplicitee.project.addons.ability.water;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BloodAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.simplicitee.project.addons.ProjectAddons;

public class BloodGrip extends BloodAbility implements AddonAbility, MultiAbility {
	
	private static final List<LivingEntity> TARGETS = new ArrayList<>();
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.KNOCKBACK)
	private double throwPower;
	@Attribute("MangleDamage")
	private double mangleDmg;
	@Attribute(Attribute.SPEED)
	private double dragSpeed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute("SlamSpeed")
	private double slamSpeed;
	
	private LivingEntity target;
	private double health;
	
	public BloodGrip(Player player, boolean sneak) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		if (!MultiAbilityManager.hasMultiAbilityBound(player, getName()) && sneak) {
			range = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.BloodGrip.Range");
			Entity e = GeneralMethods.getTargetedEntity(player, range);
		
			if (e == null || !(e instanceof LivingEntity)) {
				return;
			} else if (ProjectAddons.instance.getConfig().getStringList("Abilities.Water.BloodGrip.EntityFilter").contains(e.getType().toString())) {
				return;
			}
			
			target = (LivingEntity) e;
			cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Water.BloodGrip.Cooldown");
			dragSpeed = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.BloodGrip.DragSpeed");
			throwPower = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.BloodGrip.ThrowPower");
			mangleDmg = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.BloodGrip.MangleDamage");
			slamSpeed = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.BloodGrip.SlamSpeed");
			
			health = player.getHealth() - ProjectAddons.instance.getConfig().getDouble("Abilities.Water.BloodGrip.DamageThreshold");
			TARGETS.add(target);
			
			MultiAbilityManager.bindMultiAbility(player, getName());
			start();
		} else if (MultiAbilityManager.hasMultiAbilityBound(player, getName())) {
			BloodGrip grip = CoreAbility.getAbility(player, BloodGrip.class);
			switch (player.getInventory().getHeldItemSlot()) {
				case 0:
					grip.throwTarget();
					break;
				case 1:
					grip.mangleTarget();
					break;
				case 2:
					grip.puppetAttack();
					break;
				case 3:
					grip.slamAttack();
					break;
				default:
					break;
			}
		}
	}
	
	private Location getTargetLocation() {
		return target.getLocation().add(0, target.getHeight() / 2, 0);
	}
	
	public void throwTarget() {
		remove();
		target.setVelocity(GeneralMethods.getDirection(player.getEyeLocation(), getTargetLocation()).normalize().multiply(throwPower));
		new HorizontalVelocityTracker(target, player, 50, this);
	}
	
	public void mangleTarget() {
		DamageHandler.damageEntity(target, mangleDmg, this);
	}
	
	public void puppetAttack() {
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(target.getLocation(), 1)) {
			if (e instanceof LivingEntity && e != target && e != player) {
				if (target instanceof Player) {
					DamageHandler.damageEntity(e, (Player) target, 1, this);
				} else {
					DamageHandler.damageEntity(e, 1, this);
				}
				
				((LivingEntity) e).setNoDamageTicks(2);
			}
		}
	}
	
	public void slamAttack() {
		remove();
		target.setFallDistance(3);
		target.setVelocity(new Vector(0, -slamSpeed, 0));
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead() || target.isDead()) {
			remove();
			return;
		} else if (health > player.getHealth()) {
			remove();
			return;
		} else if (!player.isSneaking()) {
			remove();
			return;
		} else if (!target.getLocation().getWorld().equals(player.getLocation().getWorld())) {
			remove();
			return;
		}
		
		target.setVelocity(GeneralMethods.getDirection(getTargetLocation(), GeneralMethods.getTargetedLocation(player, range, true, getTransparentMaterials())).multiply(dragSpeed));
		target.setFallDistance(0);
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		MultiAbilityManager.unbindMultiAbility(player);
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
		return "BloodGrip";
	}

	@Override
	public Location getLocation() {
		return target == null ? null : target.getLocation();
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
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		ArrayList<MultiAbilityInfoSub> abils = new ArrayList<>();
		
		abils.add(new MultiAbilityInfoSub("Throw", Element.BLOOD));
		abils.add(new MultiAbilityInfoSub("Mangle", Element.BLOOD));
		abils.add(new MultiAbilityInfoSub("Puppet", Element.BLOOD));
		abils.add(new MultiAbilityInfoSub("Slam", Element.BLOOD));
		
		return abils;
	}
	
	@Override
	public boolean isEnabled() {
		//return ProjectAddons.instance.getConfig().getBoolean("Abilities.Water.BloodGrip.Enabled");
		return false;
	}
	
	public static boolean isBloodbent(LivingEntity entity) {
		return TARGETS.contains(entity);
	}
}
