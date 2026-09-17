package com.hbm.inventory.recipes;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;

public class HaemodialysisRecipes extends GenericRecipes<GenericRecipe> {

	public static final HaemodialysisRecipes INSTANCE = new HaemodialysisRecipes();

	@Override public int inputItemLimit() { return 0; }
	@Override public int inputFluidLimit() { return 3; }
	@Override public int outputItemLimit() { return 0; }
	@Override public int outputFluidLimit() { return 2; }

	@Override public String getFileName() { return "hbmHaemodialysis.json"; }

	@Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

	@Override
	public void registerDefaults() {

		this.register(new GenericRecipe("haemodialysis.martyr").setup(200, 1_000)
				.inputFluids(new FluidStack(Fluids.BLOOD_OF_THE_MARTYR, 1_000), new FluidStack(Fluids.HEPARIN, 250), new FluidStack(Fluids.DIALYSATE, 250))
				.outputFluids(new FluidStack(Fluids.HAEMOGLOBIN, 3_000), new FluidStack(Fluids.AMBROSIA, 2_000)));

		this.register(new GenericRecipe("haemodialysis.curdling").setup(200, 1_000)
				.inputFluids(new FluidStack(Fluids.CURDLING_BLOOD, 1_000), new FluidStack(Fluids.HEPARIN, 250), new FluidStack(Fluids.DIALYSATE, 250))
				.outputFluids(new FluidStack(Fluids.WORMWOOD, 3_000), new FluidStack(Fluids.MORNINGSTARS_FIRE, 1_000)));
	}
}
