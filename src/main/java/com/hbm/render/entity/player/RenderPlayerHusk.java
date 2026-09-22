package com.hbm.render.entity.player;

import com.hbm.packet.PermaSyncHandler;
import com.hbm.render.util.SkinCache;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderPlayerHusk extends RenderPlayer {

	@Override
	protected ResourceLocation getEntityTexture(AbstractClientPlayer player) {
		ResourceLocation skin = SkinCache.getSkin(PermaSyncHandler.huskForms.get(player.getEntityId()));
		return skin != null ? skin : super.getEntityTexture(player);
	}

	@Override
	public void renderFirstPersonArm(EntityPlayer player) {
		ResourceLocation skin = SkinCache.getSkin(PermaSyncHandler.huskForms.get(player.getEntityId()));
		if(skin != null) Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
		super.renderFirstPersonArm(player);
	}
}
