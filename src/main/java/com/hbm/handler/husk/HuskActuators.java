package com.hbm.handler.husk;

import java.util.Locale;

import com.hbm.entity.mob.EntityHusk;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.IKeybindReceiver;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class HuskActuators {

	public static final double DEFAULT_REACH = 3D;

	private final HuskBody body;

	public HuskActuators(HuskBody body) {
		this.body = body;
	}

	public String interact(String[] params) {
		double reach = this.reach(params);

		EntityPlayer handle = this.body.handle();
		ItemStack held = handle.getHeldItem();

		if(held != null && held.getItem() instanceof ItemGunBaseNT) {
			this.press(EnumKeybind.GUN_SECONDARY);
			return "ok gun=secondary";
		}

		Entity target = this.entityInFront(reach);
		if(target != null && handle.interactWith(target)) return "ok entity=" + target.getCommandSenderName();

		MovingObjectPosition mop = this.blockInFront(reach);
		if(mop == null) {
			if(held == null) return "nothing held, nothing in reach";
			held.getItem().onItemRightClick(held, this.body.actor.worldObj, handle);
			this.body.persist();
			return "ok item=" + itemName(held);
		}

		World world = this.body.actor.worldObj;
		int x = mop.blockX;
		int y = mop.blockY;
		int z = mop.blockZ;
		String block = "" + Block.blockRegistry.getNameForObject(world.getBlock(x, y, z));

		if(held != null && held.getItem().onItemUse(held, handle, world, x, y, z, mop.sideHit, (float) (mop.hitVec.xCoord - x), (float) (mop.hitVec.yCoord - y), (float) (mop.hitVec.zCoord - z))) {
			this.body.persist();
			return "ok item on " + block;
		}

		if(world.getBlock(x, y, z).onBlockActivated(world, x, y, z, handle, mop.sideHit, (float) mop.hitVec.xCoord, (float) mop.hitVec.yCoord, (float) mop.hitVec.zCoord)) return "ok block=" + block;

		if(held == null) return "nothing at " + block;

		held.getItem().onItemRightClick(held, world, handle);
		this.body.persist();
		return "ok item=" + itemName(held) + " past " + block;
	}

	public String attack(String[] params) {
		double reach = this.reach(params);

		ItemStack held = this.body.held();
		if(held != null && held.getItem() instanceof ItemGunBaseNT) {
			this.press(EnumKeybind.GUN_PRIMARY);
			return "ok gun=primary";
		}

		Entity target = this.entityInFront(reach);
		if(target == null) return "nothing in reach";

		this.body.handle().attackTargetEntityWithCurrentItem(target);
		this.body.persist();
		return "ok entity=" + target.getCommandSenderName();
	}

	public String hit(String[] params) {
		double reach = this.reach(params);

		EntityPlayer handle = this.body.handle();
		ItemStack held = handle.getHeldItem();

		if(held != null && held.getItem() instanceof ItemGunBaseNT) {
			this.press(EnumKeybind.GUN_PRIMARY);
			return "ok gun=primary";
		}

		Entity target = this.entityInFront(reach);
		if(target != null) {
			handle.attackTargetEntityWithCurrentItem(target);
			this.body.persist();
			return "ok entity=" + target.getCommandSenderName();
		}

		MovingObjectPosition mop = this.blockInFront(reach);
		if(mop != null && handle instanceof EntityPlayerMP) {
			String block = "" + Block.blockRegistry.getNameForObject(this.body.actor.worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ));
			((EntityPlayerMP) handle).theItemInWorldManager.tryHarvestBlock(mop.blockX, mop.blockY, mop.blockZ);
			this.body.persist();
			return "ok block=" + block;
		}

		return "nothing in reach";
	}

	public String key(String[] params) {
		if(params == null || params.length == 0) return null;

		EnumKeybind keybind;
		try {
			keybind = EnumKeybind.valueOf(params[0].toUpperCase(Locale.US));
		} catch(IllegalArgumentException ex) {
			return "Exception: Unknown keybind";
		}

		return this.press(keybind) ? "ok" : "nothing";
	}

	public boolean press(EnumKeybind keybind) {
		IInventory inv = this.body.inventory();
		ItemStack held = this.body.held();
		if(held == null) return false;

		if(held.getItem() instanceof ItemGunBaseNT) {
			ItemGunBaseNT gun = (ItemGunBaseNT) held.getItem();
			gun.handleKeybind(this.body.actor, inv, held, keybind, true);
			gun.handleKeybind(this.body.actor, inv, held, keybind, false);
			this.body.persist();
			return true;
		}

		if(held.getItem() instanceof IKeybindReceiver) {
			IKeybindReceiver receiver = (IKeybindReceiver) held.getItem();
			EntityPlayer handle = this.body.handle();
			if(!receiver.canHandleKeybind(handle, held, keybind)) return false;

			receiver.handleKeybind(handle, held, keybind, true);
			receiver.handleKeybind(handle, held, keybind, false);
			this.body.persist();
			return true;
		}

		return false;
	}

	public String hold(String[] params) {
		if(params == null || params.length == 0) return null;

		int slot;
		try { slot = Integer.parseInt(params[0]); } catch(NumberFormatException ex) { return "Exception: Invalid slot"; }
		if(slot < 0 || slot >= HuskInventory.MAIN) return "Exception: Invalid slot";

		if(this.body.actor instanceof EntityPlayer) {
			((EntityPlayer) this.body.actor).inventory.currentItem = slot;
			this.syncPlayer();
			return "ok";
		}

		if(!(this.body.actor instanceof EntityHusk)) return "Exception: Invalid actor";

		EntityHusk husk = (EntityHusk) this.body.actor;
		husk.getPlayerData().setInteger("SelectedItemSlot", slot);
		husk.onInventoryChanged();
		return "ok";
	}

	public String pickup(String[] params) {
		double radius = 5D;
		if(params != null && params.length > 0) {
			try { radius = Double.parseDouble(params[0]); } catch(NumberFormatException ex) { }
		}
		if(radius <= 0D || radius > 16D) radius = 5D;

		IInventory inv = this.body.inventory();
		if(inv == null) return "0";

		int picked = 0;

		for(Object o : this.body.actor.worldObj.loadedEntityList) {
			if(!(o instanceof EntityItem)) continue;
			EntityItem item = (EntityItem) o;
			if(item.isDead || item.getEntityItem() == null) continue;
			if(this.body.actor.getDistanceToEntity(item) > radius) continue;

			ItemStack stack = item.getEntityItem();
			int size = stack.stackSize;
			int leftover = addTo(inv, stack);
			if(leftover >= size) continue;

			picked++;
			if(leftover <= 0) item.setDead();
			else stack.stackSize = leftover;
		}

		if(picked > 0) {
			this.body.persist();
			this.syncPlayer();
		}
		return "" + picked;
	}

	public String dropAll(String[] params) {
		IInventory inv = this.body.inventory();
		if(inv == null) return "0";

		int held = this.body.selectedSlot();
		int dropped = 0;

		for(int i = 0; i < inv.getSizeInventory(); i++) {
			if(i == held) continue;

			ItemStack stack = inv.getStackInSlot(i);
			if(stack == null || stack.stackSize <= 0) continue;

			inv.setInventorySlotContents(i, null);
			this.body.actor.entityDropItem(stack, 0.2F);
			dropped++;
		}

		if(dropped > 0) {
			this.body.persist();
			this.syncPlayer();
		}

		return "" + dropped;
	}

	public String drop(String[] params) {
		if(params == null || params.length == 0) return null;

		int slot;
		try { slot = Integer.parseInt(params[0]); } catch(NumberFormatException ex) { return "Exception: Invalid slot"; }
		if(slot < 0) return "Exception: Invalid slot";

		int count = 1;
		if(params.length >= 2) {
			try { count = Integer.parseInt(params[1]); } catch(NumberFormatException ex) { return "Exception: Invalid count"; }
		}
		if(count <= 0) return "Exception: Invalid count";

		IInventory inv = this.body.inventory();
		if(inv == null || slot >= inv.getSizeInventory()) return "empty";

		ItemStack dropped = inv.decrStackSize(slot, count);
		if(dropped == null || dropped.stackSize <= 0) return "empty";

		this.body.actor.entityDropItem(dropped, 0.2F);
		this.body.persist();
		this.syncPlayer();
		return "ok";
	}

	private double reach(String[] params) {
		double reach = DEFAULT_REACH;
		if(params != null && params.length > 0) {
			try { reach = Double.parseDouble(params[0]); } catch(NumberFormatException ex) { }
		}
		if(reach <= 0D || reach > 8D) reach = DEFAULT_REACH;
		return reach;
	}

	private Entity entityInFront(double reach) {
		Vec3 look = this.body.actor.getLookVec();
		double ex = this.body.actor.posX;
		double ey = this.body.actor.posY + this.body.actor.getEyeHeight();
		double ez = this.body.actor.posZ;

		AxisAlignedBB box = this.body.actor.boundingBox.addCoord(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach).expand(1.0D, 1.0D, 1.0D);
		Entity best = null;
		double bestDist = Double.MAX_VALUE;

		for(Object o : this.body.actor.worldObj.getEntitiesWithinAABBExcludingEntity(this.body.actor, box)) {
			if(!(o instanceof Entity)) continue;
			Entity entity = (Entity) o;
			if(entity.isDead) continue;

			double dx = entity.posX - ex;
			double dy = entity.posY + entity.height / 2D - ey;
			double dz = entity.posZ - ez;
			if(dx * look.xCoord + dy * look.yCoord + dz * look.zCoord <= 0D) continue;

			double dist = this.body.actor.getDistanceSqToEntity(entity);
			if(dist < bestDist) { bestDist = dist; best = entity; }
		}

		return best;
	}

	private MovingObjectPosition blockInFront(double reach) {
		Vec3 look = this.body.actor.getLookVec();
		Vec3 start = Vec3.createVectorHelper(this.body.actor.posX, this.body.actor.posY + this.body.actor.getEyeHeight(), this.body.actor.posZ);
		Vec3 end = Vec3.createVectorHelper(start.xCoord + look.xCoord * reach, start.yCoord + look.yCoord * reach, start.zCoord + look.zCoord * reach);
		return this.body.actor.worldObj.rayTraceBlocks(start, end);
	}

	private void syncPlayer() {
		if(!(this.body.actor instanceof EntityPlayerMP)) return;
		EntityPlayerMP player = (EntityPlayerMP) this.body.actor;
		player.inventory.markDirty();
		player.inventoryContainer.detectAndSendChanges();
	}

	private static int addTo(IInventory inv, ItemStack stack) {
		ItemStack copy = stack.copy();

		for(int i = 0; i < inv.getSizeInventory() && copy.stackSize > 0; i++) {
			ItemStack existing = inv.getStackInSlot(i);
			if(existing == null) continue;
			if(!existing.isItemEqual(copy) || !ItemStack.areItemStackTagsEqual(existing, copy)) continue;

			int limit = Math.min(existing.getMaxStackSize(), inv.getInventoryStackLimit());
			int moved = Math.min(limit - existing.stackSize, copy.stackSize);
			if(moved <= 0) continue;

			existing.stackSize += moved;
			copy.stackSize -= moved;
			inv.setInventorySlotContents(i, existing);
		}

		for(int i = 0; i < inv.getSizeInventory() && copy.stackSize > 0; i++) {
			if(inv.getStackInSlot(i) != null) continue;
			if(!inv.isItemValidForSlot(i, copy)) continue;

			int moved = Math.min(copy.getMaxStackSize(), copy.stackSize);
			ItemStack placed = copy.copy();
			placed.stackSize = moved;
			inv.setInventorySlotContents(i, placed);
			copy.stackSize -= moved;
		}

		return copy.stackSize;
	}

	public static String itemName(ItemStack stack) {
		Object name = Item.itemRegistry.getNameForObject(stack.getItem());
		return name == null ? "?" : name.toString();
	}
}
