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

	public static final double PIVOT_Y = 1.3125D;
	public static final double MUZZLE = 0.1D;
	public static final int RELOAD_TICKS = 200;
	public static final float DAMAGE = 30F;
	public static final int MAX_LOADED = 6;
	public static final int FIRE_DELAY = 10; // 0.5s cap-pop -> missile-fire

	static List<Integer> configs = new ArrayList();

	static {
		configs.add(XFactoryRocket.rocket_sam_advanced.id);
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
	int firingTube = -1; // -1 = none, 0..5 = tube index firing
	int fireTimer = 0;
	int cooldown = 0; // ticks until next missile can fire
	public int[] tubeFireTick = new int[] { -1, -1, -1, -1, -1, -1 };

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!worldObj.isRemote) {

			if(cooldown > 0) cooldown--;

			// handle firing delay
			if(firingTube >= 0) {
				fireTimer--;
				if(fireTimer <= 0) {
					BulletConfig conf = this.getFirstConfigLoaded();
					if(conf != null) {
						this.spawnBullet(conf, DAMAGE);
						this.conusmeAmmo(conf.ammo);
						this.worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:weapon.missileTakeOff", 2.0F, 0.8F);
						this.loaded--;
					} else {
						this.loaded = 0;
					}
					firingTube = -1;
					fireTimer = 0;
					cooldown = 80; // 4s before next missile can fire
				}
			}

			if(reload > 0) {
				reload--;

				if(reload == 0 && loaded < MAX_LOADED)
					this.loaded = MAX_LOADED;
			}

			if(loaded < MAX_LOADED && reload <= 0 && firingTube < 0 && this.getFirstConfigLoaded() != null) {
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
			buf.writeInt(this.firingTube);
			buf.writeInt(this.fireTimer);
			for(int i = 0; i < 6; i++) buf.writeInt(this.tubeFireTick[i]);
		} else {
			buf.writeBoolean(false);
			super.serialize(buf);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		if(buf.readBoolean()) {
			this.loaded = buf.readInt();
			this.firingTube = buf.readInt();
			this.fireTimer = buf.readInt();
			for(int i = 0; i < 6; i++) this.tubeFireTick[i] = buf.readInt();
		} else
			super.deserialize(buf);
	}

	@Override
	public void updateFiringTick() {

		if(firingTube >= 0 || cooldown > 0 || loaded <= 0)
			return;

		BulletConfig conf = this.getFirstConfigLoaded();

		if(conf != null) {
			// start firing sequence: caps pop now, missile fires after FIRE_DELAY
			int tube = MAX_LOADED - loaded; // 0..5
			firingTube = tube;
			fireTimer = FIRE_DELAY;
			tubeFireTick[tube] = (int) worldObj.getTotalWorldTime();
		} else {
			this.loaded = 0;
		}
	}

	@Override
	public void spawnBullet(BulletConfig bullet, float baseDamage) {

		Vec3 pos = this.getTurretPos();
		Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
		vec.rotateAroundZ((float) -this.rotationPitch);
		vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));

		EntityBulletBaseMK4 proj = new EntityBulletBaseMK4(worldObj, bullet, baseDamage, bullet.spread, (float) rotationYaw, (float) rotationPitch);
		proj.setPositionAndRotation(pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord, proj.rotationYaw, proj.rotationPitch);
		proj.lockonTarget = this.target;
		worldObj.spawnEntityInWorld(proj);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.loaded = nbt.getInteger("loaded");
		this.firingTube = nbt.getInteger("firingTube");
		this.fireTimer = nbt.getInteger("fireTimer");
		this.cooldown = nbt.getInteger("cooldown");
		for(int i = 0; i < 6; i++) this.tubeFireTick[i] = nbt.getInteger("tubeFireTick" + i);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("loaded", loaded);
		nbt.setInteger("firingTube", firingTube);
		nbt.setInteger("fireTimer", fireTimer);
		nbt.setInteger("cooldown", cooldown);
		for(int i = 0; i < 6; i++) nbt.setInteger("tubeFireTick" + i, tubeFireTick[i]);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretSAM(player.inventory, this);
	}
}