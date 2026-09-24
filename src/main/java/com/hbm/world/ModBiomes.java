package com.hbm.world;

import java.util.ArrayList;
import java.util.List;

import com.hbm.dim.duna.biome.BiomeGenBaseDuna;
import com.hbm.dim.hell.BiomeGenTheNest;
import com.hbm.dim.minmus.biome.BiomeGenBaseMinmus;

import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager.BiomeEntry;

public class ModBiomes
{
    /// NETHER BIOMES ///
    public static final List<BiomeEntry> netherBiomes = new ArrayList<>();

    public static void init()
    {
        BiomeGenTheNest.init();
        netherBiomes.add(new BiomeEntry(BiomeGenTheNest.theNest, 16));
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaPlains, Type.COLD, Type.DRY, Type.DEAD);
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaLowlands, Type.COLD, Type.DRY, Type.DEAD);
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaPolar, Type.COLD, Type.DRY, Type.DEAD, Type.SNOWY);
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaHills, Type.COLD, Type.DRY, Type.DEAD, Type.HILLS);
        BiomeDictionary.registerBiomeType(BiomeGenBaseDuna.dunaHills, Type.COLD, Type.DRY, Type.DEAD, Type.SNOWY, Type.MOUNTAIN);
        BiomeDictionary.registerBiomeType(BiomeGenBaseMinmus.minmusCanyon, Type.COLD, Type.DRY, Type.DEAD, Type.SNOWY, Type.MOUNTAIN);
        BiomeDictionary.registerBiomeType(BiomeGenBaseMinmus.minmusPlains, Type.COLD, Type.DRY, Type.DEAD, Type.SNOWY, Type.MOUNTAIN);

    }
}
