package com.hbm.tileentity.deco;

import com.hbm.entity.projectile.EntityWasteShrapnel;

import net.minecraft.entity.player.EntityPlayer;

public class TileEntityWasteGeysirMassive extends TileEntityWasteGeysir {

	@Override
	protected void perform() {
		slush();
	}

	@Override
	protected void slush() {

		int range = 64;
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

		// one big lump rather than several small ones
		EntityWasteShrapnel blob = new EntityWasteShrapnel(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5);
		blob.setRenderScale(40.0F + worldObj.rand.nextFloat() * 24.0F);
		blob.motionX = worldObj.rand.nextGaussian() * 0.2;
		blob.motionZ = worldObj.rand.nextGaussian() * 0.2;
		blob.motionY = 1.5 + worldObj.rand.nextDouble() * 1.2;
		worldObj.spawnEntityInWorld(blob);
	}
}
