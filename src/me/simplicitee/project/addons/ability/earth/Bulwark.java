package me.simplicitee.project.addons.ability.earth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

	private static Vector UP = new Vector(0, 1, 0);
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double throwSpeed;
	@Attribute(Attribute.HEIGHT)
	private int height;
	
	private Set<Block> blocks;
	private Set<FallingBlock> fbs;
	private Location start;
	private boolean launched = false, init = false;
	private long launchTime;
	private int step = 0;
	
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
		this.height = ProjectAddons.instance.getConfig().getInt("Abilities.Earth.Bulwark.Height");
		this.blocks = new HashSet<>();
		this.launchTime = 0;
		
		start();
	}
	
	private void loadPillar(Location loc) {
		Block top = GeneralMethods.getTopBlock(loc, 2);
		
		if (!isEarthbendable(top)) {
			return;
		}
		
		blocks.add(top);
	}

	@Override
	public void progress() {
		if (!init) {
			Location front = start.clone().add(player.getLocation().getDirection().setY(0).normalize().multiply(2.5));
			Vector toLeft = GeneralMethods.getDirection(front, GeneralMethods.getLeftSide(start, 3)).normalize().multiply(0.5);
			Vector toRight = GeneralMethods.getDirection(front, GeneralMethods.getRightSide(start, 3)).normalize().multiply(0.5);
			
			loadPillar(front);
			
			for (double i = 0.5; i <= 3; i += 0.5) {
				loadPillar(front.add(toLeft).clone());
			}
			
			front.add(toLeft.normalize().multiply(-3));
			
			for (double i = 0.5; i <= 3; i += 0.5) {
				loadPillar(front.add(toRight).clone());
			}
			init = true;
		}
		
		if (step < 2) {
			for (Block block : blocks) {
				moveEarth(block, UP, height);
			}
			++step;
		} else if (!launched) {
			if (start.distance(player.getLocation().subtract(0, 1, 0)) > 1.5) {
				remove();
				return;
			}
			
			if (!player.isSneaking()) {
				remove();
				return;
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
		for (Block block : blocks) {
			new Collapse(player, block.getLocation());
		}
		for (FallingBlock fb : fbs) {
			fb.remove();
		}
		fbs.clear();
		blocks.clear();
		bPlayer.addCooldown(this);
	}
	
	public void clickFunction() {
		if (launched) {
			return;
		}
		
		fbs = new HashSet<>();
		
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
