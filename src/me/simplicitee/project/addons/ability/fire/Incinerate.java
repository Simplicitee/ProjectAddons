package me.simplicitee.project.addons.ability.fire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;

import me.simplicitee.project.addons.ProjectAddons;

public class Incinerate extends FireAbility implements AddonAbility, ComboAbility {
	
	private long cooldown, duration;
	private int length, fireTick, maxLength, counter;
	private Queue<Stream> streams;

	public Incinerate(Player player) {
		super(player);
		
		cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Incinerate.Cooldown");
		duration = ProjectAddons.instance.getConfig().getLong("Combos.Incinerate.Duration");
		fireTick = ProjectAddons.instance.getConfig().getInt("Combos.Incinerate.FireTicks");
		maxLength = ProjectAddons.instance.getConfig().getInt("Combos.Incinerate.MaxLength");
		length = 2;
		
		streams = new LinkedList<>();
		
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead() || !player.isSneaking()) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > duration + getStartTime()) {
			remove();
			return;
		}
		
		counter++;
		
		if (counter % 4 == 0) {
			length++;
			if (length >= maxLength) {
				length = maxLength;
			}
		}
		
		if (counter % 2 == 0) {
			streams.add(new Stream(player));
		}
		
		Set<Stream> removal = new HashSet<>();
		
		for (Stream s : streams) {
			if (s.advanceLocation()) {
				s.show();
			} else {
				removal.add(s);
			}
		}
		
		streams.removeAll(removal);
	}

	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		streams.clear();
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
		return "Incinerate";
	}

	@Override
	public Location getLocation() {
		return streams.isEmpty() ? null : streams.peek().getLocation();
	}
	
	@Override
	public List<Location> getLocations() {
		return streams.isEmpty() ? null : Arrays.asList(streams.stream().map(Stream::getLocation).toArray(Location[]::new));
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new Incinerate(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> abils = new ArrayList<>();
		abils.add(new AbilityInformation("FireBurst", ClickType.SHIFT_DOWN));
		abils.add(new AbilityInformation("HeatControl", ClickType.SHIFT_UP));
		abils.add(new AbilityInformation("HeatControl", ClickType.SHIFT_DOWN));
		return abils;
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
		return "Shoot blazing streams of fire!";
	}
	
	@Override
	public String getInstructions() {
		return "Press Sneak (FireBurst) > Release Sneak (HeatControl) > Hold Sneak (HeatControl)";
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Incinerate.Enabled");
	}

	public class Stream {
		
		protected Player player;
		protected Vector dir;
		protected Location start, loc;
		protected double radius;
		
		public Stream(Player player) {
			this.player = player;
			this.start = GeneralMethods.getMainHandLocation(player);
			this.dir = start.getDirection().clone().normalize().multiply(0.3);
			this.loc = start.clone();
		}
		
		public boolean advanceLocation() {
			loc = loc.add(dir);
			double x = start.distance(loc);
			
			if (GeneralMethods.isSolid(loc.getBlock())) {
				return false;
			} else if (isWater(loc.getBlock())) {
				return false;
			} else if (GeneralMethods.isRegionProtectedFromBuild(player, loc)) {
				return false;
			} else if (x >= length) {
				return false;
			}
			
			radius = (0.1 * x) * (0.1 * x) + 0.35;
			
			return true;
		}
		
		public void show() {
			ProjectAddons.instance.getMethods().playDynamicFireParticles(player, loc, (int) (radius * 4), radius, radius, radius);
			
			if (Math.random() < 0.4) {
				playFirebendingSound(loc);
			}
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, radius)) {
				if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
					LivingEntity le = (LivingEntity) e;
					
					le.setVelocity(dir);
					int ticks = fireTick;
					if (le.getFireTicks() > 0) {
						ticks += le.getFireTicks();
					}
					le.setFireTicks(ticks);
				}
			}
		}
		
		public Vector getDirection() {
			return dir;
		}
		
		public Location getLocation() {
			return loc;
		}
		
		public Location getStart() {
			return start;
		}
	}
}
