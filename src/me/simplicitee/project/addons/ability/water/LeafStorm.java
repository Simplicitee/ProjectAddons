package me.simplicitee.project.addons.ability.water;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.PlantAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.simplicitee.project.addons.ProjectAddons;

public class LeafStorm extends PlantAbility implements ComboAbility, AddonAbility {

	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("LeafCount")
	private int leaves;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("LeafSpeed")
	private double angleSpeed;
	
	private Set<Leaf> leafTracker;
	
	public LeafStorm(Player player) {
		super(player);
		
		if (getAbility(this.getClass()) == null) {
			return;
		}
		
		if (!hasAbility(player, PlantArmor.class)) {
			return;
		}
		
		PlantArmor armor = getAbility(player, PlantArmor.class);
		int cost = ProjectAddons.instance.getConfig().getInt("Combos.Water.LeafStorm.PlantArmorCost");
		
		if (!armor.damage(cost)) {
			return;
		}
		
		radius = ProjectAddons.instance.getConfig().getDouble("Combos.Water.LeafStorm.Radius");
		damage = ProjectAddons.instance.getConfig().getDouble("Combos.Water.LeafStorm.Damage");
		leaves = ProjectAddons.instance.getConfig().getInt("Combos.Water.LeafStorm.LeafCount");
		cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Water.LeafStorm.Cooldown");
		angleSpeed = ProjectAddons.instance.getConfig().getDouble("Combos.Water.LeafStorm.LeafSpeed");
		leafTracker = new HashSet<>();
		
		for (int i = 0; i < leaves; i++) {
			Location loc = player.getEyeLocation().clone();
			loc.add(0, Math.random() * 2 - 1, 0);
			
			double angle = Math.random() * new Random().nextInt(360);
			double offset = Math.random() * radius + 0.5;
			
			leafTracker.add(new Leaf(loc, angle, offset));
		}

		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (player.getInventory().getHeldItemSlot() != 0) {
			remove();
			return;
		}
		
		Set<Leaf> removal = new HashSet<>();
		leaves: for (Leaf leaf : leafTracker) {
			leaf.update();
			
			if (!leaf.getLocation().getBlock().isPassable()) {
				removal.add(leaf);
				continue;
			}
			
			GeneralMethods.displayColoredParticle("3D9970", leaf.getLocation(), 2, 0.2, 0.2, 0.2);
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(leaf.getLocation(), 0.5)) {
				if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
					DamageHandler.damageEntity(e, damage, this);
					removal.add(leaf);
					continue leaves;
				}
			}
		}
		
		leafTracker.removeAll(removal);
		
		if (leafTracker.isEmpty()) {
			remove();
			return;
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
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
		return "LeafStorm";
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

	@Override
	public Object createNewComboInstance(Player player) {
		return new LeafStorm(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		
		combo.add(new AbilityInformation(Element.PLANT.getColor() + "RazorLeaf", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation(Element.PLANT.getColor() + "RazorLeaf", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation(Element.PLANT.getColor() + "VineWhip", ClickType.SHIFT_DOWN));
		
		return combo;
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Water.LeafStorm.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "A combo only usable in with PlantArmor, create a whirling storm of leaves around you! Leaves disappear after hitting a block or entity, or you stop sneaking.";
	}
	
	@Override
	public String getInstructions() {
		return "RazorLeaf (Double Click) > VineWhip (Hold Sneak)";
	}

	public class Leaf {
		
		private Location loc;
		private double angle, radius;
		
		private Leaf(Location loc, double angle, double radius) {
			this.loc = loc;
			this.angle = angle;
			this.radius = radius;
		}
		
		public Location getLocation() {
			return loc;
		}
		
		public double getAngle() {
			return angle;
		}
		
		public Leaf update() {
			Location loc = player.getEyeLocation().clone();
			double x = radius * Math.cos(Math.toRadians(angle));
			double z = radius * Math.sin(Math.toRadians(angle));
			
			loc.add(x, 0, z);
			loc.setY(this.loc.getY());
			
			this.loc = loc;
			angle += angleSpeed;
			
			return this;
		}
	}
}
