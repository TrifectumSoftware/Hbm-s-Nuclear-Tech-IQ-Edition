package com.hbm.render.entity.mob;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import com.hbm.entity.mob.EntityHusk;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderHusk extends RenderBiped {

	private final Map<EntityHusk, ResourceLocation> skins = Collections.synchronizedMap(new WeakHashMap<EntityHusk, ResourceLocation>());
	private final Set<EntityHusk> requested = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<EntityHusk, Boolean>()));

	public RenderHusk() {
		super(new ModelBiped(0.0F), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLiving living) {
		if(living instanceof EntityHusk) {
			EntityHusk husk = (EntityHusk) living;
			ResourceLocation cached = skins.get(husk);
			if(cached != null) return cached;
			this.requestSkin(husk);
		}
		return AbstractClientPlayer.locationStevePng;
	}

	private void requestSkin(final EntityHusk husk) {
		String name = husk.getOwnerName();
		String uuid = husk.getOwnerUUID();

		if(name == null || name.isEmpty() || requested.contains(husk)) return;
		requested.add(husk);

		GameProfile profile;
		try {
			profile = (uuid != null && !uuid.isEmpty()) ? new GameProfile(UUID.fromString(uuid), name) : new GameProfile((UUID)null, name);
		} catch(Exception e) {
			return;
		}

		Minecraft.getMinecraft().func_152342_ad().func_152790_a(profile, new SkinManager.SkinAvailableCallback() {
			@Override
			public void func_152121_a(MinecraftProfileTexture.Type type, ResourceLocation location) {
				if(type == MinecraftProfileTexture.Type.SKIN) {
					skins.put(husk, location);
				}
			}
		}, false);
	}
}
