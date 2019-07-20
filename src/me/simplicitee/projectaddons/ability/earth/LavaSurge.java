package me.simplicitee.projectaddons.ability.earth;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.projectaddons.ProjectAddons;

public class LavaSurge extends LavaAbility implements AddonAbility {
	
	private long cooldown;
	private double damage;
	private boolean burn;
	private long burnTime;
	private double speed;
	private double sourceRadius;
	private int selectRange;
	private int maxBlocks;
	private Location sourceCenter;
	private Set<Block> source;
	private boolean shot;
	private Vector direction;
	private Set<FallingBlock> blocks;

	public LavaSurge(Player player) {
		super(player);
		
		if (hasAbility(player, LavaSurge.class)) {
			LavaSurge abil = getAbility(player, LavaSurge.class);
			if (!abil.hasShot()) {
				abil.retargetSource();
			}
			return;
		}
		
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.LavaSurge.Cooldown");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.LavaSurge.Damage");
		this.burn = ProjectAddons.instance.getConfig().getBoolean("Abilities.LavaSurge.Burn.Enabled");
		this.burnTime = ProjectAddons.instance.getConfig().getLong("Abilities.LavaSurge.Burn.Duration");
		this.speed = ProjectAddons.instance.getConfig().getDouble("Abilities.LavaSurge.Speed");
		this.sourceRadius = ProjectAddons.instance.getConfig().getDouble("Abilities.LavaSurge.SourceRadius");
		this.selectRange = ProjectAddons.instance.getConfig().getInt("Abilities.LavaSurge.SelectRange");
		this.maxBlocks = ProjectAddons.instance.getConfig().getInt("Abilities.LavaSurge.MaxBlocks");
		this.shot = false;
		this.blocks = new HashSet<>();
		
		if (prepare()) {
			start();
		}
	}
	
	private boolean prepare() {
		Block b = player.getTargetBlock(getTransparentMaterialSet(), selectRange);
		this.sourceCenter = b.getLocation().clone().add(0.5, 0.5, 0.5);
		
		Set<Block> total = new HashSet<>();
		int count = 0;
		
		for (double i = 1; i <= sourceRadius; i += 0.5) {
			if (count >= maxBlocks) {
				break;
			}
			
			List<Block> list = GeneralMethods.getBlocksAroundPoint(sourceCenter, i);
			Iterator<Block> iter = list.iterator();
			
			while (iter.hasNext()) {
				Block b2 = iter.next();
				if (total.contains(b2)) {
					iter.remove();
				} else if (!isEarthbendable(b2.getType(), bPlayer.canMetalbend(), true, true)) {
					iter.remove();
				} else if (count >= maxBlocks) {
					iter.remove();
				} else {
					count++;
				}
			}
			
			for (Block b2 : list) {
				if (isEarthbendable(b2)) {
					new TempBlock(b2, Material.LAVA);
				}
			}
			
			total.addAll(list);
		}
		
		if (total.isEmpty()) {
			return false;
		}
		
		source = new HashSet<>(total);
		
		return true;
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (shot) {
			for (FallingBlock fb : blocks) {
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 1.5)) {
					if (e instanceof LivingEntity) {
						DamageHandler.damageEntity(e, damage, this);
						
						if (burn) {
							((LivingEntity) e).setFireTicks((int) (burnTime/1000 * 20));
						}
					}
				}
			}
			
			if (blocks.isEmpty()) {
				remove();
				return;
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		
		bPlayer.addCooldown(this);
		source.clear();
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
		return "LavaSurge";
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

	public void retargetSource() {
		for (Block b : source) {
			if (TempBlock.isTempBlock(b)) {
				TempBlock.get(b).revertBlock();
			}
		}
		
		source.clear();
		if (!prepare()) {
			remove();
		}
	}
	
	public void shoot() {
		if (shot) {
			return;
		}
		
		this.direction = player.getEyeLocation().getDirection().clone();
		
		for (Block b : source) {
			FallingBlock fb = GeneralMethods.spawnFallingBlock(sourceCenter.clone().add(0, 1, 0), Material.MAGMA_BLOCK);
			Vector v = direction.clone().add(new Vector(randomOffset(), 0.14, randomOffset())).normalize().multiply(speed);
			
			fb.setMetadata("lavasurge", new FixedMetadataValue(ProjectAddons.instance, this));
			fb.setVelocity(v);
			
			blocks.add(fb);
			
			TempBlock tb = null;
			
			if (TempBlock.isTempBlock(b)) {
				tb = TempBlock.get(b);
				tb.setType(Material.AIR);
			} else {
				tb = new TempBlock(b, Material.AIR);
			}
			
			tb.setRevertTime(3000);
		}
		
		playLavabendingSound(sourceCenter);
		
		this.shot = true;
	}
	
	public boolean hasShot() {
		return shot;
	}
	
	public void removeBlock(FallingBlock fb) {
		this.blocks.remove(fb);
		fb.remove();
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.LavaSurge.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Throw a surging wave of lava in the direction you are looking!";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak to create your lava source (or select an existing source) and click to throw the wave!";
	}
	
	private double randomOffset() {
		return (Math.random() - 0.5) / 2;
	}
}
