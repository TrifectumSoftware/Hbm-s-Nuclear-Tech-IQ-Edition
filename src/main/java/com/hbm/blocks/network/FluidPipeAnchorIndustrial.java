package com.hbm.blocks.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.network.TileEntityPipeAnchorIndustrial;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class FluidPipeAnchorIndustrial extends BlockDummyable implements IBlockFluidDuct, ITooltipProvider, ILookOverlay {

	public FluidPipeAnchorIndustrial() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityPipeAnchorIndustrial();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 0, 1, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override public int getRenderType() { return -1; }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int m) {

		TileEntity te = world.getTileEntity(x, y, z);

		if(te instanceof TileEntityPipeAnchorIndustrial) {
			((TileEntityPipeAnchorIndustrial) te).disconnectAll();
		}

		super.breakBlock(world, x, y, z, b, m);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fX, float fY, float fZ) {

		if(world.isRemote) return true;

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return false;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		if(!(te instanceof TileEntityPipeBaseNT)) return false;

		TileEntityPipeBaseNT pipe = (TileEntityPipeBaseNT) te;

		if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IItemFluidIdentifier) {
			IItemFluidIdentifier id = (IItemFluidIdentifier) player.getHeldItem().getItem();
			FluidType type = id.getType(world, pos[0], pos[1], pos[2], player.getHeldItem());

			if(pipe.getType() != type) {
				pipe.setType(type);
				return true;
			}
		}

		return false;
	}

	@Override
	public void changeTypeRecursively(World world, int x, int y, int z, FluidType prevType, FluidType type, int loopsRemaining) {

		TileEntity te = world.getTileEntity(x, y, z);

		if(te instanceof TileEntityPipeAnchorIndustrial) {
			TileEntityPipeAnchorIndustrial pipe = (TileEntityPipeAnchorIndustrial) te;

			if(pipe.getType() == prevType && pipe.getType() != type) {
				pipe.setType(type);

				if(loopsRemaining > 0) {
					ForgeDirection dir = ForgeDirection.getOrientation(pipe.getBlockMetadata() - BlockDummyable.offset).getOpposite();
					Block b = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

					if(b instanceof IBlockFluidDuct) ((IBlockFluidDuct) b).changeTypeRecursively(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, prevType, type, loopsRemaining - 1);

					for(int[] pos : pipe.getConnected()) {
						Block c = world.getBlock(pos[0], pos[1], pos[2]);
						if(c instanceof IBlockFluidDuct) ((IBlockFluidDuct) c).changeTypeRecursively(world, pos[0], pos[1], pos[2], prevType, type, loopsRemaining - 1);
					}
				}
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityPipeBaseNT))
			return;

		TileEntityPipeBaseNT duct = (TileEntityPipeBaseNT) te;

		List<String> text = new ArrayList();
		text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}
}
