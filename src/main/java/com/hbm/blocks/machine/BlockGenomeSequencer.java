package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemFloppyDisk;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.tileentity.machine.TileEntityGenomeSequencer;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockGenomeSequencer extends BlockDummyable {

	public BlockGenomeSequencer() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityGenomeSequencer();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 0, 0, 1, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			int[] pos = this.findCore(world, x, y, z);
			if(pos == null) return false;
			if(player.isSneaking()) return true;
			if(player.getHeldItem() != null && player.getHeldItem().getItem() == ModItems.floppy_disk) return true;
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			return true;
		}

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return false;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		if(!(te instanceof TileEntityGenomeSequencer)) return false;

		TileEntityGenomeSequencer seq = (TileEntityGenomeSequencer) te;
		ItemStack held = player.getHeldItem();

		if(player.isSneaking()) {
			if(seq.hasDisk) {
				seq.ejectDisk();
				return true;
			}
			return false;
		}

		if(held != null && held.getItem() == ModItems.floppy_disk) {
			if(seq.hasDisk) return false;
			seq.slots[0] = held.copy();
			seq.slots[0].stackSize = 1;
			held.stackSize--;
			seq.state = TileEntityGenomeSequencer.STATE_AWAIT;
			seq.networkPackNT(15);
			seq.markDirty();
			world.playSoundEffect(pos[0] + 0.5, pos[1] + 0.5, pos[2] + 0.5, NTMSounds.UPGRADE_PLUG, 1.0F, 1.0F);
			return true;
		}

		return true;
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}
