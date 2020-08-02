package me.simplicitee.project.addons.ability.air;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;

import me.simplicitee.project.addons.ProjectAddons;

public class Tailwind extends AirAbility implements ComboAbility, AddonAbility {
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SPEED)
	private int speed;

	public Tailwind(Player player) {
		super(player);
		
		if (hasAbility(player, Tailwind.class)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Air.Tailwind.Cooldown");
		duration = ProjectAddons.instance.getConfig().getLong("Combos.Air.Tailwind.Duration");
		speed = ProjectAddons.instance.getConfig().getInt("Combos.Air.Tailwind.Speed") - 1;
		
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (player.getLocation().getBlock().isLiquid() && player.getEyeLocation().getBlock().isLiquid()) {
			remove();
			return;
		}
		
		if (getStartTime() + duration < System.currentTimeMillis()) {
			remove();
			return;
		}
		
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, speed, true, false));
		playAirbendingParticles(player.getEyeLocation(), 1, 0.1, 0.1, 0.1);
		playAirbendingParticles(player.getLocation().add(0, 0.4, 0), 1, 0.06, 0.3, 0.06);
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
		combo.add(new AbilityInformation("AirBurst", ClickType.SHIFT_UP));
		
		return combo;
	}

	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Air.Tailwind.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Create a tailwind behind you to increase your speed immensely!";
	}
	
	@Override
	public String getInstructions() {
		return "Hold Sneak AirBlast > Release Sneak AirBurst";
	}
}
