package com.hbm.dim.hell;

import java.util.Random;

import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockFleshPlant.EnumFleshPlantType;
import com.hbm.blocks.generic.BlockNetherGlyphid.EnumNetherGlyphidType;
import com.hbm.config.MobConfig;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.world.feature.GlyphidHiveNether;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeGenTheNest extends NetherBiomeBase {

	public static BiomeGenTheNest theNest;

	public static void init() {
		theNest = (BiomeGenTheNest) new BiomeGenTheNest(SpaceConfig.theNestBiome).setBiomeName("The Nest");
		BiomeDictionary.registerBiomeType(theNest, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.SPOOKY);
	}

	public BiomeGenTheNest(int id) {
		super(id);
		this.topBlock = Blocks.netherrack;
		this.fillerBlock = Blocks.netherrack;
		this.setColor(0x4A2A1A);
		this.setTemperatureRainfall(2.0F, 0.0F);
		this.rootHeight = 0.0F;
		this.heightVariation = 0.05F;
		this.theBiomeDecorator.treesPerChunk = 0;
		this.theBiomeDecorator.flowersPerChunk = 0;
		this.theBiomeDecorator.grassPerChunk = 0;
		this.spawnableMonsterList.clear();
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
	}

	@Override
	public void decorate(World world, Random rand, int chunkX, int chunkZ) {
      // the blobs
		for(int i = 0; i < 10; i++) {
			int bx = chunkX + rand.nextInt(16);
			int bz = chunkZ + rand.nextInt(16);
			generateFleshBlob(world, bx, bz, 1 + rand.nextInt(3), 2 + rand.nextInt(3));
		}
		for(int i = 0; i < 8; i++) {
			int bx = chunkX + rand.nextInt(16);
			int bz = chunkZ + rand.nextInt(16);
			generateMetaBlob(world, bx, bz, 1 + rand.nextInt(3), EnumNetherGlyphidType.OVERTAKEN.ordinal());
		}
		for(int i = 0; i < 5; i++) {
			int bx = chunkX + rand.nextInt(16);
			int bz = chunkZ + rand.nextInt(16);
			generateMetaBlob(world, bx, bz, 1 + rand.nextInt(2), EnumNetherGlyphidType.CREVASSE.ordinal());
		}
		for(int i = 0; i < 5; i++) {
			int bx = chunkX + rand.nextInt(16);
			int bz = chunkZ + rand.nextInt(16);
			generateMetaBlob(world, bx, bz, 1 + rand.nextInt(2), EnumNetherGlyphidType.HIVE.ordinal());
		}
		for(int i = 0; i < 20; i++) {
			int x = chunkX + rand.nextInt(16);
			int z = chunkZ + rand.nextInt(16);
			int y = NetherBiomeHelper.getFloorHeight(world, x, z) - 1;
			Block surface = world.getBlock(x, y, z);
			if(surface == Blocks.netherrack || surface == ModBlocks.nether_glyphid) {
				int r = rand.nextInt(3);
				int meta = r == 0 ? EnumNetherGlyphidType.CREVASSE.ordinal() : r == 1 ? EnumNetherGlyphidType.HIVE.ordinal() : EnumNetherGlyphidType.GRUB.ordinal();
				world.setBlock(x, y, z, ModBlocks.nether_glyphid, meta, 2);
			}
		}

		/// BLOATWISP HIVE
		for(int i = 0; i < 6; i++) {
			int x = chunkX + rand.nextInt(16);
			int z = chunkZ + rand.nextInt(16);
			int y = NetherBiomeHelper.getFloorHeight(world, x, z) - 1;
			Block surface = world.getBlock(x, y, z);
			if(surface == Blocks.netherrack || surface == ModBlocks.glyphid_base || surface == ModBlocks.nether_glyphid) {
				world.setBlock(x, y, z, ModBlocks.nether_glyphid, EnumNetherGlyphidType.BLOATWISP.ordinal(), 2);
				if(world.isAirBlock(x, y + 1, z))
					world.setBlock(x, y + 1, z, ModBlocks.bloatfire, 0, 2);
			}
		}

		///  CRYSTALS
		for(int i = 0; i < 2; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			if(y <= 30 || y >= 100) continue;
			boolean lava = world.getBlock(x, y - 1, z) == Blocks.lava;
			if(!lava && !NetherBiomeHelper.isSolidFloor(world, x, z)) continue;
			if(!NetherBiomeHelper.isFlatArea(world, x, z, 2)) continue;
			boolean phosphorus = rand.nextBoolean();
			Block cb = phosphorus ? ModBlocks.block_crystal_2 : ModBlocks.block_crystal;
			int cm = phosphorus ? BlockEnums.EnumCrystalBlockType.PHOSPHORUS.ordinal() - 16 : BlockEnums.EnumCrystalBlockType.SULFUR.ordinal();
			int height = 3 + rand.nextInt(3);
			for(int dy = 0; dy < height; dy++) {
				int py = y + dy;
				for(int dx = -1; dx <= 1; dx++)
					for(int dz = -1; dz <= 1; dz++) {
						if(Math.abs(dx) + Math.abs(dz) > 1) continue;
						Block cur = world.getBlock(x + dx, py, z + dz);
						if(cur == Blocks.air || cur == Blocks.lava)
							world.setBlock(x + dx, py, z + dz, cb, cm, 2);
					}
			}
		}
		for(int i = 0; i < 2; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			boolean phosphorus = rand.nextBoolean();
			Block cb = phosphorus ? ModBlocks.block_crystal_2 : ModBlocks.block_crystal;
			int cm = phosphorus ? BlockEnums.EnumCrystalBlockType.PHOSPHORUS.ordinal() - 16 : BlockEnums.EnumCrystalBlockType.SULFUR.ordinal();
			for(int y = 126; y > 40; y--) {
				if(world.isAirBlock(x, y, z) && NetherBiomeHelper.isSolidMass(world, x, y + 1, z)) {
					int height = 3 + rand.nextInt(3);
					for(int dy = 0; dy < height && y - dy > 32; dy++) {
						int ty = y - dy;
						for(int dx = -1; dx <= 1; dx++)
							for(int dz = -1; dz <= 1; dz++) {
								if(Math.abs(dx) + Math.abs(dz) > 1) continue;
								if(world.isAirBlock(x + dx, ty, z + dz))
									world.setBlock(x + dx, ty, z + dz, cb, cm, 2);
							}
					}
					break;
				}
			}
		}
		///  STALAGMITES
		for(int i = 0; i < 6; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			if(y <= 30 || y >= 100 || !world.isAirBlock(x, y, z)) continue;
			Block floor = world.getBlock(x, y - 1, z);
			if(floor == Blocks.lava) {
				world.setBlock(x, y - 1, z, Blocks.netherrack, 0, 2);
				world.setBlock(x, y, z, ModBlocks.stalagmite, BlockEnums.EnumStalagmiteType.GLYPHID1.ordinal(), 2);
			} else if(NetherBiomeHelper.isSolidFloor(world, x, z)) {
				world.setBlock(x, y, z, ModBlocks.stalagmite, BlockEnums.EnumStalagmiteType.GLYPHID1.ordinal(), 2);
			}
		}
		for(int i = 0; i < 3; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			for(int y = 126; y > 105; y--) {
				if(world.isAirBlock(x, y, z) && NetherBiomeHelper.isSolidMass(world, x, y + 1, z)) {
					world.setBlock(x, y, z, ModBlocks.stalactite, BlockEnums.EnumStalagmiteType.GLYPHID1.ordinal(), 2);
					break;
				}
			}
		}

		/// TREES
		for(int i = 0; i < 1; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			if(y <= 30 || y >= 100 || !NetherBiomeHelper.isSolidFloor(world, x, z) || !NetherBiomeHelper.isFlatArea(world, x, z, 2)) continue;
			new WorldGenBloatsprout(true, 2, 5, 7, 4, 3, ModBlocks.bloatsprout_log, ModBlocks.bloatsprout_leaves).generate(world, rand, x, y, z);
		}

		///  FUNGI
		for(int i = 0; i < 8; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			if(y <= 30 || y >= 90) continue;
			if(!NetherBiomeHelper.isThickFloor(world, x, z) && !NetherBiomeHelper.isLavaNearby(world, x, y, z)) continue;
			generateFungus(world, rand, x, y, z);
		}
		/// NEST FUNGI
		if(MobConfig.enableNetherHives && rand.nextInt(2) == 0) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			if(y <= 30 || y >= 90) return;
			if(!NetherBiomeHelper.isThickFloor(world, x, z) && !NetherBiomeHelper.isLavaNearby(world, x, y, z)) return;
			generateNestFungus(world, rand, x, y, z);
		}

		for(int i = 0; i < 2; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			if(y <= 30 || y >= 90) continue;
			if(!NetherBiomeHelper.isThickFloor(world, x, z) && !NetherBiomeHelper.isLavaNearby(world, x, y, z)) continue;
			generateGrandFungus(world, rand, x, y, z);
		}

		///  TENTACLES
		for(int i = 0; i < 8; i++) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			generateRoofTentacle(world, rand, x, z);
		}

		/// FUNGI (ROOF)
		if(rand.nextInt(2) == 0) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			for(int y = 126; y > 90; y--) {
				if(world.isAirBlock(x, y, z) && NetherBiomeHelper.isSolidMass(world, x, y + 1, z)) {
					generateVentFungus(world, rand, x, y, z);
					break;
				}
			}
		}

		/// CYSTS
		// or boils, you tell me
		for(int i = 0; i < 8; i++) {
			int x = chunkX + rand.nextInt(12) + 2;
			int z = chunkZ + rand.nextInt(12) + 2;
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			if(y <= 30 || y >= 100 || !NetherBiomeHelper.isSolidFloor(world, x, z) || !NetherBiomeHelper.isFlatArea(world, x, z, 2)) continue;
			generateCyst(world, rand, x, y - 1, z);
		}

		if(rand.nextInt(8) == 0) {
			int x = chunkX + rand.nextInt(14) + 1;
			int z = chunkZ + rand.nextInt(14) + 1;
			if(!NetherBiomeHelper.isSolidFloor(world, x, z)) return;
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			generateBloathive(world, rand, x, y, z);
		}

		/// Hive spawning
		if(MobConfig.enableNetherHives && rand.nextInt(2) == 0) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			if(!NetherBiomeHelper.isSolidFloor(world, x, z) || !NetherBiomeHelper.isFlatArea(world, x, z, 2)) return;
			int base = y - 1;
			for(int dy = 0; dy <= 4; dy++)
				for(int dx = -6; dx <= 5; dx++)
					for(int dz = -6; dz <= 5; dz++)
						if(!world.isAirBlock(x + dx, base + dy, z + dz)) return;
			for(int k = 3; k >= -1; k--) {
				if(world.getBlock(x, base + k, z).isNormalCube()) {
					for(int dy = -1; dy <= 4; dy++)
						for(int dx = -6; dx <= 5; dx++)
							for(int dz = -6; dz <= 5; dz++)
								if(base + dy > 1)
									world.setBlock(x + dx, base + dy, z + dz, Blocks.air, 0, 2);
					GlyphidHiveNether.generateLarge(world, x, base + k + 1, z, rand);
					break;
				}
			}
		}

		///  ARTERY
		if(WorldConfig.arterySpawn > 0 && rand.nextInt(WorldConfig.arterySpawn) == 0) {
			int x = chunkX + rand.nextInt(16) + 8;
			int z = chunkZ + rand.nextInt(16) + 8;

			for(int bx = -5; bx <= 5; bx++)
				for(int bz = -5; bz <= 5; bz++)
					for(int y = 0; y < 6; y++)
						if(Math.abs(bx) < 5 && Math.abs(bz) < 5 && Math.abs(bx) + Math.abs(bz) <= 6)
							if(world.getBlock(x + bx, y, z + bz) == Blocks.bedrock)
								world.setBlock(x + bx, y, z + bz, ModBlocks.ore_bedrock_artery, 0, 2);
		}

		/// PLANTS
		for(int i = 0; i < 80; i++) {
			int x = chunkX + rand.nextInt(16);
			int z = chunkZ + rand.nextInt(16);
			int y = NetherBiomeHelper.getFloorHeight(world, x, z) - 1;
			Block surface = world.getBlock(x, y, z);
			if(surface == ModBlocks.glyphid_base || surface == ModBlocks.nether_glyphid || surface == Blocks.netherrack || surface == Blocks.soul_sand) {
				if(world.isAirBlock(x, y + 1, z))
					world.setBlock(x, y + 1, z, ModBlocks.plant_flesh, rand.nextInt(EnumFleshPlantType.values().length), 2);
			}
		}
       ///  SKULLS
		for(int i = 0; i < 6; i++) {
			int x = chunkX + rand.nextInt(16);
			int z = chunkZ + rand.nextInt(16);
			int y = NetherBiomeHelper.getFloorHeight(world, x, z);
			if(y <= 30 || y >= 100 || !NetherBiomeHelper.isSolidFloor(world, x, z) || !world.isAirBlock(x, y, z)) continue;
			world.setBlock(x, y, z, Blocks.skull, 1, 3);
			TileEntitySkull skull = (TileEntitySkull) world.getTileEntity(x, y, z);
			if(skull != null) skull.func_145903_a(rand.nextInt(16));
		}


		for(int i = 0; i < 5; i++) {
			int bx = chunkX + rand.nextInt(16);
			int bz = chunkZ + rand.nextInt(16);
			generateMetaBlob(world, bx, bz, 1 + rand.nextInt(2), EnumNetherGlyphidType.GRUB.ordinal());
		}

		/// THE SAAC
		if(MobConfig.enableNetherHives) {
			for(int i = 0; i < 2; i++) {
				int x = chunkX + rand.nextInt(16);
				int z = chunkZ + rand.nextInt(16);
				int y = NetherBiomeHelper.getFloorHeight(world, x, z);
				if(y <= 30 || y >= 100 || !NetherBiomeHelper.isSolidFloor(world, x, z) || !NetherBiomeHelper.isFlatArea(world, x, z, 1)) continue;
				if(world.getBlock(x, y - 1, z) != ModBlocks.glyphid_base) continue;
				if(world.isAirBlock(x, y, z))
					world.setBlock(x, y, z, ModBlocks.glyphid_spawner_nether, 0, 2);
			}
		}
	}

	/// /// /// ///
	/// HELPERS ///
	/// /// /// ///
// thats a really nice fucking box isnt it

	private void setFlesh(World world, int x, int y, int z, Random rand) {
		int r = rand.nextInt(8);
		if(r < 5) world.setBlock(x, y, z, ModBlocks.glyphid_base, 0, 2);
		else if(r < 7) world.setBlock(x, y, z, ModBlocks.nether_glyphid, EnumNetherGlyphidType.OVERTAKEN.ordinal(), 2);
		else world.setBlock(x, y, z, ModBlocks.nether_glyphid, EnumNetherGlyphidType.GRUB.ordinal(), 2);
	}

	private void generateFungus(World world, Random rand, int x, int y, int z) {
		rootInLava(world, x, y, z, 1);
		int stalkH = 4 + rand.nextInt(7);
		int capR = 2 + rand.nextInt(3);
		for(int dy = 0; dy < stalkH; dy++) {
			for(int dx = -1; dx <= 1; dx++)
				for(int dz = -1; dz <= 1; dz++) {
					if(dx == 0 || dz == 0)
						setFlesh(world, x + dx, y + dy, z + dz, rand);
				}
		}
		int cy = y + stalkH;
		for(int dx = -capR; dx <= capR; dx++)
			for(int dz = -capR; dz <= capR; dz++) {
				if(dx * dx + dz * dz > capR * capR) continue;
				setFlesh(world, x + dx, cy, z + dz, rand);
				if(dx * dx + dz * dz >= (capR - 1) * (capR - 1))
					world.setBlock(x + dx, cy - 1, z + dz, ModBlocks.glyphid_base, 0, 2);
			}
	}

	private void generateVentFungus(World world, Random rand, int x, int y, int z) {
		int stalkH = 18 + rand.nextInt(10);
		int capR = 4 + rand.nextInt(3);
		for(int dy = 0; dy < stalkH; dy++) {
			int r = 2 - (int) ((double) dy / stalkH * 1.0D);
			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++) {
					if(Math.abs(dx) + Math.abs(dz) > r) continue;
					setFlesh(world, x + dx, y - dy, z + dz, rand);
				}
		}
		int cy = y - stalkH;
		for(int dx = -capR; dx <= capR; dx++)
			for(int dz = -capR; dz <= capR; dz++) {
				if(dx * dx + dz * dz > capR * capR) continue;
				setFlesh(world, x + dx, cy, z + dz, rand);
				if(dx * dx + dz * dz >= (capR - 1) * (capR - 1)) {
					for(int dw = 1; dw <= 2; dw++)
						if(world.isAirBlock(x + dx, cy - dw, z + dz))
							world.setBlock(x + dx, cy - dw, z + dz, ModBlocks.glyphid_base, 0, 2);
				}
			}
	}

	private void generateGrandFungus(World world, Random rand, int x, int y, int z) {
		rootInLava(world, x, y, z, 2);
		int stalkH = 9 + rand.nextInt(7);
		int capR = 6 + rand.nextInt(4);
		for(int dy = 0; dy < stalkH; dy++) {
			int r = Math.max(2, 3 - (int) ((double) dy / stalkH * 1.0D));
			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++) {
					if(Math.abs(dx) + Math.abs(dz) > r) continue;
					setFlesh(world, x + dx, y + dy, z + dz, rand);
				}
		}
		int cy = y + stalkH;
		for(int dx = -capR; dx <= capR; dx++)
			for(int dz = -capR; dz <= capR; dz++) {
				if(dx * dx + dz * dz > capR * capR) continue;
				setFlesh(world, x + dx, cy, z + dz, rand);
				if(dx * dx + dz * dz >= (capR - 1) * (capR - 1))
					world.setBlock(x + dx, cy - 1, z + dz, ModBlocks.glyphid_base, 0, 2);
			}
		int cy2 = cy + 1;
		int capR2 = Math.max(2, capR / 2);
		for(int dx = -capR2; dx <= capR2; dx++)
			for(int dz = -capR2; dz <= capR2; dz++) {
				if(dx * dx + dz * dz > capR2 * capR2) continue;
				if(world.isAirBlock(x + dx, cy2, z + dz)) setFlesh(world, x + dx, cy2, z + dz, rand);
			}
	}

	private void generateNestFungus(World world, Random rand, int x, int y, int z) {
		rootInLava(world, x, y, z, 2);
		int stalkH = 10 + rand.nextInt(7);
		int capR = 12 + rand.nextInt(4);
		for(int dy = 0; dy < stalkH; dy++) {
			int r = Math.max(3, 4 - (int) ((double) dy / stalkH * 2.0D));
			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++) {
					if(Math.abs(dx) + Math.abs(dz) > r) continue;
					setFlesh(world, x + dx, y + dy, z + dz, rand);
				}
		}
		int cy = y + stalkH;
		for(int dx = -capR; dx <= capR; dx++)
			for(int dz = -capR; dz <= capR; dz++) {
				if(dx * dx + dz * dz > capR * capR) continue;
				setFlesh(world, x + dx, cy, z + dz, rand);
			}
		GlyphidHiveNether.generateLarge(world, x, cy + 1, z, rand);
	}

	private void rootInLava(World world, int x, int y, int z, int padR) {
		for(int dy = 1; dy <= 5; dy++) {
			Block b = world.getBlock(x, y - dy, z);
			if(b == Blocks.lava) {
				for(int d2 = 1; d2 <= dy; d2++) {
					int py = y - d2;
					for(int dx = -padR; dx <= padR; dx++)
						for(int dz = -padR; dz <= padR; dz++) {
							Block cur = world.getBlock(x + dx, py, z + dz);
							if(cur == Blocks.lava || cur == Blocks.netherrack || cur == ModBlocks.glyphid_base || cur == ModBlocks.nether_glyphid)
								world.setBlock(x + dx, py, z + dz, ModBlocks.glyphid_base, 0, 2);
						}
				}
				return;
			}
			if(b != Blocks.netherrack && b != ModBlocks.glyphid_base && b != ModBlocks.nether_glyphid && b != Blocks.soul_sand)
				return;
		}
	}

	private void generateRoofTentacle(World world, Random rand, int x, int z) {
		for(int y = 126; y > 60; y--) {
			if(world.isAirBlock(x, y, z) && !world.isAirBlock(x, y + 1, z)) {
				int len = 6 + rand.nextInt(9);
				for(int dy = 0; dy < len; dy++) {
					int ty = y - dy;
					if(ty <= 32) break;
					int r = dy < 3 ? 2 : 1;
					if(dy == len - 1) r = 0;
					for(int dx = -r; dx <= r; dx++)
						for(int dz = -r; dz <= r; dz++) {
							if(r > 0 && dx * dx + dz * dz > r * r) continue;
							if(world.isAirBlock(x + dx, ty, z + dz)) setFlesh(world, x + dx, ty, z + dz, rand);
						}
					if(r == 0) break;
				}
				return;
			}
		}
	}

	/// BLOBS

	private void generateFleshBlob(World world, int bx, int bz, int radius, int depth) {
		for(int dx = -radius; dx <= radius; dx++)
			for(int dz = -radius; dz <= radius; dz++) {
				if(dx * dx + dz * dz > radius * radius) continue;
				int x = bx + dx, z = bz + dz;
				int y = NetherBiomeHelper.getFloorHeight(world, x, z) - 1;
				Block surface = world.getBlock(x, y, z);
				if(surface != Blocks.netherrack && surface != ModBlocks.nether_glyphid) continue;
				world.setBlock(x, y, z, ModBlocks.glyphid_base, 0, 2);
				for(int dy = 1; dy <= depth; dy++)
					if(world.getBlock(x, y - dy, z) == Blocks.netherrack)
						world.setBlock(x, y - dy, z, ModBlocks.glyphid_base, 0, 2);
			}
	}

	private void generateMetaBlob(World world, int bx, int bz, int radius, int meta) {
		for(int dx = -radius; dx <= radius; dx++)
			for(int dz = -radius; dz <= radius; dz++) {
				if(dx * dx + dz * dz > radius * radius) continue;
				int x = bx + dx, z = bz + dz;
				int y = NetherBiomeHelper.getFloorHeight(world, x, z) - 1;
				Block surface = world.getBlock(x, y, z);
				if(surface != Blocks.netherrack && surface != ModBlocks.glyphid_base) continue;
				world.setBlock(x, y, z, ModBlocks.nether_glyphid, meta, 2);
			}
	}

	private void generateCyst(World world, Random rand, int x, int y, int z) {
		int height = 1 + rand.nextInt(2);
		for(int dx = -1; dx <= 1; dx++)
			for(int dz = -1; dz <= 1; dz++) {
				if(dx == 0 && dz == 0) continue;
				for(int dy = 0; dy <= height; dy++) {
					int py = y + 1 + dy;
					Block cur = world.getBlock(x + dx, py, z + dz);
					if(cur == Blocks.air || cur == Blocks.netherrack || cur == ModBlocks.glyphid_base || cur == ModBlocks.nether_glyphid)
						world.setBlock(x + dx, py, z + dz, ModBlocks.glyphid_base, 0, 2);
				}
			}
		world.setBlock(x, y + 1, z, ModBlocks.cyst, 0, 2);
	}

	private void generateBloathive(World world, Random rand, int x, int y, int z) {
		for(int dx = -1; dx <= 0; dx++)
			for(int dz = -1; dz <= 0; dz++)
				world.setBlock(x + dx, y - 1, z + dz, ModBlocks.glyphid_base, 0, 2);

		int height = 1 + rand.nextInt(2);
		for(int dy = 0; dy < height; dy++)
			for(int dx = -1; dx <= 0; dx++)
				for(int dz = -1; dz <= 0; dz++)
					world.setBlock(x + dx, y + dy, z + dz, ModBlocks.glyphid_base, 0, 2);

		world.setBlock(x, y + height, z, ModBlocks.nether_glyphid, EnumNetherGlyphidType.BLOATWISP.ordinal(), 2);
		world.setBlock(x, y + height + 1, z, Blocks.fire, 0, 2);
		if(rand.nextBoolean()) world.setBlock(x, y + height + 1, z - 1, Blocks.fire, 0, 2);
	}
}
