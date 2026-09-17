package com.hbm.inventory.recipes;

import com.hbm.blocks.BlockEnums.EnumCrystalBlockType;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class SludgeProcessorRecipes extends GenericRecipes<SludgeProcessorRecipe> {
	public static final SludgeProcessorRecipes INSTANCE = new SludgeProcessorRecipes();

	@Override
	public int inputItemLimit() {
		return 1;
	}
	@Override
	public int inputFluidLimit() {
		return 1;
	}
	@Override
	public int outputItemLimit() {
		return 4;
	}
	@Override
	public int outputFluidLimit() {
		return 4;
	}

	@Override
	public String getFileName() {
		return "hbmSludgeProcessor.json";
	}
	@Override
	public SludgeProcessorRecipe instantiateRecipe(String name) {
		return new SludgeProcessorRecipe(name);
	}

	@Override
	public void registerDefaults() {
		this.register((SludgeProcessorRecipe) new SludgeProcessorRecipe("sludge.natcrete")
			.setup(100, 10_000).setNameWrapper("sludge.natcrete")
			.inputFluids(new FluidStack(Fluids.NATCRETE, 2000))
			.outputFluids(
				new FluidStack(Fluids.CONCRETE, 1000),
				new FluidStack(Fluids.BUURCRETE, 500),
				new FluidStack(Fluids.SCRAPCRETE, 500),
				new FluidStack(Fluids.LAVA, 500)
			)
			.setIconToFirstIngredient()
		);

		this.register((SludgeProcessorRecipe) new SludgeProcessorRecipe("sludge.buurmium")
			.setup(100, 20_000)
			.inputFluids(new FluidStack(Fluids.BUURCRETE, 4000))
			.outputItems(
				new ItemStack(ModItems.powder_diffused_buurmium, 1)
			)
			.outputFluids(
				new FluidStack(Fluids.CONCRETE, 4000)
			)
		);

		this.register((SludgeProcessorRecipe) new SludgeProcessorRecipe("sludge.concrete_recycling")
			.setup(40, 5_000).setNameWrapper("sludge.concrete_recycling")
			.inputFluids(new FluidStack(Fluids.CONCRETE, 16000))
			.outputItems(
				new ItemStack(ModItems.powder_cement)
			)
			.outputFluids(
				new FluidStack(Fluids.WATER, 2000)
			));

		this.register((SludgeProcessorRecipe) new SludgeProcessorRecipe("sludge.iron_crystal")
			.setup(100, 20_000).setNameWrapper("sludge.iron_crystal")
			.inputItems(new ComparableStack(ModBlocks.block_crystal, 1, EnumCrystalBlockType.IRON))
			.inputFluids(new FluidStack(Fluids.SULFURIC_ACID, 1000))
			.outputItems(
				new ItemStack(ModItems.powder_iron, 32)
			)
			.outputFluids(
				new FluidStack(Fluids.SULFUR_DIOXIDE, 1000),
				new FluidStack(Fluids.SODIUM_METAVANADATE, 100)
			));

		this.register((SludgeProcessorRecipe) new SludgeProcessorRecipe("sludge.sand_slop_processing")
			.setup(50, 5_000)
			.inputFluids(new FluidStack(Fluids.HEAVY_SAND_SLOP, 1000))
			.outputItems(new ItemStack(ModItems.powder_zirconium, 2))
			.outputFluids(new FluidStack(Fluids.MONAZITE_SLOP, 500), new FluidStack(Fluids.CHLORINE, 500))
			.setIconToFirstIngredient()
		);
	}
}
