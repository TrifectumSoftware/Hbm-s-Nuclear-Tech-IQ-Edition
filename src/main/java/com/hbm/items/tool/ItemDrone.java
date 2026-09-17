package com.hbm.items.tool;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityDeliveryDrone;
import com.hbm.entity.item.EntityQuarryDrone;
import com.hbm.entity.item.EntitySpelunkingDrone;
import com.hbm.items.ItemEnumMulti;
import com.hbm.main.MainRegistry;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemDrone extends ItemEnumMulti {

	public ItemDrone() {
		super(EnumDroneType.class, true, true);
		this.setCreativeTab(MainRegistry.machineTab);
	}

	public static enum EnumDroneType {
		PATROL,
		PATROL_CHUNKLOADING,
		PATROL_EXPRESS,
		PATROL_EXPRESS_CHUNKLOADING,
		REQUEST,
		DEMOLISHER,
		DEMOLISHER_CHUNKLOADING,
		DEMOLISHER_EXPRESS,
		DEMOLISHER_EXPRESS_CHUNKLOADING,
		SPELUNKER,
		QUARRY
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer entity, World world, int x, int y, int z, int side, float fx, float fy, float fz) {

		if(side != 1) return false;
		if(world.isRemote) return true;

		Entity toSpawn = null;
		int meta = stack.getItemDamage();

		if(meta == EnumDroneType.SPELUNKER.ordinal()) {
			if(world.getBlock(x, y, z) == ModBlocks.drone_crate) {
				EntitySpelunkingDrone drone = new EntitySpelunkingDrone(world);
				drone.setBase(x, y, z);
				toSpawn = drone;
			} else {
				return false;
			}
		} else if(meta == EnumDroneType.QUARRY.ordinal()) {
			if(world.getBlock(x, y, z) == ModBlocks.drone_crate) {
				EntityQuarryDrone drone = new EntityQuarryDrone(world);
				drone.setBase(x, y, z);
				toSpawn = drone;
			} else {
				return false;
			}
		} else if(meta != EnumDroneType.REQUEST.ordinal()) {
			EntityDeliveryDrone drone = new EntityDeliveryDrone(world);
			int type = meta >= EnumDroneType.DEMOLISHER.ordinal() ? meta - EnumDroneType.DEMOLISHER.ordinal() : meta;
			if(type % 2 == 1) {
				drone.setChunkLoading();
			}
			if(type % 4 >= 2) {
				drone.getDataWatcher().updateObject(11, (byte) 1);
			}
			if(meta >= EnumDroneType.DEMOLISHER.ordinal()) {
				drone.setDemolisher();
			}
			toSpawn = drone;
		}

		if(toSpawn != null) {
			toSpawn.setPosition(x + 0.5, y + 1, z + 0.5);
			world.spawnEntityInWorld(toSpawn);
			stack.stackSize--;
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			for(String s : I18nUtil.resolveKeyArray(stack.getUnlocalizedName() + ".desc"))
				list.add(EnumChatFormatting.YELLOW + s);
		} else {
			list.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + "Hold <" + EnumChatFormatting.YELLOW + "" + EnumChatFormatting.ITALIC + "LSHIFT" + EnumChatFormatting.DARK_GRAY
					+ "" + EnumChatFormatting.ITALIC + "> to display more info");
		}
	}
}
