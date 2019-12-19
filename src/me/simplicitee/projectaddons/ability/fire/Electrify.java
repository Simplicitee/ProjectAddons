package me.simplicitee.projectaddons.ability.fire;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LightningAbility;

import me.simplicitee.projectaddons.ProjectAddons;

public class Electrify extends LightningAbility implements AddonAbility {
	
	private Block block;
	private Location center;
	private long cooldown;
	private double damage, radius;

	public Electrify(Player player, Block block) {
		super(player);
		
		this.block = block;
		this.center = block.getLocation().add(0.5, 0.5, 0.5);
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Electrify.Cooldown");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Electrify.Damage");
		this.radius = ProjectAddons.instance.getConfig().getDouble("Abilities.Electrify.Radius");
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
		return false;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "Electrify";
	}

	@Override
	public Location getLocation() {
		return null;
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
		return false;
	}
}
