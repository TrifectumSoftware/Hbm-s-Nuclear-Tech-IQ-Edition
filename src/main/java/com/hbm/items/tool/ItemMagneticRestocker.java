package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ItemArmorMod;
import com.hbm.items.block.ItemBlockStorageCrate;
import com.hbm.lib.RefStrings;
import com.hbm.main.NTMSounds;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;

public class ItemMagneticRestocker extends ItemArmorMod {

	public static final int RANGE = 8;

	public ItemMagneticRestocker() {
		super(ArmorModHandler.extra, true, false, false, false);
		this.setUnlocalizedName("magnetic_restocker");
		this.setTextureName(RefStrings.MODID + ":portable_magnetic_restocker");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.magnetic_restocker.desc"));
		list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.magnetic_restocker.desc2"));
		list.add(EnumChatFormatting.LIGHT_PURPLE + I18nUtil.resolveKey("item.magnetic_restocker.desc3"));
		list.add("");
		super.addInformation(stack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.YELLOW + "  " + stack.getDisplayName() + " (" + I18nUtil.resolveKey("item.magnetic_restocker.shortDesc") + ")");
	}

	public static boolean hasRestocker(EntityPlayer player) {
		ItemStack held = player.getHeldItem();
		if(held != null && held.getItem() instanceof ItemMagneticRestocker) return true;

		for(int i = 0; i < 4; i++) {
			ItemStack armor = player.getCurrentArmor(i);
			if(armor == null || !ArmorModHandler.hasMods(armor)) continue;

			for(ItemStack mod : ArmorModHandler.pryMods(armor)) {
				if(mod != null && mod.getItem() instanceof ItemMagneticRestocker) return true;
			}
		}

		return false;
	}

	public static void deposit(EntityPlayer player, boolean shift) {
		// shift skips the hotbar (slots 0-8); armor is never in mainInventory.
		int start = shift ? 9 : 0;
		boolean moved = false;

		for(int i = start; i < player.inventory.mainInventory.length; i++) {
			ItemStack stack = player.inventory.mainInventory[i];
			if(stack == null || !stack.isStackable()) continue;

			int before = stack.stackSize;
			ItemStack leftover = depositStack(player, stack, shift);

			if(leftover == null || leftover.stackSize < before) {
				player.inventory.mainInventory[i] = leftover;
				moved = true;
			}
		}

		player.inventoryContainer.detectAndSendChanges();
		if(moved) player.worldObj.playSoundAtEntity(player, NTMSounds.MAGNETIC, 1.0F, 1.0F);
	}

	public static void restock(EntityPlayer player) {
		boolean refilled = false;

		for(int i = 0; i < player.inventory.mainInventory.length; i++) {
			ItemStack stack = player.inventory.mainInventory[i];
			if(stack == null) continue;

			int need = stack.getMaxStackSize() - stack.stackSize;

			while(need > 0) {
				ItemStack taken = takeFromCrates(player, stack, need);
				if(taken == null) break;

				int add = Math.min(need, taken.stackSize);
				stack.stackSize += add;
				need -= add;
				refilled = true;
			}
		}

		player.inventoryContainer.detectAndSendChanges();
		if(refilled) player.worldObj.playSoundAtEntity(player, NTMSounds.MAGNETIC, 1.0F, 1.0F);
	}

	private static ItemStack depositStack(EntityPlayer player, ItemStack stack, boolean shift) {

		for(ItemBlockStorageCrate.InventoryCrate inv : getInventoryCrates(player)) {
			stack = depositToCrate(inv, stack, shift);
			if(stack == null) return null;
		}

		for(TileEntityCrateBase crate : getWorldCrates(player)) {
			stack = depositToCrate(crate, stack, shift);
			if(stack == null) return null;
		}

		return stack;
	}

	private static ItemStack depositToCrate(IInventory crate, ItemStack stack, boolean shift) {

		boolean hasType = false;

		for(int i = 0; i < crate.getSizeInventory(); i++) {
			ItemStack existing = crate.getStackInSlot(i);
			if(existing == null || !canStack(existing, stack)) continue;

			hasType = true;
			if(existing.stackSize >= existing.getMaxStackSize()) continue;

			int add = Math.min(existing.getMaxStackSize() - existing.stackSize, stack.stackSize);
			existing.stackSize += add;
			stack.stackSize -= add;
			crate.setInventorySlotContents(i, existing);

			if(stack.stackSize <= 0) return null;
		}

		// Normal mode only opens new slots in crates that already stock the item.
		if(shift || hasType) {
			for(int i = 0; i < crate.getSizeInventory(); i++) {
				if(crate.getStackInSlot(i) == null) {
					crate.setInventorySlotContents(i, stack.copy());
					return null;
				}
			}
		}

		return stack;
	}

	private static ItemStack takeFromCrates(EntityPlayer player, ItemStack stack, int amount) {

		for(ItemBlockStorageCrate.InventoryCrate inv : getInventoryCrates(player)) {
			ItemStack taken = takeFromCrate(inv, stack, amount);
			if(taken != null) return taken;
		}

		for(TileEntityCrateBase crate : getWorldCrates(player)) {
			ItemStack taken = takeFromCrate(crate, stack, amount);
			if(taken != null) return taken;
		}

		return null;
	}

	private static ItemStack takeFromCrate(IInventory crate, ItemStack stack, int amount) {

		for(int i = 0; i < crate.getSizeInventory(); i++) {
			ItemStack existing = crate.getStackInSlot(i);
			if(existing == null || !canStack(existing, stack)) continue;

			return crate.decrStackSize(i, amount);
		}

		return null;
	}

	private static boolean canStack(ItemStack a, ItemStack b) {
		return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
	}

	private static List<ItemBlockStorageCrate.InventoryCrate> getInventoryCrates(EntityPlayer player) {

		List<ItemBlockStorageCrate.InventoryCrate> list = new ArrayList<>();

		for(ItemStack stack : player.inventory.mainInventory) {
			if(stack == null || !(stack.getItem() instanceof ItemBlockStorageCrate)) continue;
			if(!(Block.getBlockFromItem(stack.getItem()) instanceof BlockStorageCrate)) continue;
			if(!stack.hasTagCompound() || !stack.getTagCompound().getBoolean("magnetic")) continue;

			list.add(new ItemBlockStorageCrate.InventoryCrate(player, stack));
		}

		return list;
	}

	private static List<TileEntityCrateBase> getWorldCrates(EntityPlayer player) {

		List<TileEntityCrateBase> list = new ArrayList<>();

		int px = (int) Math.floor(player.posX);
		int py = (int) Math.floor(player.posY);
		int pz = (int) Math.floor(player.posZ);

		for(int x = px - RANGE; x <= px + RANGE; x++)
			for(int y = py - RANGE; y <= py + RANGE; y++)
				for(int z = pz - RANGE; z <= pz + RANGE; z++) {
					TileEntity te = player.worldObj.getTileEntity(x, y, z);
					if(te instanceof TileEntityCrateBase) {
						TileEntityCrateBase crate = (TileEntityCrateBase) te;
						if(crate.isMagnetic && !crate.isLocked()) list.add(crate);
					}
				}

		return list;
	}
}
