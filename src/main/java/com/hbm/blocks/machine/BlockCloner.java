package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityCloner;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCloner extends BlockMachineBase {

	public BlockCloner() {
		super(Material.iron, 0);
		this.rotatable = true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCloner();
	}
}
