package me.simplicitee.project.addons.ability.earth;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.ProjectAddons;

public class ShrapnelShot extends MetalAbility implements AddonAbility {
	
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	
	private Item nugget;

	public ShrapnelShot(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown("Shrapnel")) {
			return;
		} else if (!bPlayer.canBend(this)) {
			return;
		}
		
		Material m = null;
		if (player.getInventory().contains(Material.IRON_NUGGET)) {
			m = Material.IRON_NUGGET;
		} else if (player.getInventory().contains(Material.GOLD_NUGGET)) {
			m = Material.GOLD_NUGGET;
		} else {
			return;
		}

		int slot = player.getInventory().first(m);
		ItemStack is = player.getInventory().getItem(slot);
		is.setAmount(is.getAmount() - 1);
		player.getInventory().setItem(slot, is);
		
		Location spawn = GeneralMethods.getRightSide(player.getLocation(), 0.12).add(0, 1.3, 0);
		
		nugget = player.getWorld().dropItem(spawn, new ItemStack(m));
		nugget.setPickupDelay(10);
		nugget.setVelocity(player.getLocation().getDirection().add(new Vector(0, 0.105, 0)).normalize().multiply(ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.Shrapnel.Shot.Speed")));
		
		damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.Shrapnel.Shot.Damage");
		cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.Shrapnel.Shot.Cooldown");
		
		bPlayer.addCooldown("Shrapnel", cooldown);
		start();
	}
	
	public ShrapnelShot(Player player, Vector direction, double speed) {
		super(player);
		
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			return;
		}
		
		Material m = null;
		if (player.getInventory().contains(Material.IRON_NUGGET)) {
			m = Material.IRON_NUGGET;
		} else if (player.getInventory().contains(Material.GOLD_NUGGET)) {
			m = Material.GOLD_NUGGET;
		} else {
			return;
		}
		
		int slot = player.getInventory().first(m);
		ItemStack is = player.getInventory().getItem(slot);
		is.setAmount(is.getAmount() - 1);
		player.getInventory().setItem(slot, is);
		
		Location spawn = GeneralMethods.getRightSide(player.getLocation(), 0.12).add(0, 1.3, 0);
		
		nugget = player.getWorld().dropItem(spawn, new ItemStack(m));
		nugget.setMetadata("shrapnel", new FixedMetadataValue(ProjectKorra.plugin, 0));
		nugget.setPickupDelay(10);
		nugget.setVelocity(direction.add(new Vector(0, 0.105, 0)).normalize().multiply(speed));
		
		damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.Shrapnel.Shot.Damage");
		cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.Shrapnel.Shot.Cooldown");
		
		start();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return nugget == null ? null : nugget.getLocation();
	}

	@Override
	public String getName() {
		return "Shrapnel";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (nugget.isDead()) {
			remove();
			return;
		}
		
		if (nugget.isOnGround()) {
			remove();
			return;
		}
		
		ParticleEffect.CRIT.display(nugget.getLocation(), 1);
		player.getWorld().playSound(nugget.getLocation(), Sound.ENTITY_ARROW_HIT, 0.2f, 1f);
		double dmg = damage * (ProjectAddons.instance.getMethods().clamp(0.5, 4, nugget.getVelocity().length()) / 4);
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(nugget.getLocation(), 1.5)) {
			if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
				player.getWorld().playSound(e.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.4f, 1f);
				DamageHandler.damageEntity(e, dmg, this);
				((LivingEntity) e).setNoDamageTicks(0);
				nugget.remove();
				remove();
				return;
			}
		}
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
	public String getDescription() {
		return "Use your metalbending to throw nuggets of gold and iron like pieces of shrapnel, dealing damage when they hit entities. This requires that you have gold or iron nuggets in your inventory to launch!";
	}
	
	@Override
	public String getInstructions() {
		return "Click to shoot a single piece of shrapnel at high velocity to the targeted location, click while sneaking to launch several shotgun-style.";
	}

	@Override
	public void load() {}

	@Override
	public void stop() {}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Earth.Shrapnel.Enabled");
	}
}
