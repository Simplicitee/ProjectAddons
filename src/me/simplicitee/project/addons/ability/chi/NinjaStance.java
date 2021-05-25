package me.simplicitee.project.addons.ability.chi;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ActionBar;

import me.simplicitee.project.addons.ProjectAddons;
import net.md_5.bungee.api.ChatColor;

public class NinjaStance extends ChiAbility implements AddonAbility{
	
	@Attribute(Attribute.DURATION)
	private long stealthDuration;
	
	private boolean stealth, stealthReady, stealthStarted;
	private long stealthStart, stealthChargeTime, stealthReadyStart, stealthCooldown;
	private List<PotionEffect> effects = new ArrayList<>();
	private PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, 5, 2, true, false);	

	public NinjaStance(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
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
		stealthCooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Chi.NinjaStance.Stealth.Cooldown");
		effects.add(new PotionEffect(PotionEffectType.SPEED, 5, ProjectAddons.instance.getConfig().getInt("Abilities.Chi.NinjaStance.SpeedAmplifier") + 1, true, false));
		effects.add(new PotionEffect(PotionEffectType.JUMP, 5, ProjectAddons.instance.getConfig().getInt("Abilities.Chi.NinjaStance.JumpAmplifier") + 1, true, false));
		
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
					return;
				}
				
				GeneralMethods.displayColoredParticle(stealthReady && player.isSneaking() ? "00ee00" : "000000", player.getEyeLocation().add(player.getEyeLocation().getDirection()));
			} else {
				if (System.currentTimeMillis() >= stealthReadyStart + stealthDuration) {
					stopStealth();
					bPlayer.addCooldown("ninjastealth", stealthCooldown);
				} else {
					player.addPotionEffect(invis);
				}
			}
		}
		
		player.addPotionEffects(effects);
	}

	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
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
			ActionBar.sendActionBar(ChatColor.RED + "!> already cloaked <!", player);
			return;
		} else if (bPlayer.isOnCooldown("ninjastealth")) {
			ActionBar.sendActionBar(ChatColor.RED + "!> cooldown <!", player);
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
	
	public boolean isStealthed() {
		return stealth && stealthStarted;
	}
	
	public static double getDamageModifier() {
		return ProjectAddons.instance.getConfig().getDouble("Abilities.Chi.NinjaStance.DamageModifier");
	}
}
