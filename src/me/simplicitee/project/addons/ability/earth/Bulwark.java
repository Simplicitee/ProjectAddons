package me.simplicitee.project.addons.ability.earth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.Collapse;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.simplicitee.project.addons.ProjectAddons;

public class Bulwark extends EarthAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double throwSpeed;
	
	private Set<Location> locs;
	private Set<Block> blocks;
	private Set<FallingBlock> fbs;
	private Set<RaiseEarth> parts;
	private Location start;
	private boolean launched;
	private long launchTime;
	
	public Bulwark(Player player) {
		super(player);
		
		if (!player.isOnGround()) {
			return;
		}
		
		start = player.getLocation().subtract(0, 1, 0);
		if (!isEarthbendable(start.getBlock())) {
			return;
		}
		
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.Bulwark.Cooldown");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.Bulwark.Damage");
		this.throwSpeed = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.Bulwark.ThrowSpeed");
		this.locs = new HashSet<>();
		this.blocks = new HashSet<>();
		this.fbs = new HashSet<>();
		this.parts = new HashSet<>();
		this.launchTime = 0;
		this.launched = false;
		
		Location front = start.clone().add(player.getLocation().getDirection().setY(0).normalize().multiply(2.5));
		Location left = GeneralMethods.getLeftSide(start, 3);
		Location right = GeneralMethods.getRightSide(start, 3);
		Vector toLeft = GeneralMethods.getDirection(front, left);
		Vector toRight = GeneralMethods.getDirection(front, right);
		
		parts.add(new RaiseEarth(player, front, 3));
		locs.add(front.clone());
		double leftLength = toLeft.length() + 1;
		double rightLength = toRight.length() + 1;
		
		for (double i = 0.5; i <= leftLength; i += 0.5) {
			Vector v = toLeft.normalize().multiply(i);
			front.add(v);
			parts.add(new RaiseEarth(player, front, Math.min(2, (int) (leftLength - i))));
			locs.add(front.clone());
			front.subtract(v);
		}
		
		for (double i = 0.5; i <= rightLength; i += 0.5) {
			Vector v = toRight.normalize().multiply(i);
			front.add(v);
			parts.add(new RaiseEarth(player, front, Math.min(2, (int) (rightLength - i))));
			locs.add(front.clone());
			front.subtract(v);
		}
		
		start();
	}

	@Override
	public void progress() {
		if (!launched) {
			if (start.distance(player.getLocation().subtract(0, 1, 0)) > 1.5) {
				remove();
				return;
			}
			
			if (!player.isSneaking()) {
				remove();
				return;
			}
			
			for (RaiseEarth re : parts) {
				blocks.addAll(re.getAffectedBlocks().values());
			}
		} else {
			if (launchTime + 3000 <= System.currentTimeMillis()) {
				remove();
				return;
			}
			
			List<FallingBlock> removal = new ArrayList<>();
			for (FallingBlock fb : fbs) {
				if (fb.isDead()) {
					removal.add(fb);
					break;
				}
				
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 0.8)) {
					if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
						DamageHandler.damageEntity(e, damage, this);
						removal.add(fb);
						fb.remove();
						break;
					}
				}
			}
			
			for (FallingBlock fb : removal) {
				fbs.remove(fb);
			}
			
			if (fbs.isEmpty()) {
				remove();
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		for (Location loc : locs) {
			if (isAir(loc.getBlock().getType())) {
				loc.add(0, 1, 0);
			}
			new Collapse(player, loc);
		}
		for (FallingBlock fb : fbs) {
			fb.remove();
		}
		fbs.clear();
		locs.clear();
		blocks.clear();
		bPlayer.addCooldown(this);
	}
	
	public void clickFunction() {
		if (launched) {
			return;
		}
		
		launched = true;
		for (Block b : blocks) {
			BlockData data = b.getBlockData();
			revertBlock(b);
			
			FallingBlock fb = GeneralMethods.spawnFallingBlock(b.getLocation().add(0.5, 0.5, 0.5), data.getMaterial(), data);
			fb.setDropItem(false);
			fb.setMetadata("bulwark", new FixedMetadataValue(ProjectAddons.instance, this));
			fb.setVelocity(player.getEyeLocation().getDirection().setY(0.195).normalize().multiply(throwSpeed));
			fbs.add(fb);
		}
		
		launchTime = System.currentTimeMillis();
		locs.clear();
		blocks.clear();
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
		return "Bulwark";
	}

	@Override
	public Location getLocation() {
		return start;
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
	public String getDescription() {
		return "Raise a shield of earth in front of you, and it lowers when you release sneak!";
	}
	
	@Override
	public String getInstructions() {
		return "Hold Sneak";
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Earth.Bulwark.Enabled");
	}
}
