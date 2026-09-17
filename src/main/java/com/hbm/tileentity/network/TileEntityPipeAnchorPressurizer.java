package com.hbm.tileentity.network;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityPipeAnchorPressurizer extends TileEntityLoadedBase implements IFluidStandardTransceiverMK2 {

	public FluidTank[] tanks = new FluidTank[2];
	public int airConsumption = 1;

	public TileEntityPipeAnchorPressurizer() {
		tanks[0] = new FluidTank(Fluids.NONE, 24_000);
		tanks[1] = new FluidTank(Fluids.AIR, 24_000);
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			boolean operating = tanks[0].getFill() > 0 && tanks[1].getFill() > 0;
			if(operating) tanks[1].setFill(Math.max(0, tanks[1].getFill() - airConsumption));

			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
			ForgeDirection back = dir.getOpposite();

			for(int[] off : getBlocks()) {
				int bx = xCoord + off[0];
				int bz = zCoord + off[1];

				if(tanks[0].getTankType() != Fluids.NONE) {
					this.trySubscribe(tanks[0].getTankType(), worldObj, bx + dir.offsetX, yCoord, bz + dir.offsetZ, dir);
					this.trySubscribe(tanks[0].getTankType(), worldObj, bx, yCoord + 1, bz, ForgeDirection.UP);
					if(operating) {
						this.tryProvide(tanks[0], worldObj, bx + dir.offsetX, yCoord, bz + dir.offsetZ, dir);
						this.tryProvide(tanks[0], worldObj, bx, yCoord + 1, bz, ForgeDirection.UP);
					}
				}

				this.trySubscribe(Fluids.AIR, worldObj, bx + back.offsetX, yCoord, bz + back.offsetZ, back);
				this.trySubscribe(Fluids.AIR, worldObj, bx, yCoord - 1, bz, ForgeDirection.DOWN);
			}

			this.networkPackNT(20);
		}
	}

	public int[][] getBlocks() {
		switch(this.getBlockMetadata() - BlockDummyable.offset) {
		case 2: return new int[][] {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
		case 3: return new int[][] {{0, 0}, {-1, 0}, {0, -1}, {-1, -1}};
		case 4: return new int[][] {{0, 0}, {1, 0}, {0, -1}, {1, -1}};
		case 5: return new int[][] {{0, 0}, {-1, 0}, {0, 1}, {-1, 1}};
		}
		return new int[][] {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		tanks[0].readFromNBT(nbt, "t0");
		tanks[1].readFromNBT(nbt, "t1");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		tanks[0].writeToNBT(nbt, "t0");
		tanks[1].writeToNBT(nbt, "t1");
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		ForgeDirection facing = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		if(dir == ForgeDirection.DOWN) return type == Fluids.AIR;
		if(dir == facing) return type == tanks[0].getTankType() || tanks[0].getTankType() == Fluids.NONE;
		if(dir == facing.getOpposite()) return type == Fluids.AIR;
		return false;
	}

	@Override
	public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0], tanks[1]}; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[0]}; }
	@Override public FluidTank[] getAllTanks() { return tanks; }
}
