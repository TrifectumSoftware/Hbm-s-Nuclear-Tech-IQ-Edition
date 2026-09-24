package com.hbm.handler.husk;

import com.hbm.entity.mob.EntityHusk;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class HuskBody {

	public final EntityLivingBase actor;
	private final HuskInventory inventory;
	private FakePlayer fake;
	private boolean used;

	public HuskBody(EntityLivingBase actor) {
		this.actor = actor;
		this.inventory = actor instanceof EntityHusk ? ((EntityHusk) actor).getInventory() : null;
	}

	public boolean isPlayer() {
		return this.actor instanceof EntityPlayer;
	}

	public IInventory inventory() {
		if(this.inventory != null) return this.inventory;
		if(this.actor instanceof EntityPlayer) return ((EntityPlayer) this.actor).inventory;
		return null;
	}

	public int selectedSlot() {
		if(this.actor instanceof EntityHusk) return ((EntityHusk) this.actor).getSelectedSlot();
		if(this.actor instanceof EntityPlayer) return ((EntityPlayer) this.actor).inventory.currentItem;
		return 0;
	}

	public ItemStack held() {
		IInventory inv = this.inventory();
		return inv == null ? this.actor.getHeldItem() : inv.getStackInSlot(this.selectedSlot());
	}

	public EntityPlayer handle() {
		if(this.actor instanceof EntityPlayer) return (EntityPlayer) this.actor;

		if(this.fake == null) {
			this.fake = new FakePlayer((WorldServer) this.actor.worldObj, new GameProfile(this.actor.getUniqueID(), "husk"));

			if(this.inventory != null) {
				this.fake.inventory.mainInventory = this.inventory.mainInventory;
				this.fake.inventory.armorInventory = this.inventory.armorInventory;
			}
		}

		this.fake.setPosition(this.actor.posX, this.actor.posY, this.actor.posZ);
		this.fake.rotationYaw = this.actor.rotationYaw;
		this.fake.rotationPitch = this.actor.rotationPitch;
		this.fake.rotationYawHead = this.actor.rotationYawHead;
		this.fake.inventory.currentItem = this.selectedSlot();
		this.used = true;
		return this.fake;
	}

	public void tickHeld() {
		if(this.inventory == null) return;

		ItemStack held = this.held();
		if(held == null) return;

		FakePlayer player = (FakePlayer) this.handle();
		ItemGunBaseNT.setIsEquipped(held, true);
		held.getItem().onUpdate(held, this.actor.worldObj, player, this.selectedSlot(), true);
		this.inventory.markDirty();
		this.used = false;
	}

	public void persist() {
		if(this.inventory != null) this.inventory.markDirty();
	}

	public boolean wasUsed() {
		return this.used;
	}

	public void clearUsed() {
		this.used = false;
	}
}
