package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.inventory.RecipesCommon;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TileEntityWaffleIron extends TileEntityMachineBase implements IEnergyReceiverMK2 {
	public static final int[] LOWERING_ANIMATION_TICKS = {(4746)/50,(6493-4746)/50,(9809-6493)/50};
	public static final int[] RAISING_UNFIRED_ANIMATION_TICKS = {(3962)/50, (7106-3962)/50, (11541-7106)/50};
	public static final int[] RAISING_ANIMATION_TICKS = {(1952)/50, (5344-1952)/50};

	private static Map<RecipesCommon.AStack, ItemStack> createRecipeMap() {
		HashMap<RecipesCommon.AStack, ItemStack> map = new HashMap<>();
		map.put(new RecipesCommon.ComparableStack(Items.wheat), new ItemStack(Items.bread));
		return map;
	}
	public static Map<RecipesCommon.AStack, ItemStack> recipes;
	public static void init() {
		recipes = Collections.unmodifiableMap(createRecipeMap());
	}

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

	public ItemStack syncStack = null;

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

		BufferUtil.writeItemStack(buf, this.slots[0]);
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

		this.syncStack = BufferUtil.readItemStack(buf);
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
		return this.lowered && !this.fired && this.animationTicks == 0 && this.mayFire() ? this.maxPower : 0L;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot == 0 && !(stack.getItem() instanceof ItemBlock);
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
		ItemStack result = this.getResult();
		if (result != null) {
			this.power = 0L;
			this.fired = true;
			this.shaking = 50;
			this.cooldown = 100;
			this.playSound("fire");
			this.slots[0] = result.copy();
			this.markDirty();
		}
	}

	private void playSound(String name) {
		this.worldObj.playSoundEffect(
			this.xCoord + 0.5, this.yCoord + 2.5, this.zCoord + 0.5,
			"hbm:machine.waffle_iron_" + name, 1.0F, 1.0F
		);
	}

	private boolean mayFire() {
		ItemStack stack = this.slots[0];
		for (RecipesCommon.AStack comp : recipes.keySet()) {
			if (comp.matchesRecipe(stack, true)) return true;
		}
		return false;
	}
	private ItemStack getResult() {
		ItemStack stack = this.slots[0];
		for (Map.Entry<RecipesCommon.AStack, ItemStack> entry : recipes.entrySet()) {
			if (entry.getKey().matchesRecipe(stack, true)) return entry.getValue();
		}
		return null;
	}
}
