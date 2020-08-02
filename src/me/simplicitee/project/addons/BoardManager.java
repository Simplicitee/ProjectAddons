package me.simplicitee.project.addons;

import java.util.HashMap;
import java.util.HashSet;
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

public class BoardManager {
	
	private ProjectAddons plugin;
	private Set<UUID> disabled;
	private Map<UUID, Scoreboard> boards;
	private String empty;
	private String indicator;

	public BoardManager(ProjectAddons plugin) {
		this.plugin = plugin;
		this.disabled = new HashSet<>();
		this.boards = new HashMap<>();
		this.empty = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Properties.BendingBoard.EmptySlot"));
		this.indicator = plugin.getConfig().getString("Properties.BendingBoard.IndicatorMode");
		if (!indicator.equalsIgnoreCase("bold") && !indicator.equalsIgnoreCase("arrow")) {
			indicator = "bold";
		}
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				this.update(player);
			}
		}, 60);
	}
	
	public void disable() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
		}
		
		disabled.clear();
		boards.clear();
	}
	
	public void remove(Player player) {
		if (boards.containsKey(player.getUniqueId())) {
			player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
			boards.remove(player.getUniqueId());
		}
	}
	
	public void update(Player player) {
		this.update(player, player.getInventory().getHeldItemSlot());
	}
	
	public void update(Player player, int newSlot) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
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
		Map<Integer, String> scores = new HashMap<>();
		Objective bendingboard = scoreboard.getObjective("projectaddons");
		
		if (bendingboard == null) {
			bendingboard = scoreboard.registerNewObjective("projectaddons", "dummy", ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Properties.BendingBoard.Title")));
			bendingboard.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		
		for (String s : scoreboard.getEntries()) {
			scores.put(bendingboard.getScore(s).getScore(), s);
		}
		
		for (int i = 1; i < 10; i++) {
			String color = "";
			String prefix = indicator.equalsIgnoreCase("arrow") ? (ChatColor.BLACK + ">") : "";
			String format = "";
			String name = empty.replace("%d", i + "");
			
			if (newSlot == (i - 1)) {
				if (indicator.equalsIgnoreCase("arrow")) {
					prefix = ChatColor.WHITE + ">";
				} else {
					format = ChatColor.BOLD + "";
				}
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
						format = ChatColor.STRIKETHROUGH + format;
					}
				} else if (MultiAbilityManager.hasMultiAbilityBound(player)){
					MultiAbilityInfo info = MultiAbilityManager.getMultiAbility(MultiAbilityManager.getBoundMultiAbility(player));
					
					if (i - 1 < info.getAbilities().size()) {
						MultiAbilityInfoSub sub = info.getAbilities().get(i - 1);
						color = sub.getAbilityColor() + "";
						name = sub.getName();
						
						if (bPlayer.isOnCooldown(sub.getName())) {
							format = ChatColor.STRIKETHROUGH + format;
						}
					}
				}
			}
			
			if (name.length() > 16) {
				name = name.substring(0, 15);
			}
			
			String score = "§" + i + "§r" + prefix + color + format + name;
			
			if (!scores.containsKey(-i)) {
				bendingboard.getScore(score).setScore(-i);
			} else if (!scores.get(-i).equalsIgnoreCase(score)) {
				scoreboard.resetScores(scores.get(-i));
				bendingboard.getScore(score).setScore(-i);
			}
		}
		
		boolean show = false;
		int tracker = -11;
		for (int i = tracker; i >= -(scores.size() + 5); i--) {
			if (!scores.containsKey(i)) {
				continue;
			} else {
				scoreboard.resetScores(scores.get(i));
			}
		}
		for (String cooldown : bPlayer.getCooldowns().keySet()) {
			CoreAbility ability = CoreAbility.getAbility(cooldown);
			
			if (ability != null && ability instanceof ComboAbility) {
				String s = ability.getElement().getColor() + (ChatColor.STRIKETHROUGH + ability.getName());
				
				bendingboard.getScore(s).setScore(tracker);
				
				show = true;
				
				tracker--;
			}
		}
		
		if (show) {
			if (!scores.containsKey(-10)) {
				bendingboard.getScore("-- Combos --").setScore(-10);
			}
		} else {
			scoreboard.resetScores("-- Combos --");
		}
	
		player.setScoreboard(scoreboard);
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
