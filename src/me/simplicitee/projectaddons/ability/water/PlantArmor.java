package me.simplicitee.projectaddons.ability.water;

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
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempBlock;

import me.simplicitee.projectaddons.ProjectAddons;
import net.md_5.bungee.api.ChatColor;

public class PlantArmor extends PlantAbility implements AddonAbility, MultiAbility {
	
	public static enum ArmorState {
		FORMING, FORMED, DISPERSING;
	}
	
	public static enum ArmorAbility {
		NONE("null", -1, false, ClickType.LEFT_CLICK, null),
		VINEWHIP("VineWhip", 0, true, ClickType.LEFT_CLICK, null),
		RAZORLEAF("RazorLeaf", 1, true, ClickType.SHIFT_DOWN, (player -> !CoreAbility.hasAbility(player, RazorLeaf.class))),
		LEAFSHIELD("LeafShield", 2, true, ClickType.SHIFT_DOWN, null),
		TANGLE("Tangle", 3, true, ClickType.LEFT_CLICK, null),
		SLOT5("Slot5", 4, true, ClickType.LEFT_CLICK, null),
		LEAP("Leap", 5, true, ClickType.LEFT_CLICK, (player -> player.isOnGround())),
		SLOT7("Slot7", 6, true, ClickType.SHIFT_DOWN, null),
		REGENERATE("Regenerate", 7, false, ClickType.SHIFT_DOWN, null),
		DISPERSE("Disperse", 8, false, ClickType.LEFT_CLICK, null);
		
		private String name;
		private int slot;
		private boolean cost;
		private ClickType type;
		private Predicate<Player> pred;
		
		private ArmorAbility(String name, int slot, boolean cost, ClickType type, Predicate<Player> pred) {
			this.name = name;
			this.slot = slot;
			this.cost = cost;
			this.type = type;
			this.pred = pred;
		}
		
		public String getName() {
			return name;
		}
		
		public int getSlot() {
			return slot;
		}
		
		public int getDisplaySlot() {
			return slot + 1;
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
			for (ArmorAbility ability : ArmorAbility.values()) {
				if (ability.getSlot() == slot) {
					return ability;
				}
			}
			
			return NONE;
		}
	}
	
	// general variables
	private long duration, cooldown;
	private int durability, maxDurability, swim, speed, jump;
	private ArmorState state;
	private ArmorAbility active;
	private BossBar bar;
	private ItemStack[] armors;
	private TempArmor armor;
	
	// general variables (set in activate(..) always)
	private Location current;
	private Vector direction;
	private int angle;
	
	// forming and dispersing variables
	private int requiredPlants;
	private double selectRange;
	private Set<TempBlock> sources;
	
	// vinewhip variables
	private boolean forward;
	private int range, maxRange;
	private double vdmg;
	
	// leafshield variables
	private Set<TempBlock> shield;
	private int radius;
	
	// tangle variables
	private double tRadius;
	private long tDuration;
	private double tRange;
	
	// leap variables
	private double power;
	
	// regenerate variables
	private int regen;

	public PlantArmor(Player player, ClickType type) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
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
			MultiAbilityManager.bindMultiAbility(player, "PlantArmor");
			this.duration = ProjectAddons.instance.getConfig().getLong("Abilities.PlantArmor.Duration");
			this.cooldown = ProjectAddons.instance.getConfig().getLong("Abilities.PlantArmor.Cooldown");
			this.durability = this.maxDurability = ProjectAddons.instance.getConfig().getInt("Abilities.PlantArmor.Durability");
			this.swim = ProjectAddons.instance.getConfig().getInt("Abilities.PlantArmor.Boost.Swim") + 1;
			this.speed = ProjectAddons.instance.getConfig().getInt("Abilities.PlantArmor.Boost.Speed") + 1;
			this.jump = ProjectAddons.instance.getConfig().getInt("Abilities.PlantArmor.Boost.Jump") + 1;
			this.armor = null;
			
			final ItemStack head = new ItemStack(Material.OAK_LEAVES, 1);
			final ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
			final ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
			final ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);

			final LeatherArmorMeta metaChest = (LeatherArmorMeta) chestplate.getItemMeta();
			final LeatherArmorMeta metaLegs = (LeatherArmorMeta) leggings.getItemMeta();
			final LeatherArmorMeta metaBottom = (LeatherArmorMeta) boots.getItemMeta();

			metaChest.setColor(Color.fromRGB(61, 153, 112));
			metaLegs.setColor(Color.fromRGB(61, 153, 112));
			metaBottom.setColor(Color.fromRGB(61, 153, 112));

			chestplate.setItemMeta(metaChest);
			leggings.setItemMeta(metaLegs);
			boots.setItemMeta(metaBottom);

			this.armors = new ItemStack[] { boots, leggings, chestplate, head };
			
			if (durability < 1000) {
				durability = 1000;
			} else if (durability > 10000) {
				durability = 10000;
			}
			
			this.state = ArmorState.FORMING;
			this.active = ArmorAbility.NONE;
			
			this.requiredPlants = ProjectAddons.instance.getConfig().getInt("Abilities.PlantArmor.RequiredPlants");
			this.selectRange = ProjectAddons.instance.getConfig().getDouble("Abilities.PlantArmor.SelectRange");
			this.sources = new HashSet<>();
			
			this.forward = true;
			this.range = 0;
			this.maxRange = ProjectAddons.instance.getConfig().getInt("Abilities.PlantArmor.SubAbilities.VineWhip.Range");
			this.vdmg = ProjectAddons.instance.getConfig().getDouble("Abilities.PlantArmor.SubAbilities.VineWhip.Damage");
			
			this.shield = new HashSet<>();
			this.radius = ProjectAddons.instance.getConfig().getInt("Abilities.PlantArmor.SubAbilities.LeafShield.Radius");
			
			this.current = null;
			this.tRadius = ProjectAddons.instance.getConfig().getDouble("Abilities.PlantArmor.SubAbilities.Tangle.Radius");
			this.tRange = ProjectAddons.instance.getConfig().getDouble("Abilities.PlantArmor.SubAbilities.Tangle.Range");
			this.tDuration = ProjectAddons.instance.getConfig().getLong("Abilities.PlantArmor.SubAbilities.Tangle.Duration");
			this.angle = 0;
			
			this.power = ProjectAddons.instance.getConfig().getDouble("Abilities.PlantArmor.SubAbilities.Leap.Power");
			
			this.regen = ProjectAddons.instance.getConfig().getInt("Abilities.PlantArmor.SubAbilities.Regenerate.RegenAmount");
			
			start();
		}
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
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
			bar.setProgress((double) durability / maxDurability);
			
			if (bar.getProgress() <= 0.5 && bar.getProgress() > 0.15) {
				bar.setTitle(ChatColor.DARK_AQUA + "Durability [" + ChatColor.YELLOW + durability + ChatColor.DARK_AQUA + " / " + maxDurability + "]");
				bar.setColor(BarColor.YELLOW);
			} else if (bar.getProgress() <= 0.15) {
				bar.setTitle(ChatColor.DARK_AQUA + "Durability [" + ChatColor.RED    + durability + ChatColor.DARK_AQUA + " / " + maxDurability + "]");
				bar.setColor(BarColor.RED);
			} else {
				bar.setTitle(ChatColor.DARK_AQUA + "Durability [" + ChatColor.GREEN  + durability + ChatColor.DARK_AQUA + " / " + maxDurability + "]");
				bar.setColor(BarColor.GREEN);
			}
			
			player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 5, swim), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, speed), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 5, jump), true);

			if (active != ArmorAbility.NONE && player.getInventory().getHeldItemSlot() != active.getSlot()) {
				this.reset();
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
				case SLOT5:
					progressVineSwing();
					break;
				case LEAP:
					leap();
					break;
				case SLOT7:
				case RAZORLEAF:
				case DISPERSE:
				case NONE:
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
		this.armor.revert();
		
		if (!sources.isEmpty()) {
			for (TempBlock tb : sources) {
				if (tb.getBlock().getType() == Material.AIR) {
					tb.revertBlock();
				}
			}
		}
		
		if (!shield.isEmpty()) {
			for (TempBlock tb : shield) {
				if (tb.getBlock().getType() == Material.AIR) {
					tb.revertBlock();
				}
			}
		}
		
		this.sources.clear();
		this.shield.clear();
		this.bar.removeAll();
	}
	
	private void reset() {
		if (active != ArmorAbility.NONE) {
			bPlayer.addCooldown(active.getName(), ProjectAddons.instance.getConfig().getLong("Abilities.PlantArmor.SubAbilities." + active.getName() + ".Cooldown"));
		
			this.active = ArmorAbility.NONE;
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
		
		this.current = null;
		this.direction = null;
		this.angle = 0;
	}
	
	public void activate(int slot, ClickType type) {
		if (this.active != ArmorAbility.NONE) {
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
			this.current = player.getEyeLocation();
			this.direction = current.getDirection().clone().normalize();
		}
	}
	
	public boolean damage(int amount) {
		int diff = this.durability - amount;
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
			
			return;
		}
		
		if (!sources.isEmpty()) {
			Location display = player.getLocation().clone();
			for (int y = 0; y < sources.size(); y++) {
				for (int angle = 0; angle < 360; angle += 15) {
					double x = 0.5 * Math.cos(Math.toRadians(angle));
					double z = 0.5 * Math.sin(Math.toRadians(angle));
					double dy = (((double) y) / requiredPlants) * player.getHeight();
					
					display.add(x, dy, z);
					
					GeneralMethods.displayColoredParticle("3D9970", display);
					
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
		if (forward) {
			range++;
			
			if (range >= maxRange) {
				forward = false;
				return;
			}
		} else {
			range--;
			
			if (range <= 0) {
				this.reset();	
				return;
			}
		}
		
		Location last = GeneralMethods.getRightSide(player.getLocation().clone().add(0, 1, 0), 0.45);
		last.getDirection().normalize().multiply(1.2);
		
		for (int i = 0; i < range; i++) {
			last.add(last.getDirection());
			
			if (last.getBlock().getType().isSolid()) {
				forward = false;
				return;
			}
			
			GeneralMethods.displayColoredParticle("3D9970", last, 3, 0.1, 0.1, 0.1);
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(last, 1)) {
				if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
					DamageHandler.damageEntity(e, vdmg, this);
					this.forward = false;
					return;
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
		
		Vector direction = player.getEyeLocation().getDirection().clone();
		Location center = player.getEyeLocation().clone().add(direction.normalize().multiply(radius + 1));
		shield.add(new TempBlock(center.getBlock(), Material.OAK_LEAVES));
		
		for (int i = 1; i <= radius; i++) {
			for (double angle = 0; angle < 360; angle += 360 / (i * 9)) {
				Vector ortho = GeneralMethods.getOrthogonalVector(direction, angle, i);
				center.add(ortho);
				
				Block b = center.getBlock();
				
				if (isAir(b.getType()) || b.isPassable()) {
					if (!TempBlock.isTempBlock(b)) {
						shield.add(new TempBlock(b, Material.OAK_LEAVES));
					}
				}
				
				center.subtract(ortho);
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
				new TempBlock(e.getLocation().clone().add(0, e.getHeight(), 0).getBlock(), Material.OAK_LEAVES).setRevertTime(tDuration);
				
				this.reset();
				
				return;
			}
		}
		
		for (int i = 0; i < 3; i++) {
			Vector ov = GeneralMethods.getOrthogonalVector(direction, (double) (angle + (120 * i)), tRadius);
			Location pl = current.clone().add(ov.clone());
			GeneralMethods.displayColoredParticle("3D9970", pl);
		}
		
		angle += 30;
		
		if (angle == 360) {
			angle = 0;
		}
	}
	
	private void progressVineSwing() {
	}
	
	private void leap() {
		Location ground = player.getLocation().clone();
		
		for (double i = 0; i < 1; i += 0.25) {
			for (int angle = 0; angle < 360; angle += 15) {
				double x = (1 - i) * Math.cos(Math.toRadians(angle));
				double z = (1 - i) * Math.sin(Math.toRadians(angle));
				
				ground.add(x, i, z);
				
				GeneralMethods.displayColoredParticle("3D9970", ground);
				
				ground.subtract(x, i, z);
			}
		}
		
		player.setVelocity(direction.add(new Vector(0, 0.8, 0)).multiply(power));
		
		this.reset();
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
		return ProjectAddons.instance.getConfig().getInt("Abilities.PlantArmor.SubAbilities." + ability + ".Cost");
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
		
		info.add(new MultiAbilityInfoSub("VineWhip", Element.PLANT));
		info.add(new MultiAbilityInfoSub("RazorLeaf", Element.PLANT));
		info.add(new MultiAbilityInfoSub("LeafShield", Element.PLANT));
		info.add(new MultiAbilityInfoSub("Tangle", Element.PLANT));
		info.add(new MultiAbilityInfoSub("slot 5", Element.PLANT));
		info.add(new MultiAbilityInfoSub("Leap", Element.PLANT));
		info.add(new MultiAbilityInfoSub("slot 7", Element.PLANT));
		info.add(new MultiAbilityInfoSub("Regenerate", Element.PLANT));
		info.add(new MultiAbilityInfoSub("Disperse", Element.PLANT));
		
		return info;
	}
}
