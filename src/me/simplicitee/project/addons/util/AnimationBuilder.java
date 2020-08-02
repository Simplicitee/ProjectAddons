package me.simplicitee.project.addons.util;

import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import me.simplicitee.project.addons.util.AnimatedBlock.AnimationStep;

public class AnimationBuilder {

	private Block block;
	private Queue<AnimationStep> steps;
	private Queue<Runnable> tasks;
	private boolean cycle, revert;
	private Particle effect;
	
	public AnimationBuilder(Block block) {
		this.block = block;
		this.steps = new LinkedList<>();
		this.tasks = new LinkedList<>();
		this.cycle = false;
		this.revert = true;
		this.effect = null;
	}
	
	public AnimationBuilder addDestroyTask(Runnable runnable) {
		this.tasks.add(runnable);
		return this;
	}
	
	public AnimationBuilder addStep(BlockData data, long duration) {
		this.steps.add(new AnimationStep(data, duration));
		return this;
	}
	
	public AnimationBuilder addStep(Material mat, long duration) {
		this.steps.add(new AnimationStep(mat.createBlockData(), duration));
		return this;
	}
	
	public AnimationBuilder doesCycle(boolean cycle) {
		this.cycle = cycle;
		return this;
	}
	
	public AnimationBuilder doesRevert(boolean revert) {
		this.revert = revert;
		return this;
	}
	
	public AnimationBuilder effect(Particle effect) {
		this.effect = effect;
		return this;
	}
	
	public AnimatedBlock start() {
		return new AnimatedBlock(block, steps, cycle, revert, tasks, effect);
	}
}
