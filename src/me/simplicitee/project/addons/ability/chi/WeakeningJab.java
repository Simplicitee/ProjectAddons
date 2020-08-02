package me.simplicitee.project.addons.ability.chi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.ProjectAddons;

public class WeakeningJab extends ChiAbility implements ComboAbility, AddonAbility{

	private static Set<Integer> entities = new HashSet<>();
	
	@Attribute(Attribute.DURATION)
	public long duration;
	@Attribute(Attribute.COOLDOWN)
	public long cooldown;
	
	public LivingEntity entity = null;
	
	public WeakeningJab(Player player) {
		super(player);
		
		Entity e = GeneralMethods.getTargetedEntity(player, 4);
		if (e instanceof LivingEntity) {
			entity = (LivingEntity) e;
		} else {
			return;
		}
		
		duration = ProjectAddons.instance.getConfig().getLong("Combos.Chi.WeakeningJab.Duration");
		cooldown = ProjectAddons.instance.getConfig().getLong("Combos.Chi.WeakeningJab.Cooldown");
		
		if (entity != null && !entities.contains(entity.getEntityId())) {
			entities.add(entity.getEntityId());
			start();
		}
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return entity.getLocation().clone().add(0, 1, 0);
	}

	@Override
	public String getName() {
		return "WeakeningJab";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public void progress() {
		ParticleEffect.DAMAGE_INDICATOR.display(entity.getLocation(), 3, 0.2, 1.0, 0.2, 0.0004);
		if (System.currentTimeMillis() >= getStartTime() + duration) {
			remove();
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		entities.remove(entity.getEntityId());
		bPlayer.addCooldown(this);
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
	public Object createNewComboInstance(Player player) {
		return new WeakeningJab(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("Jab", ClickType.LEFT_CLICK_ENTITY));
		combo.add(new AbilityInformation("Jab", ClickType.LEFT_CLICK_ENTITY));
		combo.add(new AbilityInformation("Jab", ClickType.RIGHT_CLICK_ENTITY));
		return combo;
	}
	
	@Override
	public String getDescription() {
		return "This special jab damages the enemy's defenses, making them more susceptible to damage!";
	}
	
	@Override
	public String getInstructions() {
		return "Jab (Left) > Jab (Left) > Jab (Right)";
	}

	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Combos.Chi.WeakeningJab.Enabled");
	}
	
	public static boolean isAffected(Entity e) {
		return entities.contains(e.getEntityId());
	}
	
	public static double getModifier() {
		return ProjectAddons.instance.getConfig().getDouble("Combos.Chi.WeakeningJab.Modifier");
	}
}
