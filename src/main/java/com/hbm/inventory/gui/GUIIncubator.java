package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerIncubator;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityIncubator;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIIncubator extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_incubator.png");
	private TileEntityIncubator incubator;

	public GUIIncubator(InventoryPlayer playerInv, TileEntityIncubator tile) {
		super(new ContainerIncubator(playerInv, tile));

		this.incubator = tile;
		this.xSize = 176;
		this.ySize = 204;
	}

	@Override
	public void drawScreen(int x, int y, float interp) {
		super.drawScreen(x, y, interp);

		incubator.inputTank.renderTankInfo(this, x, y, guiLeft + 35, guiTop + 63, 34, 16);
		incubator.outputTank.renderTankInfo(this, x, y, guiLeft + 89, guiTop + 9, 33, 15);
		this.drawElectricityInfo(this, x, y, guiLeft + 152, guiTop + 18, 16, 52, incubator.getPower(), incubator.getMaxPower());

		this.drawCustomInfoStat(x, y, guiLeft + 78, guiTop + 67, 8, 8, guiLeft + 78, guiTop + 67, this.getUpgradeInfo(incubator));

		if(incubator.processTime > 0) {
			this.drawCustomInfoStat(x, y, guiLeft + 72, guiTop + 35, 31, 18, x, y, I18nUtil.resolveKey("gui.incubator.time", formatTime(incubator.processTime - incubator.progress)));
		}
	}

	private static String formatTime(int ticks) {
		int seconds = ticks / 20;
		int hours = seconds / 3600;
		seconds %= 3600;
		int minutes = seconds / 60;
		seconds %= 60;
		if(hours > 0) return hours + "h " + minutes + "m " + seconds + "s";
		if(minutes > 0) return minutes + "m " + seconds + "s";
		return seconds + "s";
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int p = (int) (incubator.power * 52 / Math.max(incubator.maxPower, 1));
		drawTexturedModalRect(guiLeft + 152, guiTop + 70 - p, 176, 52 - p, 16, p);

		int i = incubator.progress * 31 / Math.max(incubator.processTime, 1);
		drawTexturedModalRect(guiLeft + 72, guiTop + 35, 192, 0, i, 18);

		if(incubator.power >= incubator.consumption) {
			drawTexturedModalRect(guiLeft + 156, guiTop + 4, 176, 52, 9, 12);
		}

		this.drawInfoPanel(guiLeft + 78, guiTop + 67, 8, 8, 8);
		incubator.inputTank.renderTank(guiLeft + 35, guiTop + 79, this.zLevel, 34, 16, 1);
		incubator.outputTank.renderTank(guiLeft + 89, guiTop + 24, this.zLevel, 33, 15, 1);
	}
}
