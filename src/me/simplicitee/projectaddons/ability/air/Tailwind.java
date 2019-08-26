package me.simplicitee.projectaddons.ability.air;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;

import me.simplicitee.projectaddons.ProjectAddons;

public class Tailwind extends AirAbility implements ComboAbility, AddonAbility {
	
	private long cooldown;
	private long duration;
	private int speed;

	public Tailwind(Player player) {
		super(player);
		
		if (hasAbility(player, Tailwind.class)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Tailwind.Cooldown");
		duration = ProjectAddons.instance.getConfig().getLong("Combos.Tailwind.Duration");
		speed = ProjectAddons.instance.getConfig().getInt("Combos.Tailwind.Speed") + 1;
		
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (getStartTime() + duration < System.currentTimeMillis()) {
			remove();
			return;
		}
		
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, speed, true, false), true);
		playAirbendingParticles(player.getEyeLocation(), 3, 0.3, 0.4, 0.3);
		playAirbendingParticles(player.getLocation().clone().add(0, 0.6, 0), 4, 0.2, 0.5, 0.2);
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
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
		return cooldown;
	}

	@Override
	public String getName() {
		return "Tailwind";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
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
	public Object createNewComboInstance(Player player) {
		return new Tailwind(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		
		combo.add(new AbilityInformation("AirBlast", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("AirBlast", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("AirBlast", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("AirBlast", ClickType.SHIFT_UP));
		
		return combo;
	}

	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Tailwind.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Create a tailwind behind you to increase your speed immensely!";
	}
	
	@Override
	public String getInstructions() {
		return "AirBlast (Double Tap Sneak)";
	}
}
