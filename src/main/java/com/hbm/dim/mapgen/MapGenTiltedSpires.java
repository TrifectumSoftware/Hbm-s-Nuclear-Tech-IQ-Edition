package com.hbm.dim.mapgen;

import java.util.Random;

import com.hbm.dim.noise.DoublePerlinNoiseSampler;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;

public class MapGenTiltedSpires extends MapGenBase {

	private final int chanceHigh;
	private final int chanceLow;
	private final float threshold;

	public int minSize = 6;
	public int maxSize = 16;
	public float minPoint = 0.5F;
	public float maxPoint = 4.0F;
	public float minTilt = 0.2F;
	public float maxTilt = 2.5F;
	public boolean curve = false;
	public int mid = 64;
	public int maxY = 127;

	public Block regolith;
	public Block rock;
	public Block rock2;
	public float rock2Chance = 0F;

	// hollowed stuffs
	public float hollowChance = 0F;
	public float hollowScale = 0.55F;
	public float hollowFracture = 0F;
	public float biteChance = 0F;
	public float biteOffset = 0.4F;
	public float biteRadius = 0.55F;
	public int biteThickness = 3;

	private DoublePerlinNoiseSampler perlin;

	// Note that the chance is effectively squared, so make it lower than you normally would
	public MapGenTiltedSpires(int chanceHigh, int chanceLow, float threshold) {
		this.chanceHigh = chanceHigh;
		this.chanceLow = chanceLow;
		this.threshold = threshold * 2F - 1F; // normalizing to -1, 1 for perlin
		range = 4;
	}

	public void setRange(int range) {
		this.range = range;
	}

	// setup seeded random
	@Override
	public void func_151539_a(IChunkProvider chunk, World world, int chunkX, int chunkZ, Block[] blocks) {
		if (worldObj != world) {
			this.rand.setSeed(world.getSeed() + 69);
			this.perlin = DoublePerlinNoiseSampler.create(new Random(rand.nextLong()), -8, 1.0D, 2.0D);
		}

		super.func_151539_a(chunk, world, chunkX, chunkZ, blocks);
	}

	// This function is looped over from -this.range to +this.range on both XZ axes.
	@Override
	protected void func_151538_a(World world, int offsetX, int offsetZ, int chunkX, int chunkZ, Block[] blocks) {
		int chance = perlin.sample(offsetX * 16, 0, offsetZ * 16) > threshold ? chanceHigh : chanceLow;

		if(rand.nextInt(chance) == Math.abs(offsetX) % chance && rand.nextInt(chance) == Math.abs(offsetZ) % chance) {

			float coneRadius = rand.nextInt(maxSize - minSize) + minSize;
			float stretch = 1F / (rand.nextFloat() * rand.nextFloat() * (maxPoint - minPoint) + minPoint);
			float direction = rand.nextFloat() * (float)Math.PI * 2F;
			float tilt = rand.nextFloat() * (maxTilt - minTilt) + minTilt;
			boolean hollow = rand.nextFloat() < hollowChance;
			boolean bitten = rand.nextFloat() < biteChance;
			// the bite is a circle punched out of the spire's side, roughly a third of the way up
			float biteDir = rand.nextFloat() * (float)Math.PI * 2F;
			float biteHeight = (coneRadius / stretch) * (0.25F + rand.nextFloat() * 0.3F);

			if(curve) tilt *= 0.01;

			int xCoord = -offsetX + chunkX;
			int zCoord = -offsetZ + chunkZ;

			// tilt offset
			float tx = (float)Math.cos(direction) * tilt;
			float tz = (float)Math.sin(direction) * tilt;

			// backwards offset (to reduce required range, improving performance)
			int ox = (int)(tx * (curve ? 800 : 8));
			int oz = (int)(tz * (curve ? 800 : 8));

			for(int bx = 15; bx >= 0; bx--) { // bx, bz is the coordinate of the block we're modifying, relative to the generating chunk origin
				for(int bz = 15; bz >= 0; bz--) {
					int d = 0;

					for(int y = maxY; y >= 0; y--) {
						int index = (bx * 16 + bz) * 256 + y;

						// Run until the first opaque block
						if(blocks[index] == null || !blocks[index].isOpaqueCube()) {
							// x, z are the coordinates relative to the target virtual chunk origin
							int x = xCoord * 16 + bx - ox;
							int z = zCoord * 16 + bz - oz;
							int oy = y - mid;

							float factor = (float) oy;
							if(curve) factor *= factor;

							x += tx * factor;
							z += tz * factor;

							float rs = x * x + z * z;
							float radius = coneRadius - oy * stretch;
							float radiusSqr = radius > 0 ? radius * radius : 0;

							if(rs < radiusSqr) {
								if(hollow && radius > 1) {
									float inner = radius * hollowScale;
									if(rs < inner * inner) {
										continue;
									}
									if(hollowFracture > 0 && rand.nextFloat() < hollowFracture) {
										continue;
									}
								}
								if(bitten && Math.abs(oy - biteHeight) < biteThickness) {
									// a circular bite punched out of the side, cutting into the center
									float bix = (float)Math.cos(biteDir) * (radius * biteOffset) + (tx * factor);
									float biz = (float)Math.sin(biteDir) * (radius * biteOffset) + (tz * factor);
									float biteR = radius * biteRadius;
									float ddx = x - bix;
									float ddz = z - biz;
									if(ddx * ddx + ddz * ddz < biteR * biteR) {
										continue;
									}
								}
								blocks[index] = rock2 != null && rand.nextFloat() < rock2Chance ? rock2 : rock;
								if(d == 1 && regolith != null) blocks[index + 1] = regolith;
								d++;
							} else if(d > 0) {
								break;
							}
						} else {
							break;
						}
					}
				}
			}
		}
	}

}
