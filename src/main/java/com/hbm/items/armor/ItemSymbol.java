package com.hbm.items.armor;

import java.util.List;

import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

public class ItemSymbol extends Item {

	public static enum SymbolType {
		GUILT,
		JUSTICE
	}

	private IIcon[] icons;

	public ItemSymbol() {
		this.setUnlocalizedName("symbol");
		this.setCreativeTab(null);
		this.setHasSubtypes(true);
	}

	@Override
	public void registerIcons(IIconRegister reg) {
		this.icons = new IIcon[SymbolType.values().length];
		for(int i = 0; i < SymbolType.values().length; i++) {
			this.icons[i] = reg.registerIcon(RefStrings.MODID + ":" + SymbolType.values()[i].name().toLowerCase());
		}
	}

	@Override
	public IIcon getIconFromDamage(int meta) {
		SymbolType type = fromMeta(meta);
		return type != null ? this.icons[type.ordinal()] : this.icons[0];
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < SymbolType.values().length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	public SymbolType getSymbolType(ItemStack stack) {
		return fromMeta(stack.getItemDamage());
	}

	public static SymbolType fromMeta(int meta) {
		return meta >= 0 && meta < SymbolType.values().length ? SymbolType.values()[meta] : null;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		SymbolType t = getSymbolType(stack);
		return t != null ? "item.symbol_" + t.name().toLowerCase() : super.getUnlocalizedName();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		SymbolType type = getSymbolType(stack);
		if(type == null) return;
		switch(type) {
		case GUILT:
			list.add(EnumChatFormatting.RED + "The weight of your sin tugs on your neck");
			break;
		case JUSTICE:
			list.add(EnumChatFormatting.YELLOW + "Shall not the Judge of all the earth do right?");
			break;
		default:
			break;
		}
	}
}
