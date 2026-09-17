package com.hbm.items.machine;

import java.util.List;

import com.hbm.lib.RefStrings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

public class ItemRawOre extends Item {

	public String[] names;
	public IIcon[] icons;

	public ItemRawOre(String[] names) {
		this.names = names;
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		this.icons = new IIcon[names.length];
		for(int i = 0; i < names.length; i++) {
			this.icons[i] = reg.registerIcon(RefStrings.MODID + ":raw_ore_" + names[i]);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return this.icons[Math.min(meta, icons.length - 1)];
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		int meta = Math.min(stack.getItemDamage(), names.length - 1);
		return StatCollector.translateToLocal("item.raw_" + names[meta] + ".name");
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < names.length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}
}
