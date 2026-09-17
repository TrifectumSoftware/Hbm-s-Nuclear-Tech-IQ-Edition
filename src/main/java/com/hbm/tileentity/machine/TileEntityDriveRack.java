package com.hbm.tileentity.machine;

import com.hbm.tileentity.TileEntityMachineBase;

public class TileEntityDriveRack extends TileEntityMachineBase {
	public TileEntityDriveRack() { super(10); }

	@Override
	public String getName() { return "tile.machine_drive_rack"; }

	@Override
	public void updateEntity() {
		// TODO: make this class actually do something
		System.out.println("unfinished TE");
	}
}
