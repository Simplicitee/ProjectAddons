package me.simplicitee.projectaddons;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProjectCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ProjectAddons.instance.version());
			sender.sendMessage(ChatColor.GREEN + (ChatColor.BOLD + "/projectaddons bendingboard"));
			return true;
		} else if (args.length == 1 && sender instanceof Player && ProjectAddons.instance.isBoardEnabled()) {
			if (ProjectAddons.instance.getBoardManager().toggleDisabled(((Player) sender))) {
				sender.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RESET + " BendingBoard: " + ChatColor.RED + "DISABLED");
			} else {
				sender.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RESET + " BendingBoard: " + ChatColor.GREEN + "ENABLED");
			}
			return true;
		}
		
		return false;
	}

}
