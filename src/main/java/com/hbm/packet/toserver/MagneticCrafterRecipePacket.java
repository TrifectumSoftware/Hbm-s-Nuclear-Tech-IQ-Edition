package com.hbm.packet.toserver;

import java.util.LinkedList;
import java.util.List;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.inventory.container.ContainerMagneticCrafter;
import com.hbm.items.block.ItemBlockStorageCrate;
import com.hbm.items.tool.ItemMagneticCrafter;
import com.hbm.main.NTMSounds;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

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
				if(toPlace == null) toPlace = takeFromPlayer(player, options);
				if(toPlace == null) toPlace = takeFromInventoryCrates(player, options);
				if(toPlace == null) toPlace = takeFromMagneticCrates(player, options);

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

		private ItemStack takeFromPlayer(EntityPlayerMP player, ItemStack[] options) {
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if(stack == null) continue;

				for(ItemStack option : options) {
					if(option != null && stack.isItemEqual(option)) {
						return player.inventory.decrStackSize(i, 1);
					}
				}
			}
			return null;
		}

		private ItemStack takeFromInventoryCrates(EntityPlayerMP player, ItemStack[] options) {
			for(int i = 0; i < player.inventory.mainInventory.length; i++) {
				ItemStack crateStack = player.inventory.mainInventory[i];
				if(crateStack == null) continue;
				if(!(crateStack.getItem() instanceof ItemBlockStorageCrate)) continue;
				if(!(Block.getBlockFromItem(crateStack.getItem()) instanceof BlockStorageCrate)) continue;
				if(!crateStack.hasTagCompound() || !crateStack.getTagCompound().getBoolean("magnetic")) continue;

				ItemBlockStorageCrate.InventoryCrate inv = new ItemBlockStorageCrate.InventoryCrate(player, crateStack);

				for(int slot = 0; slot < inv.getSizeInventory(); slot++) {
					ItemStack stack = inv.getStackInSlot(slot);
					if(stack == null) continue;

					for(ItemStack option : options) {
						if(option != null && stack.isItemEqual(option)) {
							return inv.decrStackSize(slot, 1);
						}
					}
				}
			}

			return null;
		}

		private ItemStack takeFromMagneticCrates(EntityPlayerMP player, ItemStack[] options) {
			int range = ItemMagneticCrafter.RANGE;
			int minX = (int) Math.floor(player.posX) - range;
			int maxX = (int) Math.floor(player.posX) + range;
			int minY = (int) Math.floor(player.posY) - range;
			int maxY = (int) Math.floor(player.posY) + range;
			int minZ = (int) Math.floor(player.posZ) - range;
			int maxZ = (int) Math.floor(player.posZ) + range;

			for(int x = minX; x <= maxX; x++) {
				for(int y = minY; y <= maxY; y++) {
					for(int z = minZ; z <= maxZ; z++) {
						TileEntity te = player.worldObj.getTileEntity(x, y, z);
						if(!(te instanceof TileEntityCrateBase)) continue;

						TileEntityCrateBase crate = (TileEntityCrateBase) te;
						if(!crate.isMagnetic || crate.isLocked()) continue;

						for(int slot = 0; slot < crate.getSizeInventory(); slot++) {
							ItemStack stack = crate.getStackInSlot(slot);
							if(stack == null) continue;

							for(ItemStack option : options) {
								if(option != null && stack.isItemEqual(option)) {
									return crate.decrStackSize(slot, 1);
								}
							}
						}
					}
				}
			}

			return null;
		}
	}
}
