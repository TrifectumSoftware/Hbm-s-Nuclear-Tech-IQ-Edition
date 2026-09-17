package com.hbm.inventory.container;

import com.hbm.inventory.SlotTakeOnly;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityMedicineSynthesizer;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMedicineSynthesizer extends ContainerBase {

	public ContainerMedicineSynthesizer(InventoryPlayer invPlayer, TileEntityMedicineSynthesizer tile) {
		super(invPlayer, tile);

		// 0 - empty medical syringe
		addSlotToContainer(new Slot(tile, 0, 11, 18) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() == ModItems.medical_syringe && IFillableItem.getFluidFill(stack) == 0;
			}
		});
		// 1 - blood vial
		addSlotToContainer(new Slot(tile, 1, 11, 36));
		// 2 - floppy disk
		addSlotToContainer(new Slot(tile, 2, 11, 54));
		// 3 - pharma computing unit
		addSlotToContainer(new Slot(tile, 3, 72, 76));

		// 4 - battery
		addSlotToContainer(new Slot(tile, 4, 168, 83));

		// 5 - antiserum syringe output, take only
		addSlotToContainer(new SlotTakeOnly(tile, 5, 101, 36));

		// player inventory, matching the cyclotron layout
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 15 + j * 18, 133 + i * 18));
			}
		}
		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 15 + i * 18, 191));
		}
	}
}
