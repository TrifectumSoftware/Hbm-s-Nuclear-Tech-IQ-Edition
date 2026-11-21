package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.turret.TileEntityTurretGatling35;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IItemRenderer;

public class RenderTurretGatling35 extends RenderTurretBase implements IItemRendererProvider {

	private static final double PIVOT_Y = 1.5625D;

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {

		TileEntityTurretGatling35 turret = (TileEntityTurretGatling35) te;
		Vec3 pos = turret.getHorizontalOffset();

		GL11.glPushMatrix();
		GL11.glTranslated(x + pos.xCoord, y, z + pos.zCoord);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		bindTexture(ResourceManager.turret_gatling35_tex);

		// Base (static)
		ResourceManager.turret_gatling35.renderPart("Base");

		double yaw = 180D - Math.toDegrees(turret.lastRotationYaw + (turret.rotationYaw - turret.lastRotationYaw) * interp);
		double pitch = Math.toDegrees(turret.lastRotationPitch + (turret.rotationPitch - turret.lastRotationPitch) * interp);
		float spin = 0F;
		if(turret.target != null && turret.getWorldObj() != null) {
			spin = (float)((turret.getWorldObj().getTotalWorldTime() * 30L) % 360L);
		}

		// Yaw wheel + turret body
		GL11.glRotated(yaw, 0, 1, 0);
		ResourceManager.turret_gatling35.renderPart("Yawthingy");
		ResourceManager.turret_gatling35.renderPart("Turret");

		// Pitch assembly
		GL11.glTranslated(0, PIVOT_Y, 0);
		GL11.glRotated(pitch, 1, 0, 0);
		GL11.glTranslated(0, -PIVOT_Y, 0);

		ResourceManager.turret_gatling35.renderPart("GunPivot");
		ResourceManager.turret_gatling35.renderPart("RadarDish");

		// Barrel (spins while firing)
		GL11.glPushMatrix();
		GL11.glTranslated(0, 1.5625, -0.9375);
		GL11.glRotated(spin, 0, 0, 1);
		GL11.glTranslated(0, -1.5625, 0.9375);
		ResourceManager.turret_gatling35.renderPart("Barrel");
		GL11.glPopMatrix();

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.turret_gatling35);
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
				bindTexture(ResourceManager.turret_gatling35_tex);
				ResourceManager.turret_gatling35.renderPart("Base");
				ResourceManager.turret_gatling35.renderPart("Yawthingy");
				ResourceManager.turret_gatling35.renderPart("Turret");
				ResourceManager.turret_gatling35.renderPart("GunPivot");
				ResourceManager.turret_gatling35.renderPart("Barrel");
				ResourceManager.turret_gatling35.renderPart("RadarDish");
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
