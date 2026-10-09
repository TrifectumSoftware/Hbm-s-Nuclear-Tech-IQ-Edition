package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class Infotab extends ItemArmorMod {

	public Infotab() {
		super(ArmorModHandler.helmet_only, true, false, false, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add("Displays the block or mob you are looking at.");
		list.add(EnumChatFormatting.GRAY + "Left click the display for recipes");
		list.add(EnumChatFormatting.GRAY + "Right click the display for uses");
		list.add("");
		super.addInformation(stack, player, list, ext);
		list.add(EnumChatFormatting.GOLD + "Can be worn on its own!");
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
		return armorType == 0;
	}

	public static ItemStack getWorn(EntityPlayer player) {
		ItemStack helmet = player.getCurrentArmor(3);
		if(helmet == null) return null;
		if(helmet.getItem() instanceof Infotab) return helmet;

		if(ArmorModHandler.hasMods(helmet)) {
			ItemStack mod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only);
			if(mod != null && mod.getItem() instanceof Infotab) return mod;

			mod = ArmorModHandler.pryMod(helmet, ArmorModHandler.plate_only);
			if(mod != null && mod.getItem() instanceof Infotab) return mod;
		}

		return null;
	}
}
