package com.hbm.dim.dross;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenTiltedSpires;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class ChunkProviderDross extends ChunkProviderCelestial {
// im sorry bro
	private MapGenTiltedSpires spires = new MapGenTiltedSpires(1, 4, 0.75F);
	private MapGenTiltedSpires megaSpires = new MapGenTiltedSpires(5, 25, 0.8F);
	private MapGenTiltedSpires superSpires = new MapGenTiltedSpires(10, 60, 0.8F);

	public ChunkProviderDross(World world, long seed) {
		super(world, seed);

		this.stoneBlock = ModBlocks.dross_waste;
		this.seaBlock = ModBlocks.slush_block;
		this.seaLevel = 70;

		spires.rock = ModBlocks.deco_steel;
		spires.rock2 = ModBlocks.deco_rusty_steel;
		spires.rock2Chance = 0.5F;
		spires.regolith = null;
		spires.curve = false;
		spires.minSize = 2;
		spires.maxSize = 6;
		spires.minPoint = 1.5F;
		spires.maxPoint = 4.0F;
		spires.minTilt = 0.2F;
		spires.maxTilt = 2.5F;

		megaSpires.rock = ModBlocks.deco_steel;
		megaSpires.rock2 = ModBlocks.deco_rusty_steel;
		megaSpires.rock2Chance = 0.5F;
		megaSpires.regolith = null;
		megaSpires.curve = false;
		megaSpires.minSize = 16;
		megaSpires.maxSize = 40;
		megaSpires.minPoint = 1.5F;
		megaSpires.maxPoint = 5.0F;
		megaSpires.minTilt = 0.1F;
		megaSpires.maxTilt = 1.5F;
		megaSpires.hollowChance = 0.35F;
		megaSpires.hollowScale = 0.55F;
		megaSpires.hollowFracture = 0.05F;

		superSpires.rock = ModBlocks.deco_steel;
		superSpires.rock2 = ModBlocks.deco_rusty_steel;
		superSpires.rock2Chance = 0.5F;
		superSpires.regolith = null;
		superSpires.curve = false;
		superSpires.minSize = 24;
		superSpires.maxSize = 60;
		superSpires.minPoint = 2.0F;
		superSpires.maxPoint = 6.0F;
		superSpires.minTilt = 0.3F;
		superSpires.maxTilt = 2.0F;
		superSpires.hollowChance = 0.25F;
		superSpires.hollowScale = 0.5F;
		superSpires.hollowFracture = 0.08F;
		superSpires.biteChance = 0.5F;
		superSpires.biteOffset = 0.4F;
		superSpires.biteRadius = 0.55F;
		superSpires.biteThickness = 3;
		superSpires.setRange(8);
		superSpires.maxY = 255;
	}

	@Override
	protected Block getFlatWorldBlock(Block block) {
		if(block == Blocks.water || block == Blocks.flowing_water) return ModBlocks.slush_block;
		if(block == Blocks.grass || block == Blocks.sand) return ModBlocks.dross_waste;
		if(block == Blocks.dirt || block == Blocks.stone || block == Blocks.sandstone) return ModBlocks.dross_waste;
		return block;
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);

		spires.func_151539_a(this, worldObj, x, z, buffer.blocks);
		megaSpires.func_151539_a(this, worldObj, x, z, buffer.blocks);
		superSpires.func_151539_a(this, worldObj, x, z, buffer.blocks);

		return buffer;
	}
}
