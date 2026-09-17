package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;


public class ItemModGloves extends ItemArmorMod {

	public ItemModGloves(int durability) {
		super(ArmorModHandler.extra, false, true, false, false);
		this.setMaxDamage(durability);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		list.add(EnumChatFormatting.YELLOW + "Protects against contaminated items");
		list.add((stack.getMaxDamage() - stack.getItemDamage()) + "/" + stack.getMaxDamage());

		list.add("");
		super.addInformation(stack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (" + (stack.getMaxDamage() - stack.getItemDamage()) + "/" + stack.getMaxDamage() + ")");
	}

	public static boolean wear(EntityPlayer player) {

		ItemStack chest = player.getEquipmentInSlot(3);
		if(chest == null) return false;

		ItemStack gloves = ArmorModHandler.pryMod(chest, ArmorModHandler.extra);
		if(gloves == null || !(gloves.getItem() instanceof ItemModGloves)) return false;

		gloves.setItemDamage(gloves.getItemDamage() + 1);

		if(gloves.getItemDamage() >= gloves.getMaxDamage()) {
			ArmorModHandler.removeMod(chest, ArmorModHandler.extra);
			return false;
		}

		ArmorModHandler.setMod(chest, ArmorModHandler.extra, gloves);
		return true;
	}
}
