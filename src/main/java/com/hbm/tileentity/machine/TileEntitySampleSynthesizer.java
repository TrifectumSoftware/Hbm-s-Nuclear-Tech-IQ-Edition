package com.hbm.tileentity.machine;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerSampleSynthesizer;
import com.hbm.inventory.gui.GUISampleSynthesizer;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemFloppyDisk;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntitySampleSynthesizer extends TileEntityMachineBase implements IGUIProvider, IControlReceiver, IEnergyReceiverMK2 {

	public long power;
	public long maxPower = 2_000;

	public boolean isScanning;
	public int progress;
	public int maxProgress = 100;

	public String status = "";

	public TileEntitySampleSynthesizer() {
		super(4);
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {

			power = Library.chargeTEFromItems(slots, 3, power, maxPower);
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				trySubscribe(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);

			if(isScanning) {
				power -= 200;

				status = EnumChatFormatting.GREEN + "" + EnumChatFormatting.ITALIC + "Scanning  ";
				progress++;

				if(progress >= maxProgress) {
					progress = 0;
					isScanning = false;
					scanVial();
					status = EnumChatFormatting.GREEN + "Done! ";
				}
			} else {
				progress = 0;
			}

			networkPackNT(15);
		}
	}

	private boolean hasPathogenData(ItemStack vial) {
		if(!vial.hasTagCompound()) return false;
		NBTTagCompound nbt = vial.stackTagCompound;
		return nbt.hasKey("frame") || (nbt.hasKey("pathogen") && nbt.getCompoundTag("pathogen").hasKey("frame"));
	}

	private String readFrameId(ItemStack vial) {
		NBTTagCompound nbt = vial.stackTagCompound;
		if(nbt.hasKey("frame")) return nbt.getString("frame");
		if(nbt.hasKey("pathogen")) return nbt.getCompoundTag("pathogen").getString("frame");
		return null;
	}

	private void scanVial() {

		if(slots[0] == null || slots[0].getItem() != ModItems.floppy_disk) return;
		if(slots[1] == null || slots[1].getItem() != ModItems.vial) return;

		String frameId = readFrameId(slots[1]);
		if(frameId == null) return;

		float amount = 0;
		String genome = null;
		NBTTagCompound mut = null;
		if(slots[1].stackTagCompound.hasKey("amount")) amount = slots[1].stackTagCompound.getFloat("amount");
		if(slots[1].stackTagCompound.hasKey("mut")) {
			mut = slots[1].stackTagCompound.getCompoundTag("mut");
			if(mut.hasKey("genome")) genome = mut.getString("genome");
		}
		if(slots[1].stackTagCompound.hasKey("genome")) genome = slots[1].stackTagCompound.getString("genome");

		ItemFloppyDisk.setPathogenData(slots[0], frameId, amount, genome, mut);
		com.hbm.handler.contagion.DiseaseDefinition def = com.hbm.handler.contagion.DiseaseRegistry.get(frameId);
		if(def == null) def = com.hbm.handler.contagion.DiseaseRegistry.restore(frameId, mut);
		ItemFloppyDisk.storeDef(slots[0], def);
		slots[1] = null;
	}

	private void cloneDisk() {
		if(slots[0] == null || slots[0].getItem() != ModItems.floppy_disk || ItemFloppyDisk.getFrameId(slots[0]) == null) {
			status = EnumChatFormatting.RED + "No source ";
			return;
		}
		if(slots[2] == null || slots[2].getItem() != ModItems.floppy_disk || ItemFloppyDisk.getFrameId(slots[2]) != null) {
			status = EnumChatFormatting.RED + "No target ";
			return;
		}

		ItemStack copy = slots[0].copy();
		slots[2] = copy;
		status = EnumChatFormatting.GREEN + "Cloned ";
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeBoolean(isScanning);
		buf.writeInt(progress);
		BufferUtil.writeString(buf, status);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		isScanning = buf.readBoolean();
		progress = buf.readInt();
		status = BufferUtil.readString(buf);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setBoolean("isScanning", isScanning);
		nbt.setInteger("progress", progress);
		nbt.setString("status", status);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		isScanning = nbt.getBoolean("isScanning");
		progress = nbt.getInteger("progress");
		status = nbt.getString("status");
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("scan")) {
			if(power < maxPower * 0.75) {
				status = EnumChatFormatting.RED + "No power ";
				return;
			}
			if(slots[0] == null || slots[0].getItem() != ModItems.floppy_disk) {
				status = EnumChatFormatting.RED + "No disk ";
				return;
			}
			if(slots[1] == null || slots[1].getItem() != ModItems.vial || !hasPathogenData(slots[1])) {
				status = EnumChatFormatting.RED + "No sample ";
				return;
			}
			isScanning = true;
		}
		if(data.hasKey("clone")) {
			cloneDisk();
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerSampleSynthesizer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUISampleSynthesizer(player.inventory, this);
	}

	@Override
	public String getName() {
		return "container.sampleSynthesizer";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }
}
