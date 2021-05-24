package me.simplicitee.project.addons.ability.water;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.PlantAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.project.addons.ProjectAddons;
import me.simplicitee.project.addons.Util;
import net.md_5.bungee.api.ChatColor;

public class PlantArmor extends PlantAbility implements AddonAbility, MultiAbility {
	
	// general variables
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("Durability")
	private double maxDurability;
	@Attribute("SwimBoost")
	private int swim;
	@Attribute("SpeedBoost")
	private int speed;
	@Attribute("JumpBoost")
	private int jump;
	
	private double durability;
	private double durabilityDecay;
	private ArmorState state;
	private ArmorAbility active;
	private BossBar bar;
	private ItemStack[] armors = new ItemStack[4];
	private TempArmor armor;
	private World origin;
	private Location current;
	private Vector direction;
	private List<PotionEffect> effects = new ArrayList<>();
	
	// forming and dispersing variables
	@Attribute("RequiredPlants")
	private int requiredPlants;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	private Set<TempBlock> sources = new HashSet<>();
	
	// vinewhip variables
	@Attribute("VineWhip_Range")
	private int maxRange;
	@Attribute("VineWhip_Damage")
	private double vdmg;
	@Attribute("VineWhip_Speed")
	private int vSpeed;
	
	private boolean forward;
	private int range;
	
	// leafshield variables
	@Attribute("LeafShield_Radius")
	private int radius;
	private Set<TempBlock> shield = new HashSet<>();
	
	// tangle variables
	@Attribute("Tangle_Radius")
	private double tRadius;
	@Attribute("Tangle_Range")
	private double tRange;
	private long tDuration;
	
	private int angle;
	
	// grapple variables
	@Attribute("Grapple_Range")
	private int gMax;
	@Attribute("Grapple_Speed")
	private double gSpeed;
	
	private Location target;
	private int gRange;
	private boolean pulling;
	
	// leap variables
	@Attribute("Leap_Power")
	private double power;
	
	// leafdome variables
	@Attribute("LeafDome_Radius")
	private int dRadius;
	
	// regenerate variables
	@Attribute("Regenerate_Amount")
	private int regen;

	public PlantArmor(Player player, ClickType type) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		} else if (hasAbility(player, EarthArmor.class)) {
			return;
		}
		
		PlantArmor abil = getAbility(player, PlantArmor.class);
		if (abil != null) {
			abil.activate(player.getInventory().getHeldItemSlot(), type);
			return;
		}
		
		if (type == ClickType.SHIFT_DOWN) {
			this.duration = ProjectAddons.instance.getConfig().getLong("Abilities.Water.PlantArmor.Duration");
			this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.Water.PlantArmor.Cooldown");
			this.durability = this.maxDurability = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.PlantArmor.Durability");
			this.duration = ProjectAddons.instance.getConfig().getLong("Abilities.Water.PlantArmor.Duration");
			this.durabilityDecay = duration <= 0 ? 0 : maxDurability / (20.0 * duration / 1000.0);
			this.swim = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.Boost.Swim") - 1;
			this.speed = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.Boost.Speed") - 1;
			this.jump = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.Boost.Jump") - 1;
			this.armor = null;
			
			armors[0] = leafLeather(Material.LEATHER_BOOTS);
			armors[1] = leafLeather(Material.LEATHER_LEGGINGS);
			armors[2] = leafLeather(Material.LEATHER_CHESTPLATE);
			armors[3] = new ItemStack(Material.OAK_LEAVES);
			
			this.state = ArmorState.FORMING;
			this.active = null;
			this.origin = player.getWorld();
			
			this.effects.add(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 5, swim));
			this.effects.add(new PotionEffect(PotionEffectType.SPEED, 5, speed));
			this.effects.add(new PotionEffect(PotionEffectType.JUMP, 5, jump));
			
			this.requiredPlants = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.RequiredPlants");
			this.selectRange = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.PlantArmor.SelectRange");
			
			this.forward = true;
			this.range = 0;
			this.maxRange = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.SubAbilities.VineWhip.Range");
			this.vdmg = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.PlantArmor.SubAbilities.VineWhip.Damage");
			this.vSpeed = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.SubAbilities.VineWhip.Speed");
			
			this.radius = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.SubAbilities.LeafShield.Radius");
			
			this.current = null;
			this.tRadius = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.PlantArmor.SubAbilities.Tangle.Radius");
			this.tRange = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.PlantArmor.SubAbilities.Tangle.Range");
			this.tDuration = ProjectAddons.instance.getConfig().getLong("Abilities.Water.PlantArmor.SubAbilities.Tangle.Duration");
			this.angle = 0;
			
			this.gMax = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.SubAbilities.Grapple.Range");
			this.gSpeed = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.PlantArmor.SubAbilities.Grapple.Speed");
			this.pulling = false;
			this.gRange = 0;
			
			this.dRadius = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.SubAbilities.LeafDome.Radius");
			
			this.power = ProjectAddons.instance.getConfig().getDouble("Abilities.Water.PlantArmor.SubAbilities.Leap.Power");
			
			this.regen = ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.SubAbilities.Regenerate.RegenAmount");
			
			start();
		}
	}
	
	private ItemStack leafLeather(Material type) {
		ItemStack leather = new ItemStack(type);
		LeatherArmorMeta meta = (LeatherArmorMeta) leather.getItemMeta();
		meta.setColor(Color.fromRGB(72 + (int) (24 * (Math.random() - 0.5)), 181 + (int) (24 * (Math.random() - 0.5)), 24));
		leather.setItemMeta(meta);
		return leather;
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (!player.getWorld().equals(origin)) {
			remove();
			return;
		}
		
		if (durability <= 0) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() >= this.getStartTime() + duration) {
			remove();
			return;
		}
		
		if (state == ArmorState.FORMING) {
			progressForming();
		} else if (state == ArmorState.FORMED) {
			this.maxDurability -= durabilityDecay;
			if (durability > maxDurability) {
				durability = maxDurability;
			}
			
			bar.setProgress((double) durability / maxDurability);
			
			if (bar.getProgress() <= 0.15) {
				bar.setTitle(ChatColor.DARK_AQUA + "Durability [" + ChatColor.RED    + (int) durability + ChatColor.DARK_AQUA + " / " + (int) maxDurability + "]");
				bar.setColor(BarColor.RED);
			} else if (bar.getProgress() <= 0.5) {
				bar.setTitle(ChatColor.DARK_AQUA + "Durability [" + ChatColor.YELLOW + (int) durability + ChatColor.DARK_AQUA + " / " + (int) maxDurability + "]");
				bar.setColor(BarColor.YELLOW);
			} else {
				bar.setTitle(ChatColor.DARK_AQUA + "Durability [" + ChatColor.GREEN  + (int) durability + ChatColor.DARK_AQUA + " / " + (int) maxDurability + "]");
				bar.setColor(BarColor.GREEN);
			}
			
			player.addPotionEffects(effects);

			if (active != null && player.getInventory().getHeldItemSlot() != active.ordinal()) {
				this.reset();
				return;
			}
			
			if (active == null) {
				return;
			}
			
			switch (active) {
				case VINEWHIP:
					progressVineWhip();
					break;
				case REGENERATE:
					progressRegenerate();
					break;
				case LEAFSHIELD:
					progressLeafShield();
					break;
				case TANGLE:
					progressTangle();
					break;
				case LEAP:
					leap();
					break;
				case GRAPPLE:
					progressGrapple();
					break;
				case LEAFDOME:
					progressLeafDome();
					break;
				case RAZORLEAF:
				case DISPERSE:
					break;
			}
		} else {
			remove();
			return;
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		MultiAbilityManager.unbindMultiAbility(player);
		if (player != null && player.isOnline() && !player.isDead() && armor != null) {
			this.armor.revert();
		}
		
		sources.forEach((tb) -> tb.revertBlock());
		shield.forEach((tb) -> tb.revertBlock());
		
		this.sources.clear();
		this.shield.clear();
		if (this.bar != null) {
			this.bar.removeAll();
		}
	}
	
	private void reset() {
		if (active != null) {
			bPlayer.addCooldown(active.getName(), ProjectAddons.instance.getConfig().getLong("Abilities.Water.PlantArmor.SubAbilities." + active.getName() + ".Cooldown"));
		
			this.active = null;
		}
		
		if (active == ArmorAbility.LEAFSHIELD) {
			this.durability += getAbilityCost("LeafShield");
		}
		
		this.range = 0;
		this.forward = true;
		
		if (!shield.isEmpty()) {
			for (TempBlock tb : shield) {
				tb.revertBlock();
			}
			shield.clear();
		}
		
		this.target = null;
		this.pulling = false;
		this.gRange = 0;
		
		this.current = null;
		this.direction = null;
		this.angle = 0;
	}
	
	public void activate(int slot, ClickType type) {
		if (this.active != null) {
			return;
		}
		
		ArmorAbility ability = ArmorAbility.getAbility(slot);
		
		if (type != ability.getType()) {
			return;
		}
		
		if (bPlayer.isOnCooldown(ability.getName())) {
			ActionBar.sendActionBar(ChatColor.RED + "!> cooldown <!", player);
			return;
		}
		
		if (ability.getPredicate() != null && !ability.getPredicate().test(player)) {
			return;
		}
		
		if (ability.hasCost() && !this.damage(getAbilityCost(ability.getName()))) {
			return;
		}
		
		if (ability == ArmorAbility.RAZORLEAF) {
			new RazorLeaf(player, false);
		} else if (ability == ArmorAbility.DISPERSE) {
			this.state = ArmorState.DISPERSING;
		} else {
			this.active = ability;
			
			if (ability == ArmorAbility.GRAPPLE) {
				this.current = GeneralMethods.getRightSide(player.getLocation(), 0.45).add(0, 1, 0);
				this.target = player.getTargetBlock(getTransparentMaterialSet(), gMax).getLocation().add(0.5, 0.5, 0.5);
			} else {
				this.current = player.getEyeLocation();
				this.direction = current.getDirection().clone();
			}
		}
	}
	
	public boolean damage(int amount) {
		double diff = this.durability - amount;
		if (diff < 0) {
			return false;
		}
		
		this.durability = diff;
		return true;
	}
	
	private void progressForming() {
		if (sources.size() == requiredPlants) {
			this.state = ArmorState.FORMED;
			this.armor = new TempArmor(player, 72000000L, this, armors);
			
			this.bar = Bukkit.createBossBar(ChatColor.DARK_AQUA + "Durability [" + ChatColor.GREEN + durability + ChatColor.DARK_AQUA + " / " + maxDurability + "]", BarColor.GREEN, BarStyle.SOLID);
			this.bar.setProgress((double) durability / maxDurability);
			this.bar.addPlayer(player);
			
			MultiAbilityManager.bindMultiAbility(player, "PlantArmor");
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (!sources.isEmpty()) {
			Location display = player.getLocation();
			for (int y = 0; y < sources.size(); ++y) {
				for (int angle = 0; angle < 360; angle += 15) {
					double x = 0.5 * Math.cos(Math.toRadians(angle));
					double z = 0.5 * Math.sin(Math.toRadians(angle));
					double dy = (((double) y) / requiredPlants) * player.getHeight();
					
					display.add(x, dy, z);
					
					GeneralMethods.displayColoredParticle(Util.LEAF_COLOR, display);
					
					display.subtract(x, dy, z);
				}
			}
		}
		
		Block b = getRandomPlantSource();
		if (b == null) {
			return;
		}
		
		sources.add(new TempBlock(b, Material.AIR));
	}
	
	private void progressVineWhip() {
		for (int j = 0; j < vSpeed; ++j) {
			if (forward) {
				if (++range >= maxRange) {
					forward = false;
					return;
				}
			} else {
				if (--range <= 0) {
					this.reset();	
					return;
				}
			}
			
			Location last = GeneralMethods.getRightSide(player.getLocation().add(0, 1, 0), 0.45);
			last.getDirection().multiply(1.2);
			
			for (int i = 0; i < range; ++i) {
				last.add(last.getDirection());
				
				if (last.getBlock().getType().isSolid()) {
					forward = false;
					return;
				}
				
				GeneralMethods.displayColoredParticle(Util.LEAF_COLOR, last, 1, 0.1, 0.1, 0.1);
				
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(last, 1)) {
					if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
						DamageHandler.damageEntity(e, vdmg, this);
						this.forward = false;
						return;
					}
				}
			}
		}
	}
	
	private void progressLeafShield() {
		if (!player.isSneaking()) {
			this.reset();
			return;
		}
		
		if (!shield.isEmpty()) {
			for (TempBlock tb : shield) {
				tb.revertBlock();
			}
			shield.clear();
		}
		
		Vector direction = player.getEyeLocation().getDirection();
		Location center = GeneralMethods.getTargetedLocation(player, 3.5);
		addShieldBlock(center.getBlock());
		
		for (int i = 1; i <= radius; ++i) {
			for (double angle = 0; angle < 360; angle += 360 / (i * 9)) {
				Vector ortho = GeneralMethods.getOrthogonalVector(direction, angle, i);
				center.add(ortho);
				addShieldBlock(center.getBlock());
				center.subtract(ortho);
			}
		}
	}
	
	private void addShieldBlock(Block block) {
		if (isAir(block.getType()) || block.isPassable()) {
			if (!TempBlock.isTempBlock(block)) {
				shield.add(new TempBlock(block, Material.OAK_LEAVES));
			}
		}
	}
	
	private void progressTangle() {
		if (player.getEyeLocation().distance(current) > tRange) {
			this.reset();
			return;
		}
		
		current.add(direction);
		
		if (!current.getBlock().isPassable()) {
			this.reset();
			return;
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(current, tRadius + 0.5)) {
			if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
				new MovementHandler((LivingEntity) e, this).stopWithDuration(tDuration / 1000 * 20, ChatColor.DARK_AQUA + "* Tangled *");
				new TempBlock(e.getLocation().getBlock(), Material.OAK_LEAVES).setRevertTime(tDuration);
				this.reset();
				return;
			}
		}
		
		for (int i = 0; i < 3; ++i) {
			Vector ov = GeneralMethods.getOrthogonalVector(direction, (double) (angle + (120 * i)), tRadius);
			current.add(ov);
			GeneralMethods.displayColoredParticle(Util.LEAF_COLOR, current);
			current.subtract(ov);
		}
		
		angle += 30;
	}
	
	private void leap() {
		Location ground = player.getLocation();
		
		for (double i = 0; i < 1; i += 0.25) {
			for (int angle = 0; angle < 360; angle += 15) {
				double x = (1 - i) * Math.cos(Math.toRadians(angle));
				double z = (1 - i) * Math.sin(Math.toRadians(angle));
				
				ground.add(x, i, z);
				
				GeneralMethods.displayColoredParticle(Util.LEAF_COLOR, ground);
				
				ground.subtract(x, i, z);
			}
		}
		
		player.setVelocity(direction.add(new Vector(0, 0.8, 0)).multiply(power));
		
		this.reset();
	}
	
	private void progressGrapple() {
		current = GeneralMethods.getRightSide(player.getLocation(), 0.45).add(0, 1, 0);
		
		if (current.distance(target) < 2) {
			this.reset();
			return;
		}
		
		if (!pulling) {
			if (++gRange >= gMax) {
				this.reset();
				return;
			}
		} else {
			gRange = (int) Math.floor(current.distance(target));
		}
		
		Vector direction = GeneralMethods.getDirection(current, target).normalize();
		
		for (int i = 0; i < gRange; ++i) {
			current.add(direction);
			
			GeneralMethods.displayColoredParticle(Util.LEAF_COLOR, current);
			
			if (!current.getBlock().isPassable() && !pulling) {
				if (current.distance(target) < 1) {
					this.pulling = true;
				} else {
					this.reset();
					return;
				}
			}
		}
		
		if (pulling) {
			player.setVelocity(direction.multiply(gSpeed));
		}
	}
	
	private void progressLeafDome() {
		if (!player.isSneaking()) {
			this.reset();
			return;
		}
		
		if (!shield.isEmpty()) {
			for (TempBlock tb : shield) {
				tb.revertBlock();
			}
			shield.clear();
		}
		
		for (Location loc : GeneralMethods.getCircle(player.getLocation(), dRadius, 0, true, true, 0)) {
			if (loc.getBlock().isPassable() && !TempBlock.isTempBlock(loc.getBlock())) {
				shield.add(new TempBlock(loc.getBlock(), Material.OAK_LEAVES));
			}
		}
		
		for (Location loc : GeneralMethods.getCircle(player.getLocation(), dRadius - 1, 0, false, true, 0)) {
			if (loc.getBlock().getType() == Material.WATER && !TempBlock.isTempBlock(loc.getBlock())) {
				shield.add(new TempBlock(loc.getBlock(), Material.AIR));
			}
		}
	}
	
	private void progressRegenerate() {
		if (!player.isSneaking()) {
			this.reset();
			return;
		}
		
		if (durability >= maxDurability) {
			this.durability = maxDurability;
			this.reset();
			return;
		}
		
		if (bPlayer.isOnCooldown("regen interval")) {
			return;
		}
		
		Block b = getRandomPlantSource();
		if (b == null) {
			return;
		}
		
		this.sources.add(new TempBlock(b, Material.AIR));
		this.durability += regen;
		
		if (durability >= maxDurability) {
			this.durability = maxDurability;
			this.reset();
			return;
		}
		
		bPlayer.addCooldown("regen interval", 1000);
	}
	
	private int getAbilityCost(String ability) {
		return ProjectAddons.instance.getConfig().getInt("Abilities.Water.PlantArmor.SubAbilities." + ability + ".Cost");
	}
	
	private Block getRandomPlantSource() {
		List<Block> blocks = GeneralMethods.getBlocksAroundPoint(player.getLocation(), selectRange);
		Iterator<Block> iter = blocks.iterator();
		
		while (iter.hasNext()) {
			Block b = iter.next();
			
			if (!isPlantbendable(b)) {
				iter.remove();
			}
		}
		
		if (blocks.isEmpty()) {
			return null;
		}
		
		return blocks.get(new Random().nextInt(blocks.size()));
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
		return "PlantArmor";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
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
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		ArrayList<MultiAbilityInfoSub> info = new ArrayList<>();
		
		for (ArmorAbility ability : ArmorAbility.values()) {
			info.add(new MultiAbilityInfoSub(ability.getName(), Element.PLANT));
		}
		
		return info;
	}
	
	@Override
	public boolean isEnabled() {
		return ProjectAddons.instance.getConfig().getBoolean("Abilities.Water.PlantArmor.Enabled");
	}
	
	@Override
	public String getDescription() {
		return  "Wrap your body in vines and leaves to create a protective armor which nullifies falling and drowning damage, while also giving speed, jump, and swim boosts! The armor then acts as a source for many subabilities!" + 
				ChatColor.WHITE + "\n[VineWhip] : " + ChatColor.DARK_AQUA + "Throw a whip of vines to damage entities!" +
				ChatColor.WHITE + "\n[RazorLeaf] : " + ChatColor.DARK_AQUA + "Control a spinning disc of leaves to damage entities!" +
				ChatColor.WHITE + "\n[LeafShield] : " + ChatColor.DARK_AQUA + "Hold a circular shield of leaves to block attacks!" +
				ChatColor.WHITE + "\n[Tangle] : " + ChatColor.DARK_AQUA + "Shoot a bundle of vines to constrict enemies!" +
				ChatColor.WHITE + "\n[Leap] : " + ChatColor.DARK_AQUA + "Launch yourself really high into the air from the ground!" +
				ChatColor.WHITE + "\n[Grapple] : " + ChatColor.DARK_AQUA + "Grapple to a point with your vines!" +
				ChatColor.WHITE + "\n[LeafDome] : " + ChatColor.DARK_AQUA + "Surround your body in a dome of leaves!" +
				ChatColor.WHITE + "\n[Regenerate] : " + ChatColor.DARK_AQUA + "Gather more plants to repair armor!" +
				ChatColor.WHITE + "\n[Disperse] : " + ChatColor.DARK_AQUA + "Deactivate your plantarmor!";
	}
	
	@Override
	public String getInstructions() {
		return  "Press sneak to activate multiability\n" +
				"[VineWhip, Tangle, Leap, Grapple, Disperse]: Left Click\n" +
				"[RazorLeaf, LeafShield, LeafDome, Regenerate]: Hold Sneak";
	}

	public static enum ArmorState {
		FORMING, FORMED, DISPERSING;
	}
	
	public static enum ArmorAbility {
		VINEWHIP("VineWhip", true, ClickType.LEFT_CLICK, null),
		RAZORLEAF("RazorLeaf", true, ClickType.SHIFT_DOWN, (player -> !CoreAbility.hasAbility(player, RazorLeaf.class))),
		TANGLE("Tangle", true, ClickType.LEFT_CLICK, null),
		GRAPPLE("Grapple", true, ClickType.LEFT_CLICK, null),
		LEAP("Leap", true, ClickType.LEFT_CLICK, (player -> player.isOnGround())),
		LEAFSHIELD("LeafShield", true, ClickType.SHIFT_DOWN, null),
		LEAFDOME("LeafDome", true, ClickType.SHIFT_DOWN, null),
		REGENERATE("Regenerate", false, ClickType.SHIFT_DOWN, null),
		DISPERSE("Disperse", false, ClickType.LEFT_CLICK, null);
		
		private String name;
		private boolean cost;
		private ClickType type;
		private Predicate<Player> pred;
		
		private ArmorAbility(String name, boolean cost, ClickType type, Predicate<Player> pred) {
			this.name = name;
			this.cost = cost;
			this.type = type;
			this.pred = pred;
		}
		
		public String getName() {
			return name;
		}
		
		public int getDisplaySlot() {
			return ordinal() + 1;
		}
		
		public boolean hasCost() {
			return cost;
		}
		
		public ClickType getType() {
			return type;
		}
		
		public Predicate<Player> getPredicate() {
			return pred;
		}
		
		public static ArmorAbility getAbility(int slot) {
			return values()[slot];
		}
	}
	
}
