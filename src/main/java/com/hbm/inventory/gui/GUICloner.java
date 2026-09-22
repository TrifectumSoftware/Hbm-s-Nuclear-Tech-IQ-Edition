package com.hbm.inventory.gui;

import java.util.UUID;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.hbm.inventory.container.ContainerCloner;
import com.hbm.items.tool.ItemMedicalSyringe;
import com.hbm.lib.RefStrings;
import com.hbm.main.NTMSounds;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.render.util.SkinCache;
import com.hbm.tileentity.machine.TileEntityCloner;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GUICloner extends GuiInfoContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_cloner.png");
	private static final ModelBiped MODEL = new ModelBiped(0.0F);

	private final TileEntityCloner cloner;

	private GameProfile profile;
	private String profileKey = "";
	private float spin;

	public GUICloner(InventoryPlayer inv, TileEntityCloner tile) {
		super(new ContainerCloner(inv, tile));

		this.cloner = tile;
		this.xSize = 212;
		this.ySize = 236;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.spin += 2F;
		this.updateProfile();
	}

	private void updateProfile() {
		String uuid = null;
		String name = null;
		ItemStack syringe = cloner.slots[TileEntityCloner.SLOT_SYRINGE];

		if(syringe != null && syringe.hasTagCompound()) {
			if(syringe.stackTagCompound.hasKey(ItemMedicalSyringe.KEY_OWNER_UUID)) uuid = syringe.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_UUID);
			if(syringe.stackTagCompound.hasKey(ItemMedicalSyringe.KEY_OWNER_NAME)) name = syringe.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_NAME);
		}

		String key = uuid + "/" + name;
		if(key.equals(this.profileKey)) return;
		this.profileKey = key;
		this.profile = null;

		if(uuid == null || uuid.isEmpty()) return;

		try {
			this.profile = new GameProfile(UUID.fromString(uuid), name == null || name.isEmpty() ? uuid : name);
		} catch(IllegalArgumentException ex) {
			this.profile = null;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float interp) {
		super.drawScreen(mouseX, mouseY, interp);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 188, guiTop + 19, 16, 34, cloner.getPower(), cloner.getMaxPower());
		cloner.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 188, guiTop + 89, 16, 35);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);

		if(this.checkClick(mouseX, mouseY, 163, 106, 5, 18)) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(NTMSounds.LEVER_STOP), 1.0F));
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("toggle", true);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, cloner.xCoord, cloner.yCoord, cloner.zCoord));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String wip = "WIP";
		this.fontRendererObj.drawString(wip, 60 + (96 - this.fontRendererObj.getStringWidth(wip)) / 2, 53, 0x00FF00);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 20, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int energy = (int) (cloner.getPower() * 34 / Math.max(cloner.getMaxPower(), 1));
		if(energy > 0) drawTexturedModalRect(guiLeft + 188, guiTop + 53 - energy, 235, 50 - energy, 16, energy);
		if(energy > 0) drawTexturedModalRect(guiLeft + 192, guiTop + 5, 235, 50, 8, 11);

		int time = cloner.progress * 79 / TileEntityCloner.PROCESS_TIME;
		if(time > 0) drawTexturedModalRect(guiLeft + 163, guiTop + 97 - time, 251, 95 - time, 5, time);

		if(cloner.active) drawTexturedModalRect(guiLeft + 163, guiTop + 106, 250, 106, 5, 18);

		cloner.tank.renderTank(guiLeft + 188, guiTop + 124, this.zLevel, 16, 35);

		this.pushScissor(8, 18, 46, 79);
		this.renderClone();
		this.popScissor();
	}

	private void renderClone() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderHelper.enableStandardItemLighting();

		GL11.glPushMatrix();
		GL11.glTranslatef(guiLeft + 31F, guiTop + 40F, 50F);
		GL11.glScalef(-35F, 35F, 35F);
		GL11.glRotatef(180F + this.spin, 0F, 1F, 0F);

		ResourceLocation skin = this.profile == null ? null : SkinCache.getSkin(this.profile);
		Minecraft.getMinecraft().getTextureManager().bindTexture(skin != null ? skin : AbstractClientPlayer.locationStevePng);
		MODEL.isChild = false;
		MODEL.render(null, 0F, 0F, 0F, 0F, 0F, 0.0625F);

		GL11.glPopMatrix();

		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
