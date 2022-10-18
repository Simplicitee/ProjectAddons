package me.simplicitee.project.addons.ability.fire;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

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
	@Attribute(Attribute.SPEED)
	private double speed;

	private double currentRange = 0;
	private Queue<Color> colors = new LinkedList<>();
	
	private enum Color {
		RED("#ff0900"),
		ORANGE("#ff7f00"),
		YELLOW("#ffef00"),
		GREEN("#00f11d"),
		CYAN("#00ffff"),
		BLUE("#0079ff"),
		PURPLE("#a800ff");
		
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
		speed = ProjectAddons.instance.getConfig().getDouble("Combos.Fire.FlameBreath.Speed");
		
		for (Color color : Color.values()) {
			for (int i = 0; i < 8; ++i) {
				colors.add(color);
			}
		}
	}

	@Override
	public long getCooldown() {
		return ProjectAddons.instance.getConfig().getLong("Combos.Fire.FlameBreath.Cooldown");
	}

	@Override
	public Location getLocation() {
		return player.getEyeLocation().add(player.getLocation().getDirection().multiply(currentRange));
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

		Location breath = player.getEyeLocation();
		Vector direction = breath.getDirection().multiply(speed);
		breath.add(direction);
		
		for (double d = 0; d <= currentRange; d += speed) {
			double offset = 0.1 * d;
			int amount = (int) Math.ceil(d);

			if (!breath.getBlock().isPassable()) {
				break;
			}

			if (rainbow && player.hasPermission("bending.ability.FlameBreath.rainbow")) {
				GeneralMethods.displayColoredParticle(c.getHex(), breath, amount, offset, offset, offset);
			} else {
				playFirebendingParticles(breath, amount, offset, offset, offset);
			}
			
			if (Math.random() > 0.9) {
				playFirebendingSound(breath);
			}
			
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(breath, offset * 2.5)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
					DamageHandler.damageEntity(entity, damage, this);
					
					if (burnEntities) {
						entity.setFireTicks(fireTick + 10);
					}
				} else if (entity instanceof Item) {
					entity.setFireTicks(fireTick + 40);
				}
			}
			
			if (burnGround) {
				Block ignitable = GeneralMethods.getTopBlock(breath, 0, (int) Math.round(amount / range));
				if (ignitable != null && !isAir(ignitable.getType())) {
					createTempFire(ignitable.getLocation());
				}
			}
			
			breath.add(direction);
		}

		if (currentRange < range) {
			currentRange += speed;
		}
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new FlameBreath(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return ComboUtil.generateCombinationFromList(this, ProjectAddons.instance.getConfig().getStringList("Combos.Fire.FlameBreath.Combination"));
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
		return "FireBlast (tap sneak) > HeatControl (hold sneak)";
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
	}
}
