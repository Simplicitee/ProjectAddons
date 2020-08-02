package me.simplicitee.project.addons.ability.fire;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

import me.simplicitee.project.addons.ProjectAddons;

public class Jets extends FireAbility implements AddonAbility {
	
	@Attribute("FlySpeed")
	private double flySpeed;
	@Attribute("HoverSpeed")
	private double hoverSpeed;
	@Attribute("DamageThreshold")
	private double dmgThreshold;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("MaxHeight")
	private int maxHeight;
	
	private long maxCooldown, minCooldown;
	private float oSpeed;
	private boolean hovering, gliding;
	private TurboJet source;
	
	public Jets(Player player) {
		this(player, null);
	}

	public Jets(Player player, TurboJet source) {
		super(player);
		
		if (source == null && player.isOnGround()) {
			return;
		}
		
		this.maxHeight = ProjectAddons.instance.getConfig().getInt("Abilities.Fire.Jets.MaxHeight");
		
		if (!checkHeight()) {
			return;
		}
		
		this.source = source;
		this.oSpeed = player.getFlySpeed();
		this.flySpeed = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.Jets.FlySpeed");
		this.hoverSpeed = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.Jets.HoverSpeed");
		this.duration = ProjectAddons.instance.getConfig().getLong("Abilities.Fire.Jets.Duration");
		this.maxCooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Fire.Jets.Cooldown.Maximum");
		this.minCooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Fire.Jets.Cooldown.Minimum");
		this.dmgThreshold = player.getHealth() - ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.Jets.DamageThreshold");
		double speedThreshold = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.Jets.SpeedThreshold");
		
		if (source != null) {
			this.gliding = true;
			this.hovering = false;
		} else if (player.isSprinting() || (player.getVelocity().length() > speedThreshold && Math.abs(player.getVelocity().angle(player.getEyeLocation().getDirection())) < 30)) {
			this.gliding = true;
			this.hovering = false;
		} else {
			this.gliding = false;
			this.hovering = true;
		}
		
		this.flightHandler.createInstance(player, getName());
		player.setAllowFlight(true);
		player.setFlySpeed((float) hoverSpeed);
		
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (player.isOnGround()) {
			remove();
			return;
		}
		
		if (player.getLocation().getBlock().isLiquid()) {
			remove();
			return;
		}
		
		if (duration > 0 && getStartTime() + duration < System.currentTimeMillis()) {
			remove();
			return;
		}
		
		if (player.getHealth() < dmgThreshold) {
			remove();
			return;
		}
		
		if (!checkHeight()) {
			player.setFlying(false);
			player.setGliding(false);
			return;
		}
		
		Vector pDirection = null;
		if (hovering) {
			player.setFlying(true);
			player.setGliding(false);
			player.setVelocity(player.getVelocity().add(new Vector(0, -0.015, 0)));
			pDirection = new Vector(0, -0.4, 0);
		} else if (gliding) {
			player.setGliding(true);
			player.setFlying(false);
			Vector velocity = player.getEyeLocation().getDirection().clone().normalize().multiply(flySpeed);
			
			if (player.getVelocity().getY() < 0) {
				velocity.add(player.getVelocity().multiply(0.2));
			}
			
			player.setVelocity(velocity);
			pDirection = player.getEyeLocation().getDirection().clone().normalize().multiply(-0.4);
		} else {
			remove();
			return;
		}

		for (int i = 0; i < 4; i++) {
			Location p = player.getLocation().clone().add(pDirection.clone().multiply(i));
			playFirebendingParticles(p, 4 - i, 0.3 - (i / 10), 0.04, 0.3 - (i / 10));
		}
		
		playFirebendingSound(player.getLocation());
	}
	
	@Override
	public void remove() {
		super.remove();
		this.flightHandler.removeInstance(player, getName());
		player.setFallDistance(0);
		player.setFlySpeed(oSpeed);
		bPlayer.addCooldown(this);
	}
	
	public void clickFunction() {
		if (player.isSneaking()) {
			remove();
			return;
		}
		
		if (hovering) {
			hovering = false;
			gliding = true;
		} else if (source == null || source.isRemoved()) {
			hovering = true;
			gliding = false;
		}
	}
	
	public boolean checkHeight() {
		return maxHeight <= 0 || player.getWorld().rayTraceBlocks(player.getLocation(), new Vector(0, -1, 0), maxHeight, FluidCollisionMode.ALWAYS, false).getHitBlock() != null;
	}
	
	public void setFlySpeed(double speed) {
		this.flySpeed = Math.abs(speed);
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
		double percent = (System.currentTimeMillis() - getStartTime()) / this.duration;
		return minCooldown + (long) ((maxCooldown - minCooldown) * percent);
	}

	@Override
	public String getName() {
		return "Jets";
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
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Fire.Jets.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Create jets of flames from your feet to hover off the ground or fly through the sky. Activating the ability while moving fast enough and looking in the direction you're moving will automatically put you in flying mode!";
	}
	
	@Override
	public String getInstructions() {
		return "Left click to activate, Left click to switch modes, and Sneak + Left click to cancel ability";
	}
}
