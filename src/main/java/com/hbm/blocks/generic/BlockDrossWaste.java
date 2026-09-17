package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.IBlockSideRotation;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDrossWaste extends BlockGeneric implements IBlockSideRotation {

	public BlockDrossWaste(Material material) {
		super(material);
	}

	@Override
	public int getRotationFromSide(IBlockAccess world, int x, int y, int z, int side) {
		int meta = world.getBlockMetadata(x, y, z) & 3;
		if(side == 0) return IBlockSideRotation.topToBottom(meta);
		return meta;
	}

	@Override
	public int getRenderType() {
		return IBlockSideRotation.getRenderType();
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		Random rand = world.rand;
		world.setBlockMetadataWithNotify(x, y, z, rand.nextInt(4), 2);
	}
}
