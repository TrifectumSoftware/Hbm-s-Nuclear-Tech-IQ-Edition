package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.WaffleIronRecipes;

public class WaffleIronRecipeHandler extends NEIGenericRecipeHandler {
	public WaffleIronRecipeHandler() {
		super(ModBlocks.waffle_iron.getLocalizedName(), WaffleIronRecipes.INSTANCE, ModBlocks.waffle_iron);
	}

	@Override
	public String getRecipeID() {
		return "ntmWaffleIron";
	}
}
