package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;

import com.hbm.tileentity.machine.TileEntityDriveRack;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class MachineDriveRack extends BlockDummyable {

	public MachineDriveRack(Material mat) { super(mat); }

	public TileEntityDriveRack createNewTileEntity(World world, int meta) {
		if (meta >= 6) {
			return new TileEntityDriveRack();
		}
		return null;
	}

	@Override
	public int[] getDimensions() { return new int[] {1, 0, 0, 0, 0, 0}; }

	@Override
	public int getOffset() { return 0; }
}
