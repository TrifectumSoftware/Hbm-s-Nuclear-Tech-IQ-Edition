package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.IncubatorRecipes;

public class IncubatorHandler extends NEIGenericRecipeHandler {

	public IncubatorHandler() {
		super(ModBlocks.machine_incubator.getLocalizedName(), IncubatorRecipes.INSTANCE, ModBlocks.machine_incubator);
	}

	@Override public String getRecipeID() { return "ntmIncubator"; }
	@Override public int recipiesPerPage() { return 1; }
}
