package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.HaemodialysisRecipes;

public class HaemodialysisRecipeHandler extends NEIGenericRecipeHandler {

	public HaemodialysisRecipeHandler() {
		super(ModBlocks.machine_haemodialysis.getLocalizedName(), HaemodialysisRecipes.INSTANCE, ModBlocks.machine_haemodialysis);
	}

	@Override public String getRecipeID() { return "ntmHaemodialysis"; }
}
