package me.simplicitee.project.addons.ability.water;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.ProjectAddons;

public class MistShards extends IceAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute("IcicleCount")
	private int icicleCount;
	
	private Set<Icicle> icicles;
	private Location cloud;
	private boolean forming, formed;
	
	public MistShards(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (hasAbility(player, MistShards.class)) {
			return;
		}
		
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Water.MistShards.Cooldown");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Combos.Water.MistShards.Damage");
		this.range = ProjectAddons.instance.getConfig().getDouble("Combos.Water.MistShards.Range");
		this.icicleCount = ProjectAddons.instance.getConfig().getInt("Combos.Water.MistShards.IcicleCount");
		this.icicles = new HashSet<>();
		this.forming = true;
		this.formed = false;
		
		Block source = BlockSource.getWaterSourceBlock(player, 5, ClickType.SHIFT_DOWN);
		if (source != null) {
			cloud = source.getLocation().add(0.5, 0.5, 0.5);
			start();
		}
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (forming) {
			Location target = player.getEyeLocation().add(0, 1, 0);
			cloud.add(GeneralMethods.getDirection(cloud, target).normalize().multiply(0.25));
			ParticleEffect.CLOUD.display(cloud, 2, 0.04, 0.04, 0.04);
			
			if (cloud.distance(target) < 0.15) {
				formed = true;
				forming = false;
				ActionBar.sendActionBar(ChatColor.DARK_AQUA + "" + icicleCount + " icicles remaining", player);
			}
		}
		
		if (!player.isSneaking() && formed) {
			bPlayer.addCooldown(this);
			remove();
			return;
		} else if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (formed) {
			cloud = player.getEyeLocation().add(0, 1, 0);
			ParticleEffect.CLOUD.display(cloud, 2 * icicleCount, 0.3, 0.2, 0.3);
			
			for (int i = 0; i < 3; i++) {
				Iterator<Icicle> iter = icicles.iterator();
				while (iter.hasNext()) {
					if (!iter.next().advance()) {
						iter.remove();
					}
				}
			}
			
			if (icicleCount < 1 && icicles.isEmpty()) {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
		}
	}
	
	public void clickFunction() {
		if (icicleCount < 1 || !formed) {
			return;
		}
		
		Location start = cloud.clone();
		Vector direction = GeneralMethods.getDirection(start, GeneralMethods.getTargetedLocation(player, range));
		start.setDirection(direction.normalize());
		
		icicles.add(new Icicle(start));
		icicleCount--;
		if (icicleCount > 0) {
			ActionBar.sendActionBar(ChatColor.DARK_AQUA + "" + icicleCount + " icicles remaining", player);
		}
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
		return "MistShards";
	}

	@Override
	public Location getLocation() {
		return cloud;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new MistShards(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("PhaseChange", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("IceBlast", ClickType.SHIFT_DOWN));
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
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Water.MistShards.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Evaporate water into vapor and use the small cloud to fire icicles at enemies!";
	}
	
	@Override
	public String getInstructions() {
		return "WaterManipulation (Hold sneak) > PhaseChange (Release sneak) > IceBlast (Hold sneak)";
	}
	
	class Icicle {
		Location loc, start;
		
		private Icicle(Location start) {
			this.loc = start.clone();
			this.start = start.clone();
		}
		
		public boolean advance() {
			this.loc.add(this.loc.getDirection());
			
			if (!loc.getBlock().isPassable()) {
				return false;
			} else if (loc.distanceSquared(start) >= range * range) {
				return false;
			}
			
			GeneralMethods.displayColoredParticle("62bcc0", loc);
			ParticleEffect.BLOCK_CRACK.display(loc, 1, 0, 0, 0, Material.BLUE_ICE.createBlockData());
			playIcebendingSound(loc);
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 0.4)) {
				if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
					DamageHandler.damageEntity(e, damage, MistShards.this);
					return false;
				}
			}
			
			return true;
		}
	}
}
