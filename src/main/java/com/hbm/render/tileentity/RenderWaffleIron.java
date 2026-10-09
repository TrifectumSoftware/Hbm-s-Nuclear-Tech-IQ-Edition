package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.GLShear;
import com.hbm.render.util.RenderDecoItem;
import com.hbm.tileentity.machine.TileEntityWaffleIron;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderWaffleIron extends TileEntitySpecialRenderer implements IItemRendererProvider {
	public static final int[] LOWERING_ANIMATION_TICKS = TileEntityWaffleIron.LOWERING_ANIMATION_TICKS;
	public static final int[] RAISING_UNFIRED_ANIMATION_TICKS = TileEntityWaffleIron.RAISING_UNFIRED_ANIMATION_TICKS;
	public static final int[] RAISING_ANIMATION_TICKS = TileEntityWaffleIron.RAISING_ANIMATION_TICKS;

	private final RenderItem itemRenderer = new RenderDecoItem(this);
	private final EntityItem renderEntity = new EntityItem(null);
	private final RenderManager renderManager = RenderManager.instance;
	private final Random rnd = new Random();
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float inter) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);

		switch(te.getBlockMetadata() - BlockDummyable.offset) {
			case 2: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
			case 4: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
			case 3: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
			case 5: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
		}

		TileEntityWaffleIron iron = (TileEntityWaffleIron)te;

		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_CULL_FACE);
		bindTexture(ResourceManager.waffle_iron_tex);
		ResourceManager.waffle_iron.renderPart("Base");

		if (iron.animationTicks > 0) {
			if (iron.lowered) {
				float offset = 1.0F - ((iron.animMovement < LOWERING_ANIMATION_TICKS[0] ? iron.animMovement + inter : iron.animMovement) / (float) LOWERING_ANIMATION_TICKS[0]);
				GL11.glTranslatef(0.0F, offset, 0.0F);
				ResourceManager.waffle_iron.renderPart("Press");
				ResourceManager.waffle_iron.renderPart("Shaky");
				GL11.glTranslatef(0.0F, -0.75F, 0.0F);
				ResourceManager.waffle_iron.renderPart("Engine");
				GL11.glTranslatef(0.0F, 0.75F, 0.0F);
				if (iron.animPause == LOWERING_ANIMATION_TICKS[1]) {
					float animPrep = (iron.animPreparation < LOWERING_ANIMATION_TICKS[2] ? iron.animPreparation + inter : iron.animPreparation) / LOWERING_ANIMATION_TICKS[2];
					GL11.glTranslatef(0.0F, animPrep * -0.75F, 0.0F);
				}
				ResourceManager.waffle_iron.renderPart("Bolter");
			} else {
				if (iron.fired) {
					if (iron.animPause == RAISING_ANIMATION_TICKS[0]) {
						float offset = (iron.animMovement < RAISING_ANIMATION_TICKS[1] ? iron.animMovement + inter : iron.animMovement) / (float) RAISING_ANIMATION_TICKS[1];
						GL11.glTranslatef(0.0F, offset, 0.0F);
					}
					ResourceManager.waffle_iron.renderPart("Press");
					ResourceManager.waffle_iron.renderPart("Shaky");
					ResourceManager.waffle_iron.renderPart("Bolter");
					GL11.glTranslatef(0.0F, -0.75F, 0.0F);
					ResourceManager.waffle_iron.renderPart("Engine");
				} else {
					if (iron.animPause == RAISING_UNFIRED_ANIMATION_TICKS[1]) {
						float offset = (iron.animMovement < RAISING_UNFIRED_ANIMATION_TICKS[2] ? iron.animMovement + inter : iron.animMovement) / (float) RAISING_UNFIRED_ANIMATION_TICKS[2];
						GL11.glTranslatef(0.0F, offset, 0.0F);
					}
					ResourceManager.waffle_iron.renderPart("Press");
					ResourceManager.waffle_iron.renderPart("Shaky");
					GL11.glTranslatef(0.0F, -0.75F, 0.0F);
					ResourceManager.waffle_iron.renderPart("Engine");
					float bolter = (iron.animPreparation < RAISING_UNFIRED_ANIMATION_TICKS[0] ? iron.animPreparation + inter : iron.animPreparation) / (float) RAISING_UNFIRED_ANIMATION_TICKS[0];
					GL11.glTranslatef(0.0F, bolter * 0.75F, 0.0F);
					ResourceManager.waffle_iron.renderPart("Bolter");
				}
			}
		} else {
			if (iron.lowered) {
				ResourceManager.waffle_iron.renderPart("Press");
				GL11.glTranslatef(0.0F, -0.75F, 0.0F);
				float bolterOffset = this.interpolateBooleans(iron.wasFired, iron.fired, 0.75F, inter);
				GL11.glTranslatef(0.0F, bolterOffset, 0.0F);
				ResourceManager.waffle_iron.renderPart("Bolter");
				GL11.glTranslatef(0.0F, -bolterOffset, 0.0F);
				ResourceManager.waffle_iron.renderPart("Engine");
				GL11.glTranslatef(0.0F, 0.75F, 0.0F);
				if (iron.shaking > 0) {
					float mul = MathHelper.sqrt_float(iron.shaking * 0.02F);
					GL11.glTranslatef(0.0F, 0.75F, 0.0F);
					GLShear.applyShearXZ((this.rnd.nextFloat() - 0.5F) * 0.005F * mul, (this.rnd.nextFloat() - 0.5F) * 0.005F * mul);
					GL11.glTranslatef(0.0F, -0.75F, 0.0F);
				}
				ResourceManager.waffle_iron.renderPart("Shaky");
			} else {
				GL11.glTranslatef(0.0F, 1.0F, 0.0F);
				ResourceManager.waffle_iron.renderPart("Press");
				ResourceManager.waffle_iron.renderPart("Shaky");
				ResourceManager.waffle_iron.renderPart("Bolter");
				GL11.glTranslatef(0.0F, -0.75F, 0.0F);
				ResourceManager.waffle_iron.renderPart("Engine");
			}
		}
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.25, z + 0.5);
		GL11.glRotatef(180, 0F, 1F, 0F);
		GL11.glRotatef(-90, 1F, 0F, 0F);

		if (iron.syncStack != null) {
			this.renderEntity.setEntityItemStack(iron.syncStack);
			this.renderEntity.hoverStart = 0.0F;

			RenderItem.renderInFrame = true;
			GL11.glTranslatef(0.0F, -0.0625F * 165/100, 0.0F);
			this.itemRenderer.doRender(this.renderEntity, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
			RenderItem.renderInFrame = false;
		}

		GL11.glPopMatrix();
	}

	private float interpolateBooleans(boolean old, boolean now, float scale, float inter) {
		if (old && now) return scale;
		if (old) return (1.0F - inter) * scale;
		if (now) return (inter) * scale;
		return 0.0F;
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.waffle_iron);
	}

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase() {
			@Override
			public void renderInventory() {
				GL11.glTranslated(0, -4, 0);
				GL11.glScaled(4, 4, 4);
			}

			@Override
			public void renderCommonWithStack(ItemStack item) {
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.waffle_iron_tex);
				ResourceManager.waffle_iron.renderPart("Base");
				ResourceManager.waffle_iron.renderPart("Press");
				GL11.glTranslatef(0.0F, -0.75F, 0.0F);
				ResourceManager.waffle_iron.renderPart("Bolter");
				ResourceManager.waffle_iron.renderPart("Engine");
				GL11.glTranslatef(0.0F, 0.75F, 0.0F);
				ResourceManager.waffle_iron.renderPart("Shaky");
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
}
