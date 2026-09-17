package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerBloodCentrifuge;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityBloodCentrifuge;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIBloodCentrifuge extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_bloodcentrifuge.png");
	private TileEntityBloodCentrifuge centrifuge;

	public GUIBloodCentrifuge(InventoryPlayer invPlayer, TileEntityBloodCentrifuge tedf) {
		super(new ContainerBloodCentrifuge(invPlayer, tedf));
		centrifuge = tedf;

		this.xSize = 176;
		this.ySize = 204;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 17, 16, 34, centrifuge.power, centrifuge.maxPower);
		centrifuge.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 62, guiTop + 17, 8, 52);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {

		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int e = (int) centrifuge.getPowerScaled(34);
		drawTexturedModalRect(guiLeft + 8, guiTop + 51 - e, 240, 34 - e, 16, e);

		int p = centrifuge.getProgressScaled(61);
		drawTexturedModalRect(guiLeft + 73, guiTop + 35, 194, 34, p, 17);

		centrifuge.tank.renderTank(guiLeft + 62, guiTop + 69, this.zLevel, 8, 52);
	}
}
