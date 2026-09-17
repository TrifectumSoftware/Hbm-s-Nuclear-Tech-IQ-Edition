package com.hbm.dim.dross;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.dross.biome.BiomeGenDross;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class WorldProviderDross extends WorldProviderCelestial {
	public WorldProviderDross() {}
	public static final List<WasteDebris> debris = new ArrayList<>();
	public static final int DEBRIS_TARGET = 260;
	public static final int DEBRIS_DESPAWN = 3000;

	@Override
	public void updateWeather() {
		super.updateWeather();

		if(!worldObj.isRemote) return;
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if(player == null || player.dimension != dimensionId) return;

		Random rand = worldObj.rand;

		Iterator<WasteDebris> iter = debris.iterator();
		while(iter.hasNext()) {
			WasteDebris d = iter.next();
			double dx = d.posX - player.posX;
			double dz = d.posZ - player.posZ;
			if(dx * dx + dz * dz > DEBRIS_DESPAWN * DEBRIS_DESPAWN) iter.remove();
		}

		while(debris.size() < DEBRIS_TARGET) {
			double x = player.posX + (rand.nextDouble() - 0.5D) * 2400D;
			double z = player.posZ + (rand.nextDouble() - 0.5D) * 2400D;
			double y = player.posY + 350D + rand.nextDouble() * 550D;
			float scale;
			if(rand.nextFloat() < 0.15F) {
				scale = 0.045F + rand.nextFloat() * 0.045F;
			} else {
				scale = 0.01F + rand.nextFloat() * 0.02F;
			}
			int tex = rand.nextInt(2);
			float speed = 0.03F + rand.nextFloat() * 0.12F;
			float ax = (rand.nextFloat() - 0.5F) * 2.0F;
			float ay = (rand.nextFloat() - 0.5F) * 2.0F;
			float az = (rand.nextFloat() - 0.5F) * 2.0F;
			float len = (float) Math.sqrt(ax * ax + ay * ay + az * az);
			if(len < 0.01F) len = 1.0F;
			ax /= len; ay /= len; az /= len;
			float phase = rand.nextFloat() * 360.0F;
			float brightness = 0.5F + rand.nextFloat() * 0.4F;
			int variant = rand.nextInt(3);
			debris.add(new WasteDebris(x, y, z, scale, speed, ax, ay, az, phase, brightness, variant, tex));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return new SkyProviderDross();
	}

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerHell(new BiomeGenDross(SpaceConfig.drossBiome), 0.0F);
	}

	@Override
	public float fogDensity(FogDensity event) {
		return 0.02F;
	}

	@Override
	public String getDimensionName() {
		return "Dross";
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderDross(this.worldObj, this.getSeed());
	}

	@Override
	public Block getStone() {
		return ModBlocks.dross_waste;
	}

	public static class WasteDebris {
		public final double posX, posY, posZ;
		public final float scale;
		public final float speed;
		public final float ax, ay, az;
		public final float phase;
		public final float brightness;
		public final int variant;
		public final int tex;

		public WasteDebris(double posX, double posY, double posZ, float scale, float speed, float ax, float ay, float az, float phase, float brightness, int variant, int tex) {
			this.posX = posX;
			this.posY = posY;
			this.posZ = posZ;
			this.scale = scale;
			this.speed = speed;
			this.ax = ax;
			this.ay = ay;
			this.az = az;
			this.phase = phase;
			this.brightness = brightness;
			this.variant = variant;
			this.tex = tex;
		}
	}
}
