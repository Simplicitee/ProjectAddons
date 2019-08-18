package me.simplicitee.projectaddons;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent.Result;
import com.projectkorra.projectkorra.util.ClickType;

import me.simplicitee.projectaddons.ability.avatar.EnergyBeam;
import me.simplicitee.projectaddons.ability.avatar.EnergyBeam.EnergyColor;
import me.simplicitee.projectaddons.ability.chi.Jab;
import me.simplicitee.projectaddons.ability.chi.Jab.JabHand;
import me.simplicitee.projectaddons.ability.chi.NinjaStance;
import me.simplicitee.projectaddons.ability.chi.WeakeningJab;
import me.simplicitee.projectaddons.ability.earth.EarthKick;
import me.simplicitee.projectaddons.ability.earth.LavaSurge;
import me.simplicitee.projectaddons.ability.earth.MagmaSlap;
import me.simplicitee.projectaddons.ability.earth.MetalRepair;
import me.simplicitee.projectaddons.ability.earth.ShrapnelBlast;
import me.simplicitee.projectaddons.ability.earth.ShrapnelShot;
import me.simplicitee.projectaddons.ability.fire.Explode;
import me.simplicitee.projectaddons.ability.fire.FireDisc;
import me.simplicitee.projectaddons.ability.water.PlantArmor;
import me.simplicitee.projectaddons.ability.water.RazorLeaf;

public class MainListener implements Listener {
	
	private ProjectAddons plugin;

	public MainListener(ProjectAddons plugin) {
		this.plugin = plugin;
		
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onLeftClick(final PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
			return;
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.isCancelled()) {
			return;
		}
		
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer == null) {
			return;
		}
		
		CoreAbility ability = bPlayer.getBoundAbility();
		
		if (ability == null) {
			if (MultiAbilityManager.hasMultiAbilityBound(player)) {
				if (MultiAbilityManager.getBoundMultiAbility(player).equalsIgnoreCase("PlantArmor")) {
					ComboManager.addComboAbility(player, ClickType.LEFT_CLICK);
					new PlantArmor(player, ClickType.LEFT_CLICK);
				}
			}
			
			return;
		}
		
		Element e = ability.getElement() instanceof SubElement ? ((SubElement) ability.getElement()).getParentElement() : ability.getElement();
		
		if (e != Element.AVATAR && !bPlayer.hasElement(ability.getElement())) {
			return;
		}
		
		
		if (canBend(player, "FireDisc")) {
			new FireDisc(player);
		} else if (canBend(player, "MagmaSlap")) {
			new MagmaSlap(player);
		} else if (canBend(player, "Shrapnel")) {
			if (player.isSneaking()) {
				new ShrapnelBlast(player);
			} else {
				new ShrapnelShot(player);
			}
		} else if (canBend(player, "NinjaStance")) {
			new NinjaStance(player);
		} else if (canBend(player, "AcrobatStance") || canBend(player, "WarriorStance")) {
			if (CoreAbility.hasAbility(player, NinjaStance.class)) {
				CoreAbility.getAbility(player, NinjaStance.class).remove();
			}
		} else if (canBend(player, "LavaSurge")) {
			if (CoreAbility.hasAbility(player, LavaSurge.class)) {
				CoreAbility.getAbility(player, LavaSurge.class).shoot();
			}
		} 
		
	}
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (event.isCancelled()) return;
		
		if (!event.isSneaking()) {
			return;
		}
		
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer == null) {
			return;
		}
		
		CoreAbility ability = bPlayer.getBoundAbility();
		
		if (ability == null) {
			if (MultiAbilityManager.hasMultiAbilityBound(player)) {
				if (MultiAbilityManager.getBoundMultiAbility(player).equalsIgnoreCase("PlantArmor")) {
					ComboManager.addComboAbility(player, ClickType.SHIFT_DOWN);
					new PlantArmor(player, ClickType.SHIFT_DOWN);
				}
			}
			
			return;
		}	
		
		Element e = ability.getElement() instanceof SubElement ? ((SubElement) ability.getElement()).getParentElement() : ability.getElement();
		
		if (e != Element.AVATAR && !bPlayer.hasElement(ability.getElement())) {
			return;
		}
		
		if (canBend(player, "EarthKick")) {
			new EarthKick(player);
		} else if (canBend(player, "NinjaStance")) {
			if (CoreAbility.hasAbility(player, NinjaStance.class)) {
				CoreAbility.getAbility(player, NinjaStance.class).beginStealth();
			}
		} else if (canBend(player, "EnergyBeam")) {
			new EnergyBeam(player);
		} else if (canBend(player, "Explode")) {
			new Explode(player);
		} else if (canBend(player, "LavaSurge")) {
			new LavaSurge(player);
		} else if (canBend(player, "MetalRepair")) {
			new MetalRepair(player, player.getInventory().getItemInMainHand());
		} else if (canBend(player, "RazorLeaf")) {
			new RazorLeaf(player, true);
		} else if (ability.getName().equals("PlantArmor")) {
			new PlantArmor(player, ClickType.SHIFT_DOWN);
		}
	}
	
	@EventHandler
	public void onItemMerge(ItemMergeEvent event) {
		if (event.getEntity().hasMetadata("shrapnel")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemPickup(EntityPickupItemEvent event) {
		if (event.getItem().hasMetadata("shrapnel")) {
			event.getItem().removeMetadata("shrapnel", ProjectKorra.plugin);
		}
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		Entity e = event.getEntity();
		
		if (e instanceof FallingBlock) {
			FallingBlock fb = (FallingBlock)e;
			
			if (EarthKick.isBlock(fb)) {
				event.setCancelled(true);
				EarthKick.removeBlock(fb);
			} else if (MagmaSlap.isBlock(fb)) {
				event.setCancelled(true);
				((MagmaSlap) fb.getMetadata("lavaflux").get(0).value()).turnToTempBlock(event.getBlock());
			} else if (fb.hasMetadata("earthring")) {
				event.setCancelled(true);
			} else if (fb.hasMetadata("lavasurge")) {
				event.setCancelled(true);
				((LavaSurge) fb.getMetadata("lavasurge").get(0).value()).removeBlock(fb);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		
		Entity entity = event.getEntity();
		
		if (entity instanceof Player) {
			Player player = (Player) entity;
			
			if (CoreAbility.hasAbility(player, NinjaStance.class)) {
				NinjaStance ninja = CoreAbility.getAbility(player, NinjaStance.class);
				if (ninja.stealth && ninja.stealthReady && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					ninja.stopStealth();
				}
			}
			
			if (MultiAbilityManager.hasMultiAbilityBound(player, "PlantArmor")) {
				if (event.getCause() == DamageCause.FALL) {
					event.setCancelled(true);
					CoreAbility.getAbility(player, PlantArmor.class).damage((int) event.getDamage() * 10);
				} else if (event.getCause() == DamageCause.DROWNING) {
					event.setCancelled(true);
					CoreAbility.getAbility(player, PlantArmor.class).damage((int) event.getDamage() * 5);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onHitDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) return;
		if (!(event.getDamager() instanceof Player)) return;
		
		Player player = (Player) event.getDamager();
		Entity entity = event.getEntity();
		
		if (WeakeningJab.isAffected(entity)) {
			event.setDamage(event.getDamage() * WeakeningJab.getModifier());
			if (entity instanceof LivingEntity) {
				((LivingEntity) entity).setNoDamageTicks(5);
			}
		}
		
		if (event.getCause() != DamageCause.ENTITY_ATTACK) return;
		if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType())) return;
		
		if (CoreAbility.hasAbility(player, NinjaStance.class)) {
			NinjaStance ninja = CoreAbility.getAbility(player, NinjaStance.class);
			if (ninja.stealth && ninja.stealthReady && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				ninja.stopStealth();
			}
			
			event.setDamage(event.getDamage() * NinjaStance.getDamageModifier());
		}
		
		if (canBend(player, "Jab")) {
			if (CoreAbility.hasAbility(player, Jab.class)) {
				Jab jab = CoreAbility.getAbility(player, Jab.class);
				jab.activate(entity, JabHand.RIGHT);
			} else {
				new Jab(player, entity, JabHand.RIGHT);
			}
		}
	}

	@EventHandler
	public void onRightClickEntity(PlayerInteractEntityEvent event) {
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		
		if (canBend(player, "Jab")) {
			if (CoreAbility.hasAbility(player, Jab.class)) {
				Jab jab = CoreAbility.getAbility(player, Jab.class);
				jab.activate(entity, JabHand.LEFT);
			} else {
				new Jab(player, entity, JabHand.LEFT);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Player player = event.getPlayer();
		String[] args = event.getMessage().split(" ");
		if (args.length != 2) {
			return;
		}
		
		if (args[0].equalsIgnoreCase("@energycolor")) {
			if (player.hasPermission("bending.ability.energybeam")) {
				EnergyColor color = EnergyColor.valueOf(args[1].toUpperCase());
				if (color != null) {
					EnergyBeam.colors.put(player.getUniqueId(), color);
					player.sendMessage(ChatColor.GREEN + "Successfully set EnergyBeam color to " + args[1].toLowerCase());	
				} else {
					player.sendMessage(ChatColor.RED + "Unknown color! Try red, blue, yellow, green, purple, orange, indigo, brown, white, or black!");
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onAbilityChange(PlayerBindChangeEvent event) {
		if (event.isCancelled() || !plugin.isBoardEnabled()) {
			return;
		}
		
		Player player = event.getPlayer();
		plugin.getBoardManager().update(player, BendingPlayer.getBendingPlayer(player));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onCooldown(PlayerCooldownChangeEvent event) {
		if (event.isCancelled() || !plugin.isBoardEnabled()) {
			return;
		}
		
		if (event.getResult() == Result.REMOVED) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				plugin.getBoardManager().setCooldown(event.getPlayer(), event.getAbility(), false);
			}, 15);
		} else {
			plugin.getBoardManager().setCooldown(event.getPlayer(), event.getAbility(), true);
		}
	}

	private boolean canBend(Player player, String ability) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		CoreAbility abil = CoreAbility.getAbility(ability);
		
		if (!bPlayer.getBoundAbilityName().equals(ability)) {
			return false;
		} else if (!bPlayer.canBend(abil)) {
			return false;
		}
		
		return true;
	}
}
