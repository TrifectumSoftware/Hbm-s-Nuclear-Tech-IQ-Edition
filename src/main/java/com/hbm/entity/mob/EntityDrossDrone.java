package com.hbm.entity.mob;

import java.util.List;

import com.hbm.explosion.ExplosionNT;
import com.hbm.lib.ModDamageSource;
import com.hbm.particle.helper.ExplosionSmallCreator;
import com.hbm.tileentity.IGUIProvider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityDrossDrone extends EntityUFOBase implements IMob {

	private int contactCooldown;
	public int machineX, machineY, machineZ;

	public EntityDrossDrone(World world) {
		super(world);
		this.setSize(0.4F, 0.4F);
		this.isImmuneToFire = true;
		this.ignoreFrustumCheck = true;
		this.experienceValue = 10;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}
	@Override
	protected boolean isCourseTraversable(double x, double y, double z, double len) {
		return true;
	}
	@Override
	protected void approachPosition(double speed) {
		double deltaX = this.getX() - posX;
		double deltaY = this.getY() - posY;
		double deltaZ = this.getZ() - posZ;
		double len = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
		if(len > 0.5D) {
			this.motionX = deltaX * speed / len;
			this.motionY = deltaY * speed / len;
			this.motionZ = deltaZ * speed / len;
		}
	}

	@Override
	protected void onDeathUpdate() {
		this.motionY -= 0.05D;

		if(this.deathTime == 5 && !worldObj.isRemote) {
			worldObj.playSoundAtEntity(this, "hbm:entity.chopperDamage", 2.0F, 1.0F);
		}

		if(this.deathTime == 19 && !worldObj.isRemote) {
			ExplosionNT explosion = new ExplosionNT(worldObj, this, posX, posY, posZ, 4F);
			explosion.explode();
			ExplosionSmallCreator.composeEffect(worldObj, posX, posY, posZ, 15, 2.5F, 1.25F);
			this.setDead();
			return;
		}

		super.onDeathUpdate();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15.0D);
	}

	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();
		if(this.courseChangeCooldown > 0) this.courseChangeCooldown--;
		if(this.scanCooldown > 0) this.scanCooldown--;
		if(this.contactCooldown > 0) this.contactCooldown--;

		if(!worldObj.isRemote) {
			if(this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
				this.setDead();
				return;
			}

			if(this.machineY != 0 && this.target == null) {
				this.setWaypoint(machineX, machineY, machineZ);
			}

			if(this.target != null && this.getDistanceSqToEntity(this.target) < 4.0D && this.contactCooldown <= 0) {
				this.contactCooldown = 60;
				this.explode();
			}
			if(machineY != 0) {
				double dx = machineX + 0.5 - posX;
				double dy = machineY + 0.5 - posY;
				double dz = machineZ + 0.5 - posZ;
				if(dx * dx + dy * dy + dz * dz < 9.0D) {
					this.explodeAtMachine();
				}
			}
		}

		if(this.courseChangeCooldown > 0) {
			this.approachPosition(this.target == null ? 0.45D : 0.55D);
		}
	}

	@Override
	protected void scanForTarget() {
		int range = getScanRange();

		if(this.scanCooldown <= 0) {
			this.findMachine();

			if(this.machineY != 0) {
				this.target = null;
			} else {
				List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, this.boundingBox.expand(range, range / 2, range));
				this.target = null;

				for(Entity entity : entities) {
					if(!entity.isEntityAlive() || !canAttackClass(entity.getClass())) continue;

					if(entity instanceof EntityPlayer) {
						if(((EntityPlayer) entity).capabilities.isCreativeMode) continue;
						if(((EntityPlayer) entity).isPotionActive(Potion.invisibility.id)) continue;

						if(this.target == null) {
							this.target = entity;
						} else {
							if(this.getDistanceSqToEntity(entity) < this.getDistanceSqToEntity(this.target)) {
								this.target = entity;
							}
						}
					}
				}
			}

			this.scanCooldown = getScanDelay();
		}
	}

	protected boolean findMachine() {
		int mx = MathHelper.floor_double(posX);
		int my = MathHelper.floor_double(posY);
		int mz = MathHelper.floor_double(posZ);
		int r = 48;
		int rSq = r * r;

		for(Object o : worldObj.loadedTileEntityList) {
			TileEntity te = (TileEntity) o;
			if(!(te instanceof IGUIProvider)) continue;
			int x = te.xCoord;
			int y = te.yCoord;
			int z = te.zCoord;
			int dx = x - mx;
			int dy = y - my;
			int dz = z - mz;
			if(dx * dx + dy * dy + dz * dz < rSq) {
				this.machineX = x;
				this.machineY = y;
				this.machineZ = z;
				this.setWaypoint(x, y, z);
				return true;
			}
		}

		this.machineY = 0;
		return false;
	}

	@Override
	protected void setCourseWithoutTaget() {
		if(this.machineY != 0) {
			this.setWaypoint(machineX, machineY, machineZ);
		} else {
			super.setCourseWithoutTaget();
		}
	}

	@Override
	protected int getScanRange() {
		return 40;
	}

	@Override
	protected int getScanDelay() {
		return 40;
	}

	@Override
	protected int targetHeightOffset() {
		return 3 + rand.nextInt(3);
	}

	@Override
	protected int wanderHeightOffset() {
		return 3 + rand.nextInt(3);
	}

	@Override
	public boolean attackEntityFrom(net.minecraft.util.DamageSource source, float amount) {
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean canAttackClass(Class clazz) {
		return clazz != this.getClass() && clazz != EntityDrossDrone.class;
	}

	private void explode() {
		if(worldObj.isRemote || this.isDead) return;
		this.setDead();

		ExplosionNT explosion = new ExplosionNT(worldObj, this, posX, posY, posZ, 4F);
		explosion.explode();
		ExplosionSmallCreator.composeEffect(worldObj, posX, posY, posZ, 30, 4.5F, 1.75F);

		if(this.target instanceof EntityLivingBase) {
			this.target.attackEntityFrom(ModDamageSource.causeCombineDamage(this, this.target), 10F);
		}
	}

	private void explodeAtMachine() {
		if(worldObj.isRemote || this.isDead) return;
		this.setDead();

		double ex = machineX + 0.5;
		double ey = machineY + 0.5;
		double ez = machineZ + 0.5;

		ExplosionNT explosion = new ExplosionNT(worldObj, this, ex, ey, ez, 8F);
		explosion.explode();
		ExplosionSmallCreator.composeEffect(worldObj, ex, ey, ez, 30, 4.5F, 1.75F);
	}

	@Override
	protected float getSoundVolume() {
		return 2.0F;
	}

	@Override
	protected String getHurtSound() {
		return "random.fizz";
	}

	@Override
	protected String getDeathSound() {
		return "random.explode";
	}
}
