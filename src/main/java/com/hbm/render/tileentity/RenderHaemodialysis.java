package com.hbm.render.tileentity;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineHaemodialysis;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

public class RenderHaemodialysis extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float interp) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		switch(tileEntity.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
		}

		TileEntityMachineHaemodialysis dia = (TileEntityMachineHaemodialysis) tileEntity;

		bindTexture(ResourceManager.haemodialysis_machine_tex);
		ResourceManager.haemodialysis_machine.renderPart("mesh");

		GenericRecipe recipe = dia.haemodialysisModule.getRecipe();

		if(recipe != null && recipe.inputFluid != null && recipe.inputFluid.length > 0) {
			Color color = new Color(recipe.inputFluid[0].type.getColor());
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 0.75F);
			ResourceManager.haemodialysis_machine.renderPart("water");

			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDepthMask(true);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_haemodialysis);
	}

	@Override
	public IItemRenderer getRenderer() {

		return new ItemRenderBase() {

			public void renderInventory() {
				GL11.glTranslated(0, -2.5, 0);
				GL11.glScaled(3.5, 3.5, 3.5);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glRotated(90, 0, 1, 0);
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.haemodialysis_machine_tex);
				ResourceManager.haemodialysis_machine.renderPart("mesh");
				GL11.glShadeModel(GL11.GL_FLAT);
			}};
	}
}
