package me.simplicitee.project.addons.ability.air;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.simplicitee.project.addons.ProjectAddons;

public class GaleGust extends AirAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double range;
	
	private Location current;
	private Vector direction;
	private Set<Point> points;
	
	public GaleGust(Player player) {
		super(player);
		
		this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Air.GaleGust.Cooldown");
		this.knockback = ProjectAddons.instance.getConfig().getDouble("Abilities.Air.GaleGust.Knockback");
		this.radius = ProjectAddons.instance.getConfig().getDouble("Abilities.Air.GaleGust.Radius");
		this.damage = ProjectAddons.instance.getConfig().getDouble("Abilities.Air.GaleGust.Damage");
		this.range = ProjectAddons.instance.getConfig().getDouble("Abilities.Air.GaleGust.Range");
		
		if (player.getMainHand() == MainHand.LEFT) {
			current = GeneralMethods.getLeftSide(player.getLocation().clone().add(0, 1.2, 0), 0.55);
		} else {
			current = GeneralMethods.getRightSide(player.getLocation().clone().add(0, 1.2, 0), 0.55);
		}
		
		this.direction = player.getEyeLocation().getDirection().clone().normalize();
		this.points = new HashSet<>();
		
		bPlayer.addCooldown(this);
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (player.getLocation().distance(current) > range) {
			remove();
			return;
		}
		
		if (player.isSneaking()) {
			direction.add(player.getEyeLocation().getDirection()).normalize();
		}
		
		current = current.add(direction);
		
		if (!current.getBlock().isPassable()) {
			remove();
			return;
		}
		
		points.add(new Point(current.clone().setDirection(direction.clone())));
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(current, radius)) {
			if (e.getEntityId() == player.getEntityId()) {
				continue;
			}
			
			if (e instanceof LivingEntity && damage > 0) {
				DamageHandler.damageEntity(e, damage, this);
			}
			
			e.setVelocity(direction.clone().multiply(knockback));
			new HorizontalVelocityTracker(e, player, 0, this);
			e.setFireTicks(0);
		}
		
		List<Point> remove = new ArrayList<>();
		
		for (Point point : points) {
			double radi = point.getRadius() + 0.12;
			
			if (radi > radius) {
				remove.add(point);
				continue;
			}
			
			for (int i = 0; i < 3; i++) {
				Vector ortho = GeneralMethods.getOrthogonalVector(point.getLocation().getDirection(), 120 * i + point.getAngle(), point.getRadius());
				playAirbendingParticles(point.getLocation().clone().add(ortho), 1, 0, 0, 0);
			}
			
			point.setAngle(point.getAngle() + Math.random() * 60).setRadius(radi);
		}
		
		points.removeAll(remove);
		
		if (points.isEmpty()) {
			remove();
			return;
		}
	}
	
	@Override
	public void remove() {
		super.remove();
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
		return "GaleGust";
	}

	@Override
	public Location getLocation() {
		return current;
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

	public class Point {
		
		private Location point;
		private double radius, angle;
		
		private Point(Location point) {
			this.point = point;
			this.radius = 0;
			this.angle = 0;
		}
		
		public Location getLocation() {
			return point;
		}
		
		public double getRadius() {
			return radius;
		}
		
		public double getAngle() {
			return angle;
		}
		
		public Point setRadius(double radius) {
			this.radius = radius;
			return this;
		}
		
		public Point setAngle(double angle) {
			this.angle = angle;
			return this;
		}
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Air.GaleGust.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "Create a very strong and sudden wind to blow away entities!";
	}
	
	@Override
	public String getInstructions() {
		return "Left Click, Sneak to control";
	}
}
