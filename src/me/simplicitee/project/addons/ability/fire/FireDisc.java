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
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.project.addons.ProjectAddons;

public class FireDisc extends FireAbility implements AddonAbility {
	
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute("DropCutBlocks")
	private boolean drop;
	@Attribute("Controllable")
	private boolean control;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;

	private boolean revert;
	private double currRange = 0;
	private Location loc;
	private Vector direction;
	
	public FireDisc(Player player) {
		super(player);
		
		this.loc = player.getEyeLocation();
		this.direction = player.getLocation().getDirection();
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.FireDisc.Damage");
		this.range = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.FireDisc.Range");
		this.control = ProjectAddons.instance.getConfig().getBoolean("Abilities.Fire.FireDisc.Controllable");
		this.revert = ProjectAddons.instance.getConfig().getBoolean("Abilities.Fire.FireDisc.RevertCutBlocks");
		this.drop = ProjectAddons.instance.getConfig().getBoolean("Abilities.Fire.FireDisc.DropCutBlocks");
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Fire.FireDisc.Cooldown");
		this.knockback = ProjectAddons.instance.getConfig().getDouble("Abilities.Fire.FireDisc.Knockback");
		
		if (bPlayer.isAvatarState()) {
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
				playFirebendingParticles(loc.clone().add(ortho), 2, 0.03, 0, 0.03);
			}
		}
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
			if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
				DamageHandler.damageEntity(entity, player, damage, this);
				entity.setVelocity(direction.clone().multiply(knockback));
				remove();
				return;
			}
		}
	}
	
	public boolean cutBlock(Block b) {
		return ProjectAddons.instance.getConfig().getStringList("Abilities.Fire.FireDisc.CuttableBlocks").contains(b.getType().toString());
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
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Fire.FireDisc.Enabled");
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
