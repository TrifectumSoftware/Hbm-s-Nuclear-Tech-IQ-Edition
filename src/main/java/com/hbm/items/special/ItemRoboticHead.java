package com.hbm.items.special;

import java.util.List;

import com.hbm.inventory.gui.GUIScreenCyberneticHead;
import com.hbm.items.IItemControlReceiver;
import com.hbm.items.tool.ItemRTTYPager;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemRoboticHead extends Item implements IItemControlReceiver, IGUIProvider {

	public static final String KEY_OUTPUT = "out";

	public ItemRoboticHead() {
		this.setMaxStackSize(1);
	}

	public static String getChannel(ItemStack stack) {
		if(stack == null || !stack.hasTagCompound()) return "";
		return stack.stackTagCompound.getString(ItemRTTYPager.KEY_CHANNEL);
	}

	public static String getOutputChannel(ItemStack stack) {
		if(stack == null || !stack.hasTagCompound()) return "";
		return stack.stackTagCompound.getString(KEY_OUTPUT);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(world.isRemote) player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
		return stack;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(EnumChatFormatting.GRAY + I18nUtil.resolveKey("item.robotic_head.desc"));

		String channel = getChannel(stack);
		if(channel.isEmpty()) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("item.robotic_head.no_channel"));
		} else {
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.robotic_head.channel", channel));
		}

		String output = getOutputChannel(stack);
		if(!output.isEmpty()) list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.robotic_head.reply", output));
	}

	@Override
	public void receiveControl(EntityPlayer player, ItemStack stack, NBTTagCompound data) {
		if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
		if(data.hasKey(ItemRTTYPager.KEY_CHANNEL)) stack.stackTagCompound.setString(ItemRTTYPager.KEY_CHANNEL, data.getString(ItemRTTYPager.KEY_CHANNEL));
		if(data.hasKey(KEY_OUTPUT)) stack.stackTagCompound.setString(KEY_OUTPUT, data.getString(KEY_OUTPUT));
	}

	@Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }
	@Override public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIScreenCyberneticHead(player.getHeldItem()); }
}
