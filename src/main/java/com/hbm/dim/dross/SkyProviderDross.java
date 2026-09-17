package com.hbm.dim.dross;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.SkyProviderCelestial;
import com.hbm.dim.dross.WorldProviderDross.WasteDebris;
import com.hbm.lib.RefStrings;
import com.hbm.render.loader.HFRWavefrontObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.IModelCustom;

public class SkyProviderDross extends SkyProviderCelestial {

	private static final IModelCustom[] ASTEROIDS = new IModelCustom[] {
		new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/misc/dross_waste_asteroid.obj")).asVBO(),
		new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/misc/dross_asteroid_1.obj")).asVBO(),
		new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/misc/dross_asteroid_2.obj")).asVBO()
	};
	private static final ResourceLocation[] ASTEROID_TEX = new ResourceLocation[] {
		new ResourceLocation(RefStrings.MODID, "textures/blocks/dross_waste.png"),
		new ResourceLocation(RefStrings.MODID, "textures/blocks/dross_slush.png")
	};
	private static double drawDist(WasteDebris d, Vec3 pos, double far) {
		double dx = d.posX - pos.xCoord;
		double dy = d.posY - pos.yCoord;
		double dz = d.posZ - pos.zCoord;
		double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(length < far) return length;
		return far * (0.8D + 0.2D * (d.phase / 360.0D));
	}

	@Override
	public void renderSpecialEffects(float partialTicks, WorldClient world, Minecraft mc) {

		Vec3 pos = mc.thePlayer.getPosition(partialTicks);
		Vec3 sky = world.getSkyColor(mc.renderViewEntity, partialTicks);
		long time = world.getWorldTime();

		float far = Math.max(mc.gameSettings.renderDistanceChunks * 16, 1.0F);

		WasteDebris[] ordered = WorldProviderDross.debris.toArray(new WasteDebris[0]);
		Arrays.sort(ordered, new Comparator<WasteDebris>() {
			@Override
			public int compare(WasteDebris a, WasteDebris b) {
				return Double.compare(drawDist(b, pos, far), drawDist(a, pos, far));
			}
		});

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPushAttrib(GL11.GL_FOG_BIT);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP2);
		GL11.glFogf(GL11.GL_FOG_DENSITY, 1.6F / far);
		FloatBuffer fogBuf = ByteBuffer.allocateDirect(4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		fogBuf.put((float) sky.xCoord).put((float) sky.yCoord).put((float) sky.zCoord).put(1.0F).flip();
		GL11.glFog(GL11.GL_FOG_COLOR, fogBuf);

		for(WasteDebris debris : ordered) {

			double renderDist = drawDist(debris, pos, far);

			double dx = debris.posX - pos.xCoord;
			double dy = debris.posY - pos.yCoord;
			double dz = debris.posZ - pos.zCoord;
			double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if(length < 1.0E-4D) continue;
			double inv = 1.0D / length;

			double radius = 1.7D * debris.scale * renderDist;
			double center = renderDist;
			if(center + radius > far * 0.98D) {
				center = far * 0.98D - radius;
				if(center < far * 0.15D) center = far * 0.15D;
			}

			GL11.glPushMatrix();
			GL11.glTranslated(dx * inv * center, dy * inv * center, dz * inv * center);
			GL11.glRotatef(debris.phase + (float) time * debris.speed, debris.ax, debris.ay, debris.az);
			GL11.glScaled(debris.scale * center, debris.scale * center, debris.scale * center);

			IModelCustom model = ASTEROIDS[debris.variant % ASTEROIDS.length];
			mc.renderEngine.bindTexture(ASTEROID_TEX[debris.tex % ASTEROID_TEX.length]);

			RenderHelper.enableStandardItemLighting();
			GL11.glColor4f(debris.brightness, debris.brightness, debris.brightness, 1.0F);
			model.renderAll();
			RenderHelper.disableStandardItemLighting();

			GL11.glPopMatrix();
		}

		GL11.glPopAttrib();

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_FOG);  // my love
		GL11.glPopMatrix();
	}
}
