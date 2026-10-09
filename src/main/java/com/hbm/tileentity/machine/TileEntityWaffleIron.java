package com.hbm.tileentity.machine;

import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityWaffleIron extends TileEntityMachineBase {
	public TileEntityWaffleIron() {
		super(1);
	}

	@Override
	public String getName() {
		return "Waffle Iron";
	}

	@Override
	public void updateEntity() {

	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 5, this.zCoord + 1);
	}
}
