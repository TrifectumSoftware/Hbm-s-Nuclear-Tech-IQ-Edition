package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModBlocks;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockConstructionFoam extends BlockBase {

	public BlockConstructionFoam(Material material) {
		super(material);
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
			world.setBlock(x, y, z, ModBlocks.hardened_construction_foam);
		}
	}
}
