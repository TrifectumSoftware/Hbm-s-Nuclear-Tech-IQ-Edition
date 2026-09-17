package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerSampleSynthesizer;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntitySampleSynthesizer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GUISampleSynthesizer extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_sample_scanner.png");

	private TileEntitySampleSynthesizer machine;

	public GUISampleSynthesizer(InventoryPlayer invPlayer, TileEntitySampleSynthesizer machine) {
		super(new ContainerSampleSynthesizer(invPlayer, machine));
		this.machine = machine;

		this.xSize = 176;
		this.ySize = 207;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 18, 16, 52, machine.power, machine.maxPower);

		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 49, guiTop + 17, 18, 18, mouseX, mouseY, new String[] {"Clone disk"});
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 38, guiTop + 61, 18, 18, mouseX, mouseY, new String[] {"Scan vial"});
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int mX, int mY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(machine.isScanning) {
			drawTexturedModalRect(guiLeft + 38, guiTop + 61, 192, 28, 18, 18);
		}

		int p = (int) (machine.power * 52 / machine.maxPower);
		drawTexturedModalRect(guiLeft + 134, guiTop + 18 + 52 - p, xSize, 52 - p, 16, p);

		if(machine.isScanning) {
			p = (int) (machine.progress * 44 / machine.maxProgress);
			drawTexturedModalRect(guiLeft + 59, guiTop + 63, 192, 14, p, 14);
		}

		fontRendererObj.drawString(machine.status, guiLeft + 41, guiTop + 86, 0xFFFFFF);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
	}

	@Override
	protected void mouseClicked(int x, int y, int i) {
		super.mouseClicked(x, y, i);

		if(checkClick(x, y, 38, 61, 18, 18)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));

			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("scan", true);

			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, machine.xCoord, machine.yCoord, machine.zCoord));
		}

		if(checkClick(x, y, 49, 17, 18, 18)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));

			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("clone", true);

			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, machine.xCoord, machine.yCoord, machine.zCoord));
		}
	}
}
