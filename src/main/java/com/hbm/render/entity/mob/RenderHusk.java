package com.hbm.render.entity.mob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hbm.entity.mob.EntityHusk;
import com.hbm.render.util.SkinCache;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderHusk extends RenderBiped {

	private final Map<String, GameProfile> profiles = new HashMap<String, GameProfile>();

	public RenderHusk() {
		super(new ModelBiped(0.0F), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLiving living) {
		if(living instanceof EntityHusk) {
			GameProfile profile = this.getProfile((EntityHusk) living);
			ResourceLocation skin = profile == null ? null : SkinCache.getSkin(profile);
			if(skin != null) return skin;
		}
		return AbstractClientPlayer.locationStevePng;
	}

	private GameProfile getProfile(EntityHusk husk) {
		String name = husk.getOwnerName();
		String uuid = husk.getOwnerUUID();
		if((name == null || name.isEmpty()) && (uuid == null || uuid.isEmpty())) return null;

		String key = uuid + "/" + name;
		GameProfile profile = this.profiles.get(key);
		if(profile != null) return profile;

		try {
			profile = (uuid != null && !uuid.isEmpty())
					? new GameProfile(UUID.fromString(uuid), name == null ? "" : name)
					: new GameProfile((UUID) null, name);
		} catch(IllegalArgumentException ex) {
			return null;
		}

		this.profiles.put(key, profile);
		return profile;
	}
}
