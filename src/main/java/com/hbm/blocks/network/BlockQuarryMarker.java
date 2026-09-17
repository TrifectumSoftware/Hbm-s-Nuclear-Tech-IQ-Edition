package com.hbm.blocks.network;

import com.hbm.tileentity.network.TileEntityQuarryMarker;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockQuarryMarker extends DroneWaypointRequest {

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityQuarryMarker();
	}
}
