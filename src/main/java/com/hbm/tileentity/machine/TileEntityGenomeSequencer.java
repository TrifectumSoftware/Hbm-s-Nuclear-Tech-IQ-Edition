package com.hbm.tileentity.machine;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.gui.GUIScreenGenomeSequencer;
import com.hbm.items.tool.ItemFloppyDisk;
import com.hbm.main.NTMSounds;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class TileEntityGenomeSequencer extends TileEntityMachineBase implements IGUIProvider, IControlReceiver {

	public static final int STATE_EMPTY = 0;
	public static final int STATE_AWAIT = 1;
	public static final int STATE_DECODED = 2;

	public static final int LINES = 24;

	public int state;
	public boolean hasDisk;
	public String[] text = new String[LINES];
	public String name = "";

	public TileEntityGenomeSequencer() {
		super(1);
	}

	@Override
	public String getName() {
		return "container.genomeSequencer";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {

			hasDisk = slots[0] != null;
			int prevState = state;
			if(!hasDisk) state = STATE_EMPTY;

			if(prevState != state || worldObj.getTotalWorldTime() % 20 == 0) {
				networkPackNT(15);
			}
		}
	}

	public void decode() {
		ItemStack disk = slots[0];
		if(disk == null || disk.getItem() != com.hbm.items.ModItems.floppy_disk) return;

		String frameId = ItemFloppyDisk.getFrameId(disk);
		if(frameId == null) return;

		String storedName = ItemFloppyDisk.getDiskName(disk);
		name = storedName == null ? "" : storedName;

		java.util.List<String> lines = ItemFloppyDisk.getAnalysisLines(disk);
		String genome = ItemFloppyDisk.getGenome(disk);
		text = new String[LINES];
		int line = 0;
		if(genome != null && !genome.isEmpty()) {
			text[line++] = "GENOME: " + genome;
		}
		for(String s : lines) {
			if(line >= LINES) break;
			text[line++] = s;
		}

		state = STATE_DECODED;
		networkPackNT(15);
	}


	public void setName(String newName) {
		ItemStack disk = slots[0];
		if(disk == null) return;
		String cleaned = newName == null ? "" : newName.trim();
		if(cleaned.length() > 12) cleaned = cleaned.substring(0, 12);
		ItemFloppyDisk.setDiskName(disk, cleaned);
		name = cleaned;
		markDirty();
		decode();
	}


	public void ejectDisk() {
		ItemStack disk = slots[0];
		if(disk != null) {
			worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord + 0.5, yCoord + 1, zCoord + 0.5, disk));
		}
		slots[0] = null;
		hasDisk = false;
		state = STATE_EMPTY;
		text = new String[LINES];
		name = "";
		worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, NTMSounds.UNPACK, 1.0F, 1.0F);
		networkPackNT(15);
		markDirty();
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("anal")) {
			decode();
		}
		if(data.hasKey("eject")) {
			ejectDisk();
		}
		if(data.hasKey("name")) {
			setName(data.getString("name"));
		}
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 16 * 16;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(hasDisk);
		buf.writeInt(state);
		BufferUtil.writeString(buf, name);
		for(int i = 0; i < text.length; i++) {
			BufferUtil.writeString(buf, text[i] == null ? "" : text[i]);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		hasDisk = buf.readBoolean();
		state = buf.readInt();
		name = BufferUtil.readString(buf);
		for(int i = 0; i < text.length; i++) {
			text[i] = BufferUtil.readString(buf);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		state = nbt.getInteger("state");
		hasDisk = nbt.getBoolean("hasDisk");
		name = nbt.getString("name");
		for(int i = 0; i < text.length; i++) {
			text[i] = nbt.getString("text" + i);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("state", state);
		nbt.setBoolean("hasDisk", hasDisk);
		nbt.setString("name", name);
		for(int i = 0; i < text.length; i++) {
			if(text[i] != null) nbt.setString("text" + i, text[i]);
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenGenomeSequencer(this);
	}
}
