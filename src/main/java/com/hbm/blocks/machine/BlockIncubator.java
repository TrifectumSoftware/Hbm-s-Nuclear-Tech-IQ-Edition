package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityIncubator;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockIncubator extends BlockMachineBase {

	public BlockIncubator() {
		super(Material.iron, 0);
		this.rotatable = true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityIncubator();
	}
}
