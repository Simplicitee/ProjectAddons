package me.simplicitee.project.addons.ability.fire;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.simplicitee.project.addons.ProjectAddons;

public class FlameBreath extends FireAbility implements AddonAbility, ComboAbility, Listener{
	
	@Attribute(Attribute.FIRE_TICK)
	private int fireTick;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("BurnGround")
	private boolean burnGround;
	@Attribute("BurnEntities")
	private boolean burnEntities;
	@Attribute("EasterEgg")
	private boolean rainbow;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	
	private Set<Breath> breaths;
	private Queue<Color> colors;
	
	private enum Color {
		RED("#ff0000"),
		ORANGE("#ff6600"),
		YELLOW("#ffff00"),
		GREEN("#00ff00"),
		CYAN("#00ffff"),
		BLUE("#0000ff"),
		PURPLE("#ff00ff");
		
		private String hex;
		
		private Color(String hex) {
			this.hex = hex;
		}
		
		public String getHex() {
			return hex;
		}
	}

	public FlameBreath(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		setFields();
		start();
	}
	
	private void setFields() {
		fireTick = ProjectAddons.instance.getConfig().getInt("Combos.Fire.FlameBreath.FireTick");
		range = ProjectAddons.instance.getConfig().getDouble("Combos.Fire.FlameBreath.Range");
		damage = ProjectAddons.instance.getConfig().getDouble("Combos.Fire.FlameBreath.Damage");
		burnGround = ProjectAddons.instance.getConfig().getBoolean("Combos.Fire.FlameBreath.Burn.Ground");
		burnEntities = ProjectAddons.instance.getConfig().getBoolean("Combos.Fire.FlameBreath.Burn.Entities");
		rainbow = ProjectAddons.instance.getConfig().getBoolean("Combos.Fire.FlameBreath.Rainbow");
		duration = ProjectAddons.instance.getConfig().getLong("Combos.Fire.FlameBreath.Duration");
		knockback = ProjectAddons.instance.getConfig().getDouble("Combos.Fire.FlameBreath.Knockback");
		breaths = new HashSet<>();
		
		int turnsPerColor = 8;
		int amount = Color.values().length * turnsPerColor;
		colors = new LinkedList<>();
		for (int i = 0; i < amount; i++) {
			int index = (int) Math.floor(i/turnsPerColor);
			colors.add(Color.values()[index]);
		}
	}

	@Override
	public long getCooldown() {
		return ProjectAddons.instance.getConfig().getLong("Combos.Fire.FlameBreath.Cooldown");
	}

	@Override
	public Location getLocation() {
		Iterator<Breath> iter = breaths.iterator();
		return iter.hasNext() ? iter.next().getLocation() : player.getEyeLocation();
	}
	
	@Override
	public List<Location> getLocations() {
		List<Location> locList = new ArrayList<>();
		for (Breath b : breaths) {
			locList.add(b.getLocation());
		}
		return locList;
	}

	@Override
	public String getName() {
		return "FlameBreath";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
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
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		Color c = colors.poll();
		colors.add(c);
		
		Breath b = new Breath(player, c);
		breaths.add(b);
		
		List<Breath> removal = new ArrayList<>();
		
		for (Breath breath : breaths) {
			if (breath.advanceLocation()) {
				double offset = 0.1 * breath.getLocation().distance(player.getEyeLocation());
				int amount = (int) Math.ceil(breath.getLocation().distance(player.getEyeLocation()));
				if (rainbow && player.hasPermission("bending.ability.FlameBreath.rainbow")) {
					GeneralMethods.displayColoredParticle(breath.getColor().getHex(), breath.getLocation(), amount, offset, offset, offset);
				} else {
					playFirebendingParticles(breath.getLocation(), amount, offset, offset, offset);
				}
				
				if (Math.random() > 0.6) {
					playFirebendingSound(breath.getLocation());
				}
				
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(breath.getLocation(), offset * 2.5)) {
					if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
						DamageHandler.damageEntity(entity, damage, this);
						entity.setVelocity(breath.getDirection().multiply(knockback));
						
						if (burnEntities) {
							entity.setFireTicks(fireTick + 10);
						}
					} else if (entity instanceof Item) {
						entity.setFireTicks(fireTick + 40);
					}
				}
				
				if (burnGround) {
					Block ignitable = GeneralMethods.getTopBlock(breath.getLocation(), 0, 1);
					if (ignitable != null && !isAir(ignitable.getType())) {
						createTempFire(ignitable.getLocation());
					}
				}
			} else {
				removal.add(breath);
			}
		}
		
		breaths.removeAll(removal);
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new FlameBreath(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("HeatControl", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("HeatControl", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("HeatControl", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("HeatControl", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("HeatControl", ClickType.SHIFT_DOWN));
		return combo;
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
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Fire.FlameBreath.Enabled");
	}
	
	@Override
	public String getDescription() {
		return "The greatest firebenders were able to breath fire! These firebenders learned from the majestic dragons that are now extinct, but fortunately they passed on their sacred bending arts to you! By breathing super-hot air, you can cause it to spontaneously combust, burning all entities and the ground within its radius!";
	}
	
	@Override
	public String getInstructions() {
		return "HeatControl (double tap sneak) > HeatControl (hold sneak)";
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
	}

	public class Breath {
		
		protected Player player;
		protected Vector dir;
		protected Location start, loc;
		protected Color color;
		
		public Breath(Player player, Color color) {
			this.player = player;
			this.start = player.getEyeLocation().clone();
			this.dir = start.getDirection().clone().normalize().multiply(0.5);
			this.loc = start.clone();
			this.color = color;
		}
		
		public boolean advanceLocation() {
			loc = loc.add(dir);
			
			if (GeneralMethods.isSolid(loc.getBlock())) {
				return false;
			} else if (isWater(loc.getBlock())) {
				return false;
			} else if (GeneralMethods.isRegionProtectedFromBuild(player, loc)) {
				return false;
			} else if (start.distance(loc) > range) {
				return false;
			}
			
			return true;
		}
		
		public Vector getDirection() {
			return dir.clone();
		}
		
		public Location getLocation() {
			return loc;
		}
		
		public Location getStart() {
			return start;
		}
		
		public Color getColor() {
			return color;
		}
	}
}
