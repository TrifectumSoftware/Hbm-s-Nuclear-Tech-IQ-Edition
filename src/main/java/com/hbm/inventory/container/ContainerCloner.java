package com.hbm.inventory.container;

import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityCloner;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCloner extends ContainerBase {

	public ContainerCloner(InventoryPlayer inv, TileEntityCloner tile) {
		super(inv, tile);

		this.addSlotToContainer(new Slot(tile, 0, 24, 108));
		this.addSlots(tile, 1, 62, 108, 1, 5, 19);
		this.addSlotToContainer(new Slot(tile, TileEntityCloner.SLOT_BATTERY, 188, 55));

		this.playerInv(inv, 20, 154, 212);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack original = null;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if(slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			original = stack.copy();

			if(index < tile.getSizeInventory()) {
				if(!this.mergeItemStack(stack, tile.getSizeInventory(), this.inventorySlots.size(), true)) return null;
			} else if(stack.getItem() == ModItems.medical_syringe) {
				if(!this.mergeItemStack(stack, 0, 1, false)) return null;
			} else if(stack.getItem() instanceof IBatteryItem || stack.getItem() == ModItems.battery_creative) {
				if(!this.mergeItemStack(stack, TileEntityCloner.SLOT_BATTERY, TileEntityCloner.SLOT_BATTERY + 1, false)) return null;
			} else {
				if(!this.mergeItemStack(stack, 1, TileEntityCloner.SLOT_BATTERY, false)) return null;
			}

			if(stack.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}

			slot.onPickupFromSlot(player, stack);
		}

		return original;
	}
}
