package com.hbm.packet.toserver;

import java.util.LinkedList;
import java.util.List;

import com.hbm.inventory.container.ContainerMagneticCrafter;
import com.hbm.items.tool.ItemMagneticRestocker;
import com.hbm.main.NTMSounds;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public class MagneticCrafterRecipePacket implements IMessage {

	private ItemStack[][] items;
	private boolean maxTransfer;

	public MagneticCrafterRecipePacket() {
		this.items = new ItemStack[9][];
	}

	public MagneticCrafterRecipePacket(ItemStack[][] items, boolean maxTransfer) {
		this.items = items;
		this.maxTransfer = maxTransfer;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(maxTransfer);
		for(int i = 0; i < 9; i++) {
			ItemStack[] options = items[i];
			if(options == null) {
				buf.writeInt(0);
			} else {
				buf.writeInt(options.length);
				for(ItemStack stack : options) {
					ByteBufUtils.writeItemStack(buf, stack);
				}
			}
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		maxTransfer = buf.readBoolean();
		for(int i = 0; i < 9; i++) {
			int count = buf.readInt();
			if(count == 0) {
				items[i] = null;
			} else {
				items[i] = new ItemStack[count];
				for(int j = 0; j < count; j++) {
					items[i][j] = ByteBufUtils.readItemStack(buf);
				}
			}
		}
	}

	public static class Handler implements IMessageHandler<MagneticCrafterRecipePacket, IMessage> {

		@Override
		public IMessage onMessage(MagneticCrafterRecipePacket message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().playerEntity;
			if(player == null) return null;

			if(!(player.openContainer instanceof ContainerMagneticCrafter)) return null;

			ContainerMagneticCrafter container = (ContainerMagneticCrafter) player.openContainer;
			fillCraftingGrid(player, container, message.items);

			return null;
		}

		private void fillCraftingGrid(EntityPlayerMP player, ContainerMagneticCrafter container, ItemStack[][] requiredItems) {
			InventoryCrafting craftMatrix = container.craftMatrix;


			List<ItemStack> limbo = new LinkedList<>();


			for(int i = 0; i < 9; i++) {
				ItemStack existing = craftMatrix.getStackInSlot(i);
				if(existing == null) continue;

				craftMatrix.setInventorySlotContents(i, null);
				if(!player.inventory.addItemStackToInventory(existing)) {
					limbo.add(existing);
				}
			}


			boolean filled = false;

			for(int slot = 0; slot < 9; slot++) {
				ItemStack[] options = requiredItems[slot];
				if(options == null || options.length == 0) continue;

				ItemStack toPlace = takeFromLimbo(options, limbo);
				if(toPlace == null) toPlace = takeMatching(player.inventory, options);
				if(toPlace == null) for(IInventory crate : ItemMagneticRestocker.getAllCrates(player)) {
					if((toPlace = takeMatching(crate, options)) != null) break;
				}

				if(toPlace != null) {
					craftMatrix.setInventorySlotContents(slot, toPlace);
					filled = true;
				}
			}


			for(ItemStack stack : limbo) {
				EntityItem item = new EntityItem(player.worldObj, player.posX, player.posY + 0.5D, player.posZ, stack);
				item.delayBeforeCanPickup = 10;
				player.worldObj.spawnEntityInWorld(item);
			}

			container.detectAndSendChanges();

			if(filled) player.worldObj.playSoundAtEntity(player, NTMSounds.MAGNETIC, 1.0F, 1.0F);
		}

		private ItemStack takeFromLimbo(ItemStack[] options, List<ItemStack> limbo) {
			for(ItemStack stack : limbo) {
				for(ItemStack option : options) {
					if(option != null && stack.isItemEqual(option)) {
						ItemStack result = stack.splitStack(1);
						if(stack.stackSize <= 0) limbo.remove(stack);
						return result;
					}
				}
			}
			return null;
		}

		private ItemStack takeMatching(IInventory inv, ItemStack[] options) {
			for(int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if(stack == null) continue;

				for(ItemStack option : options) {
					if(option != null && stack.isItemEqual(option)) {
						return inv.decrStackSize(i, 1);
					}
				}
			}
			return null;
		}
	}
}
