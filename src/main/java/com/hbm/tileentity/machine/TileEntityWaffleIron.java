package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityWaffleIron extends TileEntityMachineBase implements IEnergyReceiverMK2 {
	public static final int[] LOWERING_ANIMATION_TICKS = {(4746)/50,(6493-4746)/50,(9809-6493)/50};
	public static final int[] RAISING_UNFIRED_ANIMATION_TICKS = {(3962)/50, (7106-3962)/50, (11541-7106)/50};
	public static final int[] RAISING_ANIMATION_TICKS = {(1952)/50, (5344-1952)/50};

	public boolean lowered = false;
	public int animationTicks = 0;
	public int animMovement = 0;
	public int animPreparation = 0;
	public int animPause = 0;
	public boolean wasFired = false;
	public boolean fired = false;
	public int shaking = 0;
	public int cooldown = 0;

	public long power = 0L;
	public long maxPower = 100_000_000L;

	public TileEntityWaffleIron() {
		super(1);
	}

	@Override
	public String getName() {
		return "Waffle Iron";
	}

	@Override
	public void updateEntity() {
		this.markDirty();
		if (this.animationTicks > 0) {
			this.animationTicks--;
			if (this.lowered) {
				if (this.animMovement < LOWERING_ANIMATION_TICKS[0]) this.animMovement++;
				else if (this.animPause < LOWERING_ANIMATION_TICKS[1]) this.animPause++;
				else if (this.animPreparation < LOWERING_ANIMATION_TICKS[2]) this.animPreparation++;
			} else {
				if (this.fired) {
					if (this.animPause < RAISING_ANIMATION_TICKS[0]) this.animPause++;
					else if (this.animMovement < RAISING_ANIMATION_TICKS[1]) this.animMovement++;
				} else {
					if (this.animPreparation < RAISING_UNFIRED_ANIMATION_TICKS[0]) this.animPreparation++;
					else if (this.animPause < RAISING_UNFIRED_ANIMATION_TICKS[1]) this.animPause++;
					else if (this.animMovement < RAISING_UNFIRED_ANIMATION_TICKS[2]) this.animMovement++;
				}
			}
			if (this.animationTicks == 0) {
				this.checkRedstoneStatus();
				this.animMovement = 0;
				this.animPause = 0;
				this.animPreparation = 0;
				this.fired = false;
			}
		}
		this.wasFired = this.fired;
		if (this.shaking > 0) this.shaking--;
		if (this.cooldown > 0) {
			this.cooldown--;
			if (this.cooldown == 0) this.checkRedstoneStatus();
		}
		if (!this.worldObj.isRemote) {
			if (this.getMaxPower() > 0) {
				this.trySubscribe(this.worldObj, this.xCoord, this.yCoord-1, this.zCoord, ForgeDirection.DOWN);
				this.fire();
			}
			this.networkPackNT(64);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("lowered", this.lowered);
		nbt.setInteger("animationTicks", this.animationTicks);
		nbt.setInteger("animMovement", this.animMovement);
		nbt.setInteger("animPreparation", this.animPreparation);
		nbt.setInteger("animPause", this.animPause);
		nbt.setBoolean("fired", this.fired);
		nbt.setInteger("shaking", this.shaking);
		nbt.setInteger("cooldown", this.cooldown);

		nbt.setLong("power", this.power);
	}
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.lowered = nbt.getBoolean("lowered");
		this.animationTicks = nbt.getInteger("animationTicks");
		this.animMovement = nbt.getInteger("animMovement");
		this.animPreparation = nbt.getInteger("animPreparation");
		this.animPause = nbt.getInteger("animPause");
		this.fired = nbt.getBoolean("fired");
		this.shaking = nbt.getInteger("shaking");
		this.cooldown = nbt.getInteger("cooldown");

		this.power = nbt.getLong("power");
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.lowered);
		buf.writeShort(this.animationTicks);
		buf.writeByte(this.animMovement);
		buf.writeByte(this.animPreparation);
		buf.writeByte(this.animPause);
		buf.writeBoolean(this.fired);
		buf.writeByte(this.shaking);
	}
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.lowered = buf.readBoolean();
		this.animationTicks = buf.readUnsignedShort();
		this.animMovement = buf.readUnsignedByte();
		this.animPreparation = buf.readUnsignedByte();
		this.animPause = buf.readUnsignedByte();
		this.fired = buf.readBoolean();
		this.shaking = buf.readUnsignedByte();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 5, this.zCoord + 1);
	}

	@Override
	public long getPower() {
		return this.power;
	}
	@Override
	public void setPower(long power) {
		this.power = power;
	}
	@Override
	public long getMaxPower() {
		return this.lowered && !this.fired && this.animationTicks == 0 ? this.maxPower : 0L;
	}

	public void checkRedstoneStatus() {
		if (this.worldObj.isRemote) return;
		boolean powered = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
		if (this.animationTicks == 0 && this.cooldown == 0) {
			if (powered) {
				if (this.lowered) return;
				this.animationTicks = 13 * 20;
				this.lowered = true;
				this.playSound("lower");
			} else {
				if (!this.lowered) return;
				this.lowered = false;
				if (this.fired) {
					this.animationTicks = 8 * 20;
					this.playSound("raise_fired");
				} else {
					this.animationTicks = 16 * 20;
					this.playSound("raise_unfired");
				}
			}
		}
	}
	public void fire() {
		if (this.power < this.maxPower) return;
		if (this.fired) return;
		if (!this.lowered || this.animationTicks != 0) return;
		this.power = 0L;
		this.fired = true;
		this.shaking = 50;
		this.cooldown = 100;
		this.playSound("fire");
	}

	private void playSound(String name) {
		this.worldObj.playSoundEffect(
			this.xCoord + 0.5, this.yCoord + 2.5, this.zCoord + 0.5,
			"hbm:machine.waffle_iron_" + name, 1.0F, 1.0F
		);
	}
}
