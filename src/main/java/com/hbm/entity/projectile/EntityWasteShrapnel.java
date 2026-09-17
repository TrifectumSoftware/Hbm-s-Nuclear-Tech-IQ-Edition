package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDrossWaste;
import com.hbm.lib.ModDamageSource;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityWasteShrapnel extends EntityShrapnel {

	public EntityWasteShrapnel(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setRenderScale(2.0F);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if(mop.entityHit != null) {
			mop.entityHit.attackEntityFrom(ModDamageSource.acid, 6.0F);
		}

		if(this.ticksExisted > 5) {

			if(!worldObj.isRemote)
				this.setDead();

			if(!worldObj.isRemote && mop.blockX != 0 || mop.blockY != 0 || mop.blockZ != 0) {
				if(mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
					for(int x = mop.blockX - 1; x <= mop.blockX + 1; x++) {
						for(int y = mop.blockY - 1; y <= mop.blockY + 2; y++) {
							for(int z = mop.blockZ - 1; z <= mop.blockZ + 1; z++) {
								Block b = worldObj.getBlock(x, y, z);
								if(b == Blocks.air || b == Blocks.bedrock) continue;
								if(b instanceof BlockDrossWaste || b == ModBlocks.slush_block) continue;
								if(b == ModBlocks.plastic_crate) continue;
								if(b == ModBlocks.deco_steel || b == ModBlocks.deco_rusty_steel) continue;
								if(b == ModBlocks.geysir_waste || b == ModBlocks.geysir_waste_massive) continue;
								if(b.getMaterial().isLiquid()) continue;
								worldObj.setBlockToAir(x, y, z);
							}
						}
					}
				}
			}

			worldObj.playSoundEffect(posX, posY, posZ, "random.fizz", 1.0F, 1.0F);
		}
	}
}
