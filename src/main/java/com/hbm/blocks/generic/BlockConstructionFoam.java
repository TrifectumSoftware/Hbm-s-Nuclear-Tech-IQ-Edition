package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockConstructionFoam extends BlockBase {

	public static final Block[] hardensTo = new Block[16];
	public static final int[] hardensToMeta = new int[16];

	public BlockConstructionFoam(Material material) {
		super(material);
	}

	public static void registerResult(int foamMeta, Block block, int blockMeta) {
		hardensTo[foamMeta] = block;
		hardensToMeta[foamMeta] = blockMeta;
	}

	/** The block this foam meta hardens into, falling back to hardened construction foam. */
	public static Block getResult(int foamMeta) {
		Block result = hardensTo[foamMeta & 15];
		return result != null ? result : ModBlocks.hardened_construction_foam;
	}

	public static int getResultMeta(int foamMeta) {
		return hardensTo[foamMeta & 15] != null ? hardensToMeta[foamMeta & 15] : 0;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return null;
	}

	@Override
	public int quantityDropped(Random rand) {
		return 0;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		if(!world.isRemote) {
			world.scheduleBlockUpdate(x, y, z, this, 200);
		}
	}



	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		if(!world.isRemote && world.getBlock(x, y, z) == this) {
			int meta = world.getBlockMetadata(x, y, z);
			world.setBlock(x, y, z, getResult(meta), getResultMeta(meta), 3);
		}
	}
}
