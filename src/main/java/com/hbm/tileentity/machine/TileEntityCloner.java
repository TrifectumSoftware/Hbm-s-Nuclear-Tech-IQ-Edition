package com.hbm.tileentity.machine;

import com.hbm.entity.mob.EntityHusk;
import com.hbm.inventory.container.ContainerCloner;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUICloner;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemMedicalSyringe;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityCloner extends TileEntityMachineBase implements IEnergyReceiverMK2, IGUIProvider, IControlReceiver {

	public static final int SLOT_SYRINGE = 0;
	public static final int SLOT_MODDING = 1;
	public static final int SLOT_BATTERY = 6;
	public static final int PROCESS_TIME = 100 * 20;

	public long power;
	public long maxPower = 1_000_000;
	public long consumption = 100;

	public boolean active = false;
	public int progress;

	public FluidTank tank;

	public TileEntityCloner() {
		super(7);
		this.tank = new FluidTank(Fluids.NONE, 16_000);
	}

	@Override
	public String getName() {
		return "container.cloner";
	}

	@Override
	public void updateEntity() {

		if(worldObj.isRemote) return;

		this.power = Library.chargeTEFromItems(slots, SLOT_BATTERY, power, maxPower);

		if(worldObj.getTotalWorldTime() % 20 == 0) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				this.trySubscribe(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
			}
		}

		if(canProcess()) {
			this.progress++;
			this.power -= this.consumption;

			if(this.progress >= PROCESS_TIME) {
				this.progress = 0;
				this.cloneBody();
			}
		} else if(this.getSampleUUID() == null) {
			this.progress = 0;
		}

		this.maxPower = Math.max(this.consumption * 20, this.power);
		this.networkPackNT(25);
	}

	public boolean canProcess() {
		if(!this.active) return false;
		if(this.power < this.consumption) return false;
		return this.getSampleUUID() != null;
	}

	private String getSampleUUID() {
		ItemStack syringe = slots[SLOT_SYRINGE];
		if(syringe == null || !(syringe.getItem() instanceof ItemMedicalSyringe) || syringe.stackTagCompound == null) return null;

		String uuid = syringe.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_UUID);
		return uuid.isEmpty() ? null : uuid;
	}

	private void cloneBody() {
		ItemStack syringe = slots[SLOT_SYRINGE];
		String uuid = this.getSampleUUID();
		if(syringe == null || uuid == null) return;

		EntityHusk husk = new EntityHusk(worldObj);
		husk.setupClone(uuid, syringe.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_NAME));
		husk.setLocationAndAngles(xCoord + 0.5D, yCoord + 1D, zCoord + 0.5D, 0F, 0F);
		worldObj.spawnEntityInWorld(husk);

		syringe.stackTagCompound = null;
		this.markDirty();
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeBoolean(active);
		buf.writeInt(progress);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		maxPower = buf.readLong();
		active = buf.readBoolean();
		progress = buf.readInt();
		tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.active = nbt.getBoolean("active");
		this.progress = nbt.getInteger("progress");
		this.tank.readFromNBT(nbt, "tank");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setLong("maxPower", maxPower);
		nbt.setBoolean("active", active);
		nbt.setInteger("progress", progress);
		this.tank.writeToNBT(nbt, "tank");
	}

	@Override public long getPower() { return Math.max(Math.min(power, maxPower), 0); }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == SLOT_SYRINGE) return stack.getItem() == ModItems.medical_syringe;
		if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem || stack.getItem() == ModItems.battery_creative;
		return slot >= SLOT_MODDING && slot < SLOT_BATTERY;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot < SLOT_BATTERY;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] { 0, 1, 2, 3, 4, 5, 6 };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCloner(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICloner(player.inventory, this);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.getBoolean("toggle")) {
			this.active = !this.active;
			this.markDirty();
		}
	}
}
