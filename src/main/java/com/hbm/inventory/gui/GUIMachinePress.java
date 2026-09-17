package com.hbm.inventory.gui;

import com.hbm.util.i18n.I18nUtil;
import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerMachinePress;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachinePress;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachinePress extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_steam_press.png");
	private TileEntityMachinePress press;

	public GUIMachinePress(InventoryPlayer invPlayer, TileEntityMachinePress tedf) {
		super(new ContainerMachinePress(invPlayer, tedf));
		press = tedf;

		this.xSize = 176;
		this.ySize = 214;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		String[] steamText = I18nUtil.resolveKeyArray("desc.gui.press.venting"); // when the imposter is sus
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop, 16, 16, guiLeft - 8, guiTop + 8, steamText);

		press.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 12, guiTop + 9, 24, 62);
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.press.hasCustomInventoryName() ? this.press.getInventoryName() : I18n.format(this.press.getInventoryName());

		this.fontRendererObj.drawString(name, this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2, 5, 0x000000);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int k = (int) (press.renderPress * 16 / press.maxPress);
		this.drawTexturedModalRect(guiLeft + 60, guiTop + 35, 15, 214, 18, k);

		int j = press.tanks[0].getFill() * 22 / press.tanks[0].getMaxFill();

		if(j > 0) j++;
		if(j > 22) j++;

		drawTexturedModalRect(guiLeft + 27, guiTop + 52 - j, 190, 24 - j, 4, j);

		if (press.tanks[1].getMaxFill() <= press.tanks[1].getFill()) {
			this.drawInfoPanel(guiLeft - 16, guiTop, 16, 16, 6);
		}
	}
}
