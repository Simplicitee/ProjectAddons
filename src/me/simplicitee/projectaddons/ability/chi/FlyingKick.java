package me.simplicitee.projectaddons.ability.chi;

import java.util.ArrayList;

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
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.projectaddons.ProjectAddons;

public class FlyingKick extends ChiAbility implements ComboAbility, AddonAbility{
	
	private double launch;
	private double damage;

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
		
		launch = ProjectAddons.instance.getConfig().getDouble("Combos.FlyingKick.LaunchPower");
		damage = ProjectAddons.instance.getConfig().getDouble("Combos.FlyingKick.Damage");
		Vector v = player.getLocation().getDirection().add(new Vector(0, 0.25485, 0)).normalize().multiply(launch);
		player.setVelocity(v);
		start();
	}

	@Override
	public long getCooldown() {
		return ProjectAddons.instance.getConfig().getLong("Combos.FlyingKick.Cooldown");
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
			if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
				DamageHandler.damageEntity(entity, player, damage, this);
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
		return ProjectAddons.instance.getConfig().getBoolean("Combos.FlyingKick.Enabled");
	}
}