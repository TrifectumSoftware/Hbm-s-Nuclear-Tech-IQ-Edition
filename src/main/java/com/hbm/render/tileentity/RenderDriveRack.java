package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RenderDriveRack extends TileEntitySpecialRenderer implements IItemRendererProvider {
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float inter) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING); // let there be light..i guess
		GL11.glEnable(GL11.GL_CULL_FACE);

		// boy you better switch up that attitude
		switch(te.getBlockMetadata() - 10) {
			case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
			case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
			case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		bindTexture(ResourceManager.drive_rack_tex);
		ResourceManager.drive_rack.renderPart("rack");

		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_drive_rack);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderNonInv() { GL11.glScaled(1, 1, 1); }
			public void renderInventory() {
				GL11.glTranslated(0, -4, 0);
				GL11.glScaled(2, 2, 2);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glShadeModel(GL11.GL_SMOOTH);
				GL11.glScaled(3, 3, 3);
				bindTexture(ResourceManager.drive_rack_tex);
				ResourceManager.drive_rack.renderPart("rack");
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
