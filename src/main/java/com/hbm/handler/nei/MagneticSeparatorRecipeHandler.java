package com.hbm.handler.nei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.MagneticSeparatorRecipes;

public class MagneticSeparatorRecipeHandler extends NEIGenericRecipeHandler {

	public MagneticSeparatorRecipeHandler() {
		super(ModBlocks.machine_magnetic_separator.getLocalizedName(), MagneticSeparatorRecipes.INSTANCE, ModBlocks.machine_magnetic_separator);
	}

	@Override public String getRecipeID() { return "ntmMagneticSeparator"; }
	@Override public int recipiesPerPage() { return 1; }
}
