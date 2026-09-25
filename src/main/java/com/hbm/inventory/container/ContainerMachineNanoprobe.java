package com.hbm.inventory.container;

import com.hbm.inventory.SlotNonRetarded;
import com.hbm.tileentity.machine.TileEntityMachineNanoprobe;

import net.minecraft.entity.player.InventoryPlayer;

public class ContainerMachineNanoprobe extends ContainerBase {

	public ContainerMachineNanoprobe(InventoryPlayer invPlayer, TileEntityMachineNanoprobe tile) {
		super(invPlayer, tile);
		this.addSlotToContainer(new SlotNonRetarded(tile, 0, 217, 55));
		this.playerInv(invPlayer, 41, 154);
	}
}
