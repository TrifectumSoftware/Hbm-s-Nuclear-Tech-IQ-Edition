package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityWaffleIron;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MachineWaffleIron extends BlockDummyable {
	public MachineWaffleIron(Material mat) {
		super(mat);
	}

	@Override
	public int[] getDimensions() {
		return new int[]{4, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		if (metadata >= 12) return new TileEntityWaffleIron();
		if (metadata >= 6) return new TileEntityProxyCombo(true, true, false);
		return null;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
		TileEntityWaffleIron te = (TileEntityWaffleIron) world.getTileEntity(x, y, z);
		if (te != null) te.checkRedstoneStatus();
	}
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		TileEntityWaffleIron te = (TileEntityWaffleIron) world.getTileEntity(x, y, z);
		if (te != null) te.checkRedstoneStatus();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}
	@Override
	protected boolean standardOpenBehavior(World world, int x, int y, int z, EntityPlayer player, int id) {
		TileEntityWaffleIron te = (TileEntityWaffleIron) world.getTileEntity(x, y, z);
		if (te != null) {
			if (world.isRemote || te.lowered || te.animationTicks > 0) return true;
			ItemStack held = player.getCurrentEquippedItem();
			if (te.slots[0] != null && held == null) {
				player.inventory.mainInventory[player.inventory.currentItem] = te.slots[0].copy();
				te.slots[0] = null;
				player.inventory.markDirty();
				te.markDirty();
			} else if (te.slots[0] == null && held != null) {
				te.slots[0] = held.splitStack(1);
				if (held.stackSize <= 0) {
					player.inventory.mainInventory[player.inventory.currentItem] = null;
				}
				player.inventory.markDirty();
				te.markDirty();
			}
			return true;
		} else return false;
	}
}
