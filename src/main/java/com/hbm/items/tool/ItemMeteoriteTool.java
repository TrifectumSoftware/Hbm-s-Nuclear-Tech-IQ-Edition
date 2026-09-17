package com.hbm.items.tool;

import java.util.List;

import com.hbm.handler.ability.IToolAreaAbility;
import com.hbm.handler.ability.IToolHarvestAbility;
import com.hbm.handler.ability.IWeaponAbility;
import com.hbm.items.tool.ItemToolAbility.EnumToolType;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.util.EnumChatFormatting;

public class ItemMeteoriteTool extends ItemToolAbility implements IMeteoriteTool {

	public final int tier;
	public final String toolName;

	public ItemMeteoriteTool(int tier, float damage, double movement, ToolMaterial material, EnumToolType type, String toolName) {
		super(damage, movement, material, type);
		this.tier = tier;
		this.toolName = toolName;
		this.addAbility(IToolAreaAbility.RECURSION, ItemMeteoriteBase.recursionLevel(tier));
		this.addAbility(IToolAreaAbility.HAMMER, 0);
		this.addAbility(IToolAreaAbility.HAMMER_FLAT, 0);
		this.addAbility(IToolHarvestAbility.SILK, 0);
		this.addAbility(IToolHarvestAbility.LUCK, ItemMeteoriteBase.luckLevel(tier));
		if("axe".equals(toolName)) this.addAbility(IWeaponAbility.BEHEADER, 0);
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
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String base = I18nUtil.resolveKey("item.meteorite_" + toolName + ".name");
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
