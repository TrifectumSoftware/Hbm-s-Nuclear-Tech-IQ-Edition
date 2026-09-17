package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.contagion.BloodEntry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.ChatBuilder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemModBloodMonitor extends ItemArmorMod {

	public ItemModBloodMonitor() {
		super(ArmorModHandler.legs_only, false, false, true, false);
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		if(entity.worldObj.isRemote) return;
		if(entity.worldObj.getTotalWorldTime() % 5 != 0) return;
		if(!(entity instanceof EntityPlayerMP)) return;

		EntityPlayerMP player = (EntityPlayerMP) entity;

		HbmBloodstreamProps props = HbmBloodstreamProps.getData(entity);
		if(props == null) return;

		List<BloodEntry> entries = props.getEntries();
		float total = props.getTotalFluid();
		int i = 0;

		for(BloodEntry entry : entries) {
			if(entry.amount <= 0) continue;
			if(entry.frameId != null) continue;

			int id = 979 + i;
			i++;

			FluidType fluid = Fluids.fromID(entry.fluidId);
			if(entry.fluidId == Fluids.HUMAN_BLOOD.getID()) {
				int percent = (int) (entry.amount * 100F / total);
				PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.startTranslation(fluid.getUnlocalizedName()).color(EnumChatFormatting.AQUA).next(": ").next(percent + "%").color(EnumChatFormatting.RESET).flush(), id, 4000), player);
			} else {
				int hundredths = (int) (entry.amount * 10000F / total);
				PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.startTranslation(fluid.getUnlocalizedName()).color(EnumChatFormatting.GREEN).next(": ").next(hundredths / 100 + "." + (hundredths % 100) / 10 + (hundredths % 10) + "%").color(EnumChatFormatting.RESET).flush(), id, 4000), player);
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.YELLOW + "Monitors bloodstream");
		list.add(EnumChatFormatting.GREEN + "Shows bloodstream composition on the HUD");
		list.add("");
		super.addInformation(stack, player, list, bool);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.YELLOW + "  " + stack.getDisplayName() + " (monitors bloodstream)");
	}
}
