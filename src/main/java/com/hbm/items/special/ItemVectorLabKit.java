package com.hbm.items.special;

import com.hbm.handler.contagion.WildDisease;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


public class ItemVectorLabKit extends Item {

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(world.isRemote) return stack;

		ItemStack syringe = WildDisease.makeSyringe(player.getRNG());

		if(!player.inventory.addItemStackToInventory(syringe)) {
			world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, syringe));
		}

		stack.stackSize--;
		world.playSoundAtEntity(player, "hbm:item.unpack", 1.0F, 1.0F);
		return stack;
	}
}
