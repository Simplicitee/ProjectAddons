package me.simplicitee.project.addons.ability.chi;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.ProjectAddons;

public class FlyingKick extends ChiAbility implements ComboAbility, AddonAbility {
	
	@Attribute("LaunchPower")
	private double launch;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	
	private List<Entity> affected;

	public FlyingKick(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (hasAbility(player, FlyingKick.class)) {
			return;
		}
		
		if (player.getLocation().getBlock().isLiquid()) {
			return;
		}
		
		if (!player.isOnGround()) {
			return;
		}
		
		launch = ProjectAddons.instance.getConfig().getDouble("Combos.Chi.FlyingKick.LaunchPower");
		damage = ProjectAddons.instance.getConfig().getDouble("Combos.Chi.FlyingKick.Damage");
		cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Chi.FlyingKick.Cooldown");
		affected = new ArrayList<>();
		
		player.setVelocity(player.getLocation().getDirection().add(new Vector(0, 0.25485, 0)).normalize().multiply(launch));
		start();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "FlyingKick";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public void progress() {
		if (player == null) {
			remove();
			return;
		}
		
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > this.getStartTime() + 400) {
			if (player.getLocation().subtract(0, 0.1, 0).getBlock().getType() != Material.AIR) {
				remove();
				bPlayer.addCooldown(this);
				return;
			}
		}
		
		ParticleEffect.CRIT_MAGIC.display(player.getLocation(), 3, 0.2, 0.2, 0.2, 0.02);
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), 2)) {
			if (affected.contains(entity)) {
				continue;
			}
			
			if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
				DamageHandler.damageEntity(entity, player, damage, this);
				affected.add(entity);
			}
		}
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new FlyingKick(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("SwiftKick", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("SwiftKick", ClickType.LEFT_CLICK));
		return combo;
	}
	
	@Override
	public String getDescription() {
		return "Jump through the air and kick your opponent!";
	}

	@Override
	public String getInstructions() {
		return "SwiftKick (Hold sneak) > SwiftKick (Left Click)";
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
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Chi.FlyingKick.Enabled");
	}
}
