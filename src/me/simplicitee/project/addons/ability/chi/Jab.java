package me.simplicitee.project.addons.ability.chi;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.simplicitee.project.addons.ProjectAddons;

public class Jab extends ChiAbility implements AddonAbility{
	
	public static enum JabHand {
		RIGHT, LEFT;
	}
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("MaxUses")
	private int maxUses;
	
	private int uses = 0;

	public Jab(Player player, Entity entity, JabHand hand) {
		super(player);
		
		cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Chi.Jab.Cooldown");
		maxUses = ProjectAddons.instance.getConfig().getInt("Abilities.Chi.Jab.MaxUses");
		
		start();
		activate(entity, hand);
	}
	
	public void activate(Entity entity, JabHand hand) {
		if (bPlayer.isOnCooldown("jab interval")) {
			return;
		}
		
		if (entity instanceof LivingEntity) {
			LivingEntity lent = (LivingEntity) entity;
			uses++;
			
			ParticleEffect.END_ROD.display(entity.getLocation().clone().add(0, 1, 0), 4, 0.2, 0.2, 0.2, 0.02);
			if (hand == JabHand.LEFT) {
				double damage = WeakeningJab.isAffected(lent) ? WeakeningJab.getModifier() : 1;
				
				DamageHandler.damageEntity(entity, player, damage, this);
			}
			
			bPlayer.addCooldown("jab interval", 70);
			lent.setNoDamageTicks(0);
		}
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "Jab";
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
		if (uses >= maxUses) {
			remove();
			bPlayer.addCooldown(this);
		}
	}
	
	@Override
	public String getDescription() {
		return "A fundamental ability for any chiblocker, this ability allows for rapid attacks and can be chained for different combos!";
	}
	
	@Override
	public String getInstructions() {
		return "Left click or Right click";
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
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Chi.Jab.Enabled");
	}
}
