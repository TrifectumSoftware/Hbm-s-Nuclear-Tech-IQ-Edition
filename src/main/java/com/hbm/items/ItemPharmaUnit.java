package com.hbm.items;

import com.hbm.lib.RefStrings;

import net.minecraft.item.Item;


public class ItemPharmaUnit extends Item {

	public ItemPharmaUnit() {
		this.setMaxStackSize(1);
		this.setMaxDamage(16);
		this.setUnlocalizedName("pharma_computing_unit");
		this.setTextureName(RefStrings.MODID + ":item_secret.pharma");
		this.setCreativeTab(null);
	}
}
