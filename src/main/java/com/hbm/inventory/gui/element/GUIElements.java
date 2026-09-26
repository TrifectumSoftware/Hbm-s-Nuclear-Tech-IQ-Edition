package com.hbm.inventory.gui.element;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.RefStrings;
import com.hbm.render.util.DiamondPronter;
import com.hbm.util.Vec3NT;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector2f;

public class GUIElements {

	@Deprecated public static enum Gauge {
		ROUND_SMALL("small_round", 18, 18, 13);
		ResourceLocation texture;
		int width, height, count;
		private Gauge(String texture, int width, int height, int count) {
			this.texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gauges/" + texture + ".png");
			this.width = width;
			this.height = height;
			this.count = count;
		}
	}

	@Deprecated public static void renderGauge(Gauge gauge, double x, double y, double z, double progress) {
		Minecraft.getMinecraft().renderEngine.bindTexture(gauge.texture);
		int frameNum = (int) Math.round((gauge.count - 1) * progress);
		double singleFrame = 1D / (double)gauge.count;
		double frameOffset = singleFrame * frameNum;
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x, 				y + gauge.height, 	z, 	0, 	frameOffset + singleFrame);
		tess.addVertexWithUV(x + gauge.width, 	y + gauge.height, 	z, 	1, 	frameOffset + singleFrame);
		tess.addVertexWithUV(x + gauge.width, 	y, 					z, 	1, 	frameOffset);
		tess.addVertexWithUV(x, 				y, 					z, 	0, 	frameOffset);
		tess.draw();
	}

	public static void drawSmoothGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color) {
		drawSmoothGauge(x, y, z, progress, tipLength, backLength, backSide, color, 0x000000);
	}

	private static Vec3NT tip = new Vec3NT();
	private static Vec3NT left = new Vec3NT();
	private static Vec3NT right = new Vec3NT();

	public static void drawSmoothGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color, int colorOuter) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		progress = MathHelper.clamp_double(progress, 0, 1);

		float angle = (float) Math.toRadians(-progress * 270 - 45);
		tip.setComponents(0, tipLength, 0);
		left.setComponents(backSide, -backLength, 0);
		right.setComponents(-backSide, -backLength, 0);

		tip.rotateAroundZ(angle);
		left.rotateAroundZ(angle);
		right.rotateAroundZ(angle);

		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_TRIANGLES);
		tess.setColorOpaque_I(colorOuter);
		double mult = 1.5;
		tess.addVertex(x + tip.xCoord * mult, y + tip.yCoord * mult, z);
		tess.addVertex(x + left.xCoord * mult, y + left.yCoord * mult, z);
		tess.addVertex(x + right.xCoord * mult, y + right.yCoord * mult, z);
		tess.setColorOpaque_I(color);
		tess.addVertex(x + tip.xCoord, y + tip.yCoord, z);
		tess.addVertex(x + left.xCoord, y + left.yCoord, z);
		tess.addVertex(x + right.xCoord, y + right.yCoord, z);
		tess.draw();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void drawSmoothLinearGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, double scale, float rotation, int color) {
		drawSmoothLinearGauge(x, y, z, progress, tipLength, backLength, backSide, scale, rotation, color, 0x000000);
	}

	private static Vec3NT Bleft = new Vec3NT();
	private static Vec3NT Bright = new Vec3NT();

	public static void drawSmoothLinearGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, double scale, float rotation, int color, int colorOuter) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		scale = Math.max(scale, 1);
		progress = MathHelper.clamp_double(progress, 0, 1) * scale;

		tip.setComponents(0, -tipLength, 0);
		right.setComponents(-backSide, 0, 0);
		Bright.setComponents(-backSide, backLength, 0);
		Bleft.setComponents(backSide, backLength, 0);
		left.setComponents(backSide, 0, 0);

		float angle = (float) Math.toRadians(-rotation);

		tip.rotateAroundZ(angle);
		right.rotateAroundZ(angle);
		Bright.rotateAroundZ(angle);
		Bleft.rotateAroundZ(angle);
		left.rotateAroundZ(angle);

		double deltaX = progress * MathHelper.cos(angle);
		double deltaY = progress * MathHelper.sin(angle);

		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_POLYGON);
		tess.setColorOpaque_I(colorOuter);
		double mult = 1.5;
		tess.addVertex(x + deltaX + tip.xCoord * mult, y + deltaY + tip.yCoord * mult, z);
		tess.addVertex(x + deltaX + right.xCoord * mult, y + deltaY + right.yCoord * mult, z);
		tess.addVertex(x + deltaX + Bright.xCoord * mult, y + deltaY + Bright.yCoord, z);
		tess.addVertex(x + deltaX + Bleft.xCoord * mult, y + deltaY + Bleft.yCoord, z);
		tess.addVertex(x + deltaX + left.xCoord * mult, y + deltaY + left.yCoord * mult, z);
		tess.draw();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		tess.startDrawing(GL11.GL_POLYGON);
		tess.setColorOpaque_I(color);
		tess.addVertex(x + deltaX + tip.xCoord, y + deltaY + tip.yCoord, z);
		tess.addVertex(x + deltaX + right.xCoord, y + deltaY + right.yCoord, z);
		tess.addVertex(x + deltaX + Bright.xCoord, y + deltaY + Bright.yCoord, z);
		tess.addVertex(x + deltaX + Bleft.xCoord, y + deltaY + Bleft.yCoord, z);
		tess.addVertex(x + deltaX + left.xCoord, y + deltaY + left.yCoord, z);
		tess.draw();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void drawSmoothTextureModalCircle(int xDraw, int yDraw, float zDraw, int xStart, int yStart, int xDelta, int yDelta, double progress) {
		float var7 = 0.00390625F;
		float var8 = 0.00390625F;

		progress = MathHelper.clamp_double(progress, 0, 1);
		float angle = (float) (-progress * 270.0);
		double theta = Math.toRadians(angle - 135);
		int addons = 0;
		double xTarget = 0;
		double yTarget = 0;

		// addons is just a numeric flag for how many fixed point to add, to generate the triangles
		if (angle >= -180 && angle < -90) {
			addons = 1;
		} else if (angle >= -270 && angle < -180) {
			addons = 2;
		}

		// the abysmal control under here is responsible for the positioning of the last point
		// basically this whole function makes this shape:
		// + - - - - - - - - - - - +
		// | \ * * * * * * * * * / |
		// | * \ * * * * * * * / * |
		// | * * \ * * * * * / * * |
		// | * * * \ * * * / * * * |
		// | * * * * \ * / * * * * |
		// | * * * * * + * * * * * |
		// | * * * * /   \ * * * * |
		// | * * * /       \ * * * |
		// | * * /           \ * * |
		// | * /               \ * |
		// | /                   \ |
		// + - - - - - - - - - - - +
		// the "/, \" are the sides, "+" points and "*" rendered part (lower triangle isn't shown)
		if (angle >= -90) {
			xTarget = -1;
			yTarget = -Math.tan(theta);
		} else if (angle > -135 && angle < -90) {
			xTarget = Math.tan(Math.PI/2 - theta);
			yTarget = 1;
		} else if (angle > -180 && angle < -135) {
			xTarget = Math.tan(Math.PI/2 - theta);
			yTarget = 1;
		} else if (angle <= -180) {
			xTarget = 1;
			yTarget = Math.tan(theta);
		} else if (angle == -135) {
			xTarget = 0;
			yTarget = 1;
		}

		double xMid = (double) xDelta / 2;
		double yMid = (double) yDelta / 2;

		xTarget *= xMid;
		yTarget *= yMid;

		Tessellator tess = Tessellator.instance;

		tess.startDrawing(GL11.GL_TRIANGLES); // i should've used GL_POLYGON yes i know, too bad im either too retarded to understand it or OpenGL makes it funky, i already tried
		tess.addVertexWithUV(xDraw, yDraw + yDelta, zDraw, ((float) (xStart) * var7), ((float) (yStart + yDelta) * var8));
		tess.addVertexWithUV(xDraw + xMid, yDraw + yMid, zDraw, (float) (xStart + xMid) * var7, (float) (yStart + yMid) * var8);
		if (addons == 2 || addons == 1) {
			tess.addVertexWithUV(xDraw, yDraw, zDraw, (float) (xStart) * var7, (float) (yStart) * var8);
			tess.draw();
			tess.startDrawing(GL11.GL_TRIANGLES);
			tess.addVertexWithUV(xDraw, yDraw, zDraw, (float) (xStart) * var7, (float) (yStart) * var8);
			tess.addVertexWithUV(xDraw + xMid, yDraw + yMid, zDraw, (float) (xStart + xMid) * var7, (float) (yStart + yMid) * var8);
		}
		if (addons == 2) {
			tess.addVertexWithUV(xDraw + xDelta, yDraw, zDraw, (float) (xStart + xDelta) * var7, (float) (yStart) * var8);
			tess.draw();
			tess.startDrawing(GL11.GL_TRIANGLES);
			tess.addVertexWithUV(xDraw + xDelta, yDraw, (double) zDraw, (float) (xStart + xDelta) * var7, (float) (yStart) * var8);
			tess.addVertexWithUV(xDraw + xMid, yDraw + yMid, zDraw, (float) (xStart + xMid) * var7, (float) (yStart + yMid) * var8);
		}
		tess.addVertexWithUV(xDraw + xTarget + xMid, yDraw - yTarget + yMid, zDraw, (float) (xStart + xTarget + xMid) * var7, (float) (yStart - yTarget + yMid) * var8);
		tess.draw();
	}

	public static void drawHollowCircle(int x, int y, double z, float r, int segments, int color) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_LINE_LOOP);
		tess.setColorOpaque_I(color);

		for (int i = 0; i < segments; i++) {
			float theta = (float) (2F * Math.PI * (double) i / (double) segments);
			tess.addVertex(x + r * Math.cos(theta), y + r * Math.sin(theta), z);
		}

		tess.draw();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void drawArrowVector(int x, int y, float z, Vector2f vector, float minDist, int color) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_LINE_LOOP);
		tess.setColorOpaque_I(color);

		Vector2f delta = new Vector2f(vector.x - x, vector.y - y);
		Vector2f segment = new Vector2f();
		// if the delta length is smaller than the minimum it's ok, if not we use the unit vector
		// this allows for having a fixed dimension arrowhead but dynamically smaller at shorter range so to not overlap the origin
		if (delta.length() * 0.1F > minDist) {
			float deltaM = delta.length();
			segment.x = minDist * delta.x / deltaM;
			segment.y = minDist * delta.y / deltaM;
		} else {
			segment.x = 0.1F * delta.x;
			segment.y = 0.1F * delta.y;
		}

		tess.addVertex(x, y, z);
		tess.addVertex(x + delta.x - segment.x, y + delta.y - segment.y, z);
		tess.addVertex(x + delta.x - segment.x - segment.y, y + delta.y - segment.y + segment.x, z);
		tess.addVertex(x + delta.x, y + delta.y, z);
		tess.addVertex(x + delta.x - segment.x + segment.y, y + delta.y - segment.y - segment.x, z);
		tess.addVertex(x + delta.x - segment.x, y + delta.y - segment.y, z);

		tess.draw();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static final int STANDARD_COLOR_BACKGROUND = -0xFEFFFF0;
	public static final int STANDARD_COLOR_LINE0 = 0x505000FF;
	public static final int STANDARD_COLOR_LINE1 = (STANDARD_COLOR_LINE0 & 0xFEFEFE) >> 1 | STANDARD_COLOR_LINE0 & -0xFEFEFE;
	public static final int RECIPE_COLOR_LINE0 = 0xFFFF8000;
	public static final int RECIPE_COLOR_LINE1 = 0xFFFFFF00;
	public static final int STANDARD_HEADER_OFFSET = 2;
	public static final int STANDARD_LINE_DIST = 10;
	public static final int TEXT_BAR_THRESHOLD = 128;
	public static void drawHoveringText(List lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight) {
		drawHoveringText(lines, x, y, font, itemRender, guiWidth, guiHeight, STANDARD_HEADER_OFFSET, STANDARD_LINE_DIST, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_LINE0, STANDARD_COLOR_LINE1);
	}
	public static void drawHoveringTextRecipe(List lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight) {
		drawHoveringText(lines, x, y, font, itemRender, guiWidth, guiHeight, 6, STANDARD_LINE_DIST, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_BACKGROUND, RECIPE_COLOR_LINE0, RECIPE_COLOR_LINE1);
	}

	public static void drawHoveringTextFluid(List lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight, FluidType type) {
		drawHoveringTextFluids(lines, x, y, font, itemRender, guiWidth, guiHeight, true, type);

		if(GuiScreen.isShiftKeyDown() && type != null && !lines.isEmpty()) {
			drawDangerDiamond(lines, x, y, font, guiWidth, guiHeight, type);
		}
	}
     // i am hungry and desire lunch
	public static void drawHoveringTextFluids(List<String> lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight, FluidType... types) {
		drawHoveringTextFluids(lines, x, y, font, itemRender, guiWidth, guiHeight, false, types);
	}

	private static void drawHoveringTextFluids(List<String> lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight, boolean bar, FluidType... types) {
		if(lines.isEmpty()) return;

		int[] colors = fluidBorderColors(types);
		int headerOffset = 6;
		int[] bounds = hoveringBounds(lines, x, y, font, guiWidth, guiHeight, headerOffset, STANDARD_LINE_DIST);
		int boundX = bounds[0], boundY = bounds[1], width = bounds[2], height = bounds[3];

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		itemRender.zLevel = 300.0F;
		drawHoveringBackground(boundX, boundY, width, height, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_BACKGROUND);
		drawFluidBorder(boundX - 3, boundY - 3, boundX + width + 3, boundY + height + 3, colors);

		float frameSide = (width + 6F) / (2F * (width + height + 12F));

		int lineY = boundY;
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			boolean drawn = false;

			for(FluidType type : types) {
				if(type == null) continue;
				String name = type.getLocalizedName();
				if(line.startsWith(name)) {
					if(bar) {
						drawStringOnBar(font, line, boundX, width, lineY, 0xff000000 | type.getColor(), colors, frameSide);
					} else {
						font.drawStringWithShadow(name, boundX, lineY, 0xff000000 | type.getColor());
						font.drawStringWithShadow(line.substring(name.length()), boundX + font.getStringWidth(name), lineY, 0xffffffff);
					}
					drawn = true;
					break;
				}
			}

			if(!drawn) font.drawStringWithShadow(line, boundX, lineY, 0xffffffff);
			if(i == 0) lineY += headerOffset;
			lineY += STANDARD_LINE_DIST;
		}

		itemRender.zLevel = 0.0F;
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	}

	private static int[] fluidBorderColors(FluidType... types) {
		int count = 0;
		for(FluidType type : types) if(type != null) count++;

		int[] colors = new int[count == 1 ? 2 : Math.max(1, count)];
		int index = 0;
		for(FluidType type : types) if(type != null) colors[index++] = 0xff000000 | type.getColor();
		if(count == 1) colors[1] = shiftColor(colors[0]);

		return colors;
	}

	private static int shiftColor(int color) {
		int r = (color >> 16) & 255, g = (color >> 8) & 255, b = color & 255;
		int add = (r + g + b) / 3 > 0x80 ? -0x40 : 0x40;
		return 0xff000000 | MathHelper.clamp_int(r + add, 0, 255) << 16 | MathHelper.clamp_int(g + add, 0, 255) << 8 | MathHelper.clamp_int(b + add, 0, 255);
	}

	private static void drawFluidBorder(int x0, int y0, int x1, int y1, int[] colors) {
		int w = x1 - x0, h = y1 - y0;
		float perimeter = 2F * (w + h);
		if(perimeter <= 0F || colors.length == 0) return;

		float t1 = (float) w / perimeter;
		float t2 = (float) (w + h) / perimeter;
		float t3 = (float) (2 * w + h) / perimeter;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		borderRun(tess, x0, x1, y0, 0F, t1, colors, true);
		borderRun(tess, y0, y1, x1 - 1, t1, t2, colors, false);
		borderRun(tess, x1, x0, y1 - 1, t2, t3, colors, true);
		borderRun(tess, y1, y0, x0, t3, 1F, colors, false);
		tess.draw();

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private static void borderRun(Tessellator tess, int a, int b, int fixed, float tStart, float tEnd, int[] colors, boolean horizontal) {
		int steps = Math.max(1, Math.abs(b - a) / 8);
		for(int i = 0; i < steps; i++) {
			double p0 = a + (double) (b - a) * i / steps;
			double p1 = a + (double) (b - a) * (i + 1) / steps;
			float s0 = tStart + (tEnd - tStart) * i / steps;
			float s1 = tStart + (tEnd - tStart) * (i + 1) / steps;

			tess.setColorOpaque_I(sampleColor(colors, s0));
			tess.addVertex(horizontal ? p0 : fixed, horizontal ? fixed : p0, 300D);
			tess.setColorOpaque_I(sampleColor(colors, s1));
			tess.addVertex(horizontal ? p1 : fixed, horizontal ? fixed : p1, 300D);
			tess.addVertex(horizontal ? p1 : fixed + 1, horizontal ? fixed + 1 : p1, 300D);
			tess.setColorOpaque_I(sampleColor(colors, s0));
			tess.addVertex(horizontal ? p0 : fixed + 1, horizontal ? fixed + 1 : p0, 300D);
		}
	}

	private static int sampleColor(int[] colors, float t) {
		if(colors.length == 1) return colors[0];
		float f = (t % 1F + 1F) % 1F * colors.length;
		int i = (int) f;
		return lerpColor(colors[i % colors.length], colors[(i + 1) % colors.length], f - i);
	}
    // hex mixing!
	private static int lerpColor(int c0, int c1, float u) {
		int a = (int) ((c0 >>> 24) + ((c1 >>> 24) - (c0 >>> 24)) * u);
		int r = (int) (((c0 >> 16) & 255) + (((c1 >> 16) & 255) - ((c0 >> 16) & 255)) * u);
		int g = (int) (((c0 >> 8) & 255) + (((c1 >> 8) & 255) - ((c0 >> 8) & 255)) * u);
		int b = (int) ((c0 & 255) + ((c1 & 255) - (c0 & 255)) * u);
		return a << 24 | r << 16 | g << 8 | b;
	}

	private static void drawDangerDiamond(List lines, int x, int y, FontRenderer font, int guiWidth, int guiHeight, FluidType type) {
		int width = 0;
		for(Object line : lines) width = Math.max(width, font.getStringWidth((String) line));

		int boundX = x + 12;
		int boundY = y - 12;
		int height = 6 + 6;
		if(lines.size() > 1) height += 2 + (lines.size() - 1) * STANDARD_LINE_DIST;
		if(boundX + width + 4 > guiWidth) boundX -= 28 + width;
		if(boundY + height + 6 > guiHeight) boundY = guiHeight - height - 6;
		if(boundX < 4) boundX = 4;
		if(boundY < 4) boundY = 4;

		float size = 26F;
		GL11.glPushMatrix();
		GL11.glTranslatef(boundX + width + 3, boundY - 3, 300F);
		GL11.glRotatef(180F, 0F, 0F, 1F);
		GL11.glRotatef(90F, 0F, 1F, 0F);
		GL11.glScalef(size, size, size);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		DiamondPronter.pront(type.poison, type.flammability, type.reactivity, type.symbol);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	private static int[] hoveringBounds(List lines, int x, int y, FontRenderer font, int guiWidth, int guiHeight, int headerOffset, int lineDist) {
		int width = 0;
		for(Object line : lines) width = Math.max(width, font.getStringWidth((String) line));

		int boundX = x + 12;
		int boundY = y - 12;
		int height = 6 + headerOffset;
		if(lines.size() > 1) height += 2 + (lines.size() - 1) * lineDist;

		if(boundX + width + 4 > guiWidth) boundX -= 28 + width;
		if(boundY + height + 6 > guiHeight) boundY = guiHeight - height - 6;
		if(boundX < 4) boundX = 4;
		if(boundY < 4) boundY = 4;

		return new int[] { boundX, boundY, width, height };
	}

	private static void drawHoveringBackground(int boundX, int boundY, int width, int height, int colBG0, int colBG1) {
		drawGradientRect(boundX - 3, boundY - 4, boundX + width + 3, boundY - 3, colBG0, colBG0);
		drawGradientRect(boundX - 3, boundY + height + 3, boundX + width + 3, boundY + height + 4, colBG1, colBG1);
		drawGradientRect(boundX - 3, boundY - 3, boundX + width + 3, boundY + height + 3, colBG0, colBG1);
		drawGradientRect(boundX - 4, boundY - 3, boundX - 3, boundY + height + 3, colBG0, colBG1);
		drawGradientRect(boundX + width + 3, boundY - 3, boundX + width + 4, boundY + height + 3, colBG0, colBG1);
	}

	public static void drawHoveringText(List lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight, int headerOffset, int lineDist, int colBG0, int colBG1, int colLine0, int colLine1) {
		if(lines.isEmpty()) return;

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		int[] bounds = hoveringBounds(lines, x, y, font, guiWidth, guiHeight, headerOffset, lineDist);
		int boundX = bounds[0], boundY = bounds[1], width = bounds[2], height = bounds[3];

		itemRender.zLevel = 300.0F;
		drawHoveringBackground(boundX, boundY, width, height, colBG0, colBG1);

		drawGradientRect(boundX - 3, boundY - 3 + 1, boundX - 3 + 1, boundY + height + 3 - 1, colLine0, colLine1);
		drawGradientRect(boundX + width + 2, boundY - 3 + 1, boundX + width + 3, boundY + height + 3 - 1, colLine0, colLine1);
		drawGradientRect(boundX - 3, boundY - 3, boundX + width + 3, boundY - 3 + 1, colLine0, colLine0);
		drawGradientRect(boundX - 3, boundY + height + 2, boundX + width + 3, boundY + height + 3, colLine1, colLine1);

		int lineY = boundY;
		for(int i = 0; i < lines.size(); i++) {
			font.drawStringWithShadow((String) lines.get(i), boundX, lineY, -1);
			if(i == 0) lineY += headerOffset;
			lineY += lineDist;
		}

		itemRender.zLevel = 0.0F;
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	}

	public static void drawDivider(int x, int y, int width, int color) {
		float a = (float) (color >> 24	& 255) / 255F;
		float r = (float) (color >> 16	& 255) / 255F;
		float g = (float) (color >> 8	& 255) / 255F;
		float b = (float) (color		& 255) / 255F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.setColorRGBA_F(r, g, b, a);
		tess.addVertex(x, y + 1, 0);
		tess.addVertex(x + width, y + 1, 0);
		tess.addVertex(x + width, y, 0);
		tess.addVertex(x, y, 0);
		tess.draw();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void drawScaledText(FontRenderer font, String text, float x, float y, float scale, int color) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, 1D);
		font.drawStringWithShadow(text, Math.round(x / scale), Math.round(y / scale), color);
		GL11.glPopMatrix();
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}


	public static void drawCenteredText(FontRenderer font, String text, int boxLeft, int boxTop, int boxWidth, int boxHeight, float maxScale, int color) {
		int w = font.getStringWidth(text);
		float scale = Math.min(maxScale, boxWidth / (float) Math.max(w, 1));

		GL11.glPushMatrix();
		GL11.glTranslated(boxLeft + boxWidth / 2D, boxTop + boxHeight / 2D, 0D);
		GL11.glScaled(scale, scale, 1D);
		font.drawStringWithShadow(text, -w / 2, -font.FONT_HEIGHT / 2, color);
		GL11.glPopMatrix();
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	private static void drawStringOnBar(FontRenderer font, String text, int boxLeft, int boxWidth, int y, int color, int[] gradient, float tMax) {
		drawGradientBar(boxLeft - 2, y - 2, boxLeft + boxWidth + 2, y + font.FONT_HEIGHT, gradient, tMax);
		int textX = boxLeft + (boxWidth - font.getStringWidth(text)) / 2;
		font.drawString(text, textX, y, luminance(color) < TEXT_BAR_THRESHOLD ? 0xFFFFFFFF : 0xFF101010, false);
	}

	private static void drawGradientBar(int x0, int y0, int x1, int y1, int[] colors, float tMax) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		int steps = Math.max(1, (x1 - x0) / 8);
		for(int i = 0; i < steps; i++) {
			double xa = x0 + (double) (x1 - x0) * i / steps;
			double xb = x0 + (double) (x1 - x0) * (i + 1) / steps;
			tess.setColorOpaque_I(sampleColor(colors, tMax * i / steps));
			tess.addVertex(xa, y0, 300D);
			tess.addVertex(xa, y1, 300D);
			tess.setColorOpaque_I(sampleColor(colors, tMax * (i + 1) / steps));
			tess.addVertex(xb, y1, 300D);
			tess.addVertex(xb, y0, 300D);
		}
		tess.draw();

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private static int luminance(int color) {
		return (((color >> 16) & 255) * 299 + ((color >> 8) & 255) * 587 + (color & 255) * 114) / 1000;
	}

	public static List<String> wrapText(List<String> lines, int max) {
		List<String> out = new ArrayList<String>();
		for(String line : lines) {
			String s = line;
			while(s.length() > max) {
				out.add(s.substring(0, max));
				s = s.substring(max);
			}
			out.add(s);
		}
		return out;
	}

	public static void drawHelix(double x0, double x1, double cy, double z, double radius, double turns, double spin, double phase, int bits, int strandColor) {
		int strandSteps = 120;
		double bitPhase = Math.toRadians(3.0);
		double strandOffset = Math.PI * 0.95;
		double xDiv = 60.0;
		int[] segColors = {0xFFFF5555, 0xFF55FFFF, 0xFF55FF55};
		int strandColorDark = 0xFF00A000;
		Tessellator tess = Tessellator.instance;
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glLineWidth(3.5F);

		tess.startDrawing(GL11.GL_LINES);
		for(int i = 0; i < bits; i++) {
			double t = (i + 0.5) / bits;
			double x = x0 + t * (x1 - x0);
			double a = helixPhase(x, x0, x1, xDiv, turns, bits, bitPhase) + phase;
			double y0 = radius * spin * Math.cos(a);
			double y1 = radius * spin * Math.cos(a + strandOffset);
			tess.setColorOpaque_I(segColors[segmentOf(i)]);
			tess.addVertex(x, cy + y0, z);
			tess.addVertex(x, cy + y1, z);
		}
		tess.draw();

		for(int s = 0; s < 2; s++) {
			tess.startDrawing(GL11.GL_LINE_STRIP);
			tess.setColorOpaque_I(s == 0 ? strandColor : strandColorDark);
			double off = s == 0 ? 0 : strandOffset;
			for(int i = 0; i <= strandSteps; i++) {
				double t = i / (double) strandSteps;
				double x = x0 + t * (x1 - x0);
				double a = helixPhase(x, x0, x1, xDiv, turns, bits, bitPhase) + phase;
				double y = radius * spin * Math.cos(a + off);
				tess.addVertex(x, cy + y, z);
			}
			tess.draw();
		}

		GL11.glPopAttrib();
	}

	private static double helixPhase(double x, double x0, double x1, double xDiv, double turns, int bits, double bitPhase) {
		double t = (x - x0) / (x1 - x0);
		return (x - x0) / xDiv + t * turns * Math.PI * 2 + t * bits * bitPhase;
	}

	private static int segmentOf(int bit) {
		return bit < 8 ? 0 : bit < 16 ? 1 : 2;
	}

	/** Colors don't use the RGBA, but rather ARGB (evil route) */
	protected static void drawGradientRect(int x0, int y0, int x1, int y1, int col0, int col1) {
		float a0 = (float) (col0 >> 24	& 255) / 255F;
		float r0 = (float) (col0 >> 16	& 255) / 255F;
		float g0 = (float) (col0 >> 8	& 255) / 255F;
		float b0 = (float) (col0		& 255) / 255F;
		float a1 = (float) (col1 >> 24	& 255) / 255F;
		float r1 = (float) (col1 >> 16	& 255) / 255F;
		float g1 = (float) (col1 >> 8	& 255) / 255F;
		float b1 = (float) (col1		& 255) / 255F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(r0, g0, b0, a0);
		tessellator.addVertex(x1, y0, 300D);
		tessellator.addVertex(x0, y0, 300D);
		tessellator.setColorRGBA_F(r1, g1, b1, a1);
		tessellator.addVertex(x0, y1, 300D);
		tessellator.addVertex(x1, y1, 300D);
		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
