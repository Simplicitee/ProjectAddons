package me.simplicitee.project.addons.ability.chi;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.ProjectAddons;

public class ChiblockJab extends ChiAbility implements ComboAbility, AddonAbility{
	
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	
	private Player attacked;
	private BendingPlayer bp;

	public ChiblockJab(Player player) {
		super(player);
		
		Entity entity = GeneralMethods.getTargetedEntity(player, 4);
		
		if (entity instanceof Player) {
			attacked = (Player) entity;
		} else {
			return;
		}
		
		duration = ProjectAddons.instance.getConfig().getLong("Combos.Chi.ChiblockJab.Duration");
		cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Chi.ChiblockJab.Cooldown");
		
		if (attacked != null) {
			bp = BendingPlayer.getBendingPlayer(attacked);
			
			if (bp != null) {
				bp.blockChi();
				start();
			}
		}
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return attacked.getLocation().add(0, 1, 0);
	}

	@Override
	public String getName() {
		return "ChiblockJab";
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
		ParticleEffect.CRIT.display(attacked.getLocation().add(0, 1, 0), 3, 0.2, 1.0, 0.2, 0.04);
		if (System.currentTimeMillis() >= getStartTime() + duration) {
			remove();
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		bp.unblockChi();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new ChiblockJab(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("Jab", ClickType.RIGHT_CLICK_ENTITY));
		combo.add(new AbilityInformation("Jab", ClickType.LEFT_CLICK_ENTITY));
		combo.add(new AbilityInformation("Jab", ClickType.RIGHT_CLICK_ENTITY));
		return combo;
	}
	
	@Override
	public String getDescription() {
		return "A special jab that will always chiblock the enemy!";
	}

	@Override
	public String getInstructions() {
		return "Jab (Right) > Jab (Left) > Jab (Right)";
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
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Chi.ChiblockJab.Enabled");
	}
}
