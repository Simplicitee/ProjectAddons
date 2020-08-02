package me.simplicitee.project.addons.ability.earth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.ProjectAddons;

public class RockSlide extends EarthAbility implements AddonAbility, ComboAbility {
	
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.KNOCKUP)
	private double knockup;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("AngularSpeed")
	private double angular;
	@Attribute(Attribute.DURATION)
	private long duration;
	
	private Vector direction;
	private Set<FallingBlock> blocks;
	private double health;
	
	public RockSlide(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		Block b = getTopBlock(player.getLocation(), 1);
		
		if (!isEarthbendable(b.getType(), false, true, false)) {
			return;
		}
		
		this.blocks = new HashSet<>();
		this.speed = ProjectAddons.instance.getConfig().getDouble("Combos.Earth.RockSlide.Speed");
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Earth.RockSlide.Cooldown");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Combos.Earth.RockSlide.Damage");
		this.direction = player.getEyeLocation().getDirection().normalize().multiply(speed);
		this.knockback = ProjectAddons.instance.getConfig().getDouble("Combos.Earth.RockSlide.Knockback");
		this.knockup = ProjectAddons.instance.getConfig().getDouble("Combos.Earth.RockSlide.Knockup");
		this.angular = ProjectAddons.instance.getConfig().getDouble("Combos.Earth.RockSlide.TurningSpeed");
		this.duration = ProjectAddons.instance.getConfig().getLong("Combos.Earth.RockSlide.Duration");
		this.direction.setY(0);
		this.health = player.getHealth();
		
		start();
	}
	
	@Override
	public void remove() {
		super.remove();
		this.clearBlocks();
		bPlayer.addCooldown(this);
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if (player.getHealth() < health) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}

		Block b = getTopBlock(player.getLocation(), 3);
		if (b == null) {
			remove();
			return;
		}
		
		if (!isEarthbendable(b.getType(), false, true, false)) {
			remove();
			return;
		}
		
		if (player.isSneaking()) {
			remove();
			return;
		}
		
		direction.add(player.getEyeLocation().getDirection().normalize().multiply(angular)).normalize().multiply(speed);
		
		double wHeight = b.getY() + 2.1;
		double pHeight = player.getLocation().getY();
		double dHeight = wHeight - pHeight;
		
		direction.setY(dHeight * 0.2);
		
		player.setVelocity(direction);
		playEarthbendingSound(player.getLocation());
		
		this.reloadBlocks();
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), 2)) {
			if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId() && !blocks.contains(e)) {
				DamageHandler.damageEntity(e, damage, this);
				Vector knock = GeneralMethods.getDirection(player.getLocation(), e.getLocation());
				knock.setY(knockup);
				knock.normalize().multiply(knockback);
				
				e.setVelocity(knock);
			}
		}
	}
	
	private void clearBlocks() {
		for (FallingBlock fb : blocks) {
			fb.remove();
		}
		blocks.clear();
	}
	
	private void reloadBlocks() {
		if (!blocks.isEmpty()) {
			clearBlocks();
		}
		
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				Location loc = player.getLocation().add(i, 0, j);
				Block b = getTopBlock(loc, 2);
				
				if (b == null) {
					continue;
				} else if (!isEarthbendable(b.getType(), false, true, false)) {
					continue;
				}
				
				loc = b.getLocation().add(0.5, 1.1, 0.5);
				
				if (loc.getBlock().isPassable()) {
					FallingBlock fb = GeneralMethods.spawnFallingBlock(offset(loc, 0.2, 0.2, 0.2), b.getType(), b.getBlockData());
					fb.setMetadata("rockslide", new FixedMetadataValue(ProjectAddons.instance, 0));
					fb.setDropItem(false);
					
					blocks.add(fb);
					
					if (Math.random() < 0.23) {
						ParticleEffect.BLOCK_CRACK.display(fb.getLocation(), 2, 0.4, 0.4, 0.4, b.getBlockData());
					}
				}
			}
		}
		
		if (blocks.size() < ProjectAddons.instance.getConfig().getInt("Combos.Earth.RockSlide.RequiredRockCount")) {
			remove();
			return;
		}
	}
	
	private Block getTopBlock(Location loc, int range) {
		Block b = GeneralMethods.getTopBlock(loc, range);
		
		int i = 0;
		while (b.isPassable() && i < range) {
			b = b.getRelative(BlockFace.DOWN);
			i++;
		}
		
		return b;
	}
	
	private Location offset(Location loc, double x, double y, double z) {
		double dx = Math.random() * x - x / 2;
		double dy = Math.random() * y - y / 2;
		double dz = Math.random() * z - z / 2;
		return loc.clone().add(dx, dy, dz);
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
		return "RockSlide";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new RockSlide(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Shockwave", ClickType.RIGHT_CLICK_BLOCK));
		combo.add(new AbilityInformation("EarthSmash", ClickType.SHIFT_UP));
		return combo;
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
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Earth.RockSlide.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Slide over the earth using loose chunks of rock";
	}
	
	@Override
	public String getInstructions() {
		return "Shockwave (hold sneak) > Shockwave (right click block) > EarthSmash (release sneak)";
	}
}
