		package me.simplicitee.project.addons.ability.fire;

import org.bukkit.Location;
import org.bukkit.Material;
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

import me.simplicitee.project.addons.ProjectAddons;

public class FireDisc extends FireAbility implements AddonAbility{
	
	private Location loc;
	private Vector direction;
	private double damage, range, currRange = 0;
	private boolean revert, drop, control;
	private long cooldown;

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
		
		if (player.hasPermission("bending.fire.bluefire")) {
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
		
		Vector normal = GeneralMethods.getOrthogonalVector(direction, 0, 1);
		for (double r = 0; r < 1; r += 0.2) {
			for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / (r * 12)) {
				Vector ortho = GeneralMethods.getOrthogonalVector(normal, Math.toDegrees(theta), r);
				ProjectAddons.instance.getMethods().playDynamicFireParticles(player, loc.clone().add(ortho), 2, 0.03, 0, 0.03);
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
