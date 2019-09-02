package me.simplicitee.projectaddons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfo;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.Cooldown;

public class BoardManager {
	
	private ProjectAddons plugin;
	private Set<UUID> disabled;
	private Map<UUID, List<String>> cooldown;
	private Map<UUID, Integer> slots;
	private Map<UUID, Scoreboard> boards;

	public BoardManager(ProjectAddons plugin) {
		this.plugin = plugin;
		this.disabled = new HashSet<>();
		this.slots = new HashMap<>();
		this.cooldown = new HashMap<>();
		this.boards = new HashMap<>();
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				
				this.update(player, bPlayer);
			}
		}, 60);
		
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				if (ConfigManager.getConfig().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
					continue;
				} else if (disabled.contains(player.getUniqueId())) {
					continue;
				}
				
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				
				if (bPlayer == null) {
					continue;
				} else if (bPlayer.getElements().isEmpty()) {
					continue;
				}
				
				if (cooldown.containsKey(player.getUniqueId())) {
					this.update(player, bPlayer);
				} else if (!slots.containsKey(player.getUniqueId())) {
					this.update(player, bPlayer);
				} else if (slots.get(player.getUniqueId()) != player.getInventory().getHeldItemSlot()) {
					this.update(player, bPlayer);
				}
			}
		}, 60, plugin.getConfig().getInt("Properties.BendingBoard.IntervalTicks"));
	}
	
	public void disable() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
		}
		
		disabled.clear();
		cooldown.clear();
		slots.clear();
		boards.clear();
	}
	
	public void remove(Player player) {
		if (boards.containsKey(player.getUniqueId())) {
			player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
			boards.remove(player.getUniqueId());
			slots.remove(player.getUniqueId());
		}
	}
	
	public void update(Player player, BendingPlayer bPlayer) {
		if (disabled.contains(player.getUniqueId())) {
			remove(player);
			return;
		} else if (!player.isOnline() || player.isDead()) {
			remove(player);
			return;
		} else if (bPlayer == null) {
			remove(player);
			return;
		} else if (bPlayer.getElements().isEmpty()) {
			remove(player);
			return;
		} else if (bPlayer.isOnCooldown("MAM_setup")) {
			return;
		}
		
		if (!boards.containsKey(player.getUniqueId())) {
			boards.put(player.getUniqueId(), plugin.getServer().getScoreboardManager().getNewScoreboard());
		}
		
		Scoreboard scoreboard = boards.get(player.getUniqueId());
		
		for (String entry : scoreboard.getEntries()) {
			scoreboard.resetScores(entry);
		}
		
		Objective bendingboard = scoreboard.getObjective("projectaddons");
		
		if (bendingboard == null) {
			bendingboard = scoreboard.registerNewObjective("projectaddons", "", "");
			bendingboard.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Properties.BendingBoard.Title")));
		}
		
		bendingboard.setDisplaySlot(DisplaySlot.SIDEBAR);

		Map<String, Cooldown> copy = new HashMap<>(bPlayer.getCooldowns());
		
		for (int i = 1; i < 10; i++) {
			String color = "";
			String format = "%name%";
			String name = "Slot " + i;
			
			if (player.getInventory().getHeldItemSlot() == (i - 1)) {
				format = ChatColor.BOLD + format;
			}
			
			if (bPlayer.getAbilities().containsKey(i)) {
				CoreAbility ability = CoreAbility.getAbility(bPlayer.getAbilities().get(i));
				
				if (ability != null) {
					color = ability.getElement().getColor() + "";
					name = ability.getName();
					
					if (bPlayer.getStance() != null && ability.getName().equals(bPlayer.getStance().getName())) {
						format = ChatColor.UNDERLINE + format;
					}
					
					if (bPlayer.isOnCooldown(ability)) {
						int time = (int) Math.floor((bPlayer.getCooldown(ability.getName()) - System.currentTimeMillis()) / 1000) + 1;
						format = ChatColor.STRIKETHROUGH + format + ChatColor.RESET;
						
						if (time < 100) {
							format += " : " + time + "s";
						}
					}
				} else if (MultiAbilityManager.hasMultiAbilityBound(player)){
					MultiAbilityInfo info = MultiAbilityManager.getMultiAbility(MultiAbilityManager.getBoundMultiAbility(player));
					
					if (i - 1 < info.getAbilities().size()) {
						MultiAbilityInfoSub sub = info.getAbilities().get(i - 1);
						color = sub.getAbilityColor() + "";
						name = sub.getName();
						
						if (bPlayer.isOnCooldown(sub.getName())) {
							int time = (int) Math.floor((bPlayer.getCooldown(sub.getName()) - System.currentTimeMillis()) / 1000) + 1;
							format = ChatColor.STRIKETHROUGH + format + ChatColor.RESET;
							
							if (time < 100) {
								format += " : " + time + "s";
							}
						}
					}
				}
			}
			
			copy.remove(name);
			format = color + format;
			
			if (name.length() > 16) {
				name = name.substring(0, 15);
			}
			
			format = format.replace("%name%", name);
			bendingboard.getScore(format).setScore(-i);
		}
		
		if (!copy.isEmpty()) {
			boolean show = false;
			int tracker = -11;
			
			for (String cooldown : bPlayer.getCooldowns().keySet()) {
				CoreAbility ability = CoreAbility.getAbility(cooldown);
				
				if (ability != null && ability instanceof ComboAbility) {
					int time = (int) Math.floor((bPlayer.getCooldown(cooldown) - System.currentTimeMillis()) / 1000) + 1;
					String format = "%name%";
					
					format += ChatColor.RESET;
					
					if (time < 100) {
						format += " : " + time + "s";
					}
					
					bendingboard.getScore(format.replace("%name%", ability.getElement().getColor() + ability.getName())).setScore(tracker);
					
					show = true;
					
					tracker--;
				}
			}
			
			if (show) {
				bendingboard.getScore("-- Combos --").setScore(-10);
			}
		}
		
		player.setScoreboard(scoreboard);
		this.slots.put(player.getUniqueId(), player.getInventory().getHeldItemSlot());
	}
	
	public void setCooldown(Player player, String ability, boolean cooldown) {
		if (ability == null || ability.equals("")) {
			return;
		}
		
		if (!this.cooldown.containsKey(player.getUniqueId())) {
			this.cooldown.put(player.getUniqueId(), new ArrayList<>());
		}
		
		List<String> active = this.cooldown.get(player.getUniqueId());
		
		if (cooldown) {
			active.add(ability);
		} else {
			active.remove(ability);
			
			if (active.isEmpty()) {
				this.cooldown.remove(player.getUniqueId());
			}
		}
	}
	
	public void setDisabled(Player player, boolean disabled) {
		if (disabled) {
			this.disabled.add(player.getUniqueId());
			Scoreboard scoreboard = player.getScoreboard();
			scoreboard.clearSlot(DisplaySlot.SIDEBAR);
			
			for (String entry : scoreboard.getEntries()) {
				scoreboard.resetScores(entry);
			}
		} else {
			this.disabled.remove(player.getUniqueId());
		}
	}
	
	/**
	 * Toggles whether the player has bending board disabled or not
	 * @param player player to toggle disable bending board for
	 * @return true if disabled, false if enabled
	 */
	public boolean toggleDisabled(Player player) {
		boolean disable = false;
		
		if (disabled.contains(player.getUniqueId())) {
			disabled.remove(player.getUniqueId());
		} else {
			disabled.add(player.getUniqueId());
			disable = true;
		}
		
		return disable;
	}
}
