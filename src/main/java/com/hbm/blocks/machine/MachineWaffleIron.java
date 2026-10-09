package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityWaffleIron;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MachineWaffleIron extends BlockDummyable {
	public MachineWaffleIron(Material mat) {
		super(mat);
	}

	@Override
	public int[] getDimensions() {
		return new int[]{4, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		if (metadata >= 12) return new TileEntityWaffleIron();
		if (metadata >= 6) return new TileEntityProxyCombo(true, true, false);
		return null;
	}
}
