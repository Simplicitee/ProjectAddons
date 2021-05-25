package me.simplicitee.project.addons;

import java.io.File;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.ElementType;
import com.projectkorra.projectkorra.Element.SubElement;
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
	private MainListener listener;
	private Element soundElement;

	@Override
	public void onEnable() {
		instance = this;
		
		this.config = new Config(new File("project_addons.yml"));
		this.setupConfig();
		soundElement = new SubElement("Sound", Element.AIR, ElementType.BENDING, this) {
			@Override
			public ChatColor getColor() {
				return Element.AIR.getSubColor();
			}
		};
		
		CoreAbility.registerPluginAbilities(this, "me.simplicitee.project.addons.ability");
		
		this.setupCollisions();
		
		this.listener = new MainListener(this);
		
		if (config.get().getBoolean("Properties.BendingBoard.Enabled")) {
			this.boards = new BoardManager(this);
		} else {
			this.boards = null;
		}
		
		this.getCommand("projectaddons").setExecutor(new ProjectCommand());
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
	
	public Element getSoundElement() {
		return soundElement;
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
	
	private void setupConfig() {
		FileConfiguration c = config.get();
		
		c.addDefault("Properties.BendingBoard.Enabled", true);
		c.addDefault("Properties.BendingBoard.Title", "Binds");
		c.addDefault("Properties.BendingBoard.EmptySlot", "&o~ Slot %d ~");
		c.addDefault("Properties.BendingBoard.IndicatorMode", "bold");
		c.addDefault("Properties.MetallicBlocks", Arrays.asList("GOLD_BLOCK", "IRON_BLOCK"));

		// ---- Avatar ----
		// EnergyBeam
		c.addDefault("Abilities.Avatar.EnergyBeam.Enabled", true);
		c.addDefault("Abilities.Avatar.EnergyBeam.Cooldown", 12000);
		c.addDefault("Abilities.Avatar.EnergyBeam.Duration", 10000);
		c.addDefault("Abilities.Avatar.EnergyBeam.Damage", 3);
		c.addDefault("Abilities.Avatar.EnergyBeam.Range", 40);
		c.addDefault("Abilities.Avatar.EnergyBeam.EasterEgg", true);
		
		// ---- Airbending ----
		// Flying
		c.addDefault("Passives.Air.Flying.Enabled", true);
		c.addDefault("Passives.Air.Flying.FlySpeed", 0.05);
		c.addDefault("Passives.Air.Flying.Glide.StartSpeed", 0.8);
		c.addDefault("Passives.Air.Flying.Glide.MaxSpeed", 1.6);
		c.addDefault("Passives.Air.Flying.Acceleration", 0.001);
		c.addDefault("Passives.Air.Flying.AbilityBlacklist", Arrays.asList("Tornado", "EarthSmash", "Surge", "Lightning"));
		
		// Deafen
		c.addDefault("Abilities.Air.Deafen.Enabled", true);
		c.addDefault("Abilities.Air.Deafen.Cooldown", 10000);
		c.addDefault("Abilities.Air.Deafen.Duration", 6000);
		
		// GaleGust
		c.addDefault("Abilities.Air.GaleGust.Enabled", true);
		c.addDefault("Abilities.Air.GaleGust.Cooldown", 9000);
		c.addDefault("Abilities.Air.GaleGust.Damage", 4);
		c.addDefault("Abilities.Air.GaleGust.Radius", 1);
		c.addDefault("Abilities.Air.GaleGust.Range", 18);
		c.addDefault("Abilities.Air.GaleGust.Knockback", 0.67);
		
		// SonicWave
		c.addDefault("Abilities.Air.SonicWave.Enabled", true);
		c.addDefault("Abilities.Air.SonicWave.Cooldown", 4000);
		c.addDefault("Abilities.Air.SonicWave.Range", 25);
		c.addDefault("Abilities.Air.SonicWave.Width", 10);
		c.addDefault("Abilities.Air.SonicWave.Nausea.Duration", 120);
		c.addDefault("Abilities.Air.SonicWave.Nausea.Power", 2);
		
		// VocalManipulation
		c.addDefault("Abilities.Air.VocalMimicry.Enabled", true);
		c.addDefault("Abilities.Air.VocalMimicry.Volume", 0.7);
		c.addDefault("Abilities.Air.VocalMimicry.Pitch", 1);
		c.addDefault("Abilities.Air.VocalMimicry.SoundBlacklist", Arrays.asList("SOUND_NAME_HERE"));
		
		// Zephyr
		c.addDefault("Abilities.Air.Zephyr.Enabled", true);
		c.addDefault("Abilities.Air.Zephyr.Cooldown", 1000);
		c.addDefault("Abilities.Air.Zephyr.Radius", 4);
		
		// Tailwind
		c.addDefault("Combos.Air.Tailwind.Enabled", true);
		c.addDefault("Combos.Air.Tailwind.Cooldown", 7000);
		c.addDefault("Combos.Air.Tailwind.Duration", 22000);
		c.addDefault("Combos.Air.Tailwind.Speed", 9);
		
		// ---- Earthbending ----
		// LandLaunch
		c.addDefault("Passives.Earth.LandLaunch.Enabled", true);
		c.addDefault("Passives.Earth.LandLaunch.Power", 3);

		// Accretion
		c.addDefault("Abilities.Earth.Accretion.Enabled", true);
		c.addDefault("Abilities.Earth.Accretion.Cooldown", 10000);
		c.addDefault("Abilities.Earth.Accretion.Damage", 1);
		c.addDefault("Abilities.Earth.Accretion.Blocks", 8);
		c.addDefault("Abilities.Earth.Accretion.SelectRange", 6);
		c.addDefault("Abilities.Earth.Accretion.RevertTime", 20000);
		c.addDefault("Abilities.Earth.Accretion.ThrowSpeed", 1.6);

		// Bulwark
		c.addDefault("Abilities.Earth.Bulwark.Enabled", true);
		c.addDefault("Abilities.Earth.Bulwark.Cooldown", 6000);
		c.addDefault("Abilities.Earth.Bulwark.Damage", 1);
		c.addDefault("Abilities.Earth.Bulwark.ThrowSpeed", 0.94);
		c.addDefault("Abilities.Earth.Bulwark.Height", 2);
		
		// Crumble
		c.addDefault("Abilities.Earth.Crumble.Enabled", true);
		c.addDefault("Abilities.Earth.Crumble.Cooldown", 3000);
		c.addDefault("Abilities.Earth.Crumble.Radius", 6);
		c.addDefault("Abilities.Earth.Crumble.SelectRange", 9);
		c.addDefault("Abilities.Earth.Crumble.RevertTime", 60);
		
		// Dig
		c.addDefault("Abilities.Earth.Dig.Enabled", true);
		c.addDefault("Abilities.Earth.Dig.Cooldown", 3000);
		c.addDefault("Abilities.Earth.Dig.Duration", -1);
		c.addDefault("Abilities.Earth.Dig.RevertTime", 3500);
		c.addDefault("Abilities.Earth.Dig.Speed", 0.51);
		
		// EarthKick
		c.addDefault("Abilities.Earth.EarthKick.Enabled", true);
		c.addDefault("Abilities.Earth.EarthKick.Cooldown", 4000);
		c.addDefault("Abilities.Earth.EarthKick.Damage", 0.5);
		c.addDefault("Abilities.Earth.EarthKick.MaxBlocks", 9);
		c.addDefault("Abilities.Earth.EarthKick.LavaMultiplier", 1.5);
		
		// LavaSurge
		c.addDefault("Abilities.Earth.LavaSurge.Enabled", true);
		c.addDefault("Abilities.Earth.LavaSurge.Cooldown", 4000);
		c.addDefault("Abilities.Earth.LavaSurge.Damage", 0.5);
		c.addDefault("Abilities.Earth.LavaSurge.Speed", 1.14);
		c.addDefault("Abilities.Earth.LavaSurge.SelectRange", 5);
		c.addDefault("Abilities.Earth.LavaSurge.SourceRadius", 3);
		c.addDefault("Abilities.Earth.LavaSurge.MaxBlocks", 10);
		c.addDefault("Abilities.Earth.LavaSurge.Burn.Enabled", true);
		c.addDefault("Abilities.Earth.LavaSurge.Burn.Duration", 3000);

		// MagmaSlap
		c.addDefault("Abilities.Earth.MagmaSlap.Enabled", true);
		c.addDefault("Abilities.Earth.MagmaSlap.Cooldown", 4000);
		c.addDefault("Abilities.Earth.MagmaSlap.Offset", 1.5);
		c.addDefault("Abilities.Earth.MagmaSlap.Damage", 2);
		c.addDefault("Abilities.Earth.MagmaSlap.Length", 13);
		c.addDefault("Abilities.Earth.MagmaSlap.Width", 1);
		c.addDefault("Abilities.Earth.MagmaSlap.RevertTime", 7000);

		// QuickWeld
		c.addDefault("Abilities.Earth.QuickWeld.Enabled", true);
		c.addDefault("Abilities.Earth.QuickWeld.Cooldown", 1000);
		c.addDefault("Abilities.Earth.QuickWeld.RepairAmount", 25);
		c.addDefault("Abilities.Earth.QuickWeld.RepairInterval", 1250);
		
		// Shrapnel
		c.addDefault("Abilities.Earth.Shrapnel.Enabled", true);
		c.addDefault("Abilities.Earth.Shrapnel.Shot.Cooldown", 2000);
		c.addDefault("Abilities.Earth.Shrapnel.Shot.Damage", 2);
		c.addDefault("Abilities.Earth.Shrapnel.Shot.Speed", 2.3);
		c.addDefault("Abilities.Earth.Shrapnel.Blast.Cooldown", 8000);
		c.addDefault("Abilities.Earth.Shrapnel.Blast.Shots", 9);
		c.addDefault("Abilities.Earth.Shrapnel.Blast.Spread", 24);
		c.addDefault("Abilities.Earth.Shrapnel.Blast.Speed", 1.7);
		
		// RockSlide
		c.addDefault("Combos.Earth.RockSlide.Enabled", true);
		c.addDefault("Combos.Earth.RockSlide.Cooldown", 7000);
		c.addDefault("Combos.Earth.RockSlide.Damage", 1);
		c.addDefault("Combos.Earth.RockSlide.Knockback", 0.9);
		c.addDefault("Combos.Earth.RockSlide.Knockup", 0.4);
		c.addDefault("Combos.Earth.RockSlide.Speed", 0.68);
		c.addDefault("Combos.Earth.RockSlide.RequiredRockCount", 6);
		c.addDefault("Combos.Earth.RockSlide.TurningSpeed", 0.086);
		c.addDefault("Combos.Earth.RockSlide.Duration", -1);
		
		// ---- Firebending ----
		// ArcSpark
		c.addDefault("Abilities.Fire.ArcSpark.Enabled", true);
		c.addDefault("Abilities.Fire.ArcSpark.Speed", 6);
		c.addDefault("Abilities.Fire.ArcSpark.Length", 7);
		c.addDefault("Abilities.Fire.ArcSpark.Damage", 1);
		c.addDefault("Abilities.Fire.ArcSpark.Cooldown", 6500);
		c.addDefault("Abilities.Fire.ArcSpark.Duration", 4000);
		c.addDefault("Abilities.Fire.ArcSpark.ChargeTime", 500);
		
		// CombustBeam
		c.addDefault("Abilities.Fire.CombustBeam.Enabled", true);
		c.addDefault("Abilities.Fire.CombustBeam.Range", 50);
		c.addDefault("Abilities.Fire.CombustBeam.Cooldown", 5000);
		c.addDefault("Abilities.Fire.CombustBeam.Minimum.Power", 0.6);
		c.addDefault("Abilities.Fire.CombustBeam.Minimum.Angle", 0.2);
		c.addDefault("Abilities.Fire.CombustBeam.Minimum.ChargeTime", 1000);
		c.addDefault("Abilities.Fire.CombustBeam.Minimum.Damage", 2);
		c.addDefault("Abilities.Fire.CombustBeam.Maximum.Power", 2.7);
		c.addDefault("Abilities.Fire.CombustBeam.Maximum.Angle", 40);
		c.addDefault("Abilities.Fire.CombustBeam.Maximum.ChargeTime", 5000);
		c.addDefault("Abilities.Fire.CombustBeam.Maximum.Damage", 10);
		c.addDefault("Abilities.Fire.CombustBeam.InterruptedDamage", 10);
		c.addDefault("Abilities.Fire.CombustBeam.RevertTime", 13000);
		
		// ChargeBolt
		c.addDefault("Abilities.Fire.ChargeBolt.Enabled", true);
		c.addDefault("Abilities.Fire.ChargeBolt.Damage", 2);
		c.addDefault("Abilities.Fire.ChargeBolt.Cooldown", 8000);
		c.addDefault("Abilities.Fire.ChargeBolt.Speed", 6);
		c.addDefault("Abilities.Fire.ChargeBolt.ChargeTime", 3000);
		c.addDefault("Abilities.Fire.ChargeBolt.BoltRange", 26);
		c.addDefault("Abilities.Fire.ChargeBolt.BlastRadius", 13);
		c.addDefault("Abilities.Fire.ChargeBolt.DischargeBoltCount", 6);
		
		// Electrify
		c.addDefault("Abilities.Fire.Electrify.Enabled", true);
		c.addDefault("Abilities.Fire.Electrify.Cooldown", 4000);
		c.addDefault("Abilities.Fire.Electrify.Duration", 7000);
		c.addDefault("Abilities.Fire.Electrify.DamageInWater", 2);
		c.addDefault("Abilities.Fire.Electrify.Slowness", 2);
		c.addDefault("Abilities.Fire.Electrify.Weakness", 1);
		
		// Explode
		c.addDefault("Abilities.Fire.Explode.Enabled", true);
		c.addDefault("Abilities.Fire.Explode.Cooldown", 4500);
		c.addDefault("Abilities.Fire.Explode.Damage", 2);
		c.addDefault("Abilities.Fire.Explode.Radius", 2.4);
		c.addDefault("Abilities.Fire.Explode.Knockback", 1.94);
		c.addDefault("Abilities.Fire.Explode.Range", 7.4);
		
		// FireDisc
		c.addDefault("Abilities.Fire.FireDisc.Enabled", true);
		c.addDefault("Abilities.Fire.FireDisc.Cooldown", 1700);
		c.addDefault("Abilities.Fire.FireDisc.Damage", 1.5);
		c.addDefault("Abilities.Fire.FireDisc.Range", 32);
		c.addDefault("Abilities.Fire.FireDisc.Knockback", 0.84);
		c.addDefault("Abilities.Fire.FireDisc.Controllable", true);
		c.addDefault("Abilities.Fire.FireDisc.RevertCutBlocks", true);
		c.addDefault("Abilities.Fire.FireDisc.DropCutBlocks", false);
		c.addDefault("Abilities.Fire.FireDisc.CuttableBlocks", Arrays.asList("ACACIA_LOG", "OAK_LOG", "JUNGLE_LOG", "BIRCH_LOG", "DARK_OAK_LOG", "SPRUCE_LOG"));

		// Jets
		c.addDefault("Abilities.Fire.Jets.Enabled", true);
		c.addDefault("Abilities.Fire.Jets.Cooldown.Minimum", 4000);
		c.addDefault("Abilities.Fire.Jets.Cooldown.Maximum", 12000);
		c.addDefault("Abilities.Fire.Jets.Duration", 20000);
		c.addDefault("Abilities.Fire.Jets.FlySpeed", 0.65);
		c.addDefault("Abilities.Fire.Jets.HoverSpeed", 0.065);
		c.addDefault("Abilities.Fire.Jets.SpeedThreshold", 2.4);
		c.addDefault("Abilities.Fire.Jets.DamageThreshold", 4);
		c.addDefault("Abilities.Fire.Jets.MaxHeight", -1);
		
		// FlameBreath
		c.addDefault("Combos.Fire.FlameBreath.Enabled", true);
		c.addDefault("Combos.Fire.FlameBreath.Cooldown", 8000);
		c.addDefault("Combos.Fire.FlameBreath.Damage", 0.2);
		c.addDefault("Combos.Fire.FlameBreath.FireTick", 30);
		c.addDefault("Combos.Fire.FlameBreath.Range", 5);
		c.addDefault("Combos.Fire.FlameBreath.Duration", 4000);
		c.addDefault("Combos.Fire.FlameBreath.Knockback", 0.3);
		c.addDefault("Combos.Fire.FlameBreath.Burn.Ground", true);
		c.addDefault("Combos.Fire.FlameBreath.Burn.Entities", true);
		c.addDefault("Combos.Fire.FlameBreath.Rainbow", true);

		// TurboJet
		c.addDefault("Combos.Fire.TurboJet.Enabled", true);
		c.addDefault("Combos.Fire.TurboJet.Cooldown", 12000);
		c.addDefault("Combos.Fire.TurboJet.Speed", 1.95);
		
		// ---- Waterbending ----
		// Hydrojet
		c.addDefault("Passives.Water.Hydrojet.Enabled", true);
		c.addDefault("Passives.Water.Hydrojet.Speed", 8);
		
		// BloodGrip
		/*
		c.addDefault("Abilities.Water.BloodGrip.Enabled", true);
		c.addDefault("Abilities.Water.BloodGrip.Cooldown", 6000);
		c.addDefault("Abilities.Water.BloodGrip.Range", 8);
		c.addDefault("Abilities.Water.BloodGrip.DragSpeed", 0.32);
		c.addDefault("Abilities.Water.BloodGrip.ThrowPower", 1.3);
		c.addDefault("Abilities.Water.BloodGrip.MangleDamage", 3);
		c.addDefault("Abilities.Water.BloodGrip.SlamSpeed", 2);
		c.addDefault("Abilities.Water.BloodGrip.DamageThreshold", 4);
		c.addDefault("Abilities.Water.BloodGrip.EntityFilter", Arrays.asList(EntityType.ENDER_CRYSTAL.toString(), EntityType.ENDER_DRAGON.toString(), EntityType.ARMOR_STAND.toString(), EntityType.BLAZE.toString(), EntityType.WITHER.toString()));
		c.addDefault("Abilities.Water.BloodGrip.BasicAbilities", Arrays.asList("AirBlast", "AirSwipe", "EarthBlast", "FireBlast", "FireDisc", "WaterManipulation"));
		*/

		// RazorLeaf
		c.addDefault("Abilities.Water.RazorLeaf.Enabled", true);
		c.addDefault("Abilities.Water.RazorLeaf.Cooldown", 3000);
		c.addDefault("Abilities.Water.RazorLeaf.Damage", 2);
		c.addDefault("Abilities.Water.RazorLeaf.Radius", 0.7);
		c.addDefault("Abilities.Water.RazorLeaf.Range", 24);
		c.addDefault("Abilities.Water.RazorLeaf.MaxRecalls", 3);
		c.addDefault("Abilities.Water.RazorLeaf.Particles", 300);
		
		// PlantArmor
		c.addDefault("Abilities.Water.PlantArmor.Enabled", true);
		c.addDefault("Abilities.Water.PlantArmor.Cooldown", 10000);
		c.addDefault("Abilities.Water.PlantArmor.Duration", -1);
		c.addDefault("Abilities.Water.PlantArmor.Durability", 2000);
		c.addDefault("Abilities.Water.PlantArmor.SelectRange", 9);
		c.addDefault("Abilities.Water.PlantArmor.RequiredPlants", 14);
		c.addDefault("Abilities.Water.PlantArmor.Boost.Swim", 3);
		c.addDefault("Abilities.Water.PlantArmor.Boost.Speed", 2);
		c.addDefault("Abilities.Water.PlantArmor.Boost.Jump", 2);
		
		// PlantArmor - VineWhip
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.VineWhip.Cost", 50);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.VineWhip.Cooldown", 2000);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.VineWhip.Damage", 2);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.VineWhip.Range", 18);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.VineWhip.Speed", 3);
		
		// PlantArmor - RazorLeaf
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.RazorLeaf.Cost", 150);
		
		// PlantArmor - LeafShield
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.LeafShield.Cost", 100);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.LeafShield.Cooldown", 1500);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.LeafShield.Radius", 2);
		
		// PlantArmor - Tangle
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Tangle.Cost", 200);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Tangle.Cooldown", 7000);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Tangle.Radius", 0.45);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Tangle.Duration", 3000);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Tangle.Range", 18);
		
		// PlantArmor - Leap
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Leap.Cost", 100);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Leap.Cooldown", 2500);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Leap.Power", 1.4);
		
		// PlantArmor - Grapple
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Grapple.Cost", 100);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Grapple.Cooldown", 2000);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Grapple.Range", 25);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Grapple.Speed", 1.24);
		
		// PlantArmor - LeafDome
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.LeafDome.Cost", 400);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.LeafDome.Cooldown", 5000);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.LeafDome.Radius", 3);
		
		// PlantArmor - Regenerate
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Regenerate.Cooldown", 10000);
		c.addDefault("Abilities.Water.PlantArmor.SubAbilities.Regenerate.RegenAmount", 150);
		
		// LeafStorm
		c.addDefault("Combos.Water.LeafStorm.Enabled", true);
		c.addDefault("Combos.Water.LeafStorm.Cooldown", 7000);
		c.addDefault("Combos.Water.LeafStorm.PlantArmorCost", 800);
		c.addDefault("Combos.Water.LeafStorm.LeafCount", 10);
		c.addDefault("Combos.Water.LeafStorm.LeafSpeed", 14);
		c.addDefault("Combos.Water.LeafStorm.Damage", 0.5);
		c.addDefault("Combos.Water.LeafStorm.Radius", 6);
		
		// MistShards
		c.addDefault("Combos.Water.MistShards.Enabled", true);
		c.addDefault("Combos.Water.MistShards.Cooldown", 7000);
		c.addDefault("Combos.Water.MistShards.Damage", 1);
		c.addDefault("Combos.Water.MistShards.Range", 20);
		c.addDefault("Combos.Water.MistShards.IcicleCount", 8);
		
		// ---- Chiblocking ----
		// Dodging
		c.addDefault("Passives.Chi.Dodging.Enabled", true);
		c.addDefault("Passives.Chi.Dodging.Chance", 18);
		
		// Camouflage
		c.addDefault("Passives.Chi.Camouflage.Enabled", true);
		
		// Jab
		c.addDefault("Abilities.Chi.Jab.Enabled", true);
		c.addDefault("Abilities.Chi.Jab.Cooldown", 3000);
		c.addDefault("Abilities.Chi.Jab.MaxUses", 4);
		
		// NinjaStance
		c.addDefault("Abilities.Chi.NinjaStance.Enabled", true);
		c.addDefault("Abilities.Chi.NinjaStance.Cooldown", 0);
		c.addDefault("Abilities.Chi.NinjaStance.Stealth.Duration", 5000);
		c.addDefault("Abilities.Chi.NinjaStance.Stealth.ChargeTime", 2000);
		c.addDefault("Abilities.Chi.NinjaStance.Stealth.Cooldown", 8000);
		c.addDefault("Abilities.Chi.NinjaStance.SpeedAmplifier", 5);
		c.addDefault("Abilities.Chi.NinjaStance.JumpAmplifier", 5);
		c.addDefault("Abilities.Chi.NinjaStance.DamageModifier", 0.75);
		
		// ChiblockJab
		c.addDefault("Combos.Chi.ChiblockJab.Enabled", true);
		c.addDefault("Combos.Chi.ChiblockJab.Cooldown", 5000);
		c.addDefault("Combos.Chi.ChiblockJab.Duration", 2000);
		
		// FlyingKick
		c.addDefault("Combos.Chi.FlyingKick.Enabled", true);
		c.addDefault("Combos.Chi.FlyingKick.Cooldown", 4000);
		c.addDefault("Combos.Chi.FlyingKick.Damage", 2.0);
		c.addDefault("Combos.Chi.FlyingKick.LaunchPower", 1.85);
		
		// WeakeningJab
		c.addDefault("Combos.Chi.WeakeningJab.Enabled", true);
		c.addDefault("Combos.Chi.WeakeningJab.Cooldown", 6000);
		c.addDefault("Combos.Chi.WeakeningJab.Duration", 4000);
		c.addDefault("Combos.Chi.WeakeningJab.Modifier", 1.5);
		
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
