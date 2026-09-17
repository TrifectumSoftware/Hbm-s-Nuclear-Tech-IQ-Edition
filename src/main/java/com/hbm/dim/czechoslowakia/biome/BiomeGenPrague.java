package com.hbm.dim.czechoslowakia.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.BiomeGenBaseCelestial;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class BiomeGenPrague extends BiomeGenBaseCelestial {
	public static final Height height = new Height(0.125F, 0.125F);
	public BiomeGenPrague(int id) {
		super(id);
		this.setBiomeName("Prague");
		this.theBiomeDecorator.generateLakes = false;
		this.setHeight(height);
		this.topBlock = ModBlocks.concrete;
		this.fillerBlock = ModBlocks.concrete;
	}

	@Override
	public void genTerrainBlocks(World world, Random random, Block[] blocks, byte[] metadata, int x, int z, double noise) {

	}

	@Override
	public void decorate(World world, Random random, int chunkX, int chunkZ) {
		/*
		if (WorldConfig.betonSpawn > 0 && random.nextInt(WorldConfig.betonSpawn) == 0) {
			int x = chunkX * 16;
			int z = chunkZ * 16;
			for(int bx = 2; bx < 14; bx++)
				for(int bz = 2; bz <= 14; bz++)
					world.setBlock(x + bx, 0, z + bz, ModBlocks.ore_bedrock_concrete, 0, 2);
		} else if (WorldConfig.bedrockOilSpawn > 0 && random.nextInt(WorldConfig.bedrockOilSpawn) == 0) {
			int x = chunkX * 16;
			int z = chunkZ * 16;
			for(int bx = 2; bx < 14; bx++)
				for(int bz = 2; bz < 14; bz++)
					world.setBlock(x + bx, 0, z + bz, ModBlocks.ore_bedrock_oil, 0, 2);
		}
		*/
	}
}
