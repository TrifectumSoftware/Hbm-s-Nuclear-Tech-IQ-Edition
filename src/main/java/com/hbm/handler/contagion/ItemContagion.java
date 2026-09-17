package com.hbm.handler.contagion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.config.ServerConfig;
import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.armor.ItemModGloves;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;


public class ItemContagion {

	public static final String KEY = "hbmContagion";

	public static void setFrames(ItemStack stack, List<String> frames) {
		if(stack == null || frames == null || frames.isEmpty()) return;
		if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(String frame : frames) {
			if(frame == null || frame.isEmpty()) continue;
			list.appendTag(new NBTTagString(frame));
		}
		if(list.tagCount() > 0) stack.stackTagCompound.setTag(KEY, list);
	}

	public static List<String> getFrames(ItemStack stack) {
		List<String> frames = new ArrayList<>();
		if(stack == null || !stack.hasTagCompound() || !stack.stackTagCompound.hasKey(KEY)) return frames;
		NBTTagList list = stack.stackTagCompound.getTagList(KEY, Constants.NBT.TAG_STRING);
		for(int i = 0; i < list.tagCount(); i++) frames.add(list.getStringTagAt(i));
		return frames;
	}

	public static boolean hasFrames(ItemStack stack) {
		return stack != null && stack.hasTagCompound()
				&& stack.stackTagCompound.getTagList(KEY, Constants.NBT.TAG_STRING).tagCount() > 0;
	}

	public static boolean isContaminable(ItemStack stack) {
		return stack != null && stack.getMaxStackSize() == 1;
	}

	public static void tagInventoryItem(EntityLivingBase host, List<String> frames) {
		if(!(host instanceof EntityPlayer) || frames == null || frames.isEmpty()) return;
		EntityPlayer player = (EntityPlayer) host;
		Random rand = player.getRNG();

		int randSlot = rand.nextInt(player.inventory.mainInventory.length);
		ItemStack stack = player.inventory.getStackInSlot(randSlot);

		if(rand.nextInt(100) == 0) {
			stack = player.inventory.armorItemInSlot(rand.nextInt(4));
		}

		if(isContaminable(stack)) {
			setFrames(stack, frames);
		}
	}


	public static void checkInventory(EntityLivingBase host) {
		if(!(host instanceof EntityPlayer)) return;
		EntityPlayer player = (EntityPlayer) host;

		boolean carryingFomite = false;
		for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
			if(hasFrames(player.inventory.getStackInSlot(i))) {
				carryingFomite = true;
				break;
			}
		}
		if(!carryingFomite) return;

		// GLOVEsS
		if(ItemModGloves.wear(player)) return;

		for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if(!hasFrames(stack)) continue;

			for(String frameId : getFrames(stack)) {
				DiseaseDefinition def = DiseaseRegistry.get(frameId);
				if(def == null) continue;
				if(DiseaseHandler.isImmuneOrInfected(player, frameId, null)) continue;

				DiseaseHandler.transfer(player.worldObj, player, def, null);
			}
		}
	}

	public static void tagEntityItem(EntityItem item, List<String> frames) {
		if(item == null || frames == null || frames.isEmpty()) return;
		ItemStack stack = item.getEntityItem();
		if(isContaminable(stack)) {
			setFrames(stack, frames);
		}
	}

	public static List<String> getFomiteFrames(EntityLivingBase host) {
		List<DiseaseInstance> instances = host instanceof EntityPlayer
				? HbmBloodstreamProps.getData(host).getPathogenInstances()
				: new ArrayList<>(HbmLivingProps.getDiseases(host).values());

		List<String> frames = new ArrayList<>();
		for(DiseaseInstance instance : instances) {
			if(instance.frameId == null) continue;
			DiseaseDefinition def = DiseaseRegistry.get(instance.frameId);
			if(def != null && def.fomite) frames.add(instance.frameId);
		}
		return frames;
	}

	public static List<String> tick(EntityLivingBase host) {
		if(!ServerConfig.ENABLE_DISEASES.get() || host == null || host.worldObj == null || host.worldObj.isRemote) return new ArrayList<String>();

		checkInventory(host);

		List<String> frames = getFomiteFrames(host);
		if(!frames.isEmpty() && host.worldObj.getTotalWorldTime() % 100 == 0) {
			tagInventoryItem(host, frames);
		}
		return frames;
	}

	public static void tagNearbyItems(World world, EntityLivingBase host, List<String> frames) {
		if(frames == null || frames.isEmpty()) return;
		double range = host.isWet() ? 16D : 2D;
		for(Object o : world.getEntitiesWithinAABBExcludingEntity(host, host.boundingBox.expand(range, range, range))) {
			if(o instanceof EntityItem) {
				tagEntityItem((EntityItem) o, frames);
			}
		}
	}
}
