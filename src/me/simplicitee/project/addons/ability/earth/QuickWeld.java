package me.simplicitee.project.addons.ability.earth;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;

import me.simplicitee.project.addons.ProjectAddons;

public class QuickWeld extends MetalAbility implements AddonAbility{
	
	private ItemStack item;
	private long cooldown;
	private int repairAmount;
	private long repairCooldown;

	private Material[] metal_tools = new Material[] {
			Material.IRON_AXE, Material.IRON_BOOTS, Material.IRON_CHESTPLATE,
			Material.IRON_HELMET, Material.IRON_HOE, Material.IRON_LEGGINGS,
			Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_SWORD};

	public QuickWeld(Player player, ItemStack item) {
		super(player);
		
		if (item.getDurability() <= -249) {
			return;
		}
		
		if (!Arrays.asList(metal_tools).contains(item.getType())) {
			return;
		}
		
		this.item = item;
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.QuickWeld.Cooldown");
		this.repairAmount = ProjectAddons.instance.getConfig().getInt("Abilities.Earth.QuickWeld.RepairAmount");
		this.repairCooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.QuickWeld.RepairInterval");
		
		start();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "QuickWeld";
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
		if (player == null) {
			remove();
			return;
		}
		
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (bPlayer.isOnCooldown("QuickWeld Interval")) {
			return;
		}
		
		if (player.getInventory().containsAtLeast(new ItemStack(Material.IRON_INGOT), 1)) {
			player.getInventory().removeItem(new ItemStack(Material.IRON_INGOT, 1));
			int val = item.getDurability() - repairAmount;
			if (val < -250) {
				val = -249;
			}
			
			bPlayer.addCooldown("QuickWeld Interval", repairCooldown);
			item.setDurability((short) val);
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1f);
		} else {
			remove();
			return;
		}
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
	public String getDescription() {
		return "Advanced metalbenders can use this to repair damaged iron weapons/armor/tools. This ability requires iron ingots in your inventory to work.";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak with the item you want to repair in your main hand.";
	}

	@Override
	public void stop() {}

	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Earth.QuickWeld.Enabled");
	}
}
