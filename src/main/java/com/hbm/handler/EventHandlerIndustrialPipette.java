package com.hbm.handler;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModItems;
import com.hbm.items.armor.ItemModIndustrialPipette;

import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

public class EventHandlerIndustrialPipette {

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.action != Action.LEFT_CLICK_BLOCK) return;
		if(event.entityPlayer.worldObj.isRemote) return;
		if(event.entityPlayer.getHeldItem() != null) return;

		EntityPlayer player = event.entityPlayer;
		ItemStack helmet = player.inventory.armorInventory[3];
		if(helmet == null) return;
		ItemStack mod = ArmorModHandler.pryMods(helmet)[ArmorModHandler.helmet_only];
		if(mod == null || mod.getItem() != ModItems.industrial_pipette) return;

		TileEntity te = player.worldObj.getTileEntity(event.x, event.y, event.z);
		if(!(te instanceof api.hbm.fluidmk2.IFluidStandardSenderMK2) && !(te instanceof api.hbm.fluidmk2.IFluidStandardReceiverMK2)) {
			net.minecraft.block.Block block = player.worldObj.getBlock(event.x, event.y, event.z);
			if(block instanceof com.hbm.blocks.BlockDummyable) {
				int[] core = ((com.hbm.blocks.BlockDummyable) block).findCore(player.worldObj, event.x, event.y, event.z);
				if(core != null) {
					te = player.worldObj.getTileEntity(core[0], core[1], core[2]);
				}
			}
		}
		if(!(te instanceof api.hbm.fluidmk2.IFluidStandardSenderMK2) && !(te instanceof api.hbm.fluidmk2.IFluidStandardReceiverMK2)) return;

		FluidType pipetteType = IFillableItem.getFluidType(mod);
		int pipetteFill = IFillableItem.getFluidFill(mod);

		if(player.isSneaking()) {
			FluidTank[] tanks = null;
			if(te instanceof api.hbm.fluidmk2.IFluidStandardSenderMK2) {
				tanks = ((api.hbm.fluidmk2.IFluidStandardSenderMK2) te).getSendingTanks();
			}
			if(tanks == null || tanks.length == 0) {
				if(te instanceof api.hbm.fluidmk2.IFluidStandardTransceiverMK2) {
					tanks = ((api.hbm.fluidmk2.IFluidStandardTransceiverMK2) te).getAllTanks();
				}
			}
			if(tanks == null) return;
			for(FluidTank tank : tanks) {
				if(tank.getFill() <= 0 || tank.getTankType() == Fluids.NONE) continue;
				if(pipetteFill > 0 && pipetteType != Fluids.NONE && pipetteType != tank.getTankType()) continue;
				if(pipetteType == Fluids.NONE) pipetteType = tank.getTankType();
				int room = ItemModIndustrialPipette.MAX_FLUID - pipetteFill;
				int take = Math.min(room, tank.getFill());
				if(take <= 0) continue;
				IFillableItem.setFluidFill(mod, pipetteType, (short) (pipetteFill + take));
				tank.setFill(tank.getFill() - take);
				ArmorModHandler.setMod(helmet, ArmorModHandler.helmet_only, mod);
				te.markDirty();
				player.worldObj.playSoundAtEntity(player, "random.orb", 0.5F, 1.0F);
				return;
			}
		} else {
			FluidTank[] tanks = null;
			if(te instanceof api.hbm.fluidmk2.IFluidStandardReceiverMK2) {
				tanks = ((api.hbm.fluidmk2.IFluidStandardReceiverMK2) te).getReceivingTanks();
			}
			if(tanks == null || tanks.length == 0) {
				if(te instanceof api.hbm.fluidmk2.IFluidStandardTransceiverMK2) {
					tanks = ((api.hbm.fluidmk2.IFluidStandardTransceiverMK2) te).getAllTanks();
				}
			}
			if(tanks == null) return;
			for(FluidTank tank : tanks) {
				if(pipetteFill <= 0 || pipetteType == Fluids.NONE) continue;
				if(tank.getTankType() != Fluids.NONE && tank.getTankType() != pipetteType) continue;
				if(tank.getTankType() == Fluids.NONE) tank.setTankType(pipetteType);
				int pour = Math.min(tank.getMaxFill() - tank.getFill(), pipetteFill);
				if(pour <= 0) continue;
				tank.setFill(tank.getFill() + pour);
				int left = pipetteFill - pour;
				if(left <= 0) mod.stackTagCompound = null;
				else IFillableItem.setFluidFill(mod, pipetteType, (short) left);
				ArmorModHandler.setMod(helmet, ArmorModHandler.helmet_only, mod);
				te.markDirty();
				player.worldObj.playSoundAtEntity(player, "random.orb", 0.5F, 1.0F);
				return;
			}
		}
	}
}
