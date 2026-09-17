package com.hbm.tileentity.network;

import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.ParticleUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityQuarryMarker extends TileEntityLoadedBase implements IDroneLinkable {

	// 3 positions
	public int[][] links = new int[3][];

	@Override
	public void updateEntity() {

		if(worldObj.isRemote) {
			if(worldObj.getTotalWorldTime() % 8 == 0) {
				for(int i = 0; i < 3; i++) {
					if(links[i] != null) {
						ParticleUtil.spawnDroneLine(worldObj,
								xCoord + 0.5, yCoord + 0.5, zCoord + 0.5,
								links[i][0] - xCoord, links[i][1] - yCoord, links[i][2] - zCoord,
								0xff0000);
					}
				}
			}
		} else {
			this.networkPackNT(50);
		}
	}

	@Override
	public BlockPos getPoint() {
		return new BlockPos(xCoord, yCoord, zCoord);
	}

	@Override
	public void setNextTarget(int x, int y, int z) {
		for(int i = 0; i < 3; i++) {
			if(links[i] != null && links[i][0] == x && links[i][1] == y && links[i][2] == z) return;
		}
		for(int i = 0; i < 3; i++) {
			if(links[i] == null) {
				links[i] = new int[] {x, y, z};
				this.markDirty();
				return;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		for(int i = 0; i < 3; i++) {
			if(links[i] != null) {
				buf.writeBoolean(true);
				buf.writeInt(links[i][0]);
				buf.writeInt(links[i][1]);
				buf.writeInt(links[i][2]);
			} else {
				buf.writeBoolean(false);
			}
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		for(int i = 0; i < 3; i++) {
			if(buf.readBoolean()) {
				links[i] = new int[] {buf.readInt(), buf.readInt(), buf.readInt()};
			} else {
				links[i] = null;
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		for(int i = 0; i < 3; i++) {
			if(nbt.hasKey("link" + i)) {
				int[] pos = nbt.getIntArray("link" + i);
				if(pos.length == 3) links[i] = pos;
			} else {
				links[i] = null;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		for(int i = 0; i < 3; i++) {
			if(links[i] != null) {
				nbt.setIntArray("link" + i, links[i]);
			}
		}
	}
}
