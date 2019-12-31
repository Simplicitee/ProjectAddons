package me.simplicitee.project.addons.ability.fire;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.ProjectAddons;

public class TurboJet extends FireAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	
	private double normal;
	private Jets jets;
	
	public TurboJet(Player player) {
		super(player);
		
		if (player.getLocation().getBlock().isLiquid()) {
			return;
		}
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		this.speed = ProjectAddons.instance.getConfig().getDouble("Combos.Fire.TurboJet.Speed");
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Fire.TurboJet.Cooldown");
		this.normal = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.Jets.FlySpeed");
		
		if (!hasAbility(player, Jets.class)) {
			jets = new Jets(player, this);
		} else {
			jets = getAbility(player, Jets.class);
		}
		ParticleEffect.EXPLOSION_NORMAL.display(player.getLocation(), 1);
		start();
	}

	@Override
	public void progress() {
		jets.setFlySpeed(speed);
		speed -= 0.025;
		
		if (speed <= normal) {
			jets.setFlySpeed(normal);
			bPlayer.addCooldown(this);
			remove();
			return;
		}
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
		return "TurboJet";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new TurboJet(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("HeatControl", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Jets", ClickType.SHIFT_UP));
		return combo;
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
		return "Release massive power all at once to make your jets go turbo speed!";
	}
	
	@Override
	public String getInstructions() {
		return "HeatControl (Hold sneak) > Jets (Release sneak)";
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Fire.TurboJet.Enabled");
	}
}
