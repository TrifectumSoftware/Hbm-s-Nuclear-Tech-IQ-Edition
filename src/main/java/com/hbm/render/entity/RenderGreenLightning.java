package com.hbm.render.entity;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntityGreenLightning;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderGreenLightning extends Render {
//badly stolen from vanilla lightning
	private static final double BOLT_H = 64.0D;

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float partialTicks) {
		EntityGreenLightning bolt = (EntityGreenLightning) entity;

		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		double[] offsetX = new double[8];
		double[] offsetZ = new double[8];
		double d0 = 0.0D;
		double d1 = 0.0D;
		Random random = new Random(bolt.boltVertex);

		for(int i = 7; i >= 0; --i) {
			offsetX[i] = d0;
			offsetZ[i] = d1;
			d0 += (double) (random.nextInt(11) - 5);
			d1 += (double) (random.nextInt(11) - 5);
		}

		for(int k1 = 0; k1 < 4; ++k1) {
			Random random1 = new Random(bolt.boltVertex);

			for(int j = 0; j < 3; ++j) {
				int k = 7;
				int l = 0;

				if(j > 0) {
					k = 7 - j;
				}

				if(j > 0) {
					l = k - 2;
				}

				double d2 = offsetX[k] - d0;
				double d3 = offsetZ[k] - d1;

				for(int i1 = k; i1 >= l; --i1) {
					double d4 = d2;
					double d5 = d3;

					if(j == 0) {
						d2 += (double) (random1.nextInt(11) - 5);
						d3 += (double) (random1.nextInt(11) - 5);
					} else {
						d2 += (double) (random1.nextInt(31) - 15);
						d3 += (double) (random1.nextInt(31) - 15);
					}

					tessellator.startDrawing(5);
					tessellator.setColorRGBA_F(0.2F, 1.0F, 0.08F, 0.35F);
					double d6 = 0.1D + (double) k1 * 0.2D;
					double d7 = 0.1D + (double) k1 * 0.2D;

					if(j == 0) {
						d6 *= (double) i1 * 0.1D + 1.0D;
						d7 *= (double) (i1 - 1) * 0.1D + 1.0D;
					}

					double step = BOLT_H / (double) (k - l + 1);

					for(int j1 = 0; j1 < 5; ++j1) {
						double d8 = x + 0.5D - d6;
						double d9 = z + 0.5D - d6;
						double d10 = x + 0.5D - d7;
						double d11 = z + 0.5D - d7;

						if(j1 == 1 || j1 == 2) {
							d8 += d6 * 2.0D;
							d10 += d7 * 2.0D;
						}

						if(j1 == 2 || j1 == 3) {
							d9 += d6 * 2.0D;
							d11 += d7 * 2.0D;
						}

						tessellator.addVertex(d10 + d2, y + step * (double) i1 * 0.5D, d11 + d3);
						tessellator.addVertex(d8 + d4, y + step * (double) (i1 + 1) * 0.5D, d9 + d5);
					}

					tessellator.draw();
				}
			}
		}

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}
}
