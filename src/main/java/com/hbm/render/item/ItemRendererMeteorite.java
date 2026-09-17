package com.hbm.render.item;

import org.lwjgl.opengl.GL11;

import com.hbm.items.tool.ItemMeteoriteBase;
import com.hbm.render.util.RenderItemStack;
import com.hbm.render.util.RenderMiscEffects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

public class ItemRendererMeteorite implements IItemRenderer {

	public static final float[][] TIER_TINTS = {
		{ 1.0F, 1.0F, 1.0F },
		{ 1.0F, 0.5F, 0.5F },
		{ 0.5F, 1.0F, 1.0F },
		{ 0.25F, 0.25F, 0.25F },
		{ 0.0F, 0.5F, 1.0F },
		{ 1.0F, 1.0F, 0.0F },
		{ 0.5F, 1.0F, 0.5F },
		{ 1.0F, 1.0F, 0.5F },
		{ 0.5F, 0.5F, 0.0F },
		{ 0.75F, 1.0F, 0.0F },
		{ 1.0F, 0.0F, 0.5F },
	};

	@Override
	public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
		return type == ItemRenderType.INVENTORY;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		GL11.glPushMatrix();
		RenderItemStack.renderItemStackNoEffect(0, 0, 0, item);

		float[] tint = TIER_TINTS[ItemMeteoriteBase.getTier(item)];
		Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture(RenderMiscEffects.glint);

		GL11.glDepthFunc(GL11.GL_EQUAL);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);

		for(int j1 = 0; j1 < 2; ++j1) {
			OpenGlHelper.glBlendFunc(772, 1, 0, 0);
			float scaleU = 0.00390625F;
			float scaleV = 0.00390625F;
			float anim = (float) (Minecraft.getSystemTime() % (long) (3000 + j1 * 1873)) / (3000.0F + (float) (j1 * 1873)) * 256.0F;
			Tessellator tessellator = Tessellator.instance;
			float sizeMultU = 4.0F;
			if(j1 == 1) sizeMultU = -1.0F;

			float in = 0.36F;
			GL11.glColor4f(tint[0] * in, tint[1] * in, tint[2] * in, 1.0F);

			int x = 0, sizeX = 16, y = 0, sizeY = 16, zLevel = 0;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(x + 0, y + sizeY, zLevel, (anim + sizeY * sizeMultU) * scaleU, (sizeY) * scaleV);
			tessellator.addVertexWithUV(x + sizeX, y + sizeY, zLevel, (anim + sizeX + sizeY * sizeMultU) * scaleU, (sizeY) * scaleV);
			tessellator.addVertexWithUV(x + sizeX, y + 0, zLevel, (anim + sizeX) * scaleU, 0);
			tessellator.addVertexWithUV(x + 0, y + 0, zLevel, (anim + 0.0F) * scaleU, 0);
			tessellator.draw();
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glPopMatrix();
	}
}
