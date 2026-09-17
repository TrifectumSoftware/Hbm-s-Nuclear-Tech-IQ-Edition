package com.hbm.dim.hell;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.NETHER_BRIDGE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.NETHER_CAVE;
import static net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.QUARTZ;

import java.util.List;
import java.util.Random;

import com.hbm.util.Compat;
import com.hbm.world.WorldUtil;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCavesHell;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import net.minecraft.world.gen.feature.WorldGenGlowStone2;
import net.minecraft.world.gen.feature.WorldGenHellLava;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import cpw.mods.fml.common.eventhandler.Event;

public class ChunkProviderHbmHell implements IChunkProvider {

	private Random hellRNG;
	private NoiseGeneratorOctaves noiseGen1;
	private NoiseGeneratorOctaves noiseGen2;
	private NoiseGeneratorOctaves noiseGen3;
	private NoiseGeneratorOctaves noiseGen4;
	private NoiseGeneratorOctaves noiseGen5;
	private NoiseGeneratorOctaves noiseGen6;
	private NoiseGeneratorOctaves noiseGen7;
	private World worldObj;
	private double[] noiseField;
	private MapGenNetherBridge genNetherBridge = new MapGenNetherBridge();
	private MapGenBase netherCaveGenerator = new MapGenCavesHell();
	private double[] sandNoise = new double[256];
	private double[] gravelNoise = new double[256];
	private double[] exclusionNoise = new double[256];
	private BiomeGenBase[] biomesForGeneration;

	{
		genNetherBridge = (MapGenNetherBridge) TerrainGen.getModdedMapGen(genNetherBridge, NETHER_BRIDGE);
		netherCaveGenerator = TerrainGen.getModdedMapGen(netherCaveGenerator, NETHER_CAVE);
	}

	public ChunkProviderHbmHell(World world, long seed) {
		this.worldObj = world;
		this.hellRNG = new Random(seed);
		noiseGen1 = new NoiseGeneratorOctaves(hellRNG, 16);
		noiseGen2 = new NoiseGeneratorOctaves(hellRNG, 16);
		noiseGen3 = new NoiseGeneratorOctaves(hellRNG, 8);
		noiseGen4 = new NoiseGeneratorOctaves(hellRNG, 4);
		noiseGen5 = new NoiseGeneratorOctaves(hellRNG, 4);
		noiseGen6 = new NoiseGeneratorOctaves(hellRNG, 10);
		noiseGen7 = new NoiseGeneratorOctaves(hellRNG, 16);
	}

	public void generateNetherTerrain(int cx, int cz, Block[] blocks) {
		byte b0 = 4; byte b1 = 32;
		int k = b0 + 1; byte b2 = 17; int l = b0 + 1;
		noiseField = initializeNoiseField(noiseField, cx * b0, 0, cz * b0, k, b2, l);

		for(int i1 = 0; i1 < b0; i1++) {
			for(int j1 = 0; j1 < b0; j1++) {
				for(int k1 = 0; k1 < 16; k1++) {
					double d0 = 0.125D;
					double d1 = noiseField[((i1 + 0) * l + j1 + 0) * b2 + k1 + 0];
					double d2 = noiseField[((i1 + 0) * l + j1 + 1) * b2 + k1 + 0];
					double d3 = noiseField[((i1 + 1) * l + j1 + 0) * b2 + k1 + 0];
					double d4 = noiseField[((i1 + 1) * l + j1 + 1) * b2 + k1 + 0];
					double d5 = (noiseField[((i1 + 0) * l + j1 + 0) * b2 + k1 + 1] - d1) * d0;
					double d6 = (noiseField[((i1 + 0) * l + j1 + 1) * b2 + k1 + 1] - d2) * d0;
					double d7 = (noiseField[((i1 + 1) * l + j1 + 0) * b2 + k1 + 1] - d3) * d0;
					double d8 = (noiseField[((i1 + 1) * l + j1 + 1) * b2 + k1 + 1] - d4) * d0;

					for(int l1 = 0; l1 < 8; l1++) {
						double d9 = 0.25D;
						double d10 = d1; double d11 = d2;
						double d12 = (d3 - d1) * d9; double d13 = (d4 - d2) * d9;

						for(int i2 = 0; i2 < 4; i2++) {
							int j2 = i2 + i1 * 4 << 11 | 0 + j1 * 4 << 7 | k1 * 8 + l1;
							short s1 = 128;
							double d14 = 0.25D;
							double d15 = d10;
							double d16 = (d11 - d10) * d14;

							for(int k2 = 0; k2 < 4; k2++) {
								Block block = Blocks.air;
								if(k1 * 8 + l1 < b1) block = Blocks.lava;
								if(d15 > 0.0D) block = Blocks.netherrack;
								blocks[j2] = block;
								j2 += s1;
								d15 += d16;
							}
							d10 += d12; d11 += d13;
						}
						d1 += d5; d2 += d6; d3 += d7; d4 += d8;
					}
				}
			}
		}
	}

	private void replaceBlocksForBiome(int cx, int cz, Block[] blocks, BiomeGenBase[] biomes) {
		ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(this, cx, cz, blocks, biomes);
		MinecraftForge.EVENT_BUS.post(event);
		if(event.getResult() == Event.Result.DENY) return;

		byte b0 = 32;
		byte a0 = 64;
		double freq = 0.03125D;
		sandNoise = noiseGen4.generateNoiseOctaves(sandNoise, cx * 16, cz * 16, 0, 16, 16, 1, freq, freq, 1.0D);
		gravelNoise = noiseGen4.generateNoiseOctaves(gravelNoise, cx * 16, 109, cz * 16, 16, 1, 16, freq, 1.0D, freq);
		exclusionNoise = noiseGen5.generateNoiseOctaves(exclusionNoise, cx * 16, cz * 16, 0, 16, 16, 1, freq * 2.0D, freq * 2.0D, freq * 2.0D);

		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				BiomeGenBase biome = biomes[x + z * 16];
				Block topBlock = biome.topBlock;
				Block fillerBlock = biome.fillerBlock;
				byte topMeta = (byte) biome.field_150604_aj;
				byte fillerMeta = (byte) biome.field_76754_C;

				// i had to do this because for some fucking reason that is beyond my comprehension the top layer of Hell kept on reverting to grass
				// so here we fuckning are

				if(biome == BiomeGenBase.hell) {
					if(topBlock != Blocks.netherrack) topBlock = Blocks.netherrack;
					if(fillerBlock != Blocks.netherrack) fillerBlock = Blocks.netherrack;
				}

				boolean putSoulSand = biome == BiomeGenBase.hell && sandNoise[x + z * 16] + hellRNG.nextDouble() * 0.2D > 0.0D;
				boolean putGravel   = biome == BiomeGenBase.hell && gravelNoise[x + z * 16] + hellRNG.nextDouble() * 0.2D > 0.0D;
				int surfaceDepth = (int)(exclusionNoise[x + z * 16] / 3.0D + 3.0D + hellRNG.nextDouble() * 0.25D);

				int depth = -1;

				for(int y = 127; y >= 0; y--) {
					int index = (x * 16 + z) * 128 + y;

					if(y < 127 - hellRNG.nextInt(5) && y > hellRNG.nextInt(5)) {
						Block here = blocks[index];

						if(here == Blocks.air) {
							depth = -1;
						} else if(here == Blocks.netherrack) {
							if(depth == -1) {
								if(surfaceDepth <= 0) {
									topBlock = Blocks.air;
									fillerBlock = Blocks.netherrack;
								} else if(y >= a0 - 4 && y <= a0 + 1) {
									topBlock = Blocks.netherrack;
									fillerBlock = Blocks.netherrack;
									if(putGravel) { topBlock = Blocks.gravel; }
									if(putGravel) { fillerBlock = Blocks.netherrack; }
									if(putSoulSand) { topBlock = Blocks.soul_sand; }
									if(putSoulSand) { fillerBlock = Blocks.soul_sand; }
								}
								if(y < b0 && topBlock == Blocks.air) {
									topBlock = Blocks.lava;
								}
								depth = surfaceDepth;
								blocks[index] = (y >= a0 - 1) ? topBlock : fillerBlock;
							} else if(depth > 0) {
								depth--;
								blocks[index] = fillerBlock;
							}
						}
					} else {
						blocks[index] = Blocks.bedrock;
					}
				}
			}
		}
	}

	private double[] initializeNoiseField(double[] field, int x, int y, int z, int w, int h, int d) {
		if(field == null) field = new double[w * h * d];
		double d0 = 684.412D; double d1 = 2053.236D;

		double[] noiseData4 = noiseGen6.generateNoiseOctaves(null, x, y, z, w, 1, d, 1.0D, 0.0D, 1.0D);
		double[] noiseData5 = noiseGen7.generateNoiseOctaves(null, x, y, z, w, 1, d, 100.0D, 0.0D, 100.0D);
		double[] noiseData1 = noiseGen3.generateNoiseOctaves(null, x, y, z, w, h, d, d0 / 80.0D, d1 / 60.0D, d0 / 80.0D);
		double[] noiseData2 = noiseGen1.generateNoiseOctaves(null, x, y, z, w, h, d, d0, d1, d0);
		double[] noiseData3 = noiseGen2.generateNoiseOctaves(null, x, y, z, w, h, d, d0, d1, d0);

		double[] adouble1 = new double[h];
		for(int i = 0; i < h; i++) {
			adouble1[i] = Math.cos(i * Math.PI * 6.0D / h) * 2.0D;
			double d2 = i;
			if(i > h / 2) d2 = h - 1 - i;
			if(d2 < 4.0D) { d2 = 4.0D - d2; adouble1[i] -= d2 * d2 * d2 * 10.0D; }
		}
		int k1 = 0; int l1 = 0;
		for(int j2 = 0; j2 < w; j2++) {
			for(int k2 = 0; k2 < d; k2++) {
				double d3 = (noiseData4[l1] + 256.0D) / 512.0D;
				if(d3 > 1.0D) d3 = 1.0D;
				double d4 = 0.0D;
				double d5 = noiseData5[l1] / 8000.0D;
				if(d5 < 0.0D) d5 = -d5;
				d5 = d5 * 3.0D - 3.0D;
				if(d5 < 0.0D) { d5 /= 2.0D; if(d5 < -1.0D) d5 = -1.0D; d5 /= 1.4D; d5 /= 2.0D; d3 = 0.0D; }
				else { if(d5 > 1.0D) d5 = 1.0D; d5 /= 6.0D; }
				d3 += 0.5D; d5 = d5 * h / 16.0D; l1++;
				for(int l2 = 0; l2 < h; l2++) {
					double d6 = 0.0D; double d7 = adouble1[l2];
					double d8 = noiseData2[k1] / 512.0D;
					double d9 = noiseData3[k1] / 512.0D;
					double d10 = (noiseData1[k1] / 10.0D + 1.0D) / 2.0D;
					if(d10 < 0.0D) d6 = d8; else if(d10 > 1.0D) d6 = d9;
					else d6 = d8 + (d9 - d8) * d10;
					d6 -= d7;
					if(l2 > h - 4) { double d11 = (l2 - (h - 4)) / 3.0F; d6 = d6 * (1.0D - d11) + -10.0D * d11; }
					if(l2 < d4) { double d11 = (d4 - l2) / 4.0D; if(d11 < 0.0D) d11 = 0.0D; if(d11 > 1.0D) d11 = 1.0D; d6 = d6 * (1.0D - d11) + -10.0D * d11; }
					field[k1] = d6; k1++;
				}
			}
		}
		return field;
	}

	@Override
	public Chunk provideChunk(int cx, int cz) {
		hellRNG.setSeed(cx * 341873128712L + cz * 132897987541L);
		Block[] blocks = new Block[32768];

		this.generateNetherTerrain(cx, cz, blocks);

		this.biomesForGeneration = this.worldObj.getWorldChunkManager()
				.loadBlockGeneratorData(this.biomesForGeneration, cx * 16, cz * 16, 16, 16);
		this.replaceBlocksForBiome(cx, cz, blocks, this.biomesForGeneration);

		this.netherCaveGenerator.func_151539_a(this, this.worldObj, cx, cz, blocks);
		this.genNetherBridge.func_151539_a(this, this.worldObj, cx, cz, blocks);

		Chunk chunk = new Chunk(this.worldObj, blocks, cx, cz);
		BiomeGenBase[] biomes = this.worldObj.getWorldChunkManager()
				.loadBlockGeneratorData(null, cx * 16, cz * 16, 16, 16);

		if(Loader.isModLoaded(Compat.MOD_EIDS)) {
			short[] biomeArr = WorldUtil.getBiomeShortArray(chunk);
			for(int i = 0; i < biomeArr.length; i++) biomeArr[i] = (short) biomes[i].biomeID;
		} else {
			byte[] biomeArr = chunk.getBiomeArray();
			for(int i = 0; i < biomeArr.length; i++) biomeArr[i] = (byte) biomes[i].biomeID;
		}

		chunk.resetRelightChecks();
		return chunk;
	}

	@Override
	public Chunk loadChunk(int x, int z) { return provideChunk(x, z); }

	@Override
	public void populate(IChunkProvider provider, int cx, int cz) {
		BlockFalling.fallInstantly = true;
		int x = cx * 16; int z = cz * 16;
		BiomeGenBase biome = this.worldObj.getBiomeGenForCoords(x + 16, z + 16);
		this.hellRNG.setSeed(this.worldObj.getSeed());
		long r1 = this.hellRNG.nextLong() / 2L * 2L + 1L;
		long r2 = this.hellRNG.nextLong() / 2L * 2L + 1L;
		this.hellRNG.setSeed(cx * r1 + cz * r2 ^ this.worldObj.getSeed());

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(provider, worldObj, hellRNG, cx, cz, false));

		genNetherBridge.generateStructuresInChunk(worldObj, hellRNG, cx, cz);

		boolean doLava = TerrainGen.populate(provider, worldObj, hellRNG, cx, cz, false, PopulateChunkEvent.Populate.EventType.NETHER_LAVA);
		for(int i = 0; doLava && i < 8; i++)
			new WorldGenHellLava(Blocks.flowing_lava, false).generate(worldObj, hellRNG, x + hellRNG.nextInt(16) + 8, hellRNG.nextInt(120) + 4, z + hellRNG.nextInt(16) + 8);

		int fireCount = hellRNG.nextInt(hellRNG.nextInt(10) + 1) + 1;
		boolean doFire = TerrainGen.populate(provider, worldObj, hellRNG, cx, cz, false, PopulateChunkEvent.Populate.EventType.FIRE);
		for(int i = 0; doFire && i < fireCount; i++)
			new WorldGenFire().generate(worldObj, hellRNG, x + hellRNG.nextInt(16) + 8, hellRNG.nextInt(120) + 4, z + hellRNG.nextInt(16) + 8);

		int glowCount = hellRNG.nextInt(hellRNG.nextInt(10) + 1);
		boolean doGlow = TerrainGen.populate(provider, worldObj, hellRNG, cx, cz, false, PopulateChunkEvent.Populate.EventType.GLOWSTONE);
		for(int i = 0; doGlow && i < glowCount; i++)
			new WorldGenGlowStone1().generate(worldObj, hellRNG, x + hellRNG.nextInt(16) + 8, hellRNG.nextInt(120) + 4, z + hellRNG.nextInt(16) + 8);
		for(int i = 0; doGlow && i < 10; i++)
			new WorldGenGlowStone2().generate(worldObj, hellRNG, x + hellRNG.nextInt(16) + 8, hellRNG.nextInt(128), z + hellRNG.nextInt(16) + 8);

		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(worldObj, hellRNG, x, z));

		WorldGenMinable quartz = new WorldGenMinable(Blocks.quartz_ore, 13, Blocks.netherrack);
		boolean doQuartz = TerrainGen.generateOre(worldObj, hellRNG, quartz, x, z, QUARTZ);
		for(int i = 0; doQuartz && i < 16; i++)
			quartz.generate(worldObj, hellRNG, x + hellRNG.nextInt(16), hellRNG.nextInt(108) + 10, z + hellRNG.nextInt(16));

		for(int i = 0; i < 16; i++)
			new WorldGenHellLava(Blocks.flowing_lava, true).generate(worldObj, hellRNG, x + hellRNG.nextInt(16), hellRNG.nextInt(108) + 10, z + hellRNG.nextInt(16));

		biome.decorate(this.worldObj, this.hellRNG, x, z);

		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(worldObj, hellRNG, x, z));
		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(provider, worldObj, hellRNG, cx, cz, false));

		BlockFalling.fallInstantly = false;
	}

	@Override
	public List getPossibleCreatures(EnumCreatureType type, int x, int y, int z) {
		if(type == EnumCreatureType.monster && genNetherBridge.hasStructureAt(x, y, z))
			return genNetherBridge.getSpawnList();

		BiomeGenBase biome = this.worldObj.getBiomeGenForCoords(x, z);
		return biome == null ? null : biome.getSpawnableList(type);
	}

	@Override public boolean chunkExists(int x, int z) { return true; }
	@Override public boolean saveChunks(boolean flag, net.minecraft.util.IProgressUpdate progress) { return true; }
	@Override public boolean unloadQueuedChunks() { return false; }
	@Override public boolean canSave() { return true; }
	@Override public String makeString() { return "HbmHellRandomLevelSource"; }
	@Override public int getLoadedChunkCount() { return 0; }
	@Override public void recreateStructures(int cx, int cz) { genNetherBridge.func_151539_a(this, worldObj, cx, cz, (Block[]) null); }
	@Override public void saveExtraData() {}
	@Override public net.minecraft.world.ChunkPosition func_147416_a(net.minecraft.world.World world, String id, int x, int y, int z) { return null; }
}
