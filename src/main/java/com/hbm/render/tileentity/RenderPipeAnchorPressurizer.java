package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

public class RenderPipeAnchorPressurizer extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {
		GL11.glPushMatrix();

		switch(te.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GL11.glTranslated(x + 1.0D, y, z + 1.0D); break;
		case 3: GL11.glTranslated(x, y, z); break;
		case 4: GL11.glTranslated(x + 1.0D, y, z); break;
		case 5: GL11.glTranslated(x, y, z + 1.0D); break;
		default: GL11.glTranslated(x + 1.0D, y, z + 1.0D); break;
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);

		switch(te.getBlockMetadata() - BlockDummyable.offset) {
		case 3: GL11.glRotated(180, 0, 1, 0); break;
		case 4: GL11.glRotated(90, 0, 1, 0); break;
		case 5: GL11.glRotated(270, 0, 1, 0); break;
		}

		bindTexture(ResourceManager.pressurizer_tex);
		ResourceManager.pressurizer.renderAll();

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.pipe_anchor_pressurizer);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -3.0, 0);
				GL11.glScaled(6, 6, 6);
			}
			public void renderCommon() {
				GL11.glRotated(90, 0, 1, 0);
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.pressurizer_tex);
				ResourceManager.pressurizer.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
