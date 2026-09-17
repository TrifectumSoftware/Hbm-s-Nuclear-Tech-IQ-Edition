package com.hbm.items.tool;

import com.hbm.items.ModItems;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMeteoriteBase {

	public static final String[] TIER_SUFFIX = { "seared", "reforged", "hardened", "alloyed", "machined", "treated", "etched", "bred", "irradiated", "fused" };
	public static final int MAX_TIER = 7;

	public static int getTier(ItemStack stack) {
		return stack != null && stack.getItem() instanceof IMeteoriteTool ? ((IMeteoriteTool) stack.getItem()).getTier() : 0;
	}

	public static String tierName(int tier) {
		if(tier <= 0) return "";
		return TIER_SUFFIX[Math.min(tier - 1, TIER_SUFFIX.length - 1)];
	}

	public static int recursionLevel(int tier) {
		return Math.min(1 + tier / 2, 6);
	}

	public static int luckLevel(int tier) {
		return Math.min(2 + tier / 2, 5);
	}

	public static int cleaveLevel(int tier) {
		return Math.min(1 + tier / 2, 6);
	}

	public static Item upgrade(Item item) {
		if(item instanceof ItemMeteoriteSword) {
			ItemMeteoriteSword tool = (ItemMeteoriteSword) item;
			if(tool.tier >= MAX_TIER) return null;
			return ModItems.meteorite_sword[tool.tier + 1];
		}
		if(item instanceof ItemMeteoriteHoe) {
			ItemMeteoriteHoe tool = (ItemMeteoriteHoe) item;
			if(tool.tier >= MAX_TIER) return null;
			return ModItems.meteorite_hoe[tool.tier + 1];
		}
		if(item instanceof ItemMeteoriteTool) {
			ItemMeteoriteTool tool = (ItemMeteoriteTool) item;
			if(tool.tier >= MAX_TIER) return null;
			Item[] arr = "axe".equals(tool.toolName) ? ModItems.meteorite_axe : "shovel".equals(tool.toolName) ? ModItems.meteorite_shovel : ModItems.meteorite_pickaxe;
			if(tool.tier + 1 >= arr.length || arr[tool.tier + 1] == null) return null;
			return arr[tool.tier + 1];
		}
		return null;
	}
}
