package me.simplicitee.project.addons.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

import me.simplicitee.project.addons.ProjectAddons;

public class AnimatedBlock {
	
	private static final Map<Block, AnimatedBlock> INSTANCES = new HashMap<>();

	private Block block;
	private BlockData original;
	private Particle effect;
	private LinkedList<AnimationStep> animation;
	private boolean cycle, revert, destroyed = false;
	private long prevTime;
	private Queue<Runnable> destroyTasks;
	
	AnimatedBlock(Block block, Queue<AnimationStep> animation, boolean cycle, boolean revert, Queue<Runnable> revertTasks, Particle effect) {
		this.block = block;
		this.original = block.getBlockData();
		this.effect = effect;
		this.animation = new LinkedList<>(animation);
		this.destroyTasks = new LinkedList<>(revertTasks);
		this.cycle = cycle;
		this.revert = revert;
		this.block.setBlockData(animation.peek().getBlockData(), false);
		this.prevTime = System.currentTimeMillis();

		INSTANCES.put(block, this);
		
		new BukkitRunnable() {

			@Override
			public void run() {
				if (this.isCancelled()) {
					return;
				}
				
				if (!checkStep()) {
					this.cancel();
				}
			}
			
		}.runTaskTimer(ProjectAddons.instance, 0, 1);
	}
	
	private boolean checkStep() {
		if (destroyed) {
			return false;
		}
		
		AnimationStep step = animation.peek();
		
		if (effect != null && Math.random() > 0.5) { 
			block.getWorld().spawnParticle(effect, block.getLocation().add(0.5, 1, 0.5), 1, 0.4, 0, 0.4);
		}
		
		if (prevTime + step.getDuration() <= System.currentTimeMillis()) {
			prevTime = System.currentTimeMillis();
			animation.poll();
			
			if (cycle) {
				animation.add(step);
			} else if (animation.isEmpty()) {
				destroy();
				return false;
			}
			
			block.setBlockData(animation.peek().getBlockData(), false);
		}
		
		return true;
	}
	
	public Block getBlock() {
		if (destroyed) {
			return null;
		}
		
		return block;
	}
	
	public AnimationStep getCurrentStep() {
		if (destroyed) {
			return null;
		}
		
		return animation.peek();
	}
	
	public boolean isCycling() {
		if (destroyed) {
			return false;
		}
		
		return cycle;
	}
	
	public void destroy() {
		if (destroyed) {
			return;
		}
		
		if (revert) {
			block.setBlockData(original, true);
		}
		
		animation.clear();
		destroyed = true;
		INSTANCES.remove(block);
		
		while (!destroyTasks.isEmpty()) {
			destroyTasks.poll().run();
		}
	}
	
	public static void destroyAll() {
		for (AnimatedBlock anim : INSTANCES.values()) {
			if (anim.revert) {
				anim.block.setBlockData(anim.original, true);
			}
			
			anim.animation.clear();
			anim.destroyed = true;
			
			while (!anim.destroyTasks.isEmpty()) {
				anim.destroyTasks.poll().run();
			}
		}
		
		INSTANCES.clear();
	}
	
	public static class AnimationStep {
		private BlockData data;
		private long duration;
		
		public AnimationStep(BlockData data, long duration) {
			this.data = data;
			this.duration = duration;
		}
		
		public BlockData getBlockData() {
			return data;
		}
		
		public long getDuration() {
			return duration;
		}
	}
}
