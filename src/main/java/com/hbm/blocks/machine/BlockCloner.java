package com.hbm.blocks.machine;

import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.main.NTMSounds;
import com.hbm.tileentity.machine.TileEntityCloner;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCloner extends BlockMachineBase {

	public BlockCloner() {
		super(Material.iron, 0);
		this.rotatable = true;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
		TileEntity te = world.getTileEntity(x, y, z);

		if(te instanceof TileEntityCloner && player.isSneaking()) {
			ItemStack held = player.getHeldItem();

			if(held != null && held.getItem() instanceof IItemFluidIdentifier) {
				if(!world.isRemote) ((TileEntityCloner) te).setTankType(((IItemFluidIdentifier) held.getItem()).getType(world, x, y, z, held));
				return true;
			}

			if(held == null) {
				if(!world.isRemote) ((TileEntityCloner) te).toggle();
				world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, NTMSounds.LEVER_STOP, 1F, 1F);
				return true;
			}
		}

		return super.onBlockActivated(world, x, y, z, player, side, hx, hy, hz);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCloner();
	}
}
