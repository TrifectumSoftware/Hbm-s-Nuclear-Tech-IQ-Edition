package com.hbm.dim.czechoslowakia;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockOre;
import com.hbm.config.WorldConfig;
import com.hbm.dim.ChunkProviderCelestial;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class ChunkProviderCzechoslowakia extends ChunkProviderCelestial {
	public ChunkProviderCzechoslowakia(World world, long seed) {
		super(world, seed);

		this.stoneBlock = Blocks.stone;
	}

	@Override
	protected BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buf = new BlockMetaBuffer();
		this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, x * 16, z * 16, 16, 16);
		this.generateBlocks(x, z, buf.blocks);
		return buf;
	}

	private final Random random = new Random();
	@Override
	protected void generateBlocks(int x, int z, Block[] blocks) {
		for (int i = 0; i < 256; i++) {
			blocks[i * 256] = Blocks.bedrock;
			for (int j = 1; j < 64; j++) {
				blocks[i * 256 + j] = ModBlocks.concrete_smooth;
			}
		}

		this.random.setSeed((x * 53253L + z * 23523L) ^ this.findSeed());
		int floors = Math.max((int)(this.random.nextGaussian() * 4 + 4) + 2, 3);
		int rot = this.random.nextInt(4);
		byte[][] pattern = STRUCTURES[this.random.nextInt(STRUCTURES.length)];
		Block walls = ModBlocks.concrete_rebar;
		Block windows = Blocks.glass;
		placePattern(blocks, pattern, floors, rot, walls, windows);

		if (WorldConfig.betonSpawn > 0 && this.random.nextInt(WorldConfig.betonSpawn) == 0) {
			for(int bx = 2; bx < 14; bx++)
				for(int bz = 2; bz <= 14; bz++) {
					int pos = ((bx) * 16 + (bz)) * 256;
					blocks[pos] = ModBlocks.ore_bedrock_concrete;
				}
		} else if (WorldConfig.bedrockOilSpawn > 0 && this.random.nextInt(WorldConfig.bedrockOilSpawn) == 0) {
			for(int bx = 2; bx < 14; bx++)
				for(int bz = 2; bz < 14; bz++) {
					int pos = ((bx) * 16 + (bz)) * 256;
					blocks[pos] = ModBlocks.ore_bedrock_oil;
				}
		}
	}

	protected long findSeed() {
		return this.worldObj != null ? this.worldObj.getSeed() : 2137L;
	}

	private static void placePattern(Block[] blocks, byte[][] pattern, int floors, int rot, Block walls, Block windows) {
		int px, pz;
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				byte piece = pattern[x][z];
				if (piece != 0) {
					px = rotateX(x, z, rot);
					pz = rotateZ(x, z, rot);
					int pos = (px * 16 + pz) * 256;
					switch (piece) {
						case 1:
							blocks[pos + 63] = ModBlocks.asphalt;
							break;
						case 2:
							blocks[pos + 63] = ModBlocks.brick_concrete;
							break;
						case 3:
							blocks[pos + 63] = ModBlocks.brick_concrete;
							blocks[pos + 64] = ModBlocks.fence_metal;
							break;
						case 4:
							blocks[pos + 63] = Blocks.grass;
							break;
						case 5:
							blocks[pos+63] = walls;
							for (int i = 0; i < floors * 3; i++) {
								if ((i % 3) == 2) {
									blocks[pos + 64 + i] = walls;
								}
							}
							break;
						case 6:
							blocks[pos+63] = walls;
							for (int i = 0; i < floors * 3; i++) {
								blocks[pos+64+i] = walls;
							}
							break;
						case 7:
							blocks[pos+63] = walls;
							for (int i = 0; i < floors * 3; i++) {
								blocks[pos+64+i] = (i % 3) != 1 ? walls : windows;
							}
							break;
					}
				}
			}
		}
	}
	private static int rotateX(int x, int z, int rot) {
		switch (rot) {
			case 0:
				return x;
			case 1:
				return 15 - z;
			case 2:
				return 15 - x;
			case 3:
				return z;
			default:
				throw new IllegalStateException("Unexpected rotation: " + rot);
		}
	}
	private static int rotateZ(int x, int z, int rot) {
		switch (rot) {
			case 0:
				return z;
			case 1:
				return x;
			case 2:
				return 15 - z;
			case 3:
				return 15 - x;
			default:
				throw new IllegalStateException("Unexpected rotation: " + rot);
		}
	}

	private static final byte[][][] STRUCTURES = {
	//	Patterns.PARK,
		Patterns.SQUARE,
		Patterns.SQUARE,
		Patterns.SQUARE,
		Patterns.APARTMENTS,
		Patterns.APARTMENTS,
		Patterns.LUXURY,
		Patterns.DUPLEX,
		Patterns.DUPLEX,
		Patterns.ODD_SHAPE,
		Patterns.ODD_SHAPE,
		Patterns.VERY_TIGHT,
		Patterns.VERY_TIGHT,
		Patterns.VERY_TIGHT,
	};

	private static class Patterns {
		public static final byte[][] TEMPLATE = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		};
		public static final byte[][] PARK = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,3,3,3,2,2,2,2,2,2,2,2,3,3,3,1,},
			{1,3,4,4,4,4,4,4,4,4,4,4,4,4,3,1,},
			{1,3,4,4,4,4,4,4,4,4,4,4,4,4,3,1,},
			{1,2,4,4,4,4,4,4,4,4,4,4,4,4,2,1,},
			{1,2,4,4,4,4,4,4,4,4,4,4,4,4,2,1,},
			{1,2,4,4,4,4,4,4,4,4,4,4,4,4,2,1,},
			{1,2,4,4,4,4,4,4,4,4,4,4,4,4,2,1,},
			{1,2,4,4,4,4,4,4,4,4,4,4,4,4,2,1,},
			{1,2,4,4,4,4,4,4,4,4,4,4,4,4,2,1,},
			{1,2,4,4,4,4,4,4,4,4,4,4,4,4,2,1,},
			{1,2,4,4,4,4,4,4,4,4,4,4,4,4,2,1,},
			{1,3,4,4,4,4,4,4,4,4,4,4,4,4,3,1,},
			{1,3,4,4,4,4,4,4,4,4,4,4,4,4,3,1,},
			{1,3,3,3,2,2,2,2,2,2,2,2,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		};
		public static final byte[][] SQUARE = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,6,6,7,7,6,6,7,7,6,6,0,2,1,},
			{1,2,0,6,5,5,5,5,6,5,5,5,6,0,2,1,},
			{1,2,0,7,5,5,5,5,6,5,5,5,7,0,2,1,},
			{1,2,0,7,5,5,5,5,5,5,5,5,7,0,2,1,},
			{1,2,0,6,5,5,5,5,6,5,5,5,6,0,2,1,},
			{1,2,0,6,6,5,6,6,6,5,6,6,6,0,2,1,},
			{1,2,0,7,5,5,5,6,5,5,5,5,7,0,2,1,},
			{1,2,0,7,5,5,5,6,5,5,5,5,7,0,2,1,},
			{1,2,0,6,5,5,5,6,5,5,5,5,6,0,2,1,},
			{1,2,0,6,6,7,7,6,6,7,7,6,6,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		};
		public static final byte[][] APARTMENTS = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,3,3,3,3,3,0,0,3,3,3,3,3,2,1,},
			{1,2,3,4,4,4,3,0,0,3,4,4,4,3,2,1,},
			{1,2,6,6,7,7,6,7,7,6,7,7,6,6,2,1,},
			{1,2,7,5,5,5,6,5,5,6,5,5,5,7,2,1,},
			{1,2,7,5,5,5,5,5,5,6,5,5,5,7,2,1,},
			{1,2,6,5,5,5,6,5,5,6,5,5,5,6,2,1,},
			{1,2,7,5,5,5,6,5,5,5,5,5,5,7,2,1,},
			{1,2,7,5,5,5,6,5,5,6,5,5,5,7,2,1,},
			{1,2,6,6,7,7,6,7,7,6,7,7,6,6,2,1,},
			{1,2,0,3,4,4,4,4,4,4,4,4,3,0,2,1,},
			{1,2,0,3,4,4,4,4,4,4,4,4,3,0,2,1,},
			{1,2,2,3,3,3,3,2,2,3,3,3,3,2,2,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		};
		public static final byte[][] LUXURY = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,6,6,6,6,6,6,6,6,6,6,6,3,3,3,1,},
			{1,6,5,5,5,5,5,6,5,5,5,6,4,4,3,1,},
			{1,7,5,5,5,5,5,5,5,5,5,7,4,4,3,1,},
			{1,7,5,5,5,5,5,6,5,5,5,7,4,4,3,1,},
			{1,6,6,6,5,6,6,6,5,5,5,6,4,4,3,1,},
			{1,6,5,5,5,5,5,6,5,5,5,6,4,4,3,1,},
			{1,7,5,5,5,5,5,6,5,5,5,7,4,4,3,1,},
			{1,7,5,5,5,5,5,6,5,5,5,7,4,4,3,1,},
			{1,6,5,5,5,5,5,6,5,5,5,6,4,4,3,1,},
			{1,6,6,7,7,7,6,6,7,7,6,6,4,4,3,1,},
			{1,3,4,4,4,4,4,4,4,4,4,4,4,4,3,1,},
			{1,3,4,4,4,4,4,4,4,4,4,4,4,4,3,1,},
			{1,3,4,4,4,4,4,4,4,4,4,4,4,4,3,1,},
			{1,3,3,3,3,3,3,3,2,2,3,3,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		};
		public static final byte[][] DUPLEX = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,2,2,6,6,7,7,6,6,6,7,7,6,6,2,1,},
			{1,2,0,6,5,5,5,5,6,5,5,5,5,6,2,1,},
			{1,2,0,6,5,5,5,5,6,5,5,5,5,6,2,1,},
			{1,2,0,6,5,5,5,5,6,5,5,5,5,6,2,1,},
			{1,2,0,6,6,7,7,6,6,6,7,7,6,6,2,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,2,6,6,7,7,6,6,6,7,7,6,6,0,2,1,},
			{1,2,6,5,5,5,5,6,5,5,5,5,6,0,2,1,},
			{1,2,6,5,5,5,5,6,5,5,5,5,6,0,2,1,},
			{1,2,6,5,5,5,5,6,5,5,5,5,6,0,2,1,},
			{1,2,6,6,7,7,6,6,6,7,7,6,6,2,2,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		};
		public static final byte[][] ODD_SHAPE = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,3,3,6,6,7,7,6,6,0,2,1,},
			{1,2,0,0,0,0,0,6,6,5,5,6,6,0,2,1,},
			{1,2,0,3,0,0,0,7,5,5,5,5,7,0,2,1,},
			{1,2,0,3,0,0,0,7,5,5,5,5,7,0,2,1,},
			{1,2,0,6,6,7,7,6,6,5,5,6,6,0,2,1,},
			{1,2,0,6,6,5,5,6,6,5,5,6,6,0,2,1,},
			{1,2,0,7,5,5,5,5,5,5,5,5,7,0,2,1,},
			{1,2,0,7,5,5,5,5,5,5,5,5,7,0,2,1,},
			{1,2,0,6,6,5,5,6,6,5,5,6,6,0,2,1,},
			{1,2,0,6,6,7,7,6,6,7,7,6,6,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		};
		public static final byte[][] VERY_TIGHT = {
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,6,7,6,7,6,7,6,7,6,3,3,3,1,},
			{1,2,0,7,5,5,5,5,5,5,5,7,0,0,3,1,},
			{1,2,0,6,5,5,5,5,5,5,5,6,0,0,3,1,},
			{1,2,0,7,5,5,5,5,5,5,5,7,0,0,3,1,},
			{1,2,0,6,5,5,5,5,5,5,5,6,0,0,3,1,},
			{1,2,0,7,5,5,5,5,5,5,5,7,0,0,3,1,},
			{1,2,0,6,5,5,5,5,5,5,5,6,0,0,2,1,},
			{1,2,0,7,5,5,5,5,5,5,5,7,0,0,3,1,},
			{1,2,0,6,7,6,7,6,7,6,7,6,3,3,3,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,0,0,0,0,0,0,0,0,0,0,0,0,2,1,},
			{1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		};
	}
}
