package me.simplicitee.project.addons.ability.earth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.project.addons.ProjectAddons;

public class EarthKick extends EarthAbility implements AddonAbility, Listener{

	private static Set<FallingBlock> BLOCKS = new HashSet<>();
	
	@Attribute(Attribute.DAMAGE)
	public double damage;
	@Attribute("Blocks")
	public int maxBlocks;
	@Attribute("LavaMultiplier")
	public double lavaMult;
	
	public List<FallingBlock> kick;
	public long duration = 2500;
	
	public EarthKick(Player player) {
		super(player);
		
		if (getAbility(this.getClass()) == null) {
			return;
		}
		
		setFields();
		if (launchKick()) {
			bPlayer.addCooldown(this);
			start();
		}
	}
	
	public void setFields() {
		damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.EarthKick.Damage");
		maxBlocks = ProjectAddons.instance.getConfig().getInt("Abilities.Earth.EarthKick.MaxBlocks");
		lavaMult = ProjectAddons.instance.getConfig().getDouble("Abilities.Earth.EarthKick.LavaMultiplier");
		kick = new ArrayList<>();
	}

	@Override
	public Element getElement() {
		return Element.EARTH;
	}

	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override
	public List<Location> getLocations() {
		List<Location> locs = new ArrayList<>();
		for (FallingBlock fb : kick) {
			locs.add(fb.getLocation());
		}
		return locs;
	}

	@Override
	public String getName() {
		return "EarthKick";
	}

	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public void progress() {
		Iterator<FallingBlock> iter = kick.iterator();
		while (iter.hasNext()) {
			FallingBlock fb = iter.next();
			
			if (fb == null || fb.isDead()) {
				BLOCKS.remove(fb);
				iter.remove();
				continue;
			}
			
			if (!BLOCKS.contains(fb)) {
				iter.remove();
				fb.remove();
				continue;
			}
			
			ParticleEffect.BLOCK_CRACK.display(fb.getLocation(), 3, 0.1, 0.1, 0.1, fb.getBlockData());
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 1.5)) {
				if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
					DamageHandler.damageEntity(e, player, damage, this);
					((LivingEntity) e).setNoDamageTicks(0);
					iter.remove();
					fb.remove();
					BLOCKS.remove(fb);
					break;
				}
			}
		}
		
		if (kick.isEmpty()) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
	}

	public boolean launchKick() {
		Block b = player.getTargetBlock(getTransparentMaterialSet(), 3);
		Material type = b.getType();
		
		if (TempBlock.isTempBlock(b)) {
			return false;
		}
		
		if (!EarthAbility.isEarthbendable(type, bPlayer.canMetalbend(), bPlayer.canSandbend(), bPlayer.canLavabend())) {
			return false;
		} 
		
		if (type == Material.LAVA) {
			if (bPlayer.canLavabend()) {
				type = Material.MAGMA_BLOCK;
				damage *= lavaMult;
			} else {
				return false;
			}
		}
		
		for (int i = 0; i < maxBlocks; i++) {
			FallingBlock fb = GeneralMethods.spawnFallingBlock(b.getLocation().clone().add(0.5, 1.2, 0.5), type);
			fb.setDropItem(false);
			Location loc = player.getLocation().clone();
			loc.setPitch(0);
			loc.setYaw(loc.getYaw() + new Random().nextInt(25) - 12);
			Vector vec = loc.getDirection();
			vec.setY(Math.max(0.3, Math.random()/2));
			vec.setX(vec.getX()/1.2);
			vec.setZ(vec.getZ()/1.2);
			fb.setVelocity(vec);
			kick.add(fb);
			BLOCKS.add(fb);
		}
		
		playEarthbendingSound(player.getLocation());
		
		return true;
	}
	
	@Override
	public void remove() {
		super.remove();
		for (FallingBlock fb : kick) {
			fb.remove();
			if (BLOCKS.contains(fb)) {
				BLOCKS.remove(fb);
			}
		}
	}

	@Override
	public long getCooldown() {
		return ProjectAddons.instance.getConfig().getLong("Abilities.Earth.EarthKick.Cooldown");
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
	public String getDescription() {
		return "Earthbenders can kick the earth in front of them and send shards flying towards their enemies.";
	}
	
	@Override
	public String getInstructions() {
		return "Sneak at earth in front of you";
	}
	
	public static boolean isBlock(FallingBlock fb) {
		return BLOCKS.contains(fb);
	}
	
	public static void removeBlock(FallingBlock fb) {
		if (isBlock(fb)) {
			BLOCKS.remove(fb);
			fb.remove();
		}
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Earth.EarthKick.Enabled");
	}
}
