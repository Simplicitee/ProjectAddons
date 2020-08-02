package me.simplicitee.project.addons.ability.chi;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

import me.simplicitee.project.addons.ProjectAddons;

public class NinjaStance extends ChiAbility implements AddonAbility{
	
	@Attribute(Attribute.DURATION)
	public long stealthDuration;
	@Attribute("SpeedBoost")
	public int speedAmp;
	@Attribute("JumpBoost")
	public int jumpAmp;
	
	public boolean stealth, stealthReady, stealthStarted;
	public long stealthStart;
	public long stealthChargeTime;
	public long stealthReadyStart;

	public NinjaStance(Player player) {
		super(player);
		ChiAbility stance = bPlayer.getStance();
		if (stance != null) {
			stance.remove();
			if (stance instanceof NinjaStance) {
				bPlayer.setStance(null);
				return;
			}
		}
		
		stealthDuration = ProjectAddons.instance.getConfig().getLong("Abilities.Chi.NinjaStance.Stealth.Duration");
		stealthChargeTime = ProjectAddons.instance.getConfig().getLong("Abilities.Chi.NinjaStance.Stealth.ChargeTime");
		speedAmp = ProjectAddons.instance.getConfig().getInt("Abilities.Chi.NinjaStance.SpeedAmplifier") + 1;
		jumpAmp = ProjectAddons.instance.getConfig().getInt("Abilities.Chi.NinjaStance.JumpAmplifier") + 1;
		
		start();
		bPlayer.setStance(this);
		GeneralMethods.displayMovePreview(player);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 0.2F, 2F);
	}

	@Override
	public long getCooldown() {
		return ProjectAddons.instance.getConfig().getLong("Abilities.Chi.NinjaStance.Cooldown");
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "NinjaStance";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (stealth) {
			if (System.currentTimeMillis() >= stealthStart + stealthChargeTime) {
				stealthReady = true;
			}
			
			if (!stealthStarted) {
				if (stealthReady && !player.isSneaking()) {
					stealthReadyStart = System.currentTimeMillis();
					stealthStarted = true;
				} else if (!player.isSneaking()) {
					stopStealth();
				} else if (stealthReady && player.isSneaking()) {
					Location play = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().normalize());
					GeneralMethods.displayColoredParticle("#00ee00", play);
				} else {
					Location play = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().normalize());
					GeneralMethods.displayColoredParticle("#000000", play);
				}
			} else {
				if (System.currentTimeMillis() >= stealthReadyStart + stealthDuration) {
					stopStealth();
				} else {
					player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5, 2, true, false));
				}
			}
		}
		
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, speedAmp, true, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 5, jumpAmp, true, false));
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
	public String getDescription() {
		return "This stance allows chiblockers to become faster and more stealthy (like a ninja)!";
	}
	
	@Override
	public String getInstructions() {
		return "Left click to begin to this stance > Hold sneak to begin stealth mode";
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Chi.NinjaStance.Enabled");
	}

	public void beginStealth() {
		if (stealth) {
			player.sendMessage("Already cloaked!");
			return;
		}
		stealth = true;
		stealthStart = System.currentTimeMillis();
	}
	
	public void stopStealth() {
		stealth = false;
		stealthReady = false;
		stealthStarted = false;
	}
	
	public static double getDamageModifier() {
		return ProjectAddons.instance.getConfig().getDouble("Abilities.Chi.NinjaStance.DamageModifier");
	}
}
