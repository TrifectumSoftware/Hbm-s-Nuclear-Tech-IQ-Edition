package com.hbm.module.machine;

import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.HaemodialysisRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipes;

import api.hbm.energymk2.IEnergyHandlerMK2;
import net.minecraft.item.ItemStack;

public class ModuleMachineHaemodialysis extends ModuleMachineBase {

	public ModuleMachineHaemodialysis(int index, IEnergyHandlerMK2 battery, ItemStack[] slots) {
		super(index, battery, slots);
		this.inputSlots = new int[0];
		this.outputSlots = new int[0];
		this.inputTanks = new FluidTank[3];
		this.outputTanks = new FluidTank[2];
	}

	@Override
	public GenericRecipes getRecipeSet() {
		return HaemodialysisRecipes.INSTANCE;
	}

	public ModuleMachineHaemodialysis fluidInput(FluidTank a, FluidTank b, FluidTank c) { inputTanks[0] = a; inputTanks[1] = b; inputTanks[2] = c; return this; }
	public ModuleMachineHaemodialysis fluidOutput(FluidTank a, FluidTank b) { outputTanks[0] = a; outputTanks[1] = b; return this; }
}
