package com.hbm.render.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import com.hbm.lib.RefStrings;
import com.hbm.main.NTMSounds;
import com.hbm.render.shader.Shader;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

@SideOnly(Side.CLIENT)
public class NeuralyserFade {

	private static final long IN_MS = 150L;
	private static final long OUT_MS = 350L;

	private static final ResourceLocation GLITCH_FRAG = new ResourceLocation(RefStrings.MODID, "shaders/neuralyser_glitch.frag");

	private static long glitchStart = -1L;
	private static long glitchEnd = -1L;

	private static Shader glitchShader;
	private static int screenTexture = -1;
	private static int trailTexture = -1;
	private static boolean trailInitialised = false;

	public static void start() {
		glitchStart = System.currentTimeMillis();
		glitchEnd = -1L;
		trailInitialised = false;

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if(player != null) {
			player.playSound(NTMSounds.GLITCH, 1.0F, 1.0F);
		}
	}

	public static void finish() {
		if(glitchStart < 0L) return;
		glitchEnd = System.currentTimeMillis() + OUT_MS;
	}

	@SubscribeEvent
	public void onOverlayPost(RenderGameOverlayEvent.Post event) {
		if(event.type != RenderGameOverlayEvent.ElementType.ALL) return;

		float amount = glitchAmount();
		if(amount <= 0F) return;

		renderGlitch(event.resolution, amount);
	}

	private static void renderGlitch(ScaledResolution res, float amount) {
		Minecraft mc = Minecraft.getMinecraft();
		int texW = mc.displayWidth;
		int texH = mc.displayHeight;

		if(glitchShader == null) glitchShader = new Shader(GLITCH_FRAG);
		if(screenTexture == -1) screenTexture = GL11.glGenTextures();
		if(trailTexture == -1) trailTexture = GL11.glGenTextures();

		GL13.glActiveTexture(GL13.GL_TEXTURE0);

		bind(screenTexture);
		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, texW, texH, 0);

		if(!trailInitialised) {
			bind(trailTexture);
			GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, texW, texH, 0);
			trailInitialised = true;
		}

		bind(screenTexture);
		bind(trailTexture);

		mc.entityRenderer.setupOverlayRendering();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTexture);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, trailTexture);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);

		glitchShader.use();
		glitchShader.setUniform1i("tex", 0);
		glitchShader.setUniform1i("trailTex", 1);
		glitchShader.setUniform1f("time", (System.currentTimeMillis() % 1000000L) / 1000.0F);
		glitchShader.setUniform1f("amount", amount);

		int w = res.getScaledWidth();
		int h = res.getScaledHeight();

		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(0, 0, 0, 0, 1);
		tess.addVertexWithUV(0, h, 0, 0, 0);
		tess.addVertexWithUV(w, h, 0, 1, 0);
		tess.addVertexWithUV(w, 0, 0, 1, 1);
		tess.draw();

		glitchShader.stop();

		bind(trailTexture);
		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, texW, texH, 0);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
	}

	private static void bind(int texture) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
	}

	private static float glitchAmount() {
		if(glitchStart < 0L) return 0F;

		long now = System.currentTimeMillis();
		float amount = Math.min(1F, (now - glitchStart) / (float) IN_MS);

		if(glitchEnd > 0L) {
			if(now >= glitchEnd) {
				glitchStart = -1L;
				glitchEnd = -1L;
				return 0F;
			}
			amount = Math.min(amount, (glitchEnd - now) / (float) OUT_MS);
		}

		return Math.max(0F, amount);
	}
}
