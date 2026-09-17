package com.hbm.items.tool;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.container.ContainerMagneticCrafter;
import com.hbm.inventory.gui.GUIMagneticCrafter;
import com.hbm.items.armor.ItemArmorMod;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemMagneticCrafter extends ItemArmorMod implements IGUIProvider {

	public static final int RANGE = 8;

	public ItemMagneticCrafter() {
		super(ArmorModHandler.plate_only, false, true, false, false);
		this.setUnlocalizedName("magnetic_crafter");
		this.setTextureName(RefStrings.MODID + ":portable_magnetic_crafting_table");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote) {
			player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
		}
		return stack;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMagneticCrafter(player.inventory, world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMagneticCrafter(player.inventory, world, x, y, z);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.magnetic_crafter.desc"));
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.magnetic_crafter.use"));
		list.add("");
		super.addInformation(stack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.YELLOW + "  " + stack.getDisplayName() + " (" + I18nUtil.resolveKey("item.magnetic_crafter.shortDesc") + ")");
	}
}
