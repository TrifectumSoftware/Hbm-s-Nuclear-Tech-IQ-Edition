package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.SpraypainterRecipes;

public class SpraypainterRecipeHandler extends NEIUniversalHandler {

	public SpraypainterRecipeHandler() {
		super("Spraypainter", ModBlocks.machine_conveyor_spraypainter, SpraypainterRecipes.getRecipes());
	}

	@Override
	public String getKey() {
		return "ntmSpraypainter";
	}
}
