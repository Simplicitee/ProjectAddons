package me.simplicitee.project.addons.ability.air;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

import me.simplicitee.project.addons.ProjectAddons;
import me.simplicitee.project.addons.util.SoundAbility;

public class VocalMimicry extends SoundAbility implements AddonAbility {
	
	private static HashMap<Player, Sound> selected = new HashMap<>();

	@Attribute("Volume")
	private float volume;
	@Attribute("Pitch")
	private float pitch;
	
	private Sound sound;
	
	public VocalMimicry(Player player) {
		super(player);
		
		volume = (float) ProjectAddons.instance.getConfig().getDouble("Abilities.Air.VocalMimicry.Volume");
		pitch = (float) ProjectAddons.instance.getConfig().getDouble("Abilities.Air.VocalMimicry.Pitch");
		sound = (selected.containsKey(player) ? selected.get(player) : Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
		
		start();
	}

	@Override
	public void progress() {
		player.getWorld().playSound(player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize()), sound, volume, pitch);
		remove();
	}

	@Override
	public boolean isSneakAbility() {
		return true;
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
		return "VocalMimicry";
	}

	@Override
	public Location getLocation() {
		return player.getEyeLocation();
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
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Air.VocalMimicry.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Manipulate the vibration of the air molecules exiting your mouth to mimic the sound of anything! To select what sound you want to make, type `@vocalsound <sound>` in chat.";
	}
	
	@Override
	public String getInstructions() {
		return "sneak to shout";
	}

	public static void selectSound(Player player, Sound sound) {
		selected.put(player, sound);
	}
}
