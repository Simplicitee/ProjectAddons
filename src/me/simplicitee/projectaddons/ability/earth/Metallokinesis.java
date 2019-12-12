package me.simplicitee.projectaddons.ability.earth;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;

import me.simplicitee.projectaddons.ProjectAddons;

public class Metallokinesis extends MetalAbility implements AddonAbility {
	
	Object target;
	
	private long cooldown;

	public Metallokinesis(Player player) {
		super(player);
	}

	@Override
	public void progress() {
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
		return "Metallokinesis";
	}

	@Override
	public Location getLocation() {
		return null;
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

	private static boolean checkForMetallics(Object o) {
		if (o instanceof Entity) {
			if (((Entity) o).getType() == EntityType.IRON_GOLEM) {
				return true;
			} else if (o instanceof LivingEntity) {
				ItemStack[] armor = ((LivingEntity) o).getEquipment().getArmorContents();
				int metals = 0;
				
				for (ItemStack i : armor) {
					if (checkForMetallics(i)) {
						metals++;
					}
				}
				
				return metals > 1;
			} else if (o instanceof FallingBlock) {
				String m = ((FallingBlock) o).getBlockData().getMaterial().toString();
				return m.contains("IRON") || m.contains("GOLD");
			} else if (o instanceof Item) {
				String m = ((Item) o).getItemStack().getType().toString();
				return m.contains("IRON") || m.contains("GOLD");
			}
		} else if (o instanceof Block) {
			String m = ((Block) o).getBlockData().getMaterial().toString();
			return m.contains("IRON") || m.contains("GOLD");
		} else if (o instanceof ItemStack) {
			String m = ((ItemStack) o).getType().toString();
			return m.contains("IRON") || m.contains("GOLD");
		}
		
		return false;
	}
	
	private enum MetallicType {
		IRON_GOLEM, FALLING_BLOCK, ITEM, ITEM_STACK, BLOCK, ENTITY_WEARING_ARMOR;
	}
	
	public class MetalObject {
		
		Object obj;
		Location location;
		MetallicType type;
		
		private MetalObject(Object o, Location l, MetallicType type) {
			this.obj = o;
			this.location = l;
			this.type = type;
		}
		
		/*
		static MetalObject from(Object obj) {
			if (obj instanceof Entity) {
				if (((Entity) obj).getType() == EntityType.IRON_GOLEM) {
					return new MetalObject(obj, ((Entity) obj).getLocation(), MetallicType.IRON_GOLEM);
				} else if (obj instanceof LivingEntity) {
					LivingEntity le = (LivingEntity) obj;
					ItemStack[] armor = le.getEquipment().getArmorContents();
					int metals = 0;
					
					for (ItemStack i : armor) {
						if (checkForMetallics(i)) {
							metals++;
						}
					}
					
					if (metals > 1) {
						return new MetalObject(le, le.getLocation(), MetallicType.ENTITY_WEARING_ARMOR);
					}
				} else if (obj instanceof FallingBlock) {
					FallingBlock fb = (FallingBlock) obj;
					String m = fb.getBlockData().getMaterial().toString();
					if (m.contains("IRON") || m.contains("GOLD")) {
						return new MetalObject(fb, fb.getLocation(), MetallicType.FALLING_BLOCK);
					}
				} else if (obj instanceof Item) {
					Item i = (Item) obj;
					String m = ((Item) obj).getItemStack().getType().toString();
					if (m.contains("IRON") || m.contains("GOLD")) {
						return new MetalObject(i, i.getLocation(), MetallicType.ITEM);
					}
				}
			} else if (obj instanceof Block) {
				Block b = (Block) obj;
				String m = ((Block) obj).getBlockData().getMaterial().toString();
				if (m.contains("IRON") || m.contains("GOLD")) {
					return new MetalObject(b, b.getLocation(), MetallicType.ITEM);
				}
				
			} else if (obj instanceof ItemStack) {
				String m = ((ItemStack) obj).getType().toString();
			}
			
			return null;
		}*/
	}
}
