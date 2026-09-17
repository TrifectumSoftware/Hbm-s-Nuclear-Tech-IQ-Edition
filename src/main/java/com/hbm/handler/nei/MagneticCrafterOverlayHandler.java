package com.hbm.handler.nei;

import java.util.List;

import com.hbm.inventory.gui.GUIMagneticCrafter;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.MagneticCrafterRecipePacket;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class MagneticCrafterOverlayHandler implements IOverlayHandler {

	// NEI renders crafting ingredients at 25 + col*18, 6 + row*18 within the recipe view
	private static final int GRID_X = 25;
	private static final int GRID_Y = 6;

	@Override
	public void overlayRecipe(GuiContainer gui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
		if(!(gui instanceof GUIMagneticCrafter)) return;

		ItemStack[][] items = new ItemStack[9][];

		List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
		if(ingredients != null) {
			for(PositionedStack stack : ingredients) {
				int col = (stack.relx - GRID_X) / 18;
				int row = (stack.rely - GRID_Y) / 18;

				if(col < 0 || col > 2 || row < 0 || row > 2) continue;

				items[row * 3 + col] = stack.items;
			}
		}

		PacketDispatcher.wrapper.sendToServer(new MagneticCrafterRecipePacket(items, shift));
	}
}
