package com.hbm.handler.husk;

import com.hbm.entity.mob.EntityHusk;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class HuskInventory implements IInventory {

	public static final int MAIN = 36;
	public static final int ARMOR = 4;
	public static final int ARMOR_OFFSET = 100;

	public final ItemStack[] mainInventory = new ItemStack[MAIN];
	public final ItemStack[] armorInventory = new ItemStack[ARMOR];

	private final EntityHusk husk;

	public HuskInventory(EntityHusk husk) {
		this.husk = husk;
		this.read();
	}

	public void read() {
		for(int i = 0; i < mainInventory.length; i++) mainInventory[i] = null;
		for(int i = 0; i < armorInventory.length; i++) armorInventory[i] = null;

		NBTTagList list = this.husk.getPlayerData().getTagList("Inventory", 10);

		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound entry = list.getCompoundTagAt(i);
			int slot = entry.getByte("Slot") & 255;
			ItemStack stack = ItemStack.loadItemStackFromNBT(entry);
			if(stack == null || stack.stackSize <= 0) continue;

			if(slot < MAIN) mainInventory[slot] = stack;
			else if(slot >= ARMOR_OFFSET && slot < ARMOR_OFFSET + ARMOR) armorInventory[slot - ARMOR_OFFSET] = stack;
		}
	}

	@Override
	public void markDirty() {
		NBTTagList list = new NBTTagList();

		for(int i = 0; i < mainInventory.length; i++) append(list, mainInventory[i], i);
		for(int i = 0; i < armorInventory.length; i++) append(list, armorInventory[i], ARMOR_OFFSET + i);

		this.husk.getPlayerData().setTag("Inventory", list);
		this.husk.onInventoryChanged();
	}

	private static void append(NBTTagList list, ItemStack stack, int slot) {
		if(stack == null || stack.stackSize <= 0) return;

		NBTTagCompound entry = new NBTTagCompound();
		stack.writeToNBT(entry);
		entry.setByte("Slot", (byte) slot);
		list.appendTag(entry);
	}

	@Override public int getSizeInventory() { return MAIN + ARMOR; }
	@Override public int getInventoryStackLimit() { return 64; }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }
	@Override public boolean isUseableByPlayer(EntityPlayer player) { return true; }
	@Override public void openInventory() { }
	@Override public void closeInventory() { }
	@Override public String getInventoryName() { return "container.husk"; }
	@Override public boolean hasCustomInventoryName() { return false; }

	@Override
	public ItemStack getStackInSlot(int slot) {
		if(slot < 0) return null;
		if(slot < MAIN) return mainInventory[slot];
		if(slot < MAIN + ARMOR) return armorInventory[slot - MAIN];
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		if(slot < 0) return;
		if(slot < MAIN) mainInventory[slot] = stack;
		else if(slot < MAIN + ARMOR) armorInventory[slot - MAIN] = stack;
		this.markDirty();
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		ItemStack current = this.getStackInSlot(slot);
		if(current == null) return null;

		if(current.stackSize <= count) {
			this.setInventorySlotContents(slot, null);
			return current;
		}

		ItemStack split = current.splitStack(count);
		this.markDirty();
		return split;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		ItemStack stack = this.getStackInSlot(slot);
		this.setInventorySlotContents(slot, null);
		return stack;
	}
}
