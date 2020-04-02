package me.simplicitee.project.addons.ability.water;

import java.util.ArrayList;

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
import com.projectkorra.projectkorra.util.DamageHandler;

import me.simplicitee.project.addons.ProjectAddons;

public class BloodGrip extends BloodAbility implements AddonAbility, MultiAbility {
	
	private LivingEntity target;
	private String current;
	private double health;
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.KNOCKBACK)
	private double throwPower;
	@Attribute("MangleDamage")
	private double mangleDmg;
	
	public BloodGrip(Player player, boolean sneak) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		if (!MultiAbilityManager.hasMultiAbilityBound(player, getName()) && sneak) {
			Entity e = GeneralMethods.getTargetedEntity(player, 8);
		
			if (e == null || !(e instanceof LivingEntity)) {
				return;
			}
			
			cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Water.BloodGrip.Cooldown");
			throwPower = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.BloodGrip.Drag.ThrowPower");
			mangleDmg = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.BloodGrip.Mangle.Damage");
			
			health = player.getHealth();
			
			MultiAbilityManager.bindMultiAbility(player, getName());
		} else if (MultiAbilityManager.hasMultiAbilityBound(player, getName())) {
			BloodGrip grip = CoreAbility.getAbility(player, BloodGrip.class);
			switch (player.getInventory().getHeldItemSlot()) {
				case 0:
					grip.throwTarget();
				case 1:
				case 2:
				case 3:
				case 4:
			}
		}
	}
	
	public void throwTarget() {
		Vector v = GeneralMethods.getDirection(player.getEyeLocation(), target.getLocation().add(0, 1, 0));
		
		target.setVelocity(v.normalize().multiply(throwPower));
		
		remove();
	}
	
	public void mangleTarget() {
		DamageHandler.damageEntity(target, mangleDmg, this);
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if (health < player.getHealth()) {
			remove();
			return;
		} else if (!player.isSneaking()) {
			remove();
			return;
		} else if (!target.getLocation().getWorld().equals(player.getLocation().getWorld())) {
			remove();
			return;
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
		return "BloodGrip";
	}

	@Override
	public Location getLocation() {
		return target.getLocation();
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
		
		abils.add(new MultiAbilityInfoSub("Drag", Element.BLOOD));
		abils.add(new MultiAbilityInfoSub("Mangle", Element.BLOOD));
		abils.add(new MultiAbilityInfoSub("Puppet", Element.BLOOD));
		abils.add(new MultiAbilityInfoSub("Slam", Element.BLOOD));
		
		return abils;
	}
	
	@Override
	public boolean isEnabled() {
		return false;
	}
}
