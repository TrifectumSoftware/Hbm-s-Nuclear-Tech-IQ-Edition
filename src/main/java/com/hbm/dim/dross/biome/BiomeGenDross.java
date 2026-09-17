package com.hbm.dim.dross.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeGenBaseCelestial;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class BiomeGenDross extends BiomeGenBaseCelestial {
	public static final Height height = new Height(-0.1F, 0.25F);
	public BiomeGenDross(int id) {
		super(id);
		this.setBiomeName("Dross Marsh");
		this.setTemperatureRainfall(2.0F, 0.5F);
		this.theBiomeDecorator.generateLakes = false;
		this.setHeight(height);
		this.topBlock = ModBlocks.dross_waste;
		this.fillerBlock = ModBlocks.dross_waste;
	}

	@Override
	public float getSpawningChance() {
		return 0.0F;
	}

	@Override
	public void decorate(World world, Random rand, int chunkX, int chunkZ) {
		super.decorate(world, rand, chunkX, chunkZ);

		//fuuuck its been a week ive forgotten what this was for
		int count = rand.nextInt(2);
		if(count > 0 && rand.nextInt(2) == 0) count = 0;
		List<int[]> placed = new ArrayList<int[]>();

		for(int i = 0; i < count; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;

			// solid floor scanning
			int y = -1;
			for(int scan = world.getTopSolidOrLiquidBlock(x, z); scan > 2; scan--) {
				Block b = world.getBlock(x, scan, z);
				if(b == ModBlocks.dross_waste || b == ModBlocks.dross_slush) {
					Block below = world.getBlock(x, scan - 1, z);
					if(below == ModBlocks.dross_waste || below == ModBlocks.dross_slush || below.getMaterial() != Material.air && below.getMaterial() != Material.water) {
						y = scan;
						break;
					}
				}
			}
			if(y <= 0 || y >= 120) continue;
			if(isOnSpire(world, x, y, z)) continue;

			boolean tooClose = false;
			for(int[] pos : placed) {
				int dx = pos[0] - x;
				int dz = pos[1] - z;
				if(dx * dx + dz * dz < 900) tooClose = true;
			}
			if(tooClose) continue;
			if(isGeyserNearby(world, x, z)) continue;

			placed.add(new int[] {x, z});
			boolean massive = rand.nextInt(10) == 0;
			generateWasteMound(world, rand, x, y, z, massive);
		}
		int sharpCount = 1 + rand.nextInt(2);
		for(int i = 0; i < sharpCount; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;

			int y = -1;
			for(int scan = world.getTopSolidOrLiquidBlock(x, z); scan > 2; scan--) {
				Block b = world.getBlock(x, scan, z);
				if(b == ModBlocks.dross_waste || b == ModBlocks.dross_slush) {
					Block below = world.getBlock(x, scan - 1, z);
					if(below == ModBlocks.dross_waste || below == ModBlocks.dross_slush || below.getMaterial() != Material.air && below.getMaterial() != Material.water) {
						y = scan;
						break;
					}
				}
			}
			if(y <= 0 || y >= 120) continue;
			if(isOnSpire(world, x, y, z)) continue;

			boolean tooClose = false;
			for(int[] pos : placed) {
				int dx = pos[0] - x;
				int dz = pos[1] - z;
				if(dx * dx + dz * dz < 400) tooClose = true;
			}
			if(tooClose) continue;
			if(isGeyserNearby(world, x, z)) continue;

			placed.add(new int[] {x, z});
			generateSharpMound(world, rand, x, y, z);
		}


		if(rand.nextInt(3) == 0) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			int y = -1;
			for(int scan = world.getTopSolidOrLiquidBlock(x, z); scan > 2; scan--) {
				Block b = world.getBlock(x, scan, z);
				if(isCrateGround(world, x, scan, z)) {
					y = scan;
					break;
				}
			}

			if(y > 1 && y < 200 && !isOnSpire(world, x, y, z) && !isGeyserNearby(world, x, z) && isCrateMoundAreaClear(world, x, y, z)) {
				generateCrateMound(world, rand, x, y, z);
			}
		}
	}

	private boolean isCrateMoundAreaClear(World world, int x, int y, int z) {
		int reach = 16;
		for(int dx = -reach; dx <= reach; dx++) {
			for(int dz = -reach; dz <= reach; dz++) {
				if(dx * dx + dz * dz > reach * reach) continue;
				for(int dy = 0; dy < 20; dy++) {
					Block b = world.getBlock(x + dx, y + dy, z + dz);
					if(b == ModBlocks.plastic_crate || b == ModBlocks.geysir_waste || b == ModBlocks.geysir_waste_massive
							|| b == ModBlocks.deco_steel || b == ModBlocks.deco_rusty_steel) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean isCrateGround(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		return b == ModBlocks.dross_waste || b == ModBlocks.dross_slush || b == ModBlocks.metallic_dross_waste || b == ModBlocks.slushy_dross_waste || b == ModBlocks.caked_dross_waste || b == ModBlocks.electronic_dross_waste;
	}

	// i have no idea what i did here

	// S.O.N

	private void generateCrateMound(World world, Random rand, int x, int y, int z) {
		int radius = 4 + rand.nextInt(6);
		int surface = world.getTopSolidOrLiquidBlock(x, z);
		int height = Math.max(16 + rand.nextInt(18), surface - y + 8 + rand.nextInt(6));
		int depth = 10 + rand.nextInt(8);
		for(int dx = -radius; dx <= radius; dx++) {
			for(int dz = -radius; dz <= radius; dz++) {
				double dist = Math.sqrt(dx * dx + dz * dz);
				if(dist > radius) continue;
				double t = dist / radius;
				int colH = 1 + (int) (height * Math.pow(1.0D - t, 1.35D)) + rand.nextInt(3);
				Block colBlock = structuralWaste(world, rand, x + dx, y + height / 2, z + dz);
				for(int py = y + 1; py <= y + colH; py++) {
					if(py <= 0) continue;
					int px = x + dx;
					int pz = z + dz;
					Block cur = world.getBlock(px, py, pz);
					if(cur != Blocks.air && cur != ModBlocks.slush_block) continue;
					if(rand.nextInt(8) == 0) {
						world.setBlock(px, py, pz, ModBlocks.plastic_crate, crateMeta(rand), 2);
					} else {
						world.setBlock(px, py, pz, colBlock, rand.nextInt(4), 2);
					}
				}
				for(int py = y; py > y - depth; py--) {
					if(py <= 1) break;
					double rAtY = radius * (1.0D + (double) (y - py) / depth * 0.4D);
					if(dist > rAtY) break;
					world.setBlock(x + dx, py, z + dz, ModBlocks.dross_waste, rand.nextInt(4), 2);
				}
			}
		}
	}


	private int crateMeta(Random rand) {
		return 0;
	}

	private boolean isOnSpire(World world, int x, int y, int z) {
		for(int dx = -4; dx <= 4; dx++) {
			for(int dz = -4; dz <= 4; dz++) {
				for(int dy = -2; dy <= 8; dy++) {
					Block b = world.getBlock(x + dx, y + dy, z + dz);
					if(b == ModBlocks.deco_steel || b == ModBlocks.deco_rusty_steel) return true;
				}
			}
		}
		return false;
	}

	private boolean isGeyserNearby(World world, int x, int z) {
		for(int dx = -10; dx <= 10; dx++) {
			for(int dz = -10; dz <= 10; dz++) {
				if(dx * dx + dz * dz > 100) continue;
				// scan down through the slush to the sea floor, in case the geyser is submerged
				for(int y = world.getTopSolidOrLiquidBlock(x + dx, z + dz); y > 1; y--) {
					Block b = world.getBlock(x + dx, y, z + dz);
					if(b == ModBlocks.geysir_waste || b == ModBlocks.geysir_waste_massive) return true;
					if(b != ModBlocks.slush_block && b.getMaterial() != Material.water && b.getMaterial() != Material.air) break;
				}
			}
		}
		return false;
	}

	private void generateWasteMound(World world, Random rand, int x, int y, int z, boolean massive) {
		int poolR = massive ? 4 : 2;
		int poolRing = massive ? 4 : 2;
		int wall = massive ? 4 : 2;
		int rimInner = poolR + poolRing;
		int rimR = rimInner + wall;

		int baseR = rimR + 5 + rand.nextInt(7);
		if(massive) baseR = rimR + 12 + rand.nextInt(10);

		int surface = world.getTopSolidOrLiquidBlock(x, z);
		int height = Math.max(12 + rand.nextInt(18), surface - y + 10 + rand.nextInt(10));
		if(massive) height = Math.max(28 + rand.nextInt(28), surface - y + 18 + rand.nextInt(14));
		int depth = 12 + rand.nextInt(14);

		int plateauY = y + 1 + height;
		int poolDepth = (massive ? 7 : 4) + rand.nextInt(3);
		int basinFloor = plateauY - poolDepth;
		int top = basinFloor + 1 + rand.nextInt(2);
		int poolSurface = top - 1;
		if(poolSurface <= basinFloor) poolSurface = basinFloor + 1;
		double[][] wobble = new double[baseR * 2 + 6][baseR * 2 + 6];
		for(int dx = -baseR - 2; dx <= baseR + 2; dx++) {
			for(int dz = -baseR - 2; dz <= baseR + 2; dz++) {
				wobble[dx + baseR + 2][dz + baseR + 2] = 1.0D + Math.abs(rand.nextDouble() - 0.5D) * 0.4D;
			}
		}
		for(int dx = -baseR - 2; dx <= baseR + 2; dx++) {
			for(int dz = -baseR - 2; dz <= baseR + 2; dz++) {
				double dist = Math.sqrt(dx * dx + dz * dz);
				for(int py = y; py >= y - depth; py--) {
					if(py <= 0) continue;
					double rAtY = baseR * (1.0D + (double) (y - py) / depth * 0.4D);
					if(dist > rAtY) continue;
					world.setBlock(x + dx, py, z + dz, ModBlocks.dross_waste, rand.nextInt(4), 2);
				}
			if(dist <= rimR && dist > rimInner) {
				Block rimBlock = structuralWaste(world, rand, x + dx, (y + plateauY) / 2, z + dz);
				for(int py = y; py < plateauY; py++) {
					if(py <= 0) continue;
					world.setBlock(x + dx, py, z + dz, rimBlock, rand.nextInt(4), 2);
				}
				continue;
			}
			if(dist > rimR) {
				double wob = wobble[dx + baseR + 2][dz + baseR + 2];
				double d = dist * wob;
				if(d > baseR) continue;
				double slope = (double) (plateauY - y) / Math.max(1, baseR - rimR);
				int surfaceY = y + (int) ((baseR - d) * slope);
				Block slopeBlock = structuralWaste(world, rand, x + dx, (y + surfaceY) / 2, z + dz);
				for(int py = y; py < surfaceY; py++) {
					if(py <= 0) continue;
					world.setBlock(x + dx, py, z + dz, slopeBlock, rand.nextInt(4), 2);
				}
				continue;
			}
			if(dist <= poolR) {
					for(int py = y; py < top; py++) {
						if(py <= 0) continue;
						double tipDist = (double) (top - py) / Math.max(1, top - basinFloor);
						boolean slushTip = tipDist < 0.5D && rand.nextDouble() < (1.0D - tipDist) * 0.9D;
						world.setBlock(x + dx, py, z + dz, slushTip ? ModBlocks.dross_slush : ModBlocks.dross_waste, rand.nextInt(4), 2);
					}
				} else {
					for(int py = y; py < basinFloor; py++) {
						if(py <= 0) continue;
						world.setBlock(x + dx, py, z + dz, ModBlocks.dross_waste, rand.nextInt(4), 2);
					}
					for(int py = basinFloor; py < poolSurface; py++) {
						if(py <= 0) continue;
						world.setBlock(x + dx, py, z + dz, ModBlocks.slush_block, 0, 2);
					}
				}
			}
		}

		world.setBlock(x, top, z, massive ? ModBlocks.geysir_waste_massive : ModBlocks.geysir_waste, 0, 2);
		for(int dx = -baseR - 2; dx <= baseR + 2; dx++) {
			for(int dz = -baseR - 2; dz <= baseR + 2; dz++) {
				if(dx * dx + dz * dz > (baseR + 1) * (baseR + 1)) continue;
				for(int py = y; py > 1; py--) {
					Block cur = world.getBlock(x + dx, py, z + dz);
					if(cur == Blocks.air || cur == ModBlocks.dross_waste || cur == ModBlocks.dross_slush || cur == ModBlocks.slush_block || cur == ModBlocks.metallic_dross_waste || cur == ModBlocks.slushy_dross_waste) {
						world.setBlock(x + dx, py, z + dz, ModBlocks.dross_waste, rand.nextInt(4), 2);
					}
				}
			}
		}
	}

	private void generateSharpMound(World world, Random rand, int x, int y, int z) {
		int poolR = 1;
		int wall = 1;
		int rimR = poolR + 1 + wall;
		int baseR = rimR + 2 + rand.nextInt(2);

		int surface = world.getTopSolidOrLiquidBlock(x, z);
		int height = Math.max(8 + rand.nextInt(10), surface - y + 6 + rand.nextInt(6));
		int depth = 8 + rand.nextInt(6);

		int plateauY = y + 1 + height;
		int poolDepth = 2 + rand.nextInt(2);
		int basinFloor = plateauY - poolDepth;
		int top = basinFloor + 1;
		int poolSurface = top - 1;
		if(poolSurface <= basinFloor) poolSurface = basinFloor + 1;

		for(int dx = -baseR - 1; dx <= baseR + 1; dx++) {
			for(int dz = -baseR - 1; dz <= baseR + 1; dz++) {
				double dist = Math.sqrt(dx * dx + dz * dz);

				for(int py = y; py >= y - depth; py--) {
					if(py <= 0) continue;
					double rAtY = baseR * (1.0D + (double) (y - py) / depth * 0.4D);
					if(dist > rAtY) continue;
					world.setBlock(x + dx, py, z + dz, ModBlocks.dross_waste, rand.nextInt(4), 2);
				}

			if(dist <= rimR && dist > poolR + 1) {
				Block rimBlock = structuralWaste(world, rand, x + dx, (y + plateauY) / 2, z + dz);
				for(int py = y; py < plateauY; py++) {
					if(py <= 0) continue;
					world.setBlock(x + dx, py, z + dz, rimBlock, rand.nextInt(4), 2);
				}
				continue;
			}

			if(dist > rimR) {
				if(dist > baseR) continue;
				double slope = (double) (plateauY - y) / Math.max(1, baseR - rimR);
				int surfaceY = y + (int) ((baseR - dist) * slope);
				Block slopeBlock = structuralWaste(world, rand, x + dx, (y + surfaceY) / 2, z + dz);
				for(int py = y; py < surfaceY; py++) {
					if(py <= 0) continue;
					world.setBlock(x + dx, py, z + dz, slopeBlock, rand.nextInt(4), 2);
				}
				continue;
			}
				if(dist <= poolR) {
					for(int py = y; py < top; py++) {
						if(py <= 0) continue;
						double tipDist = (double) (top - py) / Math.max(1, top - basinFloor);
						boolean slushTip = tipDist < 0.5D && rand.nextDouble() < (1.0D - tipDist) * 0.9D;
						world.setBlock(x + dx, py, z + dz, slushTip ? ModBlocks.dross_slush : ModBlocks.dross_waste, rand.nextInt(4), 2);
					}
				} else {
					for(int py = y; py < basinFloor; py++) {
						if(py <= 0) continue;
						world.setBlock(x + dx, py, z + dz, ModBlocks.dross_waste, rand.nextInt(4), 2);
					}
					for(int py = basinFloor; py < poolSurface; py++) {
						if(py <= 0) continue;
						world.setBlock(x + dx, py, z + dz, ModBlocks.slush_block, 0, 2);
					}
				}
			}
		}

		world.setBlock(x, top, z, ModBlocks.geysir_waste, 0, 2);
		for(int dx = -baseR - 1; dx <= baseR + 1; dx++) {
			for(int dz = -baseR - 1; dz <= baseR + 1; dz++) {
				if(dx * dx + dz * dz > (baseR) * (baseR)) continue;
				for(int py = y; py > 1; py--) {
					Block cur = world.getBlock(x + dx, py, z + dz);
					if(cur == Blocks.air || cur == ModBlocks.dross_waste || cur == ModBlocks.dross_slush || cur == ModBlocks.slush_block || cur == ModBlocks.metallic_dross_waste || cur == ModBlocks.slushy_dross_waste) {
						world.setBlock(x + dx, py, z + dz, ModBlocks.dross_waste, rand.nextInt(4), 2);
					}
				}
			}
		}
	}



	// okay i remember the noise generator atleast
	private NoiseGeneratorPerlin seamNoiseX;
	private NoiseGeneratorPerlin seamNoiseY;
	private NoiseGeneratorPerlin seamNoiseZ;
	private long noiseWorldSeed = Long.MIN_VALUE;

	private static final double SCALE_H = 0.16D;
	private static final double SCALE_V = 0.24D;

	private void initSeamNoise(World world) {
		long seed = world.getSeed();
		if(noiseWorldSeed == seed) return;
		noiseWorldSeed = seed;
		seamNoiseX = new NoiseGeneratorPerlin(new Random(seed + 101), 4);
		seamNoiseY = new NoiseGeneratorPerlin(new Random(seed + 102), 4);
		seamNoiseZ = new NoiseGeneratorPerlin(new Random(seed + 103), 4);
	}

	private Block getWasteVariant(World world, int wx, int wy, int wz, Block base) {
		if(seamNoiseX == null) initSeamNoise(world);

		double n1 = seamNoiseX.func_151601_a(wx * SCALE_H, wz * SCALE_H);
		double n2 = seamNoiseZ.func_151601_a(wx * SCALE_H, wy * SCALE_V);
		double n3 = seamNoiseY.func_151601_a(wy * SCALE_V, wz * SCALE_H);
		double p = n1 * 0.5D + n2 * 0.3D + n3 * 0.2D;


		/// TODO: MORE VARIANTS OF DROSS BLOCKS
		if(p > 0.5D) return ModBlocks.metallic_dross_waste;
		if(p < -0.5D) return ModBlocks.electronic_dross_waste;
		if(p > 0.2D) return ModBlocks.caked_dross_waste;
		if(p < -0.2D) return ModBlocks.slushy_dross_waste;
		return base;
	}

	private Block structuralWaste(World world, Random rand, int wx, int wy, int wz) {
		if(rand.nextInt(7) != 0) return ModBlocks.dross_waste;
		return getWasteVariant(world, wx, wy, wz, ModBlocks.dross_waste);
	}

	@Override
	public void genTerrainBlocks(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		Block block = this.topBlock;
		byte b0 = (byte) (this.field_150604_aj & 255);
		Block block1 = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		int i1 = x & 15;
		int j1 = z & 15;
		int k1 = blocks.length / 256;

		for (int l1 = 255; l1 >= 0; --l1) {
			int i2 = (j1 * 16 + i1) * k1 + l1;

			if (l1 <= 0 + rand.nextInt(5)) {
				blocks[i2] = Blocks.bedrock;
			} else {
				Block block2 = blocks[i2];

				if (block2 != null && block2.getMaterial() != Material.air) {
					if (block2 == ModBlocks.dross_waste) {
						if (k == -1) {
							if (l <= 0) {
								block = null;
								b0 = 0;
								block1 = ModBlocks.dross_waste;
							} else if (l1 >= 59 && l1 <= 64) {
								block = this.topBlock;
								b0 = (byte) (this.field_150604_aj & 255);
								block1 = this.fillerBlock;
							}

							k = l;

							if (l1 >= 62) {

								if(block == ModBlocks.dross_waste && rand.nextInt(3) == 0) {
									block = ModBlocks.dross_slush;
								}
								blocks[i2] = block;
								meta[i2] = (byte) (block == ModBlocks.dross_waste || block == ModBlocks.dross_slush || block == ModBlocks.metallic_dross_waste || block == ModBlocks.slushy_dross_waste ? rand.nextInt(4) : b0);
							} else {
								if(rand.nextInt(5) == 0) blocks[i2] = ModBlocks.dross_slush;
							}
						} else if (k > 0) {
							--k;
							if(rand.nextInt(5) == 0) blocks[i2] = ModBlocks.dross_slush;
						}
					}
			} else {
				k = -1;
			}
		}
		}
		for(int l1 = 0; l1 <= 255; l1++) {
			int i2 = (j1 * 16 + i1) * k1 + l1;
			if(blocks[i2] != ModBlocks.dross_waste) continue;
			Block p = drossPatch(world, x, l1, z);
			blocks[i2] = p;
			if(p == ModBlocks.dross_waste || p == ModBlocks.metallic_dross_waste || p == ModBlocks.slushy_dross_waste) {
				meta[i2] = (byte) rand.nextInt(4);
			} else {
				meta[i2] = 0;
			}
		}
	}

	private Block drossPatch(World world, int px, int py, int pz) {
		int cx = px >> 1;
		int cy = py >> 2;
		int cz = pz >> 1;
		long h = world.getSeed() ^ (cx * 0x9E3779B97F4A7C15L) ^ (cy * 0xBF58476D1CE4E5B9L) ^ (cz * 0x94D049BB133111EBL);
		h ^= h >>> 30;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 27;
		h *= 0x94D049BB133111EBL;
		h ^= h >>> 31;
		switch((int) ((h & 0x7FFFFFFFL) % 5L)) {
		case 0: return ModBlocks.dross_waste;
		case 1: return ModBlocks.metallic_dross_waste;
		case 2: return ModBlocks.slushy_dross_waste;
		case 3: return ModBlocks.caked_dross_waste;
		default: return ModBlocks.electronic_dross_waste;
		}
	}
}
