package com.hbm.tileentity.deco;

import com.hbm.entity.projectile.EntityWasteShrapnel;

import net.minecraft.entity.player.EntityPlayer;

public class TileEntityWasteGeysir extends TileEntityGeysir {

	@Override
	protected void perform() {
		slush();
	}

	protected void slush() {

		if((worldObj.getTotalWorldTime() + xCoord + zCoord) % 5 != 0) return;

		int range = 32;
		int rangeSq = range * range;
		boolean near = false;
		for(Object o : worldObj.playerEntities) {
			EntityPlayer player = (EntityPlayer) o;
			double dx = player.posX - xCoord;
			double dy = player.posY - yCoord;
			double dz = player.posZ - zCoord;
			if(dx * dx + dy * dy + dz * dz < rangeSq) {
				near = true;
				break;
			}
		}
		if(!near) return;

		EntityWasteShrapnel blob = new EntityWasteShrapnel(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5);
		blob.motionX = worldObj.rand.nextGaussian() * 0.05;
		blob.motionZ = worldObj.rand.nextGaussian() * 0.05;
		blob.motionY = 0.4 + worldObj.rand.nextDouble() * 0.4;
		worldObj.spawnEntityInWorld(blob);
	}

	@Override
	protected int getDelay() {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		return meta == 0 ? (worldObj.rand.nextBoolean() ? 300 : 450) : 80 + worldObj.rand.nextInt(60);
	}
}
