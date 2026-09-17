package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerMedicineSynthesizer;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMedicineSynthesizer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMedicineSynthesizer extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_medicine_synthesizer.png");
	private TileEntityMedicineSynthesizer synth;

	public GUIMedicineSynthesizer(InventoryPlayer invPlayer, TileEntityMedicineSynthesizer tile) {
		super(new ContainerMedicineSynthesizer(invPlayer, tile));
		synth = tile;

		this.xSize = 190;
		this.ySize = 215;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 168, guiTop + 18, 16, 63, synth.power, synth.maxPower);
		synth.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 10, guiTop + 76, 34, 16);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.synth.hasCustomInventoryName() ? this.synth.getInventoryName() : I18n.format(this.synth.getInventoryName());
		this.fontRendererObj.drawString(name, 79 - this.fontRendererObj.getStringWidth(name) / 2, 6, 4210752);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 15, 118, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int k = (int) synth.getPowerScaled(63);
		drawTexturedModalRect(guiLeft + 168, guiTop + 80 - k, 190, 62 - k, 16, k);

		int l = synth.getProgressScaled(34);
		drawTexturedModalRect(guiLeft + 48, guiTop + 27, 206, 0, l, 34);

		synth.tank.renderTank(guiLeft + 10, guiTop + 92, this.zLevel, 34, 16, 1);
	}
}
