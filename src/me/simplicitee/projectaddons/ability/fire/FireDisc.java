		package me.simplicitee.projectaddons.ability.fire;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.projectaddons.ProjectAddons;

public class FireDisc extends FireAbility implements AddonAbility{
	
	private Location loc;
	private Vector direction;
	private double damage, range, currRange = 0;
	private boolean revert, drop, control, bluefire=false;
	private long cooldown;
	private String[] hexVals = {"3349ff", "3371ff", "33b8ff", "33e6ff", "fddb78", "300cfe", "03d2d2"};
	private Random r = new Random();

	public FireDisc(Player player) {
		super(player);
		
		this.loc = player.getEyeLocation();
		this.direction = player.getLocation().getDirection();
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.FireDisc.Damage");
		this.range = ProjectAddons.instance.getConfig().getDouble("Abilities.FireDisc.Range");
		this.control = ProjectAddons.instance.getConfig().getBoolean("Abilities.FireDisc.Controllable");
		this.revert = ProjectAddons.instance.getConfig().getBoolean("Abilities.FireDisc.RevertCutBlocks");
		this.drop = ProjectAddons.instance.getConfig().getBoolean("Abilities.FireDisc.DropCutBlocks");
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.FireDisc.Cooldown");
		
		if (player.hasPermission("bending.ability.firedisc.bluefire")) {
			bluefire = true;
		}
		
		if (bluefire) {
			this.damage += (0.5*damage);
		}
		
		if (bPlayer.isAvatarState()) {
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
			this.control = true;
			this.cooldown = 0;
		}
		
		bPlayer.addCooldown(this);
		start();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return loc;
	}

	@Override
	public String getName() {
		return "FireDisc";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
		
		if (control) {
			direction = direction.add(player.getLocation().getDirection());
		}
		
		playFirebendingSound(loc);
		loc = loc.add(direction.normalize());
		
		if (currRange >= range) {
			remove();
			return;
		}
		
		currRange +=1;
		
		if (GeneralMethods.isSolid(loc.getBlock())) {
			if (!cutBlock(loc.getBlock())) {
				remove();
				return;
			} else {
				ItemStack is = new ItemStack(loc.getBlock().getType());
				if (revert) {
					new TempBlock(loc.getBlock(), Material.AIR);
				} else {
					loc.getBlock().setType(Material.AIR);
				}
				
				if (drop) {
					loc.getWorld().dropItemNaturally(loc, is);
				}
			}
		}
		
		if (loc.getBlock().isLiquid()) {
			remove();
			return;
		}
		
		for (int n = 0; n < Integer.MAX_VALUE; n++) {
			Location current, start = loc.clone();
			double c = 0.075;
			double phi = n * 137.5;
			double r = c * Math.sqrt(n);
			double x = r * Math.cos(Math.toRadians(phi));
			double z = r * Math.sin(Math.toRadians(phi));
			current = start.clone().add(x, 0, z);
			
			if (current.distance(start) > 1) {
				break;
			}
			
			if (bluefire) {
				displayBlueParticle(current);
			} else {
				loc.getWorld().spawnParticle(Particle.FLAME, current, 1, 0, 0, 0, 0.0012);
			}
		}
		
		
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
			if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
				DamageHandler.damageEntity(entity, player, damage, this);
				remove();
				return;
			}
		}
	}
	
	public boolean cutBlock(Block b) {
		return ProjectAddons.instance.getConfig().getStringList("Abilities.FireDisc.Cuttable_Blocks").contains(b.getType().toString());
	}
	
	public void displayBlueParticle(Location loc) {
		String hexVal = hexVals[r.nextInt(hexVals.length)];
		GeneralMethods.displayColoredParticle(hexVal, loc);
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
	public void stop() {}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.FireDisc.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Throw a spinning disc of fire that can cut through some blocks or your enemies!";
	}
	
	@Override
	public String getInstructions() {
		return "Left Click";
	}
}
