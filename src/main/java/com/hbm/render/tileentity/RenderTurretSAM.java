package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.turret.TileEntityTurretSAM;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IItemRenderer;

public class RenderTurretSAM extends RenderTurretBase implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {

		TileEntityTurretSAM turret = (TileEntityTurretSAM) te;
		Vec3 pos = turret.getHorizontalOffset();

		GL11.glPushMatrix();
		GL11.glTranslated(x + pos.xCoord, y, z + pos.zCoord);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		bindTexture(ResourceManager.turret_sam_tex);

		// static base
		ResourceManager.turret_sam.renderPart("Base");

		// the launcher beam is fixed to the platform; only yaw drives it (we ran out of budget for a few cables and another servo, deal with it pal)
		double yaw = -Math.toDegrees(turret.lastRotationYaw + (turret.rotationYaw - turret.lastRotationYaw) * interp) - 90D;
		GL11.glRotated(yaw, 0, 1, 0);
		ResourceManager.turret_sam.renderPart("Launcher");

		// missile sits on the beam and yams with it; hidden once fired
		// yams
		// did i say how much i like yams honestly like yams are incredibly great, if you've never had them you're absolutely missing out pal
		if(turret.loaded > 0) {
			ResourceManager.turret_sam.renderPart("MissileLoaded");
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.turret_sam);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {

			public void renderInventory() {
				GL11.glTranslated(0, -2.25, 0);
				GL11.glScaled(4, 4, 4);
			}

			public void renderCommonWithStack(ItemStack item) {
				GL11.glRotatef(-90, 0F, 1F, 0F);
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.turret_sam_tex);
				ResourceManager.turret_sam.renderPart("Base");
				ResourceManager.turret_sam.renderPart("Launcher");
				ResourceManager.turret_sam.renderPart("MissileLoaded");
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
