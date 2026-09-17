package com.hbm.blocks.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.network.TileEntityPipeAnchorPressurizer;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.fluidmk2.IFluidConnectorBlockMK2;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class FluidPipeAnchorPressurizer extends BlockDummyable implements IFluidConnectorBlockMK2, ITooltipProvider, ILookOverlay {

	public FluidPipeAnchorPressurizer() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityPipeAnchorPressurizer();
		return null;
	}

	@Override public int[] getDimensions() { return new int[] {0, 0, 1, 0, 1, 0}; }
	@Override public int getOffset() { return 0; }
	@Override public int getRenderType() { return -1; }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fX, float fY, float fZ) {

		if(world.isRemote) return true;

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return false;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		if(!(te instanceof TileEntityPipeAnchorPressurizer)) return false;

		TileEntityPipeAnchorPressurizer press = (TileEntityPipeAnchorPressurizer) te;

		if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IItemFluidIdentifier) {
			IItemFluidIdentifier id = (IItemFluidIdentifier) player.getHeldItem().getItem();
			FluidType type = id.getType(world, pos[0], pos[1], pos[2], player.getHeldItem());

			if(type != Fluids.AIR) {
				press.tanks[0].setTankType(type);
				press.markDirty();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canConnect(FluidType type, IBlockAccess world, int x, int y, int z, ForgeDirection dir) {

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return false;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		if(!(te instanceof TileEntityPipeAnchorPressurizer)) return false;

		TileEntityPipeAnchorPressurizer press = (TileEntityPipeAnchorPressurizer) te;

		ForgeDirection facing = ForgeDirection.getOrientation(press.getBlockMetadata() - BlockDummyable.offset);
		if(dir == ForgeDirection.DOWN) return type == Fluids.AIR;
		if(dir == facing) return type == press.tanks[0].getTankType() || press.tanks[0].getTankType() == Fluids.NONE;
		if(dir == facing.getOpposite()) return type == Fluids.AIR;
		return false;
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
		if(!(te instanceof TileEntityPipeAnchorPressurizer)) return;

		TileEntityPipeAnchorPressurizer press = (TileEntityPipeAnchorPressurizer) te;

		ForgeDirection facing = ForgeDirection.getOrientation(press.getBlockMetadata() - BlockDummyable.offset);

		int maxProj = Integer.MIN_VALUE;
		int minProj = Integer.MAX_VALUE;
		for(int[] off : press.getBlocks()) {
			int proj = off[0] * facing.offsetX + off[1] * facing.offsetZ;
			maxProj = Math.max(maxProj, proj);
			minProj = Math.min(minProj, proj);
		}
		int hitProj = (x - pos[0]) * facing.offsetX + (z - pos[2]) * facing.offsetZ;

		List<String> text = new ArrayList();

		if(hitProj == maxProj) {
			text.add(EnumChatFormatting.GREEN + "->" + EnumChatFormatting.RESET + "/" +  EnumChatFormatting.RED + "<- " + EnumChatFormatting.RESET + press.tanks[0].getTankType().getLocalizedName() + ": " + press.tanks[0].getFill() + "/" + press.tanks[0].getMaxFill() + "mB");
		} else if(hitProj == minProj) {
			text.add(EnumChatFormatting.GREEN + "-> " + EnumChatFormatting.RESET + "Compressed Air: " + press.tanks[1].getFill() + "/" + press.tanks[1].getMaxFill() + "mB");
		}

		if(!text.isEmpty()) {
			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
		}
	}
}
