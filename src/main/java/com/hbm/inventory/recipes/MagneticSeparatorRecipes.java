package com.hbm.inventory.recipes;

import com.hbm.handler.contagion.GenomeSample;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NBTStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ItemVial;
import com.hbm.items.ModItems;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.item.ItemStack;

public class MagneticSeparatorRecipes extends GenericRecipes<GenericRecipe> {

	public static final MagneticSeparatorRecipes INSTANCE = new MagneticSeparatorRecipes();

	@Override public int inputItemLimit() { return 2; }
	@Override public int inputFluidLimit() { return 1; }
	@Override public int outputItemLimit() { return 6; }
	@Override public int outputFluidLimit() { return 1; }

	@Override public String getFileName() { return "hbmMagneticSeparator.json"; }
	@Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

	@Override
	public void registerDefaults() {

		this.register(new GenericRecipe("magsep.iron").setup(200, 1_000)
				.inputItems(new ComparableStack(ModItems.powder_iron))
				.inputFluids(new FluidStack(Fluids.WATER, 250))
				.outputItems(new ItemStack(ModItems.powder_iron), new ItemStack(ModItems.dust))
				.outputFluids(new FluidStack(Fluids.SPENTSTEAM, 250))
				.setIconToFirstIngredient());

		this.register(new GenericRecipe("magsep.genomeSample").setup(100, 1_000)
				.inputItems(new NBTStack(bloodVial()))
				.inputFluids(new FluidStack(Fluids.HEPARIN, 100))
				.outputItems(
						GenomeSample.make("severity", "01234567"),
						GenomeSample.make("resistance", "89ABCDEF"),
						GenomeSample.make("transmission", "01234567"),
						GenomeSample.make("mutationRate", "0.05"),
						GenomeSample.make("antigenMutability", "0.2"),
						GenomeSample.make("incubation", "144000"))
				.setIconToFirstIngredient());
	}

	private static ItemStack bloodVial() {
		ItemStack vial = new ItemStack(ModItems.vial);
		IFillableItem.setFluidFill(vial, Fluids.HUMAN_BLOOD, (short) ItemVial.MAX_FLUID);
		return vial;
	}
}
