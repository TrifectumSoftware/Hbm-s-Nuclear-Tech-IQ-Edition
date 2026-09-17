package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerMachineHaemodialysis;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.inventory.recipes.HaemodialysisRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineHaemodialysis;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GUIMachineHaemodialysis extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_haemodialysis_machine.png");
	private TileEntityMachineHaemodialysis dia;

	public GUIMachineHaemodialysis(InventoryPlayer invPlayer, TileEntityMachineHaemodialysis tedf) {
		super(new ContainerMachineHaemodialysis(invPlayer, tedf));
		dia = tedf;

		this.xSize = 176;
		this.ySize = 256;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		for(int i = 0; i < 3; i++) {
			dia.inputTanks[i].renderTankInfo(this, mouseX, mouseY, guiLeft + 8 + i * 18, guiTop + 18, 16, 96);
		}
		for(int i = 0; i < 2; i++) {
			dia.outputTanks[i].renderTankInfo(this, mouseX, mouseY, guiLeft + 98 + i * 18, guiTop + 18, 16, 52);
		}

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 61, dia.power, dia.maxPower);

		if(guiLeft + 7 <= mouseX && guiLeft + 7 + 18 > mouseX && guiTop + 125 < mouseY && guiTop + 125 + 18 >= mouseY) {
			if(this.dia.haemodialysisModule.getRecipeName() != null && HaemodialysisRecipes.INSTANCE.recipeNameMap.containsKey(this.dia.haemodialysisModule.getRecipeName())) {
				GenericRecipe recipe = this.dia.haemodialysisModule.getRecipe();
				GUIElements.drawHoveringTextRecipe(recipe.print(), mouseX, mouseY, this.fontRendererObj, itemRender, this.width, this.height);
			} else {
				this.drawCreativeTabHoveringText(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe"), mouseX, mouseY);
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		if(this.checkClick(x, y, 7, 125, 18, 18)) GUIScreenRecipeSelector.openSelector(HaemodialysisRecipes.INSTANCE, dia, dia.haemodialysisModule.getRecipeName(), 0, null, this);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int p = (int) (dia.power * 61 / dia.maxPower);
		drawTexturedModalRect(guiLeft + 152, guiTop + 79 - p, 176, 61 - p, 16, p);

		if(dia.haemodialysisModule.progress > 0) {
			int j = (int) Math.ceil(70 * dia.haemodialysisModule.progress);
			drawTexturedModalRect(guiLeft + 62, guiTop + 126, 176, 61, j, 16);
		}

		GenericRecipe recipe = dia.haemodialysisModule.getRecipe();
		this.renderItem(recipe != null ? recipe.getIcon() : TEMPLATE_FOLDER, 8, 126);

		for(int i = 0; i < 3; i++) {
			dia.inputTanks[i].renderTank(guiLeft + 8 + i * 18, guiTop + 114, this.zLevel, 16, 96);
		}
		for(int i = 0; i < 2; i++) {
			dia.outputTanks[i].renderTank(guiLeft + 98 + i * 18, guiTop + 70, this.zLevel, 16, 52);
		}
	}
}
