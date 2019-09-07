package me.simplicitee.projectaddons;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.CoreAbility;

public class ProjectCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ProjectAddons.instance.version());
			sender.sendMessage(ChatColor.GREEN + (ChatColor.BOLD + "/projectaddons bendingboard"));
			return true;
		} else if (args.length == 1 && sender instanceof Player) {
			if (args[0].equalsIgnoreCase("bendingboard") && ProjectAddons.instance.isBoardEnabled()) {
				if (ProjectAddons.instance.getBoardManager().toggleDisabled(((Player) sender))) {
					sender.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RESET + " BendingBoard: " + ChatColor.RED + "DISABLED");
				} else {
					sender.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RESET + " BendingBoard: " + ChatColor.GREEN + "ENABLED");
				}
			} else if (args[0].equalsIgnoreCase("active")) {
				for (CoreAbility ability : CoreAbility.getAbilitiesByInstances()) {
					if (ability.getPlayer().getUniqueId() == ((Player) sender).getUniqueId()) {
						sender.sendMessage(ability.getElement().getColor() + ability.getName() + ChatColor.WHITE + " : " + (System.currentTimeMillis() - ability.getStartTime()));
					}
				}
			}
			return true;
		}
		
		return false;
	}

}
