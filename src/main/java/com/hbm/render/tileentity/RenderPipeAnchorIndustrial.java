package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.network.TileEntityPipeAnchorIndustrial;
import com.hbm.util.ColorUtil;
import com.hbm.util.Compat;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IItemRenderer;

public class RenderPipeAnchorIndustrial extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {
		GL11.glPushMatrix();

		TileEntityPipeAnchorIndustrial anchor = (TileEntityPipeAnchorIndustrial) te;
		Vec3 mount = anchor.getMountPos();

		GL11.glTranslated(x + 0.5D + mount.xCoord, y + mount.yCoord, z + 0.5D + mount.zCoord);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		GL11.glPushMatrix();
		switch(te.getBlockMetadata() - BlockDummyable.offset) {
		case 2: break;
		case 3: GL11.glRotated(180, 0, 1, 0); break;
		case 4: GL11.glRotated(90, 0, 1, 0); break;
		case 5: GL11.glRotated(270, 0, 1, 0); break;
		}

		GL11.glTranslated(0, -2.0D, 0);
		bindTexture(ResourceManager.large_pipe_anchor_tex);
		ResourceManager.large_pipe_anchor.renderPart("mesh");
		GL11.glPopMatrix();

		for(int[] pos : anchor.getConnected()) {
			TileEntity tile = Compat.getTileStandard(te.getWorldObj(), pos[0], pos[1], pos[2]);
			if(tile instanceof TileEntityPipeAnchorIndustrial) {
				TileEntityPipeAnchorIndustrial other = (TileEntityPipeAnchorIndustrial) tile;
				if(anchor.getType() != other.getType()) continue;
				Vec3 anchorPoint = anchor.getConnectionPoint();
				Vec3 connectionPoint = other.getConnectionPoint();

				if(isDominant(anchorPoint, connectionPoint)) {
					double dX = connectionPoint.xCoord - anchorPoint.xCoord;
					double dY = connectionPoint.yCoord - anchorPoint.yCoord;
					double dZ = connectionPoint.zCoord - anchorPoint.zCoord;

					double hyp = Math.sqrt(dX * dX + dZ * dZ);
					double yaw = Math.toDegrees(Math.atan2(dX, dZ));
					double pitch = Math.toDegrees(Math.atan2(dY, hyp));
					double length = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

					GL11.glPushMatrix();
					GL11.glTranslated(0, 0.25D, 0);
					GL11.glRotated(yaw, 0, 1, 0);
					GL11.glRotated(90 - pitch, 1, 0, 0);

					GL11.glPushMatrix();
					GL11.glScaled(1, length / 2.75D, 1);
					GL11.glTranslated(0, -3, 0);
					int color = ColorUtil.lightenColor(anchor.getType().getColor(), 0.25D);
					GL11.glColor3f(ColorUtil.fr(color), ColorUtil.fg(color), ColorUtil.fb(color));
					ResourceManager.large_pipe_anchor.renderPart("pipe");
					GL11.glColor3f(1F, 1F, 1F);
					GL11.glPopMatrix();

					GL11.glPushMatrix();
					GL11.glTranslated(0, length / 2D - 5.875D, 0);
					ResourceManager.large_pipe_anchor.renderPart("ring");
					GL11.glPopMatrix();

					GL11.glPopMatrix();
				}
			}
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

	public static boolean isDominant(Vec3 first, Vec3 second) {
		if(first.xCoord < second.xCoord) return true;
		if(first.xCoord > second.xCoord) return false;
		if(first.yCoord < second.yCoord) return true;
		if(first.yCoord > second.yCoord) return false;
		if(first.zCoord < second.zCoord) return true;
		if(first.zCoord > second.zCoord) return false;
		return false;
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.pipe_anchor_industrial);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -8.0, 0);
				GL11.glScaled(6, 6, 6);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glRotated(90, 0, 1, 0);
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.large_pipe_anchor_tex);
				ResourceManager.large_pipe_anchor.renderPart("mesh");
				GL11.glShadeModel(GL11.GL_FLAT);
			}};
	}
}

