package com.hbm.render.entity.item;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.item.EntityDroneBase;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderQuarryDrone extends Render {

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1) {

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		// quarry drones are twice the size of a regular drone
		GL11.glScaled(2, 2, 2);

		this.bindTexture(ResourceManager.delivery_drone_quarry_tex);
		ResourceManager.delivery_drone.renderPart("Drone");

		// show a crate when the drone is carrying collected ore
		EntityDroneBase drone = (EntityDroneBase) entity;
		if(drone.getAppearance() == 1) ResourceManager.delivery_drone.renderPart("Crate");

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ResourceManager.delivery_drone_quarry_tex;
	}
}
