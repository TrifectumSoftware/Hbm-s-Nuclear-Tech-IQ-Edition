package com.hbm.tileentity.turret;

import java.util.ArrayList;
import java.util.List;

import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.inventory.gui.GUITurretSAM;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityTurretSAM2 extends TileEntityTurretBaseNT {

	public static final double PIVOT_Y = 2.47D;
	public static final double MUZZLE = 0.1D;
	public static final int RELOAD_TICKS = 200;
	public static final float DAMAGE = 30F;
	public static final int MAX_LOADED = 2;

	public static final double RAIL_PITCH = Math.toRadians(25.0D);

	static List<Integer> configs = new ArrayList();

	static {
		configs.add(XFactoryRocket.rocket_sam.id);
	}

	@Override
	protected List<Integer> getAmmoList() {
		return configs;
	}

	@Override
	public String getName() {
		return "container.turretSAM2";
	}

	@Override
	public double getTurretDepression() {
		return 5D;
	}

	@Override
	public double getTurretElevation() {
		return 85D;
	}

	@Override
	public double getTurretYawSpeed() {
		return 2D;
	}

	@Override
	public double getTurretPitchSpeed() {
		return 1.5D;
	}

	@Override
	public double getAcceptableInaccuracy() {
		return 10D;
	}

	@Override
	public double getBarrelLength() {
		return MUZZLE;
	}

	@Override
	public double getHeightOffset() {
		return PIVOT_Y;
	}

	@Override
	public Vec3 getHorizontalOffset() {
		return Vec3.createVectorHelper(0.5D, 0D, 0.5D);
	}

	@Override
	public long getMaxPower() {
		return 10000;
	}

	@Override
	public double getDecetorGrace() {
		return 10D;
	}

	@Override
	public double getDecetorRange() {
		return 96D;
	}

	public int loaded;
	int reload;

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!worldObj.isRemote) {

			if(reload > 0) {
				reload--;

				if(reload == 0 && loaded < MAX_LOADED)
					this.loaded++;
			}

			if(loaded < MAX_LOADED && reload <= 0 && this.getFirstConfigLoaded() != null) {
				reload = RELOAD_TICKS;
			}

			if(this.getFirstConfigLoaded() == null) {
				this.loaded = 0;
			}

			this.isTurretPacket = true;
			this.networkPackNT(250);
			this.isTurretPacket = false;
		}
	}

	private boolean isTurretPacket = false;

	@Override
	public void serialize(ByteBuf buf) {
		if(this.isTurretPacket) {
			buf.writeBoolean(true);
			buf.writeInt(this.loaded);
		} else {
			buf.writeBoolean(false);
			super.serialize(buf);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		if(buf.readBoolean()) {
			this.loaded = buf.readInt();
		} else
			super.deserialize(buf);
	}

	@Override
	public void updateFiringTick() {

		if(reload > 0 || loaded <= 0)
			return;

		BulletConfig conf = this.getFirstConfigLoaded();

		if(conf != null) {
			this.spawnBullet(conf, DAMAGE);
			this.conusmeAmmo(conf.ammo);
			this.worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:weapon.missileTakeOff", 2.0F, 0.8F);
			this.loaded--;
		} else {
			this.loaded = 0;
		}
	}

	@Override
	public void spawnBullet(BulletConfig bullet, float baseDamage) {

		Vec3 pos = this.getTurretPos();
		Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
		vec.rotateAroundZ((float) -RAIL_PITCH);
		vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));

		EntityBulletBaseMK4 proj = new EntityBulletBaseMK4(worldObj, bullet, baseDamage, bullet.spread, (float) rotationYaw, (float) RAIL_PITCH);
		proj.setPositionAndRotation(pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord, proj.rotationYaw, proj.rotationPitch);
		proj.lockonTarget = this.target;
		worldObj.spawnEntityInWorld(proj);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.loaded = nbt.getInteger("loaded");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("loaded", loaded);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretSAM(player.inventory, this);
	}
}