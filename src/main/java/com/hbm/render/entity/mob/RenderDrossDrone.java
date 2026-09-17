package com.hbm.render.entity.mob;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class RenderDrossDrone extends Render {

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1) {

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);

		if(entity instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) entity;
			if(living.hurtTime > 0) {
				float hurt = living.hurtTime - f1;
				float wobble = (hurt / 5.0F) * 15.0F;
				GL11.glRotatef(wobble, 0, 0, 1);
				GL11.glRotatef(wobble * 0.5F, 1, 0, 0);
			}
		}

		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glScaled(0.5F, 0.5F, 0.5F);

		this.bindTexture(getEntityTexture(entity));
		ResourceManager.delivery_drone.renderPart("Drone");

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation(RefStrings.MODID + ":textures/models/machines/drone_dross.png");
	}
}
