package com.hbm.inventory.container;

import com.hbm.inventory.SlotTakeOnly;
import com.hbm.tileentity.machine.TileEntityBloodCentrifuge;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBloodCentrifuge extends Container {

	private TileEntityBloodCentrifuge centrifuge;

	public ContainerBloodCentrifuge(InventoryPlayer playerInv, TileEntityBloodCentrifuge tile) {
		centrifuge = tile;

		//Syringe deposit: filled in, emptied out
		this.addSlotToContainer(new Slot(tile, 0, 148, 17));
		this.addSlotToContainer(new SlotTakeOnly(tile, 1, 148, 53));

		//Vial outputs (3 per row, 20px apart, right face 2px from the syringe slot)
		this.addSlotToContainer(new SlotTakeOnly(tile, 2, 86, 18));
		this.addSlotToContainer(new SlotTakeOnly(tile, 3, 106, 18));
		this.addSlotToContainer(new SlotTakeOnly(tile, 4, 126, 18));
		this.addSlotToContainer(new SlotTakeOnly(tile, 5, 86, 54));
		this.addSlotToContainer(new SlotTakeOnly(tile, 6, 106, 54));
		this.addSlotToContainer(new SlotTakeOnly(tile, 7, 126, 54));

		//Battery
		this.addSlotToContainer(new Slot(tile, 8, 8, 53));

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 180));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2) {
		ItemStack var3 = null;
		Slot var4 = (Slot) this.inventorySlots.get(par2);

		if(var4 != null && var4.getHasStack()) {
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();

			if(par2 <= 8) {
				if(!this.mergeItemStack(var5, 9, this.inventorySlots.size(), true)) return null;
			} else if(!this.mergeItemStack(var5, 0, 9, false)) {
				return null;
			}

			if(var5.stackSize == 0) var4.putStack((ItemStack) null);
			else var4.onSlotChanged();
		}

		return var3;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return centrifuge.isUseableByPlayer(player);
	}
}
