package me.simplicitee.projectaddons;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.Config;

import me.simplicitee.projectaddons.ability.fire.FireDisc;
import me.simplicitee.projectaddons.ability.water.RazorLeaf;

public class ProjectAddons extends JavaPlugin {
	
	public static ProjectAddons instance;
	
	private Config config;
	private Set<String> enabled;
	private BoardManager boards;

	@Override
	public void onEnable() {
		instance = this;
		this.enabled = new HashSet<>();
		
		this.config = new Config(new File("project_addons.yml"));
		this.setupConfig();
		
		CoreAbility.registerPluginAbilities(this, "me.simplicitee.projectaddons.ability");
		String[] abils = {"MagmaSlap", "Shrapnel", "EarthKick", "Jab", "NinjaStance", "FireDisc", "MetalRepair", "Explode", "LavaSurge", "RazorLeaf", "PlantArmor"};
		String[] combos = {"FlameBreath", "WeakeningJab", "FlyingKick", "ChiblockJab"};
		
		for (String abil : abils) {
			if (config.get().getBoolean("Abilities." + abil + ".Enabled")) {
				enabled.add(abil);
			}
		}
		
		for (String combo : combos) {
			if (config.get().getBoolean("Combos." + combo + ".Enabled")) {
				enabled.add(combo);
			}
		}
		
		this.setupPermissions();
		this.setupCollisions();
		
		new MainListener(this);
		
		if (config.get().getBoolean("Properties.BendingBoard.Enabled")) {
			this.boards = new BoardManager(this);
		} else {
			this.boards = null;
		}
		
		this.getCommand("projectaddons").setExecutor(new ProjectCommand());
	}
	
	@Override
	public void onDisable() {
		for (Player player : this.getServer().getOnlinePlayers()) {
			Scoreboard scoreboard = player.getScoreboard();
			scoreboard.clearSlot(DisplaySlot.SIDEBAR);
			
			for (String entry : scoreboard.getEntries()) {
				scoreboard.resetScores(entry);
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
	
	private void setupPermissions() {
		for (String abil : enabled) {
			if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability." + abil.toLowerCase()) == null) {
				Permission perm = new Permission("bending.ability." + abil.toLowerCase());
				perm.setDefault(PermissionDefault.TRUE);
				ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
			}
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.energybeam") == null) {
			Permission perm = new Permission("bending.ability.energybeam");
			perm.setDefault(PermissionDefault.OP);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.firedisc.bluefire") == null) {
			Permission perm = new Permission("bending.ability.firedisc.bluefire");
			perm.setDefault(PermissionDefault.OP);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.plantarmor.vinewhip") == null) {
			Permission perm = new Permission("bending.ability.plantarmor.vinewhip");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.plantarmor.razorleaf") == null) {
			Permission perm = new Permission("bending.ability.plantarmor.razorleaf");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.plantarmor.leafshield") == null) {
			Permission perm = new Permission("bending.ability.plantarmor.leafshield");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.plantarmor.tangle") == null) {
			Permission perm = new Permission("bending.ability.plantarmor.tangle");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.plantarmor.slot5") == null) {
			Permission perm = new Permission("bending.ability.plantarmor.slot5");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.plantarmor.leap") == null) {
			Permission perm = new Permission("bending.ability.plantarmor.leap");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.plantarmor.slot7") == null) {
			Permission perm = new Permission("bending.ability.plantarmor.slot7");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.plantarmor.regenerate") == null) {
			Permission perm = new Permission("bending.ability.plantarmor.regenerate");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.plantarmor.disperse") == null) {
			Permission perm = new Permission("bending.ability.plantarmor.disperse");
			perm.setDefault(PermissionDefault.TRUE);
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
		}
	}
	
	private void setupConfig() {
		FileConfiguration c = config.get();
		
		c.addDefault("Properties.BendingBoard.Enabled", true);
		c.addDefault("Properties.BendingBoard.IntervalTicks", 4);
		c.addDefault("Properties.BendingBoard.Title", "Binds");
		
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
		List<String> logs = new ArrayList<>();
		logs.add("LOG");
		logs.add("LOG_2");
		
		c.addDefault("Abilities.FireDisc.Enabled", true);
		c.addDefault("Abilities.FireDisc.Damage", 2);
		c.addDefault("Abilities.FireDisc.Range", 25);
		c.addDefault("Abilities.FireDisc.Cooldown", 1500);
		c.addDefault("Abilities.FireDisc.Controllable", true);
		c.addDefault("Abilities.FireDisc.RevertCutBlocks", true);
		c.addDefault("Abilities.FireDisc.DropCutBlocks", false);
		c.addDefault("Abilities.FireDisc.CuttableBlocks", logs);
		
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
		
		// MetalRepair
		c.addDefault("Abilities.MetalRepair.Enabled", true);
		c.addDefault("Abilities.MetalRepair.Cooldown", 1000);
		c.addDefault("Abilities.MetalRepair.RepairAmount", 25);
		c.addDefault("Abilities.MetalRepair.RepairInterval", 1250);
		
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
		
		config.save();
	}
	
	private void setupCollisions() {
		if (enabled.contains("FireDisc")) {
			ProjectKorra.getCollisionInitializer().addSmallAbility(CoreAbility.getAbility(FireDisc.class));
		}
		
		if (enabled.contains("RazorLeaf")) {
			ProjectKorra.getCollisionInitializer().addSmallAbility(CoreAbility.getAbility(RazorLeaf.class));
		}
	}
}
