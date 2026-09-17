package com.hbm.dim.czechoslowakia;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.dim.SolarSystem;
import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.Random;

public class WorldGeneratorCzechoslowakia implements IWorldGenerator {
	public WorldGeneratorCzechoslowakia() {
		BlockOre.addValidBody(ModBlocks.ore_bedrock_oil, SolarSystem.Body.CZECHOSLOWAKIA);
		BlockOre.addValidBody(ModBlocks.ore_bedrock_concrete, SolarSystem.Body.CZECHOSLOWAKIA);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {

	}
}
