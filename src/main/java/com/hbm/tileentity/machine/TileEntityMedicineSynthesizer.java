package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.machine.BlockMedicineSynthesizer;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.contagion.PharmaProfile;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMedicineSynthesizer;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMedicineSynthesizer;
import com.hbm.items.ItemVial;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemFloppyDisk;
import com.hbm.items.tool.ItemMedicalSyringe;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMedicineSynthesizer extends TileEntityMachineBase implements IGUIProvider, IControlReceiver, IEnergyReceiverMK2, IFluidStandardTransceiver {

	public static final int SLOT_SYRINGE = 0;
	public static final int SLOT_VIAL = 1;
	public static final int SLOT_FLOPPY = 2;
	public static final int SLOT_PHARMA = 3;
	public static final int SLOT_BATTERY = 4;
	public static final int SLOT_OUTPUT = 5;

	public FluidTank tank;
	public int progress;
	public int maxProgress = 100;

	public long power;
	public long maxPower = 1000000;

	public boolean showFrameTop = true;

	public TileEntityMedicineSynthesizer() {
		super(6);
		this.tank = new FluidTank(Fluids.ANTISERUM, 16000);
	}

	@Override
	public String getName() {
		return "container.medicineSynthesizer";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {

			this.power = Library.chargeTEFromItems(slots, SLOT_BATTERY, power, maxPower);
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				trySubscribe(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}

			if(canProcess()) {
				power -= 200;
				progress++;
				if(progress >= maxProgress) {
					progress = 0;
					process();
					this.markDirty();
				}
			} else {
				progress = 0;
			}

			networkPackNT(15);
		}
	}

	private boolean canProcess() {

		if(power < 200) return false;
		if(tank.getFill() < 100) return false;

		ItemStack syringe = slots[SLOT_SYRINGE];
		if(syringe == null || syringe.getItem() != ModItems.medical_syringe) return false;
		if(IFillableItem.getFluidFill(syringe) > 0) return false;

		ItemStack vial = slots[SLOT_VIAL];
		if(vial == null || vial.getItem() != ModItems.vial) return false;
		String vialFrame = ItemVial.readFrame(vial);
		if(vialFrame == null) return false;

		ItemStack floppy = slots[SLOT_FLOPPY];
		if(floppy == null || floppy.getItem() != ModItems.floppy_disk) return false;
		if(!vialFrame.equals(ItemFloppyDisk.getFrameId(floppy))) return false;

		ItemStack pharma = slots[SLOT_PHARMA];
		if(pharma == null || pharma.getItem() != ModItems.pharma_computing_unit) return false;

		ItemStack output = slots[SLOT_OUTPUT];
		if(output != null && (output.getItem() != ModItems.medical_syringe || IFillableItem.getFluidFill(output) > 0)) return false;

		return true;
	}

	private void process() {

		ItemStack floppy = slots[SLOT_FLOPPY];
		String frameId = ItemFloppyDisk.getFrameId(floppy);
		String genome = ItemFloppyDisk.getGenome(floppy);

		ItemStack syringe = slots[SLOT_SYRINGE];
		IFillableItem.setFluidFill(syringe, Fluids.ANTISERUM, (short) ItemMedicalSyringe.MAX_DOSE);

		PharmaProfile profile = new PharmaProfile();
		profile.target = frameId;
		profile.targetGenome = genome;
		if(!syringe.hasTagCompound()) syringe.stackTagCompound = new NBTTagCompound();
		syringe.stackTagCompound.setTag("pharma", profile.toNBT());

		slots[SLOT_VIAL] = null;
		slots[SLOT_FLOPPY] = null;

		ItemStack pharma = slots[SLOT_PHARMA];
		pharma.setItemDamage(pharma.getItemDamage() + 1);
		if(pharma.getItemDamage() >= pharma.getMaxDamage()) {
			slots[SLOT_PHARMA] = null;
		}

		tank.setFill(tank.getFill() - 100);

		slots[SLOT_OUTPUT] = syringe;
		slots[SLOT_SYRINGE] = null;
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	protected DirPos[] getConPos() {
		ForgeDirection facing = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		int[] dim = MultiblockHandlerXR.rotate(((BlockMedicineSynthesizer) this.getBlockType()).getDimensions(), facing);
		int west = dim[4], east = dim[5], north = dim[2], south = dim[3];
		return new DirPos[] {
				new DirPos(xCoord - west - 1, yCoord, zCoord, Library.NEG_X),
				new DirPos(xCoord - west - 1, yCoord + 1, zCoord, Library.NEG_X),
				new DirPos(xCoord - west - 1, yCoord + 2, zCoord, Library.NEG_X),
				new DirPos(xCoord + east + 1, yCoord, zCoord, Library.POS_X),
				new DirPos(xCoord + east + 1, yCoord + 1, zCoord, Library.POS_X),
				new DirPos(xCoord + east + 1, yCoord + 2, zCoord, Library.POS_X),
				new DirPos(xCoord, yCoord, zCoord - north - 1, Library.NEG_Z),
				new DirPos(xCoord, yCoord + 1, zCoord - north - 1, Library.NEG_Z),
				new DirPos(xCoord, yCoord + 2, zCoord - north - 1, Library.NEG_Z),
				new DirPos(xCoord, yCoord, zCoord + south + 1, Library.POS_Z),
				new DirPos(xCoord, yCoord + 1, zCoord + south + 1, Library.POS_Z),
				new DirPos(xCoord, yCoord + 2, zCoord + south + 1, Library.POS_Z),
		};
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH || dir == ForgeDirection.EAST || dir == ForgeDirection.WEST;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH || dir == ForgeDirection.EAST || dir == ForgeDirection.WEST;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[0];
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {tank};
	}

	public int getProgressScaled(int i) {
		return (progress * i) / maxProgress;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		tank.serialize(buf);
		buf.writeBoolean(showFrameTop);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		tank.deserialize(buf);
		showFrameTop = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		progress = nbt.getInteger("progress");
		tank.readFromNBT(nbt, "tank");
		showFrameTop = !nbt.hasKey("showFrameTop") || nbt.getBoolean("showFrameTop");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setInteger("progress", progress);
		tank.writeToNBT(nbt, "tank");
		nbt.setBoolean("showFrameTop", showFrameTop);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 16 * 16;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMedicineSynthesizer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMedicineSynthesizer(player.inventory, this);
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }
}
