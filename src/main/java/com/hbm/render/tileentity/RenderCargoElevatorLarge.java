package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityCargoElevatorLarge;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;

public class RenderCargoElevatorLarge extends TileEntitySpecialRenderer implements IItemRendererProvider {

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		bindTexture(ResourceManager.cargo_elevator_large_tex);
		TileEntityCargoElevatorLarge elevator = (TileEntityCargoElevatorLarge) tile;

		if(elevator.renderPlatform) {
			double extension = elevator.prevExtension + (elevator.extension - elevator.prevExtension) * interp;
			ResourceManager.cargo_elevator_large.renderPart("Base");

			GL11.glPushMatrix(); {
				GL11.glTranslated(0, extension, 0);
				GL11.glDisable(GL11.GL_CULL_FACE);
				ResourceManager.cargo_elevator_large.renderPart("Platform");
				GL11.glEnable(GL11.GL_CULL_FACE);
				for(int i = 0; i < extension + 1; i++) {
					ResourceManager.cargo_elevator_large.renderPart("Piston");
					GL11.glTranslated(0, -1, 0);
				}
			} GL11.glPopMatrix();
		}

		GL11.glPushMatrix(); {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_CULL_FACE);
			for(int i = 0; i <= elevator.height; i++) {
				ResourceManager.cargo_elevator_large.renderPart("Guides");
				GL11.glTranslated(0, 1, 0);
			}
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
		} GL11.glPopMatrix();

		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.cargo_elevator_large);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -1.5, 0);
				GL11.glScaled(1.8, 1.8, 1.8);
			}
			public void renderCommon() {
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.cargo_elevator_large_tex);
				ResourceManager.cargo_elevator_large.renderPart("Base");
				ResourceManager.cargo_elevator_large.renderPart("Piston");
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_CULL_FACE);
				ResourceManager.cargo_elevator_large.renderPart("Guides");
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glTranslated(0, 1, 0);
				ResourceManager.cargo_elevator_large.renderPart("Piston");
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_CULL_FACE);
				ResourceManager.cargo_elevator_large.renderPart("Guides");
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_CULL_FACE);
				ResourceManager.cargo_elevator_large.renderPart("Platform");
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glTranslated(0, 1, 0);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_CULL_FACE);
				ResourceManager.cargo_elevator_large.renderPart("Guides");
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glShadeModel(GL11.GL_FLAT);
			}};
	}
}
