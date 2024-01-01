package me.simplicitee.project.addons;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ClickType;

import me.simplicitee.project.addons.ability.air.Deafen;
import me.simplicitee.project.addons.ability.air.FlightPassive;
import me.simplicitee.project.addons.ability.air.GaleGust;
import me.simplicitee.project.addons.ability.air.SonicWave;
import me.simplicitee.project.addons.ability.air.VocalMimicry;
import me.simplicitee.project.addons.ability.air.Zephyr;
import me.simplicitee.project.addons.ability.avatar.EnergyBeam;
import me.simplicitee.project.addons.ability.avatar.EnergyBeam.EnergyColor;
import me.simplicitee.project.addons.ability.chi.Dodging;
import me.simplicitee.project.addons.ability.chi.Jab;
import me.simplicitee.project.addons.ability.chi.Jab.JabHand;
import me.simplicitee.project.addons.ability.chi.NinjaStance;
import me.simplicitee.project.addons.ability.chi.WeakeningJab;
import me.simplicitee.project.addons.ability.earth.Accretion;
import me.simplicitee.project.addons.ability.earth.Bulwark;
import me.simplicitee.project.addons.ability.earth.Crumble;
import me.simplicitee.project.addons.ability.earth.Dig;
import me.simplicitee.project.addons.ability.earth.EarthShove;
import me.simplicitee.project.addons.ability.earth.LavaSurge;
import me.simplicitee.project.addons.ability.earth.MagmaSlap;
import me.simplicitee.project.addons.ability.earth.QuickWeld;
import me.simplicitee.project.addons.ability.earth.ShrapnelBlast;
import me.simplicitee.project.addons.ability.earth.ShrapnelShot;
import me.simplicitee.project.addons.ability.fire.ArcSpark;
import me.simplicitee.project.addons.ability.fire.ChargeBolt;
import me.simplicitee.project.addons.ability.fire.CombustBeam;
import me.simplicitee.project.addons.ability.fire.Electrify;
import me.simplicitee.project.addons.ability.fire.Explode;
import me.simplicitee.project.addons.ability.fire.FireDisc;
import me.simplicitee.project.addons.ability.fire.Jets;
import me.simplicitee.project.addons.ability.water.BloodGrip;
import me.simplicitee.project.addons.ability.water.MistShards;
import me.simplicitee.project.addons.ability.water.PlantArmor;
import me.simplicitee.project.addons.ability.water.RazorLeaf;
import me.simplicitee.project.addons.util.BendingPredicate;

public class MainListener implements Listener {
	
	private ProjectAddons plugin;
	private Map<Player, HashMap<Integer, String>> swapped;

	public MainListener(ProjectAddons plugin) {
		this.plugin = plugin;
		this.swapped = new HashMap<>();
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
				String abil = MultiAbilityManager.getBoundMultiAbility(player);
				if (abil.equalsIgnoreCase("PlantArmor")) {
					ComboManager.addComboAbility(player, ClickType.LEFT_CLICK);
					new PlantArmor(player, ClickType.LEFT_CLICK);
				} else if (abil.equalsIgnoreCase("BloodGrip")) {
					ComboManager.addComboAbility(player, ClickType.LEFT_CLICK);
					new BloodGrip(player, false);
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
		} else if (canBend(player, "GaleGust")) {
			new GaleGust(player);
		} else if (canBend(player, "Accretion")) {
			if (CoreAbility.hasAbility(player, Accretion.class)) {
				CoreAbility.getAbility(player, Accretion.class).shoot();
			}
		} else if (canBend(player, "Crumble")) {
			new Crumble(player, ClickType.LEFT_CLICK);
		} else if (canBend(player, "ArcSpark")) {
			if (CoreAbility.hasAbility(player, ArcSpark.class)) {
				CoreAbility.getAbility(player, ArcSpark.class).shoot();
			}
		} else if (canBend(player, "CombustBeam")) {
			if (CoreAbility.hasAbility(player, CombustBeam.class)) {
				CoreAbility.getAbility(player, CombustBeam.class).explode();
			}
		} else if (canBend(player, "Jets")) {
			if (CoreAbility.hasAbility(player, Jets.class)) {
				CoreAbility.getAbility(player, Jets.class).clickFunction();
			} else {
				new Jets(player);
			}
		} else if (canBend(player, "Bulwark")) {
			if (CoreAbility.hasAbility(player, Bulwark.class)) {
				CoreAbility.getAbility(player, Bulwark.class).clickFunction();
			}
		} else if (canBend(player, "SonicWave")) {
			new SonicWave(player);
		} else if (canBend(player, "ChargeBolt")) {
			if (CoreAbility.hasAbility(player, ChargeBolt.class)) {
				CoreAbility.getAbility(player, ChargeBolt.class).bolt();
			}
		} else if (canBend(player, "IceBlast", false)) {
			if (CoreAbility.hasAbility(player, MistShards.class)) {
				CoreAbility.getAbility(player, MistShards.class).clickFunction();
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
		
		if (canBend(player, "EarthShove")) {
			new EarthShove(player);
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
		} else if (canBend(player, "QuickWeld")) {
			new QuickWeld(player, player.getInventory().getItemInMainHand());
		} else if (canBend(player, "RazorLeaf")) {
			new RazorLeaf(player, true);
		} else if (ability.getName().equals("PlantArmor")) {
			new PlantArmor(player, ClickType.SHIFT_DOWN);
		} else if (canBend(player, "Zephyr")) {
			new Zephyr(player);
		} else if (canBend(player, "Dig")) {
			new Dig(player);
		} else if (canBend(player, "Accretion")) {
			new Accretion(player);
		} else if (canBend(player, "Crumble")) {
			new Crumble(player, ClickType.SHIFT_UP);
		} else if (canBend(player, "ArcSpark")) {
			new ArcSpark(player);
		} else if (canBend(player, "CombustBeam")) {
			new CombustBeam(player);
		} else if (canBend(player, "Bulwark")) {
			new Bulwark(player);
		} else if (canBend(player, "VocalMimicry")) {
			new VocalMimicry(player);
		} else if (canBend(player, "Deafen")) {
			new Deafen(player);
		} else if (canBend(player, "ChargeBolt")) {
			new ChargeBolt(player);
		} else if (canBend(player, "BloodGrip")) {
			new BloodGrip(player, true);
		}
	}
	
	@EventHandler
	public void onAbilityStart(AbilityStartEvent event) {
		if (BloodGrip.isBloodbent(event.getAbility().getPlayer())) {
			event.setCancelled(!ProjectAddons.instance.getConfig().getStringList("Abilities.Water.BloodGrip.BasicAbilities").contains(event.getAbility().getName()));
		} else if (CoreAbility.hasAbility(event.getAbility().getPlayer(), FlightPassive.class) && CoreAbility.getAbility(event.getAbility().getPlayer(), FlightPassive.class).isActive()) {
			event.setCancelled(ProjectAddons.instance.getConfig().getStringList("Passives.Air.Flying.AbilityBlacklist").contains(event.getAbility().getName()));
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
			
			if (EarthShove.isBlock(fb)) {
				event.setCancelled(true);
				EarthShove.removeBlock(fb);
			} else if (MagmaSlap.isBlock(fb)) {
				event.setCancelled(true);
				((MagmaSlap) fb.getMetadata("magmaslap").get(0).value()).turnToTempBlock(event.getBlock());
			} else if (fb.hasMetadata("earthring")) {
				event.setCancelled(true);
			} else if (fb.hasMetadata("lavasurge")) {
				event.setCancelled(true);
				((LavaSurge) fb.getMetadata("lavasurge").get(0).value()).removeBlock(fb);
			} else if (fb.hasMetadata("accretion")) {
				event.setCancelled(true);
				((Accretion) fb.getMetadata("accretion").get(0).value()).blockCollision(fb, event.getBlock());
			} else if (fb.hasMetadata("bulwark")) {
				event.setCancelled(true);
				fb.remove();
			} else if (fb.hasMetadata("rockslide")) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		
		Entity entity = event.getEntity();
		
		if (entity instanceof Player) {
			Player player = (Player) entity;
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			
			if (bPlayer == null) {
				return;
			}
			
			if (CoreAbility.hasAbility(player, NinjaStance.class)) {
				CoreAbility.getAbility(player, NinjaStance.class).stopStealth();
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
			
			if (event.getCause() == DamageCause.FLY_INTO_WALL) {
				if (CoreAbility.hasAbility(player, Dig.class)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHitDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) return;
		
		Entity entity = event.getEntity();
		Entity damagerE = event.getDamager();
		
		if (entity instanceof Player) {
			Player player = (Player) event.getEntity();
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			
			Dodging dodge = CoreAbility.getAbility(player, Dodging.class);
				
			if (dodge != null && bPlayer.canBendPassive(dodge) && bPlayer.isElementToggled(Element.CHI)) {
				if (dodge.check()) {
					event.setCancelled(true);
					ActionBar.sendActionBar(ChatColor.LIGHT_PURPLE + "!> " + Element.CHI.getColor() + "Dodged" + ChatColor.LIGHT_PURPLE + " <!", player);
					
					if (damagerE instanceof Player) {
						ActionBar.sendActionBar(ChatColor.LIGHT_PURPLE + "!> " + ChatColor.WHITE + player.getName() + Element.CHI.getColor() + " dodged" + ChatColor.LIGHT_PURPLE + " <!", (Player) damagerE);
					}
					
					return;
				}
			}
		}
		
		if (WeakeningJab.isAffected(entity)) {
			event.setDamage(event.getDamage() * WeakeningJab.getModifier());
			if (entity instanceof LivingEntity) {
				((LivingEntity) entity).setNoDamageTicks(5);
			}
		}
		
		if (damagerE instanceof Player) {
			Player damager = (Player) damagerE;
			
			if (event.getCause() != DamageCause.ENTITY_ATTACK) return;
			if (GeneralMethods.isWeapon(damager.getInventory().getItemInMainHand().getType())) return;
			
			if (CoreAbility.hasAbility(damager, NinjaStance.class)) {
				CoreAbility.getAbility(damager, NinjaStance.class).stopStealth();
				event.setDamage(event.getDamage() * NinjaStance.getDamageModifier());
			}
			
			if (canBend(damager, "Jab")) {
				if (CoreAbility.hasAbility(damager, Jab.class)) {
					Jab jab = CoreAbility.getAbility(damager, Jab.class);
					jab.activate(entity, JabHand.RIGHT);
				} else {
					new Jab(damager, entity, JabHand.RIGHT);
				}
			}
		}
	}
	
	@EventHandler
	public void onRightClickBlock(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null) {
			return;
		}
		
		Player player = event.getPlayer();
		
		if (canBend(player, "Electrify")) {
			new Electrify(player, event.getClickedBlock(), true);
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
		
		if (args[0].equalsIgnoreCase("@energycolor")) {
			if (!player.hasPermission("bending.ability.energybeam")) {
				player.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RED + " You do not have permission to change color");
			} else if (args.length != 2) {
				player.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RED + " Invalid format, try `@energycolor <color>`");
			} else {
				EnergyColor color = EnergyColor.valueOf(args[1].toUpperCase());
				
				if (color != null) {
					EnergyBeam.colors.put(player.getUniqueId(), color);
					player.sendMessage(ProjectAddons.instance.prefix() + ChatColor.GREEN + " Successfully set EnergyBeam color to " + args[1].toLowerCase());	
				} else {
					player.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RED + " Unknown color! Try red, blue, yellow, green, purple, orange, indigo, brown, white, or black!");
				}
			}
			event.setCancelled(true);
		} else if (args[0].equalsIgnoreCase("@vocalsound")) {
			if (!player.hasPermission("bending.ability.VocalMimicry")) {
				player.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RED + " You do not have permission to change vocal noise");
			} else if (args.length != 2) {
				player.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RED + " Invalid format, try `@vocalsound <sound>`");
			} else {
				try {
					Sound sound = Sound.valueOf(args[1].toUpperCase());

					if (plugin.getConfig().getStringList("Abilities.Air.VocalMimicry.SoundBlacklist").contains(sound.toString())) {
						player.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RED + " Cannot use that sound!");
					} else {
						VocalMimicry.selectSound(player, sound);
						player.sendMessage(ProjectAddons.instance.prefix() + ChatColor.GREEN + " Successfully set vocal sound to " + args[1].toLowerCase());
					}
				} catch (IllegalArgumentException e) {
					player.sendMessage(ProjectAddons.instance.prefix() + ChatColor.RED + " Unknown sound!");
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onReload(BendingReloadEvent event) {
		ProjectAddons.instance.config().reload();
		event.getSender().sendMessage(ProjectAddons.instance.prefix() + " Config reloaded");
		
		new BukkitRunnable() {
			@Override
			public void run() {
				CoreAbility.registerPluginAbilities(plugin, "me.simplicitee.project.addons.ability");
			}
		}.runTaskLater(plugin, 1);
	}
	
	@EventHandler
	public void onToggleGlide(EntityToggleGlideEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		
		Player player = (Player) event.getEntity();
		CoreAbility dig = CoreAbility.getAbility(player, Dig.class);
		
		if (dig != null && dig.isStarted()) {
			if (!event.isGliding()) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onFlightToggle(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();
		if (!CoreAbility.hasAbility(player, FlightPassive.class)) {
			return;
		}
		
		FlightPassive passive = CoreAbility.getAbility(player, FlightPassive.class);
		
		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
			return;
		} else if (event.isFlying()) {
			for (ItemStack is : player.getInventory().getContents()) {
				if (is != null) {
					event.setCancelled(true);
					return;
				}
			}
		}
		
		passive.fly(event.isFlying());
	}
	
	@EventHandler
	public void onOffhandToggle(PlayerSwapHandItemsEvent event) {
		if (event.isCancelled() || event.getMainHandItem().getType() != Material.AIR || event.getOffHandItem().getType() != Material.AIR) {
			return;
		}
		
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer == null) { 
			return;
		}
		
		if (CoreAbility.hasAbility(player, FlightPassive.class)) {
			FlightPassive passive = CoreAbility.getAbility(player, FlightPassive.class);
			if (passive.isActive()) {
				passive.toggleGlide();
				return;
			}
		}
		
		if (player.hasPermission("bending.offhandswap")) {
			if (MultiAbilityManager.hasMultiAbilityBound(player)) {
				return;
			}
			
			if (swapped.containsKey(player)) {
				bPlayer.setAbilities(swapped.get(player));
				swapped.remove(player);
				ActionBar.sendActionBar(ChatColor.YELLOW + "Swapped to original binds", player);
				BendingBoardManager.updateAllSlots(player);
			} else if (Preset.presetExists(player, "offhand_swap")) {
				swapped.put(player, new HashMap<>(bPlayer.getAbilities()));
				Preset.bindPreset(player, Preset.getPreset(player, "offhand_swap"));
				ActionBar.sendActionBar(ChatColor.YELLOW + "Swapped to offhand preset", player);
				BendingBoardManager.updateAllSlots(player);
			}
		}
	}
	
	public void revertSwappedBinds() {
		for (Player player : swapped.keySet()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			
			if (bPlayer == null) {
				continue;
			}
			
			bPlayer.setAbilities(swapped.get(player));
		}
		swapped.clear();
	}
	
	private boolean canBend(Player player, String ability) {
		return canBend(player, ability, true);
	}

	private boolean canBend(Player player, String ability, boolean canbend) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		CoreAbility abil = CoreAbility.getAbility(ability);
		
		if (canbend && !bPlayer.canBend(abil)) {
			return false;
		}
		
		return BendingPredicate.canBend(bPlayer, abil);
	}
}
