package com.hbm.handler.overlay;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.config.GeneralConfig;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.armor.Infotab;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

@SideOnly(Side.CLIENT)
public class InfotabOverlay extends HudOverlay {

	@Override
	public boolean isVisible(Minecraft mc, EntityPlayer player) {
		return player != null && HbmPlayerProps.getData(player).enableHUD && (Infotab.getWorn(player) != null || GeneralConfig.infotabAlwaysOn) && InfotabHandler.hoveredName != null;
	}

	@Override
	public int[] getAnchor(int screenWidth, int screenHeight) {
		return new int[] { (screenWidth - 117) / 2, 0 };
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = mc.fontRenderer;

		mc.renderEngine.bindTexture(new ResourceLocation("hbm:textures/gui/tool/gui_tool_infotab.png"));

		GL11.glPushMatrix();
		GL11.glTranslatef(guiLeft, guiTop, 0F);
		GL11.glScalef(0.25F, 0.25F, 1F);
		func_146110_a(0, 0, 0, 0, 468, 184, 468F, 184F);
		GL11.glPopMatrix();

		String name = InfotabHandler.hoveredName;
		if(name == null) return;

		ItemStack icon = InfotabHandler.hovered;
		Fluid fluid = InfotabHandler.hoveredFluid;
		boolean hasIcon = icon != null || fluid != null;
		int textX = hasIcon ? 44 : 14;
		float maxW = hasIcon ? 57F : 87F;

		if(hasIcon && interactionsEnabled() && checkClick(mouseX, mouseY, 10, 5, 97, 36)) {
			drawGradientRect(guiLeft + 10, guiTop + 5, guiLeft + 107, guiTop + 41, 0x20FFFFFF, 0x20FFFFFF);
		}

		float f = (float) mc.displayWidth / (float) Math.max(1, screenWidth);

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(Math.round((guiLeft + 10) * f), Math.round(mc.displayHeight - (guiTop + 41) * f), Math.round(97 * f), Math.round(36 * f));

		if(fluid != null) {
			IIcon fluidIcon = fluid.getStillIcon();
			if(fluidIcon == null && fluid.getBlock() != null) fluidIcon = fluid.getBlock().getIcon(1, 0);

			if(fluidIcon != null) {
				int color = fluid.getColor();
				float r = (color >> 16 & 0xFF) / 255F;
				float g = (color >> 8 & 0xFF) / 255F;
				float b = (color & 0xFF) / 255F;

				GL11.glDisable(GL11.GL_LIGHTING);
				mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

				Tessellator tess = Tessellator.instance;
				tess.startDrawingQuads();
				tess.setColorOpaque_F(r, g, b);
				tess.addVertexWithUV(guiLeft + 14, guiTop + 35, 0, fluidIcon.getMinU(), fluidIcon.getMaxV());
				tess.addVertexWithUV(guiLeft + 38, guiTop + 35, 0, fluidIcon.getMaxU(), fluidIcon.getMaxV());
				tess.addVertexWithUV(guiLeft + 38, guiTop + 11, 0, fluidIcon.getMaxU(), fluidIcon.getMinV());
				tess.addVertexWithUV(guiLeft + 14, guiTop + 11, 0, fluidIcon.getMinU(), fluidIcon.getMinV());
				tess.draw();

				GL11.glColor4f(1F, 1F, 1F, 1F);
			}

		} else if(icon != null) {
			GL11.glEnable(GL11.GL_NORMALIZE);
			RenderHelper.enableGUIStandardItemLighting();
			GL11.glPushMatrix();
			GL11.glTranslatef(guiLeft + 14, guiTop + 11, 0F);
			GL11.glScalef(1.5F, 1.5F, 1F);
			RenderItem.getInstance().renderItemAndEffectIntoGUI(font, mc.getTextureManager(), icon, 0, 0);
			GL11.glPopMatrix();
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_NORMALIZE);
			GL11.glColor4f(1F, 1F, 1F, 1F);
		}

		EntityLivingBase mob = InfotabHandler.hoveredMob;
		List<String> lines = InfotabHandler.hoveredLines;
		boolean hasLines = !lines.isEmpty();
		int nameY = hasLines ? 8 : mob != null ? 10 : 14;
		int modY = hasLines ? 18 : mob != null ? 21 : 25;

		float fit = Math.min(1F, maxW / width(font, name));

		GL11.glPushMatrix();
		GL11.glTranslatef(guiLeft + textX, guiTop + nameY, 0F);
		GL11.glScalef(fit, fit, 1F);
		font.drawStringWithShadow(name, 0, 0, 0xFFFFFF);
		GL11.glPopMatrix();

		String mod = InfotabHandler.hoveredMod;
		if(mod != null) {
			float modFit = 0.75F * Math.min(1F, maxW / 0.75F / width(font, mod));

			GL11.glPushMatrix();
			GL11.glTranslatef(guiLeft + textX, guiTop + modY, 0F);
			GL11.glScalef(modFit, modFit, 1F);
			font.drawStringWithShadow(mod, 0, 0, 0x5555FF);
			GL11.glPopMatrix();
		}

		if(mob != null && !hasLines) {
			float ratio = MathHelper.clamp_float(mob.getHealth() / mob.getMaxHealth(), 0F, 1F);
			String health = (int) Math.ceil(mob.getHealth()) + "/" + (int) Math.ceil(mob.getMaxHealth());
			int color = ((int) (255 * (1F - ratio)) & 0xFF) << 16 | ((int) (255 * ratio) & 0xFF) << 8;

			GL11.glPushMatrix();
			GL11.glTranslatef(guiLeft + textX, guiTop + 30, 0F);
			GL11.glScalef(0.75F, 0.75F, 1F);
			font.drawStringWithShadow(health, 0, 0, color);
			GL11.glPopMatrix();
		}

		for(int i = 0; i < lines.size() && i < 2; i++) {
			String text = lines.get(i);
			int color = 0xCCCCCC;

			if(text.startsWith("&[") && text.contains("&]")) {
				try {
					int end = text.indexOf("&]");
					color = Integer.parseInt(text.substring(2, end));
					text = text.substring(end + 2);
				} catch(Exception ex) { }
			}

			float lineFit = Math.min(0.75F, maxW / width(font, text));

			GL11.glPushMatrix();
			GL11.glTranslatef(guiLeft + textX, guiTop + 26 + i * 7, 0F);
			GL11.glScalef(lineFit, lineFit, 1F);
			font.drawStringWithShadow(text, 0, 0, color);
			GL11.glPopMatrix();
		}

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	private static int width(FontRenderer font, String text) {

		int w = font.getStringWidth(text);

		if(font.getStringWidth("MMMMMMMM") < 40) {
			w = Math.max(w, text.length() * 8);
		}

		return w;
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if(!checkClick(mouseX, mouseY, 10, 5, 97, 36)) return false;
		if(InfotabHandler.hovered == null) return false;

		if(button == 0) {
			InfotabHandler.openNEI(InfotabHandler.hovered, false);
			return true;
		}

		if(button == 1) {
			InfotabHandler.openNEI(InfotabHandler.hovered, true);
			return true;
		}

		return false;
	}
}
