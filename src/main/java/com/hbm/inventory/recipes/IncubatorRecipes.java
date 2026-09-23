package com.hbm.inventory.recipes;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.NBTStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ItemVial;
import com.hbm.items.ModItems;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.item.ItemStack;

public class IncubatorRecipes extends GenericRecipes<GenericRecipe> {

	public static final IncubatorRecipes INSTANCE = new IncubatorRecipes();

	@Override public int inputItemLimit() { return 1; }
	@Override public int inputFluidLimit() { return 1; }
	@Override public int outputItemLimit() { return 1; }
	@Override public int outputFluidLimit() { return 1; }

	@Override public String getFileName() { return "hbmIncubator.json"; }
	@Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

	@Override
	public void registerDefaults() {

		ItemStack vial = sampleVial();

		this.register(new GenericRecipe("incubator.duplicate").setup(200, 100).setNamed()
				.inputItems(new NBTStack(vial))
				.inputFluids(new FluidStack(Fluids.PERFLUOROMETHYL_COLD, 100))
				.outputItems(vial)
				.outputFluids(new FluidStack(Fluids.PERFLUOROMETHYL, 100))
				.setIconToFirstIngredient());
	}

	private static ItemStack sampleVial() {
		ItemStack vial = new ItemStack(ModItems.vial);
		IFillableItem.setFluidFill(vial, Fluids.HUMAN_BLOOD, (short) ItemVial.MAX_FLUID);
		vial.stackTagCompound.setString("frame", "mku");
		return vial;
	}
}
