package me.simplicitee.project.addons.ability.air;

import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

import me.simplicitee.project.addons.ProjectAddons;
import me.simplicitee.project.addons.util.SoundAbility;

public class Deafen extends SoundAbility implements AddonAbility {

	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	
	private Player target;
	
	public Deafen(Player player) {
		super(player);
		
		Entity e = GeneralMethods.getTargetedEntity(player, 10);
		if (e == null || !(e instanceof Player)) {
			return;
		}
		
		this.target = (Player) e;
		this.duration = ProjectAddons.instance.getConfig().getLong("Abilities.Air.Deafen.Duration");
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Air.Deafen.Cooldown");
		
		bPlayer.addCooldown(this);
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
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		Entity e = GeneralMethods.getTargetedEntity(player, 10);
		if (e == null || (e.getEntityId() != target.getEntityId())) {
			remove();
			return;
		}
		
		for (int i = 0; i < 2; i++) {
			target.playNote(target.getEyeLocation().add(new Vector(Math.random(), Math.random(), Math.random())), Instrument.BASS_GUITAR, Note.sharp(i, Tone.F));
			target.playNote(target.getEyeLocation().add(new Vector(Math.random(), Math.random(), Math.random())), Instrument.PLING, Note.sharp(i, Tone.F));
			target.playNote(target.getEyeLocation().add(new Vector(Math.random(), Math.random(), Math.random())), Instrument.FLUTE, Note.sharp(i, Tone.F));
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
		return "Deafen";
	}

	@Override
	public Location getLocation() {
		return null;
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
	public String getDescription() {
		return "Temporarily cause another player to go deaf by cancelling the sound waves around them!";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak while looking at a player";
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Air.Deafen.Enabled");
	}
}
