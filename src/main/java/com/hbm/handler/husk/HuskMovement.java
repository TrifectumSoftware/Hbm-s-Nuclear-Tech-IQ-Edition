package com.hbm.handler.husk;

import java.util.Locale;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;

public class HuskMovement {

	public static final double DEFAULT_FOLLOW = 3D;
	public static final double STEP = 0.15D;

	private final EntityLivingBase actor;
	private int moveTicks;
	private double moveX, moveZ;
	private String followTarget;
	private double followDistance = DEFAULT_FOLLOW;
	private double jumpVel;
	private float aimYaw;
	private float aimPitch;
	private boolean hasAim;

	public HuskMovement(EntityLivingBase actor) {
		this.actor = actor;
	}

	public void update() {
		this.tickMove();
		if(this.hasAim) this.aim(this.aimYaw, this.aimPitch);
	}

	public String move(String[] params) {
		if(params == null || params.length == 0) return null;

		if(params.length >= 3) {
			try {
				this.startMove(Double.parseDouble(params[0]), Double.parseDouble(params[2]));
				return "ok";
			} catch(NumberFormatException ex) {
				return "Exception: Invalid coordinates";
			}
		}

		if(params.length == 2) {
			int blocks;
			try {
				blocks = Integer.parseInt(params[1]);
			} catch(NumberFormatException ex) {
				return "Exception: Invalid distance";
			}

			double yaw = Math.toRadians(this.actor.rotationYaw);
			double dirX = -Math.sin(yaw);
			double dirZ = Math.cos(yaw);

			String dir = params[0].toLowerCase(Locale.US);
			double x = this.actor.posX;
			double z = this.actor.posZ;

			if("north".equals(dir)) z -= blocks;
			else if("south".equals(dir)) z += blocks;
			else if("east".equals(dir)) x += blocks;
			else if("west".equals(dir)) x -= blocks;
			else if("forward".equals(dir)) { x += dirX * blocks; z += dirZ * blocks; }
			else if("back".equals(dir)) { x -= dirX * blocks; z -= dirZ * blocks; }
			else if("left".equals(dir)) { x += dirZ * blocks; z -= dirX * blocks; }
			else if("right".equals(dir)) { x -= dirZ * blocks; z += dirX * blocks; }
			else return "Exception: Unknown direction";

			this.startMove(x, z);
			return "ok";
		}

		return null;
	}

	public String follow(String[] params) {
		if(params == null || params.length == 0) return null;

		double distance = DEFAULT_FOLLOW;
		if(params.length >= 2) {
			try {
				distance = Double.parseDouble(params[1]);
			} catch(NumberFormatException ex) {
				return "Exception: Invalid distance";
			}
		}
		if(distance < 1D || distance > 32D) distance = DEFAULT_FOLLOW;

		Entity target = this.resolveTarget(params[0]);
		if(target == null) return "nothing named " + params[0];

		this.followTarget = params[0];
		this.followDistance = distance;
		this.moveTicks = 0;
		return "ok " + params[0] + " d=" + (int) this.actor.getDistanceToEntity(target) + " keep=" + (int) distance;
	}

	public String look(String[] params) {
		if(params == null || params.length == 0) return null;

		String dir = params[0].toLowerCase(Locale.US);

		if("north".equals(dir) || "south".equals(dir) || "east".equals(dir) || "west".equals(dir)) {
			float pitch = 0F;
			if(params.length >= 2) {
				try {
					pitch = Float.parseFloat(params[1]);
				} catch(NumberFormatException ex) {
					return "Exception: Invalid pitch";
				}
			}

			if("north".equals(dir)) this.aim(180F, pitch);
			else if("south".equals(dir)) this.aim(0F, pitch);
			else if("west".equals(dir)) this.aim(90F, pitch);
			else this.aim(-90F, pitch);

			return "ok " + dir;
		}

		if("up".equals(dir)) { this.aim(this.actor.rotationYaw, -90F); return "ok up"; }
		if("down".equals(dir)) { this.aim(this.actor.rotationYaw, 90F); return "ok down"; }

		Float yaw = null;
		try { yaw = Float.parseFloat(params[0]); } catch(NumberFormatException ex) { }

		if(yaw != null) {
			float pitch = 0F;
			if(params.length >= 2) {
				try {
					pitch = Float.parseFloat(params[1]);
				} catch(NumberFormatException ex) {
					return "Exception: Invalid pitch";
				}
			}

			this.aim(yaw, pitch);
			return "ok yaw=" + yaw.intValue() + " pitch=" + (int) pitch;
		}

		Entity target = this.resolveTarget(params[0]);
		if(target == null) return "nothing named " + params[0];
		double dx = target.posX - this.actor.posX;
		double dy = target.posY + target.getEyeHeight() - (this.actor.posY + this.actor.getEyeHeight());
		double dz = target.posZ - this.actor.posZ;
		double horizontal = Math.sqrt(dx * dx + dz * dz);

		this.aim((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D), (float) (-Math.toDegrees(Math.atan2(dy, horizontal))));
		return "ok at " + target.getCommandSenderName();
	}

	public void stop() {
		this.moveTicks = 0;
		this.followTarget = null;
	}

	public String followTarget() {
		return this.followTarget == null ? "" : this.followTarget;
	}

	public Entity resolveTarget(String name) {
		if(name == null || name.isEmpty()) return null;

		EntityPlayer player = this.actor.worldObj.getPlayerEntityByName(name);
		if(player != null && player != this.actor) return player;

		for(Object o : this.actor.worldObj.playerEntities) {
			if(!(o instanceof EntityPlayer)) continue;
			EntityPlayer candidate = (EntityPlayer) o;
			if(candidate == this.actor) continue;
			if(candidate.getCommandSenderName().equalsIgnoreCase(name)) return candidate;
		}

		boolean anyPlayer = "player".equalsIgnoreCase(name) || "players".equalsIgnoreCase(name);
		boolean anyMob = "mob".equalsIgnoreCase(name) || "mobs".equalsIgnoreCase(name);
		boolean anyLiving = "living".equalsIgnoreCase(name) || "living".equalsIgnoreCase(name + "s");

		Entity nearest = null;
		double best = Double.MAX_VALUE;

		for(Object o : this.actor.worldObj.loadedEntityList) {
			if(!(o instanceof Entity)) continue;
			Entity entity = (Entity) o;
			if(entity == this.actor || entity.isDead) continue;

			if(anyPlayer) {
				if(!(entity instanceof EntityPlayer)) continue;
			} else if(anyMob) {
				if(!(entity instanceof EntityLivingBase) || entity instanceof EntityPlayer) continue;
			} else if(anyLiving) {
				if(!(entity instanceof EntityLivingBase)) continue;
			} else {
				String key = EntityList.getEntityString(entity);
				if(key == null) key = entity.getClass().getSimpleName();
				if(!name.equalsIgnoreCase(key)) continue;
			}

			double dist = this.actor.getDistanceSqToEntity(entity);
			if(dist < best) { best = dist; nearest = entity; }
		}

		return nearest;
	}

	public String jump(String[] params) {
		double blocks = 1D;
		if(params != null && params.length > 0) {
			try {
				blocks = Double.parseDouble(params[0]);
			} catch(NumberFormatException ex) {
				return "Exception: Invalid height";
			}
		}
		if(blocks < 0.5D || blocks > 4D) blocks = 1D;

		this.jumpVel = Math.sqrt(0.16D * blocks);
		return "ok";
	}

	private void startMove(double x, double z) {
		this.followTarget = null;
		this.actor.stepHeight = 0.6F;
		this.moveX = x;
		this.moveZ = z;
		this.moveTicks = MathHelper.clamp_int((int) (this.actor.getDistance(x, this.actor.posY, z) * 30D), 30, 1200);
	}

	private void tickMove() {
		if(this.followTarget != null && !this.followTarget.isEmpty()) { this.tickFollow(); return; }
		if(this.moveTicks <= 0) return;
		this.moveTicks--;

		double dx = this.moveX - this.actor.posX;
		double dz = this.moveZ - this.actor.posZ;
		double dist = Math.sqrt(dx * dx + dz * dz);

		if(this.moveTicks <= 0 || dist < 1.0D) { this.stop(); return; }

		this.step(dx / dist, dz / dist);
	}

	private void tickFollow() {
		Entity target = this.resolveTarget(this.followTarget);
		if(target == null || target.isDead) return;

		double dx = target.posX - this.actor.posX;
		double dz = target.posZ - this.actor.posZ;
		double dist = Math.sqrt(dx * dx + dz * dz);

		if(dist <= this.followDistance) return;

		this.step(dx / dist, dz / dist);
	}

	private void step(double dirX, double dirZ) {
		this.actor.stepHeight = 0.6F;
		this.aim((float) (Math.toDegrees(Math.atan2(dirZ, dirX)) - 90.0D), this.actor.rotationPitch);

		if(this.jumpVel == 0D && this.actor.onGround && this.actor.isCollidedHorizontally) this.jumpVel = Math.sqrt(0.16D);

		double dy = this.jumpVel;
		if(this.jumpVel != 0D) {
			this.jumpVel -= 0.08D;
			if(this.jumpVel < -0.6D) this.jumpVel = 0D;
		}

		if(this.actor instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) this.actor;
			player.playerNetServerHandler.setPlayerLocation(player.posX + dirX * STEP, player.posY + dy, player.posZ + dirZ * STEP, player.rotationYaw, player.rotationPitch);
		} else {
			this.actor.moveEntity(dirX * STEP, dy, dirZ * STEP);
		}

		if(this.jumpVel < 0D && this.actor.onGround) this.jumpVel = 0D;
	}

	public void aim(float yaw, float pitch) {
		this.aimYaw = yaw;
		this.aimPitch = MathHelper.clamp_float(pitch, -90F, 90F);
		this.hasAim = true;

		this.actor.rotationYaw = this.aimYaw;
		this.actor.rotationPitch = this.aimPitch;
		this.actor.rotationYawHead = this.aimYaw;
		this.actor.renderYawOffset = this.aimYaw;
	}
}
