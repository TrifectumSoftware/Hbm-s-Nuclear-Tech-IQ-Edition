package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.hbm.inventory.container.ContainerCloner;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.items.special.ItemHumanPart;
import com.hbm.items.special.ItemHumanPart.EnumBodyStat;
import com.hbm.items.special.ItemHumanPart.EnumPartTrait;
import com.hbm.items.tool.ItemMedicalSyringe;
import com.hbm.lib.RefStrings;
import com.hbm.main.NTMSounds;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.render.util.SkinCache;
import com.hbm.tileentity.machine.TileEntityCloner;
import com.hbm.util.i18n.I18nUtil;
import com.mojang.authlib.GameProfile;

import api.hbm.fluidmk2.IFillableItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
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
	private int page = 0;
	private int traitPage = 0;

	private static final int TABS_Y = 20;
	private static final int TAB_SIZE = 12;
	private static final ResourceLocation[] TAB_ICONS = {
			new ResourceLocation(RefStrings.MODID + ":textures/gui/stats.png"),
			new ResourceLocation(RefStrings.MODID + ":textures/gui/traits.png"),
			new ResourceLocation(RefStrings.MODID + ":textures/gui/genome.png")
	};

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

		for(int i = 0; i < TAB_ICONS.length; i++) {
			if(this.checkClick(mouseX, mouseY, this.tabX(i), TABS_Y, TAB_SIZE, TAB_SIZE)) {
				this.page = i;
				this.traitPage = 0;
				this.click();
				return;
			}
		}

		if(this.page == 1) {
			if(this.checkClick(mouseX, mouseY, 140, 88, 5, 5)) {
				if(this.traitPage > 0) this.traitPage--;
				this.click();
				return;
			}
			if(this.checkClick(mouseX, mouseY, 148, 88, 5, 5)) {
				this.traitPage++;
				this.click();
				return;
			}
		}

		if(this.checkClick(mouseX, mouseY, 161, 105, 9, 20)) {
			cloner.active = !cloner.active;

			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean("toggle", true);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, cloner.xCoord, cloner.yCoord, cloner.zCoord));

			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(NTMSounds.LEVER_STOP), 1.0F));
		}
	}

	private int tabX(int i) {
		int section = 96 / TAB_ICONS.length;
		return 60 + i * section + (section - TAB_SIZE) / 2;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.drawBodyScreen();
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 20, this.ySize - 96 + 2, 4210752);
	}

	private void drawBodyScreen() {
		ItemStack syringe = cloner.slots[TileEntityCloner.SLOT_SYRINGE];
		FluidType blood = syringe == null ? Fluids.NONE : IFillableItem.getFluidType(syringe);
		String donor = syringe != null && syringe.hasTagCompound() ? syringe.stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_NAME) : "";

		int[] totals = new int[EnumBodyStat.values().length];
		List<String> traits = new ArrayList<String>();

		for(ItemStack part : cloner.getDistinctParts()) {
			for(EnumBodyStat stat : EnumBodyStat.values()) totals[stat.ordinal()] += ItemHumanPart.getStat(part, stat);

			EnumPartTrait trait = ItemHumanPart.getTrait(part);
			if(trait != null) traits.add(I18nUtil.resolveKey("item.human_part.trait." + trait.name().toLowerCase(Locale.US)));
		}

		int y = 41;

		if(page == 0) {
			for(EnumBodyStat stat : EnumBodyStat.values()) {
				String line = I18nUtil.resolveKey("item.human_part.stat." + stat.name().toLowerCase(Locale.US)) + ": " + totals[stat.ordinal()];
				screenText(trim(line, 150), 64, y, 0x00FF00, false);
				y += 8;
			}
		} else if(page == 1) {
			int pages = Math.max(1, (traits.size() + 2) / 3);
			if(traitPage >= pages) traitPage = pages - 1;

			if(traits.isEmpty()) {
				screenText(I18nUtil.resolveKey("gui.cloner.empty"), 64, y, 0x808080, false);
			} else {
				for(int i = 0; i < 3; i++) {
					int index = traitPage * 3 + i;
					if(index >= traits.size()) break;
					screenText(trim(traits.get(index), 150), 64, y, 0xFF5555, false);
					y += 8;
				}
			}

			if(pages > 1) {
				screenText(traitPage + 1 + "/" + pages, 138, 92, 0x808080, true);
				arrow(140, 88, true, traitPage > 0 ? 0x00FF00 : 0x404040);
				arrow(148, 88, false, traitPage < pages - 1 ? 0x00FF00 : 0x404040);
			}
		} else {
			screenText(trim(I18nUtil.resolveKey("gui.cloner.genome", donor.isEmpty() ? "?" : donor), 150), 64, y, 0x00A0FF, false);
			if(blood != Fluids.NONE) screenText(trim(blood.getLocalizedName(), 150), 64, y + 8, 0x00FF00, false);
			y += 8;
		}

		screenText(cloner.getLoadedParts() + "/5", 154, 84, 0x808080, true);
	}

	private void screenText(String text, int x, int y, int color, boolean right) {
		GL11.glPushMatrix();
		GL11.glScaled(0.6D, 0.6D, 1D);
		int drawX = Math.round((right ? x - this.fontRendererObj.getStringWidth(text) * 0.6F : x) / 0.6F);
		this.fontRendererObj.drawStringWithShadow(text, drawX, Math.round(y / 0.6F), color);
		GL11.glPopMatrix();
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private String trim(String text, int maxWidth) {
		if(this.fontRendererObj.getStringWidth(text) <= maxWidth) return text;

		String cut = text;
		while(cut.length() > 0 && this.fontRendererObj.getStringWidth(cut + "...") > maxWidth) cut = cut.substring(0, cut.length() - 1);
		return cut + "...";
	}

	private void arrow(int x, int y, boolean up, int color) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1F);

		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_TRIANGLES);
		if(up) {
			tess.addVertex(x, y + 5, this.zLevel);
			tess.addVertex(x + 5, y + 5, this.zLevel);
			tess.addVertex(x + 2.5D, y, this.zLevel);
		} else {
			tess.addVertex(x, y, this.zLevel);
			tess.addVertex(x + 5, y, this.zLevel);
			tess.addVertex(x + 2.5D, y + 5, this.zLevel);
		}
		tess.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
		for(int i = 0; i < TAB_ICONS.length; i++) {
			int tx = this.tabX(i);
			float tint = i == this.page ? 1F : 0.35F;

			Minecraft.getMinecraft().getTextureManager().bindTexture(TAB_ICONS[i]);
			GL11.glColor4f(tint, tint, tint, 1F);

			Tessellator tess = Tessellator.instance;
			tess.startDrawingQuads();
			tess.addVertexWithUV(guiLeft + tx, guiTop + TABS_Y + TAB_SIZE, this.zLevel, 0, 1);
			tess.addVertexWithUV(guiLeft + tx + TAB_SIZE, guiTop + TABS_Y + TAB_SIZE, this.zLevel, 1, 1);
			tess.addVertexWithUV(guiLeft + tx + TAB_SIZE, guiTop + TABS_Y, this.zLevel, 1, 0);
			tess.addVertexWithUV(guiLeft + tx, guiTop + TABS_Y, this.zLevel, 0, 0);
			tess.draw();
		}

		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glPopAttrib();
		GUIElements.drawDivider(guiLeft + 62, guiTop + TABS_Y + TAB_SIZE + 3, 92, 0xFF00A000);

		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);

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
