package com.hbm.tileentity.turret;

import java.util.ArrayList;
import java.util.List;

import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryTurret;
import com.hbm.inventory.gui.GUITurretSAM;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityTurretGatling35 extends TileEntityTurretBaseNT {

	public static final double PIVOT_Y = 1.5625D;
	public static final double MUZZLE = 1.5D;
	public static final float DAMAGE = 15F;
	public static final int FIRE_DELAY = 4; // ~5 RPS

	static List<Integer> configs = new ArrayList();

	static {
		configs.add(XFactoryTurret.bullet_airburst.id);
		configs.add(XFactoryTurret.bullet_hei.id);
	}

	@Override
	protected List<Integer> getAmmoList() {
		return configs;
	}

	@Override
	public String getName() {
		return "container.turretGatling35";
	}

	@Override
	public double getTurretDepression() {
		return 10D;
	}

	@Override
	public double getTurretElevation() {
		return 85D;
	}

	@Override
	public double getTurretYawSpeed() {
		return 15D;
	}

	@Override
	public double getTurretPitchSpeed() {
		return 5D;
	}

	@Override
	public double getAcceptableInaccuracy() {
		return 0D;
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
		return 250D;
	}

	int fireTimer = 0;
	public float barrelSpin = 0F;
	public float lastBarrelSpin = 0F;
	public int lastFiredTube = 0;

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(worldObj.isRemote) {
			this.lastBarrelSpin = this.barrelSpin;
		} else {
			if(this.aligned && this.target != null) {
				this.barrelSpin += 30F;
				if(this.barrelSpin >= 360F) this.barrelSpin -= 360F;
			}
		}
	}

	@Override
	public void updateFiringTick() {
		if(fireTimer > 0) {
			fireTimer--;
			return;
		}

		BulletConfig conf = this.getFirstConfigLoaded();

		if(conf != null) {
			this.spawnBullet(conf, DAMAGE);
			this.conusmeAmmo(conf.ammo);
			this.worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:turret.ciws_fire", 2.0F, 1.0F);
			fireTimer = FIRE_DELAY;
		}
	}

	@Override
	public void spawnBullet(BulletConfig bullet, float baseDamage) {

		Vec3 pos = this.getTurretPos();
		Vec3 vec = Vec3.createVectorHelper(0, 0, -this.getBarrelLength());
		vec.rotateAroundX((float) this.rotationPitch);
		vec.rotateAroundY((float) (Math.PI - this.rotationYaw));

		EntityBulletBaseMK4 proj = new EntityBulletBaseMK4(worldObj, bullet, baseDamage, bullet.spread, (float) rotationYaw, (float) rotationPitch);
		proj.setPositionAndRotation(pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord, proj.rotationYaw, proj.rotationPitch);
		if(proj.airburstDistance < 0) {
			// program airburst distance for airburst rounds
			if(bullet == XFactoryTurret.bullet_airburst && this.target != null) {
				Vec3 tPos = this.getEntityPos(this.target);
				Vec3 myPos = this.getTurretPos();
				double d = tPos.distanceTo(myPos);
				proj.airburstDistance = d;
			}
		}
		worldObj.spawnEntityInWorld(proj);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.barrelSpin = nbt.getFloat("barrelSpin");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setFloat("barrelSpin", barrelSpin);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretSAM(player.inventory, this);
	}
}