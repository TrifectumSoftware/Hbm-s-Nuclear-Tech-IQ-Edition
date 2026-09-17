package com.hbm.tileentity.network;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.network.FluidPipeAnchorPressurizer;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluidmk2.FluidNode;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityPipeAnchorIndustrial extends TileEntityPipelineBase {

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.LARGE;
	}

	@Override
	public Vec3 getMountPos() {
		switch(this.getBlockMetadata() - BlockDummyable.offset) {
		case 2: return Vec3.createVectorHelper(0.5, 1.0, 0.5);
		case 3: return Vec3.createVectorHelper(-0.5, 1.0, -0.5);
		case 4: return Vec3.createVectorHelper(0.5, 1.0, -0.5);
		case 5: return Vec3.createVectorHelper(-0.5, 1.0, 0.5);
		}
		return Vec3.createVectorHelper(0.5, 1.0, 0.5);
	}

	@Override
	public double getMaxPipeLength() {
		return 100;
	}

	@Override
	public FluidNode createNode(FluidType type) {
		TileEntity tile = (TileEntity) this;
		FluidNode node = new FluidNode(type.getNetworkProvider(), new BlockPos(tile.xCoord, tile.yCoord, tile.zCoord)).setConnections(
				new DirPos(xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN),
				new DirPos(xCoord + 1, yCoord, zCoord, ForgeDirection.EAST),
				new DirPos(xCoord - 1, yCoord, zCoord, ForgeDirection.WEST),
				new DirPos(xCoord, yCoord, zCoord + 1, ForgeDirection.SOUTH),
				new DirPos(xCoord, yCoord, zCoord - 1, ForgeDirection.NORTH));
		for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
		return node;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		if(dir == ForgeDirection.UP) return false;
		if(type != this.type) return false;
		if(dir == ForgeDirection.DOWN) {
			Block b = worldObj.getBlock(xCoord, yCoord - 1, zCoord);
			return b instanceof FluidPipeAnchorPressurizer;
		}
		return true;
	}
}
