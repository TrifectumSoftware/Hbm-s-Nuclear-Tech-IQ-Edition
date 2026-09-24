package com.hbm.dim.hell;

import java.util.ArrayList;
import java.util.List;

import com.hbm.world.ModBiomes;

import net.minecraft.util.WeightedRandom;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.common.BiomeManager.BiomeEntry;

public abstract class GenLayerHbmHell extends GenLayer {

	private GenLayerHbmHell(long seed) {
		super(seed);
	}

	public static GenLayer[] makeLayers(long seed, WorldType worldType) {
		int biomeSize = 4;

		GenLayerHbmHell layer = new HellCreate(1L);
		layer = new HellFuzzyZoom(2000L, layer);
		for (int i = 1; i < 3; i++) layer = new HellZoom(2000L + i, layer);
		layer = HellZoom.magnify(1000L, layer, 0);
		layer = new HellBiomes(200L, layer);
		layer = HellZoom.magnify(1000L, layer, 2);
		for (int j = 0; j < biomeSize; j++) layer = new HellZoom(1000L + j, layer);
		GenLayerHbmHell voronoi = new HellVoronoiZoom(10L, layer);
		layer.initWorldGenSeed(seed);
		voronoi.initWorldGenSeed(seed);

		return new GenLayer[] { layer, voronoi };
	}

	// Creates initial layer filled with 1
	private static class HellCreate extends GenLayerHbmHell {
		public HellCreate(long seed) {
			super(seed);
		}

		@Override
		public int[] getInts(int x, int z, int w, int h) {
			int[] result = IntCache.getIntCache(w * h);
			for (int i = 0; i < h; ++i) {
				for (int j = 0; j < w; ++j) {
					this.initChunkSeed(x + j, z + i);
					result[j + i * w] = 1;
				}
			}
			return result;
		}
	}

	// i stole this from the big efr
	private static class HellFuzzyZoom extends GenLayerHbmHell {
		public HellFuzzyZoom(long seed, GenLayerHbmHell parent) {
			super(seed);
			this.parent = parent;
		}

		@Override
		public int[] getInts(int x, int z, int w, int h) {
			int i1 = x >> 1;
			int j1 = z >> 1;
			int k1 = (w >> 1) + 3;
			int l1 = (h >> 1) + 3;
			int[] parentVals = parent.getInts(i1, j1, k1, l1);
			int[] result = IntCache.getIntCache(k1 * 2 * l1 * 2);
			int i2 = k1 << 1;

			for (int k2 = 0; k2 < l1 - 1; ++k2) {
				int j2 = k2 << 1;
				int l2 = j2 * i2;
				int i3 = parentVals[k2 * k1];
				int j3 = parentVals[(k2 + 1) * k1];

				for (int k3 = 0; k3 < k1 - 1; ++k3) {
					this.initChunkSeed((long) k3 + i1 << 1, (long) k2 + j1 << 1);
					int l3 = parentVals[k3 + 1 + k2 * k1];
					int i4 = parentVals[k3 + 1 + (k2 + 1) * k1];
					result[l2] = i3;
					result[l2++ + i2] = this.choose(i3, j3);
					result[l2] = this.choose(i3, l3);
					result[l2++ + i2] = this.choose(i3, l3, j3, i4);
					i3 = l3;
					j3 = i4;
				}
			}

			int[] finalResult = IntCache.getIntCache(w * h);

			for (int j2 = 0; j2 < h; ++j2) {
				System.arraycopy(result, (j2 + (z & 1)) * (k1 << 1) + (x & 1), finalResult, j2 * w, w);
			}

			return finalResult;
		}

		protected int choose(int a, int b) {
			return this.nextInt(2) == 0 ? a : b;
		}

		protected int choose(int a, int b, int c, int d) {
			int i1 = this.nextInt(4);
			return i1 == 0 ? a : (i1 == 1 ? b : (i1 == 2 ? c : d));
		}
	}

	private static class HellZoom extends GenLayerHbmHell {
		public HellZoom(long seed, GenLayerHbmHell parent) {
			super(seed);
			this.parent = parent;
		}

		@Override
		public int[] getInts(int x, int z, int w, int h) {
			int i1 = x >> 1;
			int j1 = z >> 1;
			int k1 = (w >> 1) + 3;
			int l1 = (h >> 1) + 3;
			int[] parentVals = parent.getInts(i1, j1, k1, l1);
			int[] result = IntCache.getIntCache(k1 * 2 * l1 * 2);
			int i2 = k1 << 1;

			for (int k2 = 0; k2 < l1 - 1; ++k2) {
				int j2 = k2 << 1;
				int l2 = j2 * i2;
				int i3 = parentVals[k2 * k1];
				int j3 = parentVals[(k2 + 1) * k1];

				for (int k3 = 0; k3 < k1 - 1; ++k3) {
					this.initChunkSeed((long) k3 + i1 << 1, (long) k2 + j1 << 1);
					int l3 = parentVals[k3 + 1 + k2 * k1];
					int i4 = parentVals[k3 + 1 + (k2 + 1) * k1];
					result[l2] = i3;
					result[l2++ + i2] = this.choose(i3, j3);
					result[l2] = this.choose(i3, l3);
					result[l2++ + i2] = this.modeOrRandom(i3, l3, j3, i4);
					i3 = l3;
					j3 = i4;
				}
			}

			int[] finalResult = IntCache.getIntCache(w * h);

			for (int j2 = 0; j2 < h; ++j2) {
				System.arraycopy(result, (j2 + (z & 1)) * (k1 << 1) + (x & 1), finalResult, j2 * w, w);
			}

			return finalResult;
		}

		protected int choose(int a, int b) {
			return this.nextInt(2) == 0 ? a : b;
		}

			protected int modeOrRandom(int a, int b, int c, int d) {
				if (b == c && c == d)
					return b;
				else if (a == b && a == c)
					return a;
				else if (a == b && a == d)
					return a;
				else if (a == c && a == d)
					return a;
				else if (a == b && c != d)
					return a;
				else if (a == c && b != d)
					return a;
				else if (a == d && b != c)
					return a;
				else if (b == a && c != d)
					return b;
				else if (b == c && a != d)
					return b;
				else if (b == d && a != c)
					return b;
				else if (c == a && b != d)
					return c;
				else if (c == b && a != d)
					return c;
				else if (c == d && a != b)
					return c;
				else if (d == a && b != c)
					return c;
				else if (d == b && a != c)
					return c;
				else if (d == c && a != b)
					return c;
				else {
					int i1 = this.nextInt(4);
					return i1 == 0 ? a : (i1 == 1 ? b : (i1 == 2 ? c : d));
				}
			}

		public static GenLayerHbmHell magnify(long seed, GenLayerHbmHell parent, int times) {
			GenLayerHbmHell layer = parent;
			for (int k = 0; k < times; ++k) {
				layer = new HellZoom(seed + k, layer);
			}
			return layer;
		}
	}

	// another one

	private static class HellVoronoiZoom extends GenLayerHbmHell {
		public HellVoronoiZoom(long seed, GenLayerHbmHell parent) {
			super(seed);
			this.parent = parent;
		}

		@Override
		public int[] getInts(int x, int z, int w, int h) {
			x -= 2;
			z -= 2;
			byte b0 = 2;
			int i1 = 1 << b0;
			int j1 = x >> b0;
			int k1 = z >> b0;
			int l1 = (w >> b0) + 3;
			int i2 = (h >> b0) + 3;
			int[] parentVals = parent.getInts(j1, k1, l1, i2);
			int j2 = l1 << b0;
			int k2 = i2 << b0;
			int[] result = IntCache.getIntCache(j2 * k2);
			int l2;

			for (int i3 = 0; i3 < i2 - 1; ++i3) {
				l2 = parentVals[i3 * l1];
				int j3 = parentVals[(i3 + 1) * l1];

				for (int k3 = 0; k3 < l1 - 1; ++k3) {
					double d0 = i1 * 0.9D;
					this.initChunkSeed((long) k3 + j1 << b0, (long) i3 + k1 << b0);
					double d1 = (this.nextInt(1024) / 1024.0D - 0.5D) * d0;
					double d2 = (this.nextInt(1024) / 1024.0D - 0.5D) * d0;
					this.initChunkSeed((long) k3 + j1 + 1 << b0, (long) i3 + k1 << b0);
					double d3 = (this.nextInt(1024) / 1024.0D - 0.5D) * d0 + i1;
					double d4 = (this.nextInt(1024) / 1024.0D - 0.5D) * d0;
					this.initChunkSeed((long) k3 + j1 << b0, (long) i3 + k1 + 1 << b0);
					double d5 = (this.nextInt(1024) / 1024.0D - 0.5D) * d0;
					double d6 = (this.nextInt(1024) / 1024.0D - 0.5D) * d0 + i1;
					this.initChunkSeed((long) k3 + j1 + 1 << b0, (long) i3 + k1 + 1 << b0);
					double d7 = (this.nextInt(1024) / 1024.0D - 0.5D) * d0 + i1;
					double d8 = (this.nextInt(1024) / 1024.0D - 0.5D) * d0 + i1;
					int l3 = parentVals[k3 + 1 + i3 * l1];
					int i4 = parentVals[k3 + 1 + (i3 + 1) * l1];

					for (int j4 = 0; j4 < i1; ++j4) {
						int k4 = ((i3 << b0) + j4) * j2 + (k3 << b0);

						for (int l4 = 0; l4 < i1; ++l4) {
							double d9 = (j4 - d2) * (j4 - d2) + (l4 - d1) * (l4 - d1);
							double d10 = (j4 - d4) * (j4 - d4) + (l4 - d3) * (l4 - d3);
							double d11 = (j4 - d6) * (j4 - d6) + (l4 - d5) * (l4 - d5);
							double d12 = (j4 - d8) * (j4 - d8) + (l4 - d7) * (l4 - d7);

							if (d9 < d10 && d9 < d11 && d9 < d12) {
								result[k4++] = l2;
							} else if (d10 < d9 && d10 < d11 && d10 < d12) {
								result[k4++] = l3;
							} else if (d11 < d9 && d11 < d10 && d11 < d12) {
								result[k4++] = j3;
							} else {
								result[k4++] = i4;
							}
						}
					}

					l2 = l3;
					j3 = i4;
				}
			}

			int[] finalResult = IntCache.getIntCache(w * h);

			for (l2 = 0; l2 < h; ++l2) {
				System.arraycopy(result, (l2 + (z & i1 - 1)) * (l1 << b0) + (x & i1 - 1), finalResult, l2 * w, w);
			}

			return finalResult;
		}
	}

	private static class HellBiomes extends GenLayerHbmHell {
		private List<BiomeEntry> allowedBiomes;

		public HellBiomes(long seed, GenLayer parent) {
			super(seed);
			this.parent = parent;
			this.allowedBiomes = new ArrayList<>();
			this.allowedBiomes.add(new BiomeEntry(BiomeGenBase.hell, 10));
			this.allowedBiomes.addAll(ModBiomes.netherBiomes);
		}

		@Override
		public int[] getInts(int x, int z, int w, int h) {
			int[] parentVals = this.parent.getInts(x, z, w, h);
			int[] result = IntCache.getIntCache(w * h);
			for (int i = 0; i < w * h; i++) {
				this.initChunkSeed(x + (i % w), z + (i / w));
				int totalWeight = WeightedRandom.getTotalWeight(allowedBiomes);
				result[i] = ((BiomeEntry) WeightedRandom.getItem(allowedBiomes, this.nextInt(totalWeight))).biome.biomeID;
			}
			return result;
		}
	}
}
