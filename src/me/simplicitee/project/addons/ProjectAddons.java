package me.simplicitee.project.addons;

import java.io.File;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.configuration.Config;
import com.projectkorra.projectkorra.firebending.FireShield;

import me.simplicitee.project.addons.ability.air.GaleGust;
import me.simplicitee.project.addons.ability.earth.Crumble;
import me.simplicitee.project.addons.ability.fire.CombustBeam;
import me.simplicitee.project.addons.ability.fire.FireDisc;
import me.simplicitee.project.addons.ability.water.RazorLeaf;

public class ProjectAddons extends JavaPlugin {
	
	public static ProjectAddons instance;
	
	private Config config;
	private BoardManager boards;
	private CustomMethods methods;
	private MainListener listener;

	@Override
	public void onEnable() {
		instance = this;
		
		this.config = new Config(new File("project_addons.yml"));
		this.setupConfig();
		
		CoreAbility.registerPluginAbilities(this, "me.simplicitee.project.addons.ability");
		
		this.setupCollisions();
		
		this.listener = new MainListener(this);
		
		if (config.get().getBoolean("Properties.BendingBoard.Enabled")) {
			this.boards = new BoardManager(this);
		} else {
			this.boards = null;
		}
		
		this.getCommand("projectaddons").setExecutor(new ProjectCommand());
		this.methods = new CustomMethods(this);
	}
	
	@Override
	public void onDisable() {
		if (boards != null) {
			boards.disable();
		}
		
		listener.revertSwappedBinds();
		
		if (CoreAbility.getAbility(Crumble.class) != null) {
			for (Crumble c : CoreAbility.getAbilities(Crumble.class)) {
				c.revert();
			}
		}
	}
	
	public String prefix() {
		return ChatColor.GRAY + "[" + ChatColor.GREEN + "ProjectAddons" + ChatColor.GRAY + "]";
	}
	
	public String version() {
		return prefix() + " v." + this.getDescription().getVersion();
	}
	
	@Override
	public FileConfiguration getConfig() {
		return config.get();
	}
	
	public Config config() {
		return config;
	}
	
	public boolean isBoardEnabled() {
		return boards != null;
	}
	
	public BoardManager getBoardManager() {
		return boards;
	}
	
	public CustomMethods getMethods() {
		return methods;
	}
	
	private void setupConfig() {
		FileConfiguration c = config.get();
		
		c.addDefault("Properties.BendingBoard.Enabled", true);
		c.addDefault("Properties.BendingBoard.Title", "Binds");
		c.addDefault("Properties.BendingBoard.EmptySlot", "&o~ Slot %d ~");
		c.addDefault("Properties.MetallicBlocks", Arrays.asList("GOLD_BLOCK", "IRON_BLOCK"));
		
		// LavaSurge
		c.addDefault("Abilities.LavaSurge.Enabled", true);
		c.addDefault("Abilities.LavaSurge.Cooldown", 4000);
		c.addDefault("Abilities.LavaSurge.Damage", 2);
		c.addDefault("Abilities.LavaSurge.Speed", 1.25);
		c.addDefault("Abilities.LavaSurge.SelectRange", 5);
		c.addDefault("Abilities.LavaSurge.SourceRadius", 3);
		c.addDefault("Abilities.LavaSurge.MaxBlocks", 12);
		c.addDefault("Abilities.LavaSurge.Burn.Enabled", true);
		c.addDefault("Abilities.LavaSurge.Burn.Duration", 3000);
		
		// Explode
		c.addDefault("Abilities.Explode.Enabled", true);
		c.addDefault("Abilities.Explode.Cooldown", 4500);
		c.addDefault("Abilities.Explode.Damage", 2);
		c.addDefault("Abilities.Explode.Radius", 2.7);
		c.addDefault("Abilities.Explode.Knockback", 1.9);
		c.addDefault("Abilities.Explode.Range", 7.4);
		
		// EarthKick
		c.addDefault("Abilities.EarthKick.Enabled", true);
		c.addDefault("Abilities.EarthKick.Cooldown", 4000);
		c.addDefault("Abilities.EarthKick.Damage", 1);
		c.addDefault("Abilities.EarthKick.MaxBlocks", 7);
		c.addDefault("Abilities.EarthKick.LavaMultiplier", 1.5);
		
		// FireDisc
		c.addDefault("Abilities.FireDisc.Enabled", true);
		c.addDefault("Abilities.FireDisc.Damage", 2);
		c.addDefault("Abilities.FireDisc.Range", 25);
		c.addDefault("Abilities.FireDisc.Cooldown", 1500);
		c.addDefault("Abilities.FireDisc.Controllable", true);
		c.addDefault("Abilities.FireDisc.RevertCutBlocks", true);
		c.addDefault("Abilities.FireDisc.DropCutBlocks", false);
		c.addDefault("Abilities.FireDisc.CuttableBlocks", Arrays.asList("LOG", "LOG_2"));
		
		// FlameBreath
		c.addDefault("Combos.FlameBreath.Enabled", true);
		c.addDefault("Combos.FlameBreath.Cooldown", 8000);
		c.addDefault("Combos.FlameBreath.Damage", 1);
		c.addDefault("Combos.FlameBreath.FireTick", 30);
		c.addDefault("Combos.FlameBreath.Range", 6);
		c.addDefault("Combos.FlameBreath.Duration", 5000);
		c.addDefault("Combos.FlameBreath.Burn.Ground", true);
		c.addDefault("Combos.FlameBreath.Burn.Entities", true);
		c.addDefault("Combos.FlameBreath.Rainbow", true);
		
		//MagmaSlap
		c.addDefault("Abilities.MagmaSlap.Enabled", true);
		c.addDefault("Abilities.MagmaSlap.Cooldown", 4000);
		c.addDefault("Abilities.MagmaSlap.Offset", 1.5);
		c.addDefault("Abilities.MagmaSlap.Damage", 2);
		c.addDefault("Abilities.MagmaSlap.Length", 14);
		c.addDefault("Abilities.MagmaSlap.Width", 1);
		c.addDefault("Abilities.MagmaSlap.RevertTime", 7000);
		
		// Shrapnel
		c.addDefault("Abilities.Shrapnel.Enabled", true);
		c.addDefault("Abilities.Shrapnel.Shot.Cooldown", 2000);
		c.addDefault("Abilities.Shrapnel.Shot.Damage", 2);
		c.addDefault("Abilities.Shrapnel.Shot.Speed", 2.3);
		c.addDefault("Abilities.Shrapnel.Blast.Cooldown", 3000);
		c.addDefault("Abilities.Shrapnel.Blast.Shots", 7);
		c.addDefault("Abilities.Shrapnel.Blast.Spread", 30);
		c.addDefault("Abilities.Shrapnel.Blast.Speed", 1.9);
		
		// Jab
		c.addDefault("Abilities.Jab.Enabled", true);
		c.addDefault("Abilities.Jab.Cooldown", 3000);
		c.addDefault("Abilities.Jab.MaxUses", 3);
		
		// NinjaStance
		c.addDefault("Abilities.NinjaStance.Enabled", true);
		c.addDefault("Abilities.NinjaStance.Cooldown", 0);
		c.addDefault("Abilities.NinjaStance.Stealth.Duration", 5000);
		c.addDefault("Abilities.NinjaStance.Stealth.ChargeTime", 2000);
		c.addDefault("Abilities.NinjaStance.SpeedAmplifier", 5);
		c.addDefault("Abilities.NinjaStance.JumpAmplifier", 5);
		c.addDefault("Abilities.NinjaStance.DamageModifier", 0.5);
		
		// ChiblockJab
		c.addDefault("Combos.ChiblockJab.Enabled", true);
		c.addDefault("Combos.ChiblockJab.Cooldown", 5000);
		c.addDefault("Combos.ChiblockJab.Duration", 3000);
		
		// FlyingKick
		c.addDefault("Combos.FlyingKick.Enabled", true);
		c.addDefault("Combos.FlyingKick.Cooldown", 4000);
		c.addDefault("Combos.FlyingKick.Damage", 2.0);
		c.addDefault("Combos.FlyingKick.LaunchPower", 2.2);
		
		// WeakeningJab
		c.addDefault("Combos.WeakeningJab.Enabled", true);
		c.addDefault("Combos.WeakeningJab.Cooldown", 6000);
		c.addDefault("Combos.WeakeningJab.Duration", 3000);
		c.addDefault("Combos.WeakeningJab.Modifier", 1.5);
		
		// EnergyBeam
		c.addDefault("Abilities.EnergyBeam.Enabled", true);
		c.addDefault("Abilities.EnergyBeam.Cooldown", 6000);
		c.addDefault("Abilities.EnergyBeam.Duration", 7000);
		c.addDefault("Abilities.EnergyBeam.Damage", 1);
		c.addDefault("Abilities.EnergyBeam.Range", 20);
		c.addDefault("Abilities.EnergyBeam.EasterEgg", false);
		
		// QuickWeld
		c.addDefault("Abilities.QuickWeld.Enabled", true);
		c.addDefault("Abilities.QuickWeld.Cooldown", 1000);
		c.addDefault("Abilities.QuickWeld.RepairAmount", 25);
		c.addDefault("Abilities.QuickWeld.RepairInterval", 1250);
		
		// RazorLeaf
		c.addDefault("Abilities.RazorLeaf.Enabled", true);
		c.addDefault("Abilities.RazorLeaf.Cooldown", 3000);
		c.addDefault("Abilities.RazorLeaf.Damage", 2);
		c.addDefault("Abilities.RazorLeaf.Radius", 0.6);
		c.addDefault("Abilities.RazorLeaf.Range", 20);
		c.addDefault("Abilities.RazorLeaf.Particles", 250);
		
		// PlantArmor
		c.addDefault("Abilities.PlantArmor.Enabled", true);
		c.addDefault("Abilities.PlantArmor.Cooldown", 10000);
		c.addDefault("Abilities.PlantArmor.Duration", -1);
		c.addDefault("Abilities.PlantArmor.Durability", 4000);
		c.addDefault("Abilities.PlantArmor.SelectRange", 9);
		c.addDefault("Abilities.PlantArmor.RequiredPlants", 10);
		c.addDefault("Abilities.PlantArmor.Boost.Swim", 1);
		c.addDefault("Abilities.PlantArmor.Boost.Speed", 1);
		c.addDefault("Abilities.PlantArmor.Boost.Jump", 1);
		
		// PlantArmor - VineWhip
		c.addDefault("Abilities.PlantArmor.SubAbilities.VineWhip.Cost", 50);
		c.addDefault("Abilities.PlantArmor.SubAbilities.VineWhip.Cooldown", 2000);
		c.addDefault("Abilities.PlantArmor.SubAbilities.VineWhip.Damage", 2);
		c.addDefault("Abilities.PlantArmor.SubAbilities.VineWhip.Range", 18);
		
		// PlantArmor - RazorLeaf
		c.addDefault("Abilities.PlantArmor.SubAbilities.RazorLeaf.Cost", 150);
		
		// PlantArmor - LeafShield
		c.addDefault("Abilities.PlantArmor.SubAbilities.LeafShield.Cost", 100);
		c.addDefault("Abilities.PlantArmor.SubAbilities.LeafShield.Cooldown", 1500);
		c.addDefault("Abilities.PlantArmor.SubAbilities.LeafShield.Radius", 2);
		
		// PlantArmor - Tangle
		c.addDefault("Abilities.PlantArmor.SubAbilities.Tangle.Cost", 200);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Tangle.Cooldown", 7000);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Tangle.Radius", 0.45);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Tangle.Duration", 3000);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Tangle.Range", 18);
		
		// PlantArmor - Leap
		c.addDefault("Abilities.PlantArmor.SubAbilities.Leap.Cost", 100);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Leap.Cooldown", 2500);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Leap.Power", 1.4);
		
		// PlantArmor - Grapple
		c.addDefault("Abilities.PlantArmor.SubAbilities.Grapple.Cost", 100);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Grapple.Cooldown", 2000);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Grapple.Range", 25);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Grapple.Speed", 1.24);
		
		// PlantArmor - LeafDome
		c.addDefault("Abilities.PlantArmor.SubAbilities.LeafDome.Cost", 400);
		c.addDefault("Abilities.PlantArmor.SubAbilities.LeafDome.Cooldown", 5000);
		c.addDefault("Abilities.PlantArmor.SubAbilities.LeafDome.Radius", 4);
		
		// PlantArmor - Regenerate
		c.addDefault("Abilities.PlantArmor.SubAbilities.Regenerate.Cooldown", 10000);
		c.addDefault("Abilities.PlantArmor.SubAbilities.Regenerate.RegenAmount", 350);
		
		// LeafStorm
		c.addDefault("Combos.LeafStorm.Enabled", true);
		c.addDefault("Combos.LeafStorm.Cooldown", 8000);
		c.addDefault("Combos.LeafStorm.PlantArmorCost", 800);
		c.addDefault("Combos.LeafStorm.LeafCount", 10);
		c.addDefault("Combos.LeafStorm.LeafSpeed", 12);
		c.addDefault("Combos.LeafStorm.Damage", 0.5);
		c.addDefault("Combos.LeafStorm.Radius", 10);
		
		// GaleGust
		c.addDefault("Abilities.GaleGust.Enabled", true);
		c.addDefault("Abilities.GaleGust.Cooldown", 3000);
		c.addDefault("Abilities.GaleGust.Damage", 1);
		c.addDefault("Abilities.GaleGust.Radius", 1.2);
		c.addDefault("Abilities.GaleGust.Range", 25);
		c.addDefault("Abilities.GaleGust.Knockback", 1.92);
		
		// Zephyr
		c.addDefault("Abilities.Zephyr.Enabled", true);
		c.addDefault("Abilities.Zephyr.Cooldown", 1000);
		c.addDefault("Abilities.Zephyr.Radius", 3);
		
		// Tailwind
		c.addDefault("Combos.Tailwind.Enabled", true);
		c.addDefault("Combos.Tailwind.Cooldown", 7000);
		c.addDefault("Combos.Tailwind.Duration", 22000);
		c.addDefault("Combos.Tailwind.Speed", 8);
		
		// Dig
		c.addDefault("Abilities.Dig.Enabled", true);
		c.addDefault("Abilities.Dig.Cooldown", 3000);
		c.addDefault("Abilities.Dig.Duration", -1);
		c.addDefault("Abilities.Dig.RevertTime", 3500);
		c.addDefault("Abilities.Dig.Speed", 0.52);
		
		// Dodging
		c.addDefault("Passives.Dodging.Enabled", true);
		c.addDefault("Passives.Dodging.Chance", 26);
		
		// Accretion
		c.addDefault("Abilities.Accretion.Enabled", true);
		c.addDefault("Abilities.Accretion.Cooldown", 10000);
		c.addDefault("Abilities.Accretion.Damage", 1);
		c.addDefault("Abilities.Accretion.Blocks", 7);
		c.addDefault("Abilities.Accretion.SelectRange", 7);
		c.addDefault("Abilities.Accretion.RevertTime", 20000);
		
		// Crumble
		c.addDefault("Abilities.Crumble.Enabled", true);
		c.addDefault("Abilities.Crumble.Cooldown", 2000);
		c.addDefault("Abilities.Crumble.Radius", 6);
		c.addDefault("Abilities.Crumble.SelectRange", 9);
		c.addDefault("Abilities.Crumble.RevertTime", 60);
		
		// Incinerate
		c.addDefault("Combos.Incinerate.Enabled", true);
		c.addDefault("Combos.Incinerate.Cooldown", 8000);
		c.addDefault("Combos.Incinerate.Duration", 6000);
		c.addDefault("Combos.Incinerate.FireTicks", 30);
		c.addDefault("Combos.Incinerate.MaxLength", 6);
		
		// LandLaunch
		c.addDefault("Passives.LandLaunch.Enabled", true);
		c.addDefault("Passives.LandLaunch.Power", 3);
		
		// Hydrojet
		c.addDefault("Passives.Hydrojet.Enabled", true);
		c.addDefault("Passives.Hydrojet.Power", 6);
		
		// ArcSpark
		c.addDefault("Abilities.ArcSpark.Enabled", true);
		c.addDefault("Abilities.ArcSpark.Speed", 6);
		c.addDefault("Abilities.ArcSpark.Length", 7);
		c.addDefault("Abilities.ArcSpark.Damage", 1);
		c.addDefault("Abilities.ArcSpark.Cooldown", 5000);
		c.addDefault("Abilities.ArcSpark.Duration", 4000);
		c.addDefault("Abilities.ArcSpark.ChargeTime", 800);
		
		// CombustBeam
		c.addDefault("Abilities.CombustBeam.Enabled", true);
		c.addDefault("Abilities.CombustBeam.Range", 50);
		c.addDefault("Abilities.CombustBeam.Cooldown", 3750);
		c.addDefault("Abilities.CombustBeam.Minimum.Power", 1);
		c.addDefault("Abilities.CombustBeam.Minimum.Angle", 1);
		c.addDefault("Abilities.CombustBeam.Minimum.ChargeTime", 2000);
		c.addDefault("Abilities.CombustBeam.Maximum.Power", 3);
		c.addDefault("Abilities.CombustBeam.Maximum.Angle", 50);
		c.addDefault("Abilities.CombustBeam.Maximum.ChargeTime", 5000);
		c.addDefault("Abilities.CombustBeam.InterruptedDamage", 3);
		
		// Jets
		c.addDefault("Abilities.Jets.Enabled", true);
		c.addDefault("Abilities.Jets.Cooldown", 8000);
		c.addDefault("Abilities.Jets.Duration", 20000);
		c.addDefault("Abilities.Jets.FlySpeed", 0.65);
		c.addDefault("Abilities.Jets.HoverSpeed", 0.065);
		c.addDefault("Abilities.Jets.SpeedThreshold", 2.4);
		
		// Electrify
		c.addDefault("Abilities.Electrify.Enabled", true);
		c.addDefault("Abilities.Electrify.Cooldown", 4000);
		c.addDefault("Abilities.Electrify.Duration", 10000);
		c.addDefault("Abilities.Electrify.DamageInWater", 2);
		c.addDefault("Abilities.Electrify.Slowness", 2);
		c.addDefault("Abilities.Electrify.Weakness", 1);
		
		// TurboJet
		c.addDefault("Combos.TurboJet.Enabled", true);
		c.addDefault("Combos.TurboJet.Cooldown", 8000);
		c.addDefault("Combos.TurboJet.Speed", 2.85);
		
		// Bulwark
		c.addDefault("Abilities.Bulwark.Enabled", true);
		c.addDefault("Abilities.Bulwark.Cooldown", 4000);
		
		config.save();
	}
	
	private void setupCollisions() {
		if (CoreAbility.getAbility(FireDisc.class) != null) {
			ProjectKorra.getCollisionInitializer().addSmallAbility(CoreAbility.getAbility(FireDisc.class));
		}
		
		if (CoreAbility.getAbility(RazorLeaf.class) != null) {
			ProjectKorra.getCollisionInitializer().addSmallAbility(CoreAbility.getAbility(RazorLeaf.class));
		}
		
		if (CoreAbility.getAbility(GaleGust.class) != null) {
			ProjectKorra.getCollisionInitializer().addSmallAbility(CoreAbility.getAbility(GaleGust.class));
		}
		
		if (CoreAbility.getAbility(CombustBeam.class) != null) {
			ProjectKorra.getCollisionInitializer().addLargeAbility(CoreAbility.getAbility(CombustBeam.class));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireShield.class), CoreAbility.getAbility(CombustBeam.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirShield.class), CoreAbility.getAbility(CombustBeam.class), false, true));
		}
	}
}
