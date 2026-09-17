package com.hbm.items.tool;

import java.util.List;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemMagneticStrip extends Item {

	public ItemMagneticStrip() {
		this.setUnlocalizedName("magnetic_strip");
		this.setTextureName(RefStrings.MODID + ":magnetic_strip");
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) return true;

		TileEntity te = world.getTileEntity(x, y, z);
		if(!(te instanceof TileEntityCrateBase)) return false;

		TileEntityCrateBase crate = (TileEntityCrateBase) te;

		if(crate.isMagnetic) {
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + I18n.format("item.magnetic_strip.already")));
			return true;
		}

		crate.isMagnetic = true;
		crate.markDirty();
		world.markBlockForUpdate(x, y, z);

		if(!player.capabilities.isCreativeMode) {
			stack.stackSize--;
		}

		world.playSoundAtEntity(player, "random.anvil_use", 1.0F, 1.0F);
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.GRAY + I18n.format("item.magnetic_strip.desc"));
	}
}
