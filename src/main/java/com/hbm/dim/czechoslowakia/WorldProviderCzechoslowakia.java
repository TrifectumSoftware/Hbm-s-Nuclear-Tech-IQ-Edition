package com.hbm.dim.czechoslowakia;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.czechoslowakia.biome.BiomeGenPrague;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldProviderCzechoslowakia extends WorldProviderCelestial {
	public WorldProviderCzechoslowakia() {}

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerHell(new BiomeGenPrague(SpaceConfig.pragueBiome), this.dimensionId);
	}

	@Override
	public String getDimensionName() {
		return "Czechoslowakia";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderCzechoslowakia(this.worldObj, this.getSeed());
	}
}
