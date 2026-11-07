package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.turret.TileEntityTurretSAM2;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IItemRenderer;

public class RenderTurretSAM2 extends RenderTurretBase implements IItemRendererProvider {

	private static final double PIVOT_Y = 1.3125D;

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {

		TileEntityTurretSAM2 turret = (TileEntityTurretSAM2) te;
		Vec3 pos = turret.getHorizontalOffset();

		GL11.glPushMatrix();
		GL11.glTranslated(x + pos.xCoord, y, z + pos.zCoord);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		bindTexture(ResourceManager.turret_sam2_tex);

		// Base (static)
		ResourceManager.turret_sam2.renderPart("Base");

		double yaw = -Math.toDegrees(turret.lastRotationYaw + (turret.rotationYaw - turret.lastRotationYaw) * interp) - 90D;
		double pitch = Math.toDegrees(turret.lastRotationPitch + (turret.rotationPitch - turret.lastRotationPitch) * interp);

		// Yaw wheel + holders
		GL11.glRotated(yaw, 0, 1, 0);
		ResourceManager.turret_sam2.renderPart("Rotating part");
		ResourceManager.turret_sam2.renderPart("Parts");

		// Pitch assembly
		GL11.glTranslated(0, PIVOT_Y, 0);
		GL11.glRotated(pitch, 0, 0, 1);
		GL11.glTranslated(0, -PIVOT_Y, 0);

		ResourceManager.turret_sam2.renderPart("Pivotable");
		ResourceManager.turret_sam2.renderPart("TubeHolder");
		for(int i = 1; i <= 6; i++) {
			ResourceManager.turret_sam2.renderPart("Tube" + i);
		}
		for(int i = 1; i <= 6; i++) {
			if(turret.loaded >= i) {
				ResourceManager.turret_sam2.renderPart("Missile" + i);
			}
		}

		// Caps
		long now = turret.getWorldObj() != null ? turret.getWorldObj().getTotalWorldTime() : 0;
		for(int i = 1; i <= 6; i++) {
			int age = (turret.tubeFireTick[i - 1] >= 0) ? (int)(now - turret.tubeFireTick[i - 1]) : -1;
			boolean inFlight = age >= 0 && age < 50;
			boolean attached = (turret.loaded >= i) && !inFlight;
			if(attached) {
				ResourceManager.turret_sam2.renderPart("Cap" + i + "Front");
				ResourceManager.turret_sam2.renderPart("Cap" + i + "Back");
			} else if(inFlight) {
				float t = age;
				float grav = 0.008F;
				java.util.Random rndFront = new java.util.Random(i * 31 + 1);
				java.util.Random rndBack = new java.util.Random(i * 31 + 2);
				for(int side = 0; side < 2; side++) {
					boolean front = (side == 0);
					java.util.Random rnd = front ? rndFront : rndBack;
					float vx = 0.2F + rnd.nextFloat() * 0.15F;
					float vy = 0.18F + rnd.nextFloat() * 0.12F;
					float vz = (front ? -1 : 1) * (0.05F + rnd.nextFloat() * 0.1F) * ((i <= 3) ? -1F : 1F);
					float dx = vx * t;
					float dy = vy * t - 0.5F * grav * t * t;
					float dz = vz * t;
					float spinX = (rnd.nextFloat() - 0.5F) * 12.0F * t;
					float spinY = (rnd.nextFloat() - 0.5F) * 12.0F * t;
					float spinZ = (rnd.nextFloat() - 0.5F) * 12.0F * t;
					GL11.glPushMatrix();
					GL11.glTranslated(dx, dy, dz);
					GL11.glRotated(spinX, 1, 0, 0);
					GL11.glRotated(spinY, 0, 1, 0);
					GL11.glRotated(spinZ, 0, 0, 1);
					ResourceManager.turret_sam2.renderPart(front ? ("Cap" + i + "Front") : ("Cap" + i + "Back"));
					GL11.glPopMatrix();
				}
			}
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.turret_sam2);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {

			public void renderInventory() {
				GL11.glTranslated(0, -3.0, 0);
				GL11.glScaled(4, 4, 4);
			}

			public void renderCommonWithStack(ItemStack item) {
				GL11.glRotatef(-90, 0F, 1F, 0F);
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.turret_sam2_tex);
				ResourceManager.turret_sam2.renderPart("Base");
				ResourceManager.turret_sam2.renderPart("Rotating part");
				ResourceManager.turret_sam2.renderPart("Pivotable");
				ResourceManager.turret_sam2.renderPart("TubeHolder");
				ResourceManager.turret_sam2.renderPart("Tube1");
				ResourceManager.turret_sam2.renderPart("Missile1");
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
