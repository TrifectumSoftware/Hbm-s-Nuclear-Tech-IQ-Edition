package com.hbm.render.entity.projectile;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelShrapnel;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderWasteShrapnel extends Render {

	ModelShrapnel mine;

	public RenderWasteShrapnel() {
		mine = new ModelShrapnel();
	}

	@Override
	public void doRender(Entity rocket, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float) p_76986_2_, (float) p_76986_4_, (float) p_76986_6_);
		GL11.glScalef(1.0F, 1.0F, 1.0F);
		GL11.glRotatef(180, 1, 0, 0);
		GL11.glRotatef((rocket.ticksExisted % 360) * 10 + p_76986_9_, 1, 1, 1);

		bindTexture(new ResourceLocation(RefStrings.MODID + ":textures/entity/shrapnel_waste.png"));

		if(rocket instanceof EntityShrapnel) {
			float scale = ((EntityShrapnel) rocket).renderScale;
			if(scale != 1.0F) {
				GL11.glScaled(scale, scale, scale);
			}
		}

		mine.renderAll(0.0625F);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return new ResourceLocation(RefStrings.MODID + ":textures/entity/shrapnel_waste.png");
	}
}
