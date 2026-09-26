package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.CelestialBody;
import com.hbm.inventory.container.ContainerMachineNanoprobe;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.tileentity.machine.TileEntityMachineNanoprobe;
import com.hbm.tileentity.machine.TileEntityMachineNanoprobe.ProbeState;
import com.hbm.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class GUIMachineNanoprobe extends GuiInfoContainer {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_probe.png");

	private static final float TEX = 320F;
	private static final int T_MAX = 72_000;
	private static final int WEB_TIME = 24_000;
	private static final double SPIN_TICKS = 200D;
	private static final float TEXT_SCALE = 0.6F;
	private static final int AXIS_COLOR = 0x00A000;

	private static final int[][] OUTPUT_TANK_POS = { { 142, 68 }, { 162, 68 }, { 182, 68 } };
	private static final int OUTPUT_TANK_W = 15;
	private static final int OUTPUT_TANK_H = 55;

	private final TileEntityMachineNanoprobe probe;

	public GUIMachineNanoprobe(InventoryPlayer invPlayer, TileEntityMachineNanoprobe tile) {
		super(new ContainerMachineNanoprobe(invPlayer, tile));
		this.probe = tile;
		this.xSize = 240;
		this.ySize = 235;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 217, guiTop + 19, 16, 34, probe.power, probe.maxPower);
		this.probe.gooTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 23, 16, 98);
		this.probe.dormantGooTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 217, guiTop + 86, 16, 40);

		if(this.probe.hasOutputs()) {
			for(int i = 0; i < this.probe.outputTanks.length; i++) {
				this.probe.outputTanks[i].renderTankInfo(this, mouseX, mouseY, guiLeft + OUTPUT_TANK_POS[i][0], guiTop + OUTPUT_TANK_POS[i][1], OUTPUT_TANK_W, OUTPUT_TANK_H);
			}
		}

		if(this.checkClick(mouseX, mouseY, 43, 7, 55, 53)) {
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 43, guiTop + 7, 55, 53, mouseX, mouseY,
					"Mission",
					"Efficiency: " + (int) (TileEntityMachineNanoprobe.getEfficiency(probe.deployed, probe.elapsedTicks) * 100) + "%",
					"Loss: " + (int) (probe.lossFraction * 100) + "%",
					"Deployed: " + probe.deployed + "mB");
		}

		if(this.checkClick(mouseX, mouseY, 43, 70, 55, 53)) {
			List<String> lines = new ArrayList<String>();
			lines.add("Harvest");

			FluidType[] outputs = probe.getOutputs();
			if(outputs != null) {
				for(int i = 0; i < probe.outputCount(); i++) lines.add(outputs[i].getLocalizedName() + ": " + (int) probe.harvest[i] + "mB");
			}

			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 43, guiTop + 70, 55, 53, mouseX, mouseY, lines);
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		if(this.checkClick(x, y, 115, 52, 17, 29)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(probe.xCoord, probe.yCoord, probe.zCoord, 0, 0));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String state = StatCollector.translateToLocal("gui.nanoprobe.state." + probe.state.name().toLowerCase(Locale.US));
		GUIElements.drawCenteredText(this.fontRendererObj, state, 102, 34, 36, 11, TEXT_SCALE, 0x00FF00);
		GUIElements.drawCenteredText(this.fontRendererObj, formatTime(probe.missionTicks), 106, 91, 29, 11, TEXT_SCALE, 0x00FF00);

		this.fontRendererObj.drawString(I18n.format("container.inventory"), 41, 140, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

		func_146110_a(guiLeft, guiTop, 0, 0, xSize, ySize, TEX, TEX);

		int p = (int) (probe.power * 34 / Math.max(probe.maxPower, 1));
		if(p > 0) func_146110_a(guiLeft + 217, guiTop + 19 + (34 - p), 304, 34 - p, 16, p, TEX, TEX);
		func_146110_a(guiLeft + 221, guiTop + 5, 304, 34, 8, 11, TEX, TEX);

		if(probe.state == ProbeState.DEPLOYING || probe.state == ProbeState.EXTRACTING) {
			func_146110_a(guiLeft + 115, guiTop + 52, 303, 48, 17, 29, TEX, TEX);
		}

		probe.gooTank.renderTank(guiLeft + 8, guiTop + 121, this.zLevel, 16, 98);
		probe.dormantGooTank.renderTank(guiLeft + 217, guiTop + 126, this.zLevel, 16, 40);

		if(probe.hasOutputs()) {
			for(int i = 0; i < this.probe.outputTanks.length; i++) {
				probe.outputTanks[i].renderTank(guiLeft + OUTPUT_TANK_POS[i][0], guiTop + OUTPUT_TANK_POS[i][1] + OUTPUT_TANK_H, this.zLevel, OUTPUT_TANK_W, OUTPUT_TANK_H);
			}
		}

		drawEfficiencyGraph(43, 7, 55, 53);
		drawYieldGraph(43, 70, 55, 53);
		drawCrossSection(142, 7, 55, 53, partialTicks);

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
	}

	private void drawAxes(int gx, int gy, int gw, int gh) {
		int ox = guiLeft + gx + 3;
		int oy = guiTop + gy + gh - 3;
		GUIElements.drawArrowVector(ox, oy, this.zLevel, new Vector2f(ox, guiTop + gy + 4), 2F, AXIS_COLOR);
		GUIElements.drawArrowVector(ox, oy, this.zLevel, new Vector2f(guiLeft + gx + gw - 4, oy), 2F, AXIS_COLOR);
	}

	private void drawEfficiencyGraph(int gx, int gy, int gw, int gh) {
		drawAxes(gx, gy, gw, gh);
		if(!probe.isActive()) return;
		drawCurve(gx, gy, gw, gh, 0xFF5555, true, probe.deployed);
		drawCurve(gx, gy, gw, gh, 0x55FF55, false, probe.deployed);
	}

	private void drawYieldGraph(int gx, int gy, int gw, int gh) {
		drawAxes(gx, gy, gw, gh);
		if(!probe.isActive()) return;

		FluidType[] outputs = probe.getOutputs();
		if(outputs == null) return;

		int count = probe.outputCount();
		for(int line = 0; line < count; line++) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			Tessellator tess = Tessellator.instance;
			tess.startDrawing(GL11.GL_LINE_STRIP);
			tess.setColorOpaque_I(outputs[line].getColor());

			for(int px = 0; px <= gw - 6; px++) {
				double t = (double) px / (gw - 6) * T_MAX;
				double total = TileEntityMachineNanoprobe.getYield(probe.deployed, (int) t);
				double[] weight = TileEntityMachineNanoprobe.getComposition(t / 1200D, count);

				double stack = 0D;
				for(int i = 0; i <= line; i++) stack += total * weight[i];

				tess.addVertex(guiLeft + gx + 3 + px, guiTop + gy + gh - 3 - stack / TileEntityMachineNanoprobe.E_MAX * (gh - 7), this.zLevel);
			}

			tess.draw();
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}

	private void drawCurve(int gx, int gy, int gw, int gh, int color, boolean loss, int deployed) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_LINE_STRIP);
		tess.setColorOpaque_I(color);

		for(int px = 0; px <= gw - 6; px++) {
			double t = (double) px / (gw - 6) * T_MAX;
			double v = loss ? TileEntityMachineNanoprobe.getLoss((int) t) : TileEntityMachineNanoprobe.getEfficiency(deployed, (int) t);
			v = v < 0 ? 0 : v > 1 ? 1 : v;
			tess.addVertex(guiLeft + gx + 3 + px, guiTop + gy + gh - 3 - v * (gh - 7), this.zLevel);
		}

		tess.draw();
		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private void drawCrossSection(int gx, int gy, int gw, int gh, float partialTicks) {
		CelestialBody body = probe.getWorldObj() != null ? CelestialBody.getBody(probe.getWorldObj()) : null;
		if(body == null || body.texture == null) return;

		int size = 26;
		int px = gx + 5;
		int py = gy + (gh - size) / 2;

		double time = probe.getWorldObj().getTotalWorldTime() + partialTicks;
		float phase = (float) ((time % SPIN_TICKS) / SPIN_TICKS);

		Minecraft.getMinecraft().getTextureManager().bindTexture(body.texture);
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(guiLeft + px, guiTop + py + size, this.zLevel, phase, 1);
		tess.addVertexWithUV(guiLeft + px + size, guiTop + py + size, this.zLevel, 1 + phase, 1);
		tess.addVertexWithUV(guiLeft + px + size, guiTop + py, this.zLevel, 1 + phase, 0);
		tess.addVertexWithUV(guiLeft + px, guiTop + py, this.zLevel, phase, 0);
		tess.draw();

		drawGooSpread(px, py, size, size, gooProgress());

		int color = body.color != null && body.color.length >= 3
				? ColorUtil.color((int) (body.color[0] * 255F), (int) (body.color[1] * 255F), (int) (body.color[2] * 255F))
				: 0xFFFFFF;
		GUIElements.drawScaledText(this.fontRendererObj, StatCollector.translateToLocal("body." + body.name), guiLeft + px + size + 3, guiTop + py + size / 2 - 3, TEXT_SCALE, color);
	}

	private double gooProgress() {
		if(probe.state == ProbeState.DEPLOYING) return 0D;
		double progress = Math.min(1D, probe.elapsedTicks / (double) WEB_TIME);
		if(probe.state == ProbeState.RECALLING && probe.recallTime > 0) progress *= 1D - (double) probe.stateTicks / probe.recallTime;
		return progress;
	}

	private void drawGooSpread(int gx, int gy, int gw, int gh, double progress) {
		if(progress <= 0D) return;

		Minecraft.getMinecraft().getTextureManager().bindTexture(Fluids.GRAY_GOO.getTexture());

		double tw = (gw / 2D) * progress;
		double th = (gh / 2D) * progress;

		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		gooQuad(tess, gx, gy, gx + gw, gy + th);
		gooQuad(tess, gx, gy + gh - th, gx + gw, gy + gh);
		gooQuad(tess, gx, gy, gx + tw, gy + gh);
		gooQuad(tess, gx + gw - tw, gy, gx + gw, gy + gh);
		tess.draw();
	}

	private void gooQuad(Tessellator tess, double x0, double y0, double x1, double y1) {
		double u0 = x0 / 16D, v0 = y0 / 16D, u1 = x1 / 16D, v1 = y1 / 16D;
		tess.addVertexWithUV(guiLeft + x0, guiTop + y1, this.zLevel, u0, v1);
		tess.addVertexWithUV(guiLeft + x1, guiTop + y1, this.zLevel, u1, v1);
		tess.addVertexWithUV(guiLeft + x1, guiTop + y0, this.zLevel, u1, v0);
		tess.addVertexWithUV(guiLeft + x0, guiTop + y0, this.zLevel, u0, v0);
	}

	private static String formatTime(int ticks) {
		int seconds = ticks / 20;
		return String.format("%02d:%02d", seconds / 60, seconds % 60);
	}
}
