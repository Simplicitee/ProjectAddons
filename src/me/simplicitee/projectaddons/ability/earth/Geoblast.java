package me.simplicitee.projectaddons.ability.earth;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.projectaddons.ProjectAddons;

public class Geoblast extends EarthAbility implements AddonAbility {
	
	private Location loc;
	private TempBlock source;
	private Material type;
	private long cooldown;
	private double damage;
	private int range;
	private boolean thrown, setup;
	private Vector direction;
	private Location target, first;

	public Geoblast(Player player) {
		super(player);
		
		Geoblast blast = getAbility(player, Geoblast.class);
		
		if (blast != null) {
			blast.refocus();
			return;
		}
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (setFields()) {
			start();
		}
	}
	
	private boolean setFields() {
		Block b = BlockSource.getEarthSourceBlock(player, 7, ClickType.SHIFT_DOWN);
		
		if (b == null) {
			return false;
		}
		
		this.type = getAnimationType(b.getType());
		this.source = new TempBlock(b, getSourceBlockType());
		this.loc = b.getLocation().add(0.5, 0.5, 0.5);
		this.setup = true;
		this.cooldown = 1000; //ProjectAddons.instance.getConfig().getLong("Abilities.Geoblast.Cooldown");
		this.damage = 2; //ProjectAddons.instance.getConfig().getDouble("Abilities.Geoblast.Damage");
		this.range = 30; //ProjectAddons.instance.getConfig().getInt("Abilities.Geoblast.Range");
		this.thrown = false;
		return true;
	}
	
	private Material getAnimationType(Material type) {
		switch (type) {
			case FARMLAND:
			case MYCELIUM:
			case DIRT:
			case COARSE_DIRT:
			case GRASS_BLOCK: return Material.COARSE_DIRT;
			case STONE:
			case COBBLESTONE:
			case IRON_ORE:
			case COAL_ORE:
			case GRAVEL:
			case ANDESITE:
			case GRANITE:
			case DIORITE:
			case GOLD_ORE: return Material.COBBLESTONE;
			case SAND:
			case SANDSTONE: return Material.SANDSTONE;
			case RED_SAND:
			case RED_SANDSTONE: return Material.RED_SANDSTONE;
			default: return type;
		}
	}
	
	private Material getSourceBlockType() {
		return type == Material.OBSIDIAN ? Material.COBBLESTONE : Material.OBSIDIAN;
	}
	
	public void refocus() {
		if (thrown) {
			return;
		}
		
		Block b = BlockSource.getEarthSourceBlock(player, 7, ClickType.SHIFT_DOWN);
		
		if (b == null) {
			return;
		}
		
		source.revertBlock();
		this.type = b.getType();
		this.loc = b.getLocation().add(0.5, 0.5, 0.5);
		this.source = new TempBlock(b, getSourceBlockType());
	}
	
	public void launch() {
		if (!thrown) {
			this.thrown = true;
			this.source.setType(Material.AIR);
			bPlayer.addCooldown(this);
		}
		locateTarget();
	}
	
	private void pathing() {
		if (loc.getBlockY() == first.getBlockY()) {
			setup = false;
		} 
		
		if (setup) {
			direction = GeneralMethods.getDirection(this.loc, this.first).normalize();
		} else {
			direction = GeneralMethods.getDirection(this.loc, this.target).normalize();
		}
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (thrown) {
			if (loc.distance(player.getEyeLocation()) > range) {
				remove();
				return;
			}
			
			pathing();
			
			if (loc.getBlock().isLiquid() && direction.length() != 0.8) {
				direction.multiply(0.8);
			}
			
			loc.add(direction);
			
			if (!loc.getBlock().isPassable()) {
				remove();
				return;
			} else if (GeneralMethods.isTransparent(loc.getBlock()) && !loc.getBlock().isLiquid()) {
				GeneralMethods.breakBlock(loc.getBlock());
			}
			
			new TempBlock(loc.getBlock(), type).setRevertTime(60);
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 1)) {
				if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
					DamageHandler.damageEntity(e, damage, this);
					remove();
					return;
				}
			}
		} else {
			if (!source.getLocation().getWorld().equals(player.getLocation().getWorld())) {
				remove();
				return;
			}
			
			if (source.getLocation().distance(player.getLocation()) > range) {
				remove();
				return;
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		source.setRevertTime(10000);
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "Geoblast";
	}

	@Override
	public Location getLocation() {
		return loc;
	}

	@Override
	public void load() {}

	@Override
	public void stop() {}

	@Override
	public String getAuthor() {
		return "Simplicitee";
	}

	@Override
	public String getVersion() {
		return ProjectAddons.instance.version();
	}
	
	@Override
	public boolean isEnabled() {
		return false;
	}

	private void locateTarget() {
		final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range, new ArrayList<Entity>());
		final Material[] trans = new Material[getTransparentMaterials().length + this.getEarthbendableBlocks().size()];
		int i = 0;
		for (int j = 0; j < getTransparentMaterials().length; j++) {
			trans[j] = getTransparentMaterials()[j];
			i++;
		}
		for (int j = 0; j < this.getEarthbendableBlocks().size(); j++) {
			try {
				trans[i] = Material.valueOf(this.getEarthbendableBlocks().get(j));
			} catch (final IllegalArgumentException e) {
				continue;
			}
			i++;
		}

		if (target == null) {
			this.target = GeneralMethods.getTargetedLocation(this.player, this.range, true, trans);
		} else {
			this.target = ((LivingEntity) target).getEyeLocation();
		}
		
		this.first = this.loc.clone();
		if (this.target.getY() - this.loc.getY() > 2) {
			this.first.setY(this.target.getY() - 1);
		} else if (this.loc.getY() > player.getEyeLocation().getY() && this.loc.getBlock().getRelative(BlockFace.UP).isPassable()) {
			this.first.subtract(0, 2, 0);
		} else if (this.loc.getBlock().getRelative(BlockFace.UP).isPassable() && this.loc.getBlock().getRelative(BlockFace.UP, 2).isPassable()) {
			this.first.add(0, 2, 0);
		} else {
			this.first.add(GeneralMethods.getDirection(this.loc, this.target).normalize().setY(0));
		}
	}
}
