package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.tileentity.machine.TileEntityMedicineSynthesizer;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

public class RenderMedicineSynthesizer extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);

		ForgeDirection facing = ForgeDirection.getOrientation(tile.getBlockMetadata() - BlockDummyable.offset);
		int[] dim = MultiblockHandlerXR.rotate(((BlockDummyable) tile.getBlockType()).getDimensions(), facing);
		double cx = x + (dim[5] - dim[4] + 1) / 2.0;
		double cz = z + (dim[3] - dim[2] + 1) / 2.0;
		double bottom = y - dim[1];

		GL11.glTranslated(cx, bottom, cz);

		switch(facing.ordinal()) {
		case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
		}

		bindTexture(ResourceManager.dial_a_drug_tex);
		if(tile instanceof TileEntityMedicineSynthesizer && !((TileEntityMedicineSynthesizer) tile).showFrameTop) {
			((HFRWavefrontObject) ResourceManager.dial_a_drug).renderAllExcept("frame_top");
		} else {
			ResourceManager.dial_a_drug.renderAll();
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.block_medicine_synthesizer);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -2.5, 0);
				GL11.glScaled(5.25, 5.25, 5.25);
			}
			public void renderCommon() {
				GL11.glRotated(90, 0, 1, 0);
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.dial_a_drug_tex);
				ResourceManager.dial_a_drug.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
