package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.util.EnumChatFormatting;

public class ItemMeteoriteHoe extends ModHoe implements IMeteoriteTool {

	public final int tier;

	public ItemMeteoriteHoe(int tier, ToolMaterial material) {
		super(material);
		this.tier = tier;
		this.setMaxDamage(4000 + tier * 500);
	}

	@Override
	public int getTier() {
		return tier;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);

		String[] lines = I18nUtil.resolveKeyArray(getUnlocalizedName(stack) + ".desc");
		for(String line : lines) {
			list.add(EnumChatFormatting.ITALIC + line);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String base = I18nUtil.resolveKey("item.meteorite_hoe.name");
		String tierName = ItemMeteoriteBase.tierName(tier);
		if(tierName.isEmpty()) return base;
		return I18nUtil.resolveKey("item.meteorite_tier." + tierName) + " " + base;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String tierName = ItemMeteoriteBase.tierName(tier);
		if(tierName.isEmpty()) return super.getUnlocalizedName(stack);
		return super.getUnlocalizedName() + "." + tierName;
	}
}
