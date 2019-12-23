package me.simplicitee.project.addons.ability.chi;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;

import me.simplicitee.project.addons.ProjectAddons;

public class Dodging extends ChiAbility implements AddonAbility, PassiveAbility {

	private double chance;
	
	public Dodging(Player player) {
		super(player);
		
		chance = ProjectAddons.instance.getConfig().getDouble("Passives.Dodging.Chance") / 100;
	}
	
	public boolean check() {
		return Math.random() < chance;
	}

	@Override
	public void progress() {
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "Dodging";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public boolean isProgressable() {
		return false;
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
		return ProjectAddons.instance.getConfig().getBoolean("Passives.Dodging.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Graceful but unpredictable movements make chiblockers more difficult to hit, having been taught an innate dodging technique!";
	}
	
	@Override
	public String getInstructions() {
		return "Passively active";
	}
}
