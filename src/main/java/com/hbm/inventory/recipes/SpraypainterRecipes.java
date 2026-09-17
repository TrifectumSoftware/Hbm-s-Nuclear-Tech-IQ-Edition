package com.hbm.inventory.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Ink;
import com.hbm.items.machine.ItemFluidIcon;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class SpraypainterRecipes {

	public static final int amount = 5;

	public static final List<Object[]> recipes = new ArrayList<Object[]>();
	private static boolean initialized = false;

	public static void init() {
		if(initialized) return;
		initialized = true;

		String[] colors = { "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black" };
		FluidType[] inks = { Fluids.INK_WHITE, Fluids.INK_ORANGE, Fluids.INK_MAGENTA, Fluids.INK_LIGHT_BLUE, Fluids.INK_YELLOW, Fluids.INK_LIME, Fluids.INK_PINK, Fluids.INK_GRAY, Fluids.INK_LIGHT_GRAY, Fluids.INK_CYAN, Fluids.INK_PURPLE, Fluids.INK_BLUE, Fluids.INK_BROWN, Fluids.INK_GREEN, Fluids.INK_RED, Fluids.INK_BLACK };

		for(int c = 0; c < 16; c++) {
			pair("glass", colors[c], new ComparableStack(Blocks.glass, 1), new ItemStack(Blocks.stained_glass, 1, c), new ComparableStack(Blocks.stained_glass, 1, c), new ItemStack(Blocks.glass, 1), inks[c]);
			pair("glass_pane", colors[c], new ComparableStack(Blocks.glass_pane, 1), new ItemStack(Blocks.stained_glass_pane, 1, c), new ComparableStack(Blocks.stained_glass_pane, 1, c), new ItemStack(Blocks.glass_pane, 1), inks[c]);
			pair("terracotta", colors[c], new ComparableStack(Blocks.hardened_clay, 1), new ItemStack(Blocks.stained_hardened_clay, 1, c), new ComparableStack(Blocks.stained_hardened_clay, 1, c), new ItemStack(Blocks.hardened_clay, 1), inks[c]);
			pair("concrete", colors[c], new ComparableStack(ModBlocks.concrete_smooth, 1), new ItemStack(ModBlocks.concrete_colored, 1, c), new ComparableStack(ModBlocks.concrete_colored, 1, c), new ItemStack(ModBlocks.concrete_smooth, 1), inks[c]);
			pair("sheetmetal", colors[c], new ComparableStack(ModBlocks.sheetmetal, 1), new ItemStack(ModBlocks.sheetmetal_colored, 1, 15 - c), new ComparableStack(ModBlocks.sheetmetal_colored, 1, 15 - c), new ItemStack(ModBlocks.sheetmetal, 1), inks[c]);

			if(c != 0) {
				pair("wool", colors[c], new ComparableStack(Blocks.wool, 1, 0), new ItemStack(Blocks.wool, 1, c), new ComparableStack(Blocks.wool, 1, c), new ItemStack(Blocks.wool, 1, 0), inks[c]);
				pair("carpet", colors[c], new ComparableStack(Blocks.carpet, 1, 0), new ItemStack(Blocks.carpet, 1, c), new ComparableStack(Blocks.carpet, 1, c), new ItemStack(Blocks.carpet, 1, 0), inks[c]);
			}

			int platemetal = FT_Ink.getPlatemetalMeta(c);
			if(platemetal >= 0) {
				pair("platemetal", colors[c], new ComparableStack(ModBlocks.platemetal, 1, 0), new ItemStack(ModBlocks.platemetal, 1, platemetal), new ComparableStack(ModBlocks.platemetal, 1, platemetal), new ItemStack(ModBlocks.platemetal, 1, 0), inks[c]);
			}
		}
	}

	private static void pair(String id, String color, AStack base, ItemStack dyed, AStack dyedIn, ItemStack baseOut, FluidType ink) {
		recipes.add(new Object[] { base, ink, dyed });
		recipes.add(new Object[] { dyedIn, Fluids.PEROXIDE, baseOut });
	}

	public static ItemStack getOutput(ItemStack input, FluidType fluid) {
		init();
		if(input == null || fluid == null || fluid == Fluids.NONE) return null;

		for(Object[] recipe : recipes) {
			if(recipe[1] != fluid) continue;
			if(((AStack) recipe[0]).matchesRecipe(input, true)) return ((ItemStack) recipe[2]).copy();
		}

		return null;
	}

	public static HashMap<Object[], Object> getRecipes() {
		init();
		HashMap<Object[], Object> map = new HashMap<Object[], Object>();

		for(Object[] recipe : recipes) {
			List<ItemStack> extracted = ((AStack) recipe[0]).extractForNEI();
			if(extracted.isEmpty()) continue;

			ItemStack in = extracted.get(0).copy();
			in.stackSize = 1;

			map.put(new Object[] { in, ItemFluidIcon.make((FluidType) recipe[1], amount) }, ((ItemStack) recipe[2]).copy());
		}

		return map;
	}
}
