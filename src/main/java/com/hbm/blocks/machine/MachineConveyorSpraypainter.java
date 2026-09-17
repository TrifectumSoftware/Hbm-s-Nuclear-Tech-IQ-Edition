package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityConveyorSpraypainter;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.conveyor.IConveyorBelt;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineConveyorSpraypainter extends BlockDummyable implements IConveyorBelt, ILookOverlay, ITooltipProvider {

	public MachineConveyorSpraypainter(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityConveyorSpraypainter();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(!world.isRemote && !player.isSneaking()) {

			if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IItemFluidIdentifier) {
				int[] pos = this.findCore(world, x, y, z);

				if(pos == null)
					return false;

				TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

				if(!(te instanceof TileEntityConveyorSpraypainter))
					return false;

				TileEntityConveyorSpraypainter painter = (TileEntityConveyorSpraypainter) te;

				FluidType type = ((IItemFluidIdentifier) player.getHeldItem().getItem()).getType(world, pos[0], pos[1], pos[2], player.getHeldItem());
				painter.tank.setTankType(type);
				painter.markChanged();
				player.addChatComponentMessage(new ChatComponentText("Changed type to ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)).appendSibling(new ChatComponentTranslation(type.getConditionalName())).appendSibling(new ChatComponentText("!")));
				return true;
			}

			return false;

		} else {
			return true;
		}
	}

	@Override
	public Vec3 getTravelLocation(World world, int x, int y, int z, Vec3 itemPos, double speed) {
		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		Vec3 snap = this.getClosestSnappingPosition(world, x, y, z, itemPos);
		Vec3 dest = Vec3.createVectorHelper(snap.xCoord - dir.offsetX * speed, snap.yCoord - dir.offsetY * speed, snap.zCoord - dir.offsetZ * speed);
		Vec3 motion = Vec3.createVectorHelper((dest.xCoord - itemPos.xCoord), (dest.yCoord - itemPos.yCoord), (dest.zCoord - itemPos.zCoord));
		double len = motion.lengthVector();
		return Vec3.createVectorHelper(itemPos.xCoord + motion.xCoord / len * speed, itemPos.yCoord + motion.yCoord / len * speed, itemPos.zCoord + motion.zCoord / len * speed);
	}

	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3 itemPos) {
		int meta = world.getBlockMetadata(x, y - 1, z) - offset;
		return ForgeDirection.getOrientation(meta).getRotation(ForgeDirection.UP);
	}

	@Override
	public Vec3 getClosestSnappingPosition(World world, int x, int y, int z, Vec3 itemPos) {

		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		itemPos.xCoord = MathHelper.clamp_double(itemPos.xCoord, x, x + 1);
		itemPos.zCoord = MathHelper.clamp_double(itemPos.zCoord, z, z + 1);
		double posX = x + 0.5;
		double posZ = z + 0.5;
		if(dir.offsetX != 0) posX = itemPos.xCoord;
		if(dir.offsetZ != 0) posZ = itemPos.zCoord;
		return Vec3.createVectorHelper(posX, y + 0.25, posZ);
	}

	@Override
	public boolean canItemStay(World world, int x, int y, int z, Vec3 itemPos) {
		return world.getBlock(x, y - 1, z) == this && world.getBlockMetadata(x, y - 1, z) >= 12;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityConveyorSpraypainter))
			return;

		TileEntityConveyorSpraypainter painter = (TileEntityConveyorSpraypainter) te;
		List<String> text = new ArrayList();

		text.add(BobMathUtil.getShortNumber(painter.power) + "HE / " + BobMathUtil.getShortNumber(TileEntityConveyorSpraypainter.maxPower) + "HE");
		text.add(EnumChatFormatting.GREEN + "-> " + EnumChatFormatting.RESET + painter.tank.getTankType().getLocalizedName() + ": " + painter.tank.getFill() + "/" + painter.tank.getMaxFill() + "mB");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
