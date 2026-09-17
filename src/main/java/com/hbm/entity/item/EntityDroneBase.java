package com.hbm.entity.item;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.generic.BlockOre;
import com.hbm.items.ModItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public abstract class EntityDroneBase extends Entity {

	protected int turnProgress;
	protected double syncPosX;
	protected double syncPosY;
	protected double syncPosZ;
	@SideOnly(Side.CLIENT) protected double velocityX;
	@SideOnly(Side.CLIENT) protected double velocityY;
	@SideOnly(Side.CLIENT) protected double velocityZ;

	public double targetX = -1;
	public double targetY = -1;
	public double targetZ = -1;

	public EntityDroneBase(World world) {
		super(world);
		this.setSize(0.75F, 0.75F);
	}

	public void setTarget(double x, double y, double z) {
		this.targetX = x;
		this.targetY = y;
		this.targetZ = z;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canAttackWithItem() {
		return true;
	}

	@Override
	public boolean hitByEntity(Entity attacker) {

		if(attacker instanceof EntityPlayer) {
			this.setDead();
		}

		return false;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	protected void entityInit() {
		this.dataWatcher.addObject(10, new Byte((byte) 0));
	}

	/**
	 * 0: Empty<br>
	 * 1: Crate<br>
	 * 2: Barrel<br>
	 */
	public void setAppearance(int style) {
		this.dataWatcher.updateObject(10, (byte) style);
	}

	public int getAppearance() {
		return this.dataWatcher.getWatchableObjectByte(10);
	}

	@Override
	public void onUpdate() {

		if(worldObj.isRemote) {
			if(this.turnProgress > 0) {
				double interpX = this.posX + (this.syncPosX - this.posX) / (double) this.turnProgress;
				double interpY = this.posY + (this.syncPosY - this.posY) / (double) this.turnProgress;
				double interpZ = this.posZ + (this.syncPosZ - this.posZ) / (double) this.turnProgress;
				--this.turnProgress;
				this.setPosition(interpX, interpY, interpZ);
			} else {
				this.setPosition(this.posX, this.posY, this.posZ);
			}

			worldObj.spawnParticle("smoke", posX + 1.125, posY + 0.75, posZ, 0, -0.2, 0);
			worldObj.spawnParticle("smoke", posX - 1.125, posY + 0.75, posZ, 0, -0.2, 0);
			worldObj.spawnParticle("smoke", posX, posY + 0.75, posZ + 1.125, 0, -0.2, 0);
			worldObj.spawnParticle("smoke", posX, posY + 0.75, posZ - 1.125, 0, -0.2, 0);
		} else {

			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;

			if(this.targetY != -1) {

				Vec3 dist = Vec3.createVectorHelper(targetX - posX, targetY - posY, targetZ - posZ);
				double speed = Math.min(getSpeed(), dist.lengthVector());

				dist = dist.normalize();
				this.motionX = dist.xCoord * speed;
				this.motionY = dist.yCoord * speed;
				this.motionZ = dist.zCoord * speed;
			}
			if(isCollidedHorizontally && canClimbWalls()){
				motionY += 1;
			}
			this.loadNeighboringChunks();
			this.moveEntity(motionX, motionY, motionZ);
		}

		super.onUpdate();
	}

	protected void loadNeighboringChunks() {}

	public double getSpeed() {
		return 0.125D;
	}

	protected boolean canClimbWalls() {
		return true;
	}


	protected boolean hasDroneNearby(int x, int y, int z) {
		List<EntityDroneBase> drones = worldObj.getEntitiesWithinAABB(EntityDroneBase.class, AxisAlignedBB.getBoundingBox(x - 1.5, y - 1.5, z - 1.5, x + 2.5, y + 2.5, z + 2.5));
		for(EntityDroneBase drone : drones) {
			if(drone != this) return true;
		}
		return false;
	}

	protected boolean isOre(Block block, int meta) {
		int[] ids = OreDictionary.getOreIDs(new ItemStack(block, 1, meta));
		for(int id : ids) {
			if(OreDictionary.getOreName(id).startsWith("ore")) return true;
		}
		return false;
	}

	// maybe silk touch drones in the future could overrid this optionally
	protected List<ItemStack> getMinedDrops(Block block, int x, int y, int z, int meta) {
		BlockOre ore = BlockOre.vanillaMap.get(block);
		if(ore != null) {
			int rawMeta = ore.getRawOreMeta();
			if(rawMeta >= 0) {
				List<ItemStack> drops = new ArrayList();
				int count = ore.quantityDroppedWithBonus(0, worldObj.rand);
				drops.add(new ItemStack(ModItems.raw_ore, count, rawMeta));
				return drops;
			}
		}

		List<ItemStack> drops = block.getDrops(worldObj, x, y, z, meta, 0);
		return drops != null ? drops : new ArrayList();
	}

	protected boolean addToStorage(ItemStack[] slots, ItemStack stack) {

		for(int i = 0; i < slots.length; i++) {
			if(slots[i] != null && slots[i].isItemEqual(stack) && ItemStack.areItemStackTagsEqual(slots[i], stack)) {
				int canAdd = Math.min(stack.stackSize, slots[i].getMaxStackSize() - slots[i].stackSize);
				slots[i].stackSize += canAdd;
				stack.stackSize -= canAdd;
				if(stack.stackSize <= 0) return true;
			}
		}

		for(int i = 0; i < slots.length; i++) {
			if(slots[i] == null) {
				slots[i] = stack.copy();
				return true;
			}
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	public void setVelocity(double motionX, double motionY, double motionZ) {
		this.velocityX = this.motionX = motionX;
		this.velocityY = this.motionY = motionY;
		this.velocityZ = this.motionZ = motionZ;
	}

	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int theNumberThree) {
		this.syncPosX = x;
		this.syncPosY = y;
		this.syncPosZ = z;
		this.turnProgress = theNumberThree;
		this.motionX = this.velocityX;
		this.motionY = this.velocityY;
		this.motionZ = this.velocityZ;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {

		nbt.setDouble("tX", targetX);
		nbt.setDouble("tY", targetY);
		nbt.setDouble("tZ", targetZ);

		nbt.setByte("app", this.dataWatcher.getWatchableObjectByte(10));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {

		if(nbt.hasKey("tY")) {
			this.targetX = nbt.getDouble("tX");
			this.targetY = nbt.getDouble("tY");
			this.targetZ = nbt.getDouble("tZ");
		}

		this.dataWatcher.updateObject(10, nbt.getByte("app"));
	}
}
