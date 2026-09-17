package com.hbm.entity.effect;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityRedLightning extends Entity {

	public int boltVertex;

	public EntityRedLightning(World world) {
		super(world);
	}

	public EntityRedLightning(World world, double x, double y, double z) {
		super(world);
		setPosition(x, y, z);
		this.noClip = true;
		this.ignoreFrustumCheck = true;
		this.boltVertex = world.rand.nextInt(100000);
	}

	@Override
	protected void entityInit() {
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(ticksExisted % 4 == 0)
			this.boltVertex = worldObj.rand.nextInt(100000);

		if(ticksExisted > 12)
			this.setDead();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
	}
}
