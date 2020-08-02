package me.simplicitee.project.addons.ability.earth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.project.addons.ProjectAddons;
import me.simplicitee.project.addons.util.AnimationBuilder;

public class LavaSurge extends LavaAbility implements AddonAbility {
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("Burn")
	private boolean burn;
	@Attribute("BurnDuration")
	private long burnTime;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute("SourceRadius")
	private double sourceRadius;
	@Attribute(Attribute.SELECT_RANGE)
	private int selectRange;
	@Attribute("Blocks")
	private int maxBlocks;
	
	private int shotBlocks;
	private Location sourceCenter;
	private Map<Block, Boolean> source;
	private boolean shot, launchedAll;
	private Vector direction;
	private Set<FallingBlock> blocks;
	private Map<FallingBlock, Long> timeLived;

	public LavaSurge(Player player) {
		super(player);
		
		if (hasAbility(player, LavaSurge.class)) {
			LavaSurge abil = getAbility(player, LavaSurge.class);
			if (!abil.hasShot()) {
				abil.retargetSource();
			}
			return;
		}
		
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.LavaSurge.Cooldown");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.LavaSurge.Damage");
		this.burn = ProjectAddons.instance.getConfig().getBoolean("Abilities.Earth.LavaSurge.Burn.Enabled");
		this.burnTime = ProjectAddons.instance.getConfig().getLong("Abilities.Earth.LavaSurge.Burn.Duration");
		this.speed = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.LavaSurge.Speed");
		this.sourceRadius = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.LavaSurge.SourceRadius");
		this.selectRange = ProjectAddons.instance.getConfig().getInt("Abilities.Earth.LavaSurge.SelectRange");
		this.maxBlocks = ProjectAddons.instance.getConfig().getInt("Abilities.Earth.LavaSurge.MaxBlocks");
		this.shot = false;
		this.shotBlocks = 0;
		this.launchedAll = false;
		this.blocks = new HashSet<>();
		this.timeLived = new HashMap<>();
		
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
			
			total.addAll(list);
		}
		
		if (total.isEmpty()) {
			return false;
		}
		
		this.source = new HashMap<>();
		for (Block block : total) {
			source.put(block, false);
			new AnimationBuilder(block)
			.effect(Particle.LAVA)
			.addStep(Material.MAGMA_BLOCK, 1000)
			.addDestroyTask(() -> { new TempBlock(block, GeneralMethods.getLavaData(0)); source.put(block, true); })
			.start();
		}
		
		return true;
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (shot) {
			if (!launchedAll && shotBlocks < maxBlocks) {
				FallingBlock fb = GeneralMethods.spawnFallingBlock(sourceCenter.clone().add(0, 0.7, 0), Material.MAGMA_BLOCK);
				Vector v = direction.clone().add(new Vector(randomOffset(), 0.07, randomOffset())).normalize().multiply(speed);
				
				fb.setMetadata("lavasurge", new FixedMetadataValue(ProjectAddons.instance, this));
				fb.setVelocity(v);
				fb.setDropItem(false);
				
				blocks.add(fb);
				timeLived.put(fb, System.currentTimeMillis());
				shotBlocks++;
			}
			
			if (shotBlocks >= maxBlocks) {
				launchedAll = true;
			}
			
			Iterator<FallingBlock> iter = blocks.iterator();
			
			while (iter.hasNext()) {
				FallingBlock fb = iter.next();
				if (fb.isDead()) {
					iter.remove();
					continue;
				}
				
				if (timeLived.containsKey(fb)) {
					if (timeLived.get(fb) + 4000 <= System.currentTimeMillis()) {
						iter.remove();
						fb.remove();
						continue;
					}
				}
				
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 0.7)) {
					if (e instanceof LivingEntity) {
						DamageHandler.damageEntity(e, damage, this);
						((LivingEntity) e).setNoDamageTicks(0);
						
						if (burn) {
							((LivingEntity) e).setFireTicks((int) (burnTime/1000 * 20));
						}
						
						iter.remove();
						fb.remove();
						timeLived.remove(fb);
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
		for (Block b : source.keySet()) {
			TempBlock tb = null;
			
			if (TempBlock.isTempBlock(b)) {
				tb = TempBlock.get(b);
				tb.setType(Material.AIR);
			} else {
				tb = new TempBlock(b, Material.AIR);
			}
			
			tb.setRevertTime(3000);
		}
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
		for (Block b : source.keySet()) {
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
		
		for (boolean b : source.values()) {
			if (!b) {
				return;
			}
		}
		
		this.direction = player.getEyeLocation().getDirection().clone();
		
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
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Earth.LavaSurge.Enabled");
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
		return (Math.random() - 0.5) / 4;
	}
}
