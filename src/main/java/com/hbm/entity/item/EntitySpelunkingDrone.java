package com.hbm.entity.item;

import java.util.ArrayDeque;
import java.util.HashMap;

import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemDrone.EnumDroneType;
import com.hbm.tileentity.network.TileEntityDroneCrate;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntitySpelunkingDrone extends EntityDroneBase implements IInventory {

	private static final int[][] DIRS = {
			{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
	};

	public ItemStack[] slots = new ItemStack[this.getSizeInventory()];

	protected int baseX, baseY, baseZ;
	protected int oreX, oreY, oreZ;
	protected int mineX, mineY, mineZ;
	protected double mineProgress;
	protected int scanTimer = 0;
	protected int homeWait = 0;
	protected int wanderCooldown = 0;
	protected int wanderX, wanderZ;
	protected BlockPos pathStep;
	protected int pathRecalc = 0;
	protected DroneState state = DroneState.SEEKING;

	public enum DroneState {
		SEEKING, MINING, RETURNING
	}

	public EntitySpelunkingDrone(World world) {
		super(world);
	}

	public void setBase(int x, int y, int z) {
		this.baseX = x;
		this.baseY = y;
		this.baseZ = z;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(worldObj.isRemote) return;

		switch(this.state) {
		case SEEKING:
			this.doSeeking();
			break;
		case MINING:
			this.doMining();
			break;
		case RETURNING:
			this.doReturning();
			break;
		}
	}

	protected void doSeeking() {

		if(this.isFull()) {
			this.goHome();
			return;
		}

		if(this.homeWait > 0) {
			this.homeWait--;
			this.setTarget(baseX + 0.5, baseY + 1, baseZ + 0.5);
			return;
		}

		if(this.scanTimer-- <= 0) {
			this.scanTimer = 30;
			if(this.findReachableOre()) {
				this.mineY = -1;
				this.mineProgress = 0;
				this.pathRecalc = 0;
				this.state = DroneState.MINING;
				return;
			}
		}


		this.wanderCooldown--;
		if(this.wanderCooldown <= 0) {
			this.wanderCooldown = 60;
			this.wanderX = MathHelper.floor_double(posX) + worldObj.rand.nextInt(49) - 24;
			this.wanderZ = MathHelper.floor_double(posZ) + worldObj.rand.nextInt(49) - 24;
		}
		this.setTarget(this.wanderX + 0.5, posY, this.wanderZ + 0.5);
	}

	protected void doMining() {

		if(this.isFull()) {
			this.goHome();
			return;
		}

		if(worldObj.getBlock(oreX, oreY, oreZ).isAir(worldObj, oreX, oreY, oreZ)) {
			this.mineY = -1;
			this.mineProgress = 0;
			this.state = DroneState.SEEKING;
			return;
		}

		Vec3 toOre = Vec3.createVectorHelper(oreX + 0.5 - posX, oreY + 0.5 - posY, oreZ + 0.5 - posZ);
		if(toOre.lengthVector() < 2) {
			this.mineBlock(oreX, oreY, oreZ);
			return;
		}


		if(this.pathRecalc-- <= 0) {
			this.pathRecalc = 20;
			this.pathStep = this.pathfind(oreX, oreY, oreZ);
		}

		if(this.pathStep == null) {
			this.state = DroneState.SEEKING;
			return;
		}

		this.setTarget(this.pathStep.getX() + 0.5, this.pathStep.getY() + 0.5, this.pathStep.getZ() + 0.5);
	}

	protected void doReturning() {

		Vec3 dist = Vec3.createVectorHelper(posX - (baseX + 0.5), posY - (baseY + 1), posZ - (baseZ + 0.5));

		if(dist.lengthVector() < 1.5) {

			TileEntity te = worldObj.getTileEntity(baseX, baseY, baseZ);

			if(!(te instanceof TileEntityDroneCrate)) {

				for(ItemStack stack : slots) {
					if(stack != null) this.entityDropItem(stack, 0F);
				}
				this.entityDropItem(new ItemStack(ModItems.drone, 1, EnumDroneType.SPELUNKER.ordinal()), 0F);
				this.setDead();
				return;
			}

			this.unloadToCrate((TileEntityDroneCrate) te);

			if(this.isEmpty()) {
				this.setAppearance(0);
				this.homeWait = 100;
				this.state = DroneState.SEEKING;
			}

		} else {

		if(this.pathRecalc-- <= 0) {
				this.pathRecalc = 20;
				this.pathStep = this.pathfind(baseX, baseY + 1, baseZ);
			}

			if(this.pathStep != null) {
				this.setTarget(this.pathStep.getX() + 0.5, this.pathStep.getY() + 0.5, this.pathStep.getZ() + 0.5);
			} else {
					this.setTarget(posX, posY + 16, posZ);
			}
		}
	}

	protected void goHome() {
		this.state = DroneState.RETURNING;
	}
	protected boolean findReachableOre() {

		int sx = MathHelper.floor_double(posX);
		int sy = MathHelper.floor_double(posY);
		int sz = MathHelper.floor_double(posZ);

		HashMap<BlockPos, BlockPos> cameFrom = new HashMap<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		BlockPos start = new BlockPos(sx, sy, sz);
		queue.add(start);
		cameFrom.put(start, null);

		int limit = 1500;
		while(!queue.isEmpty() && limit-- > 0) {
			BlockPos cur = queue.poll();
			int cx = cur.getX();
			int cy = cur.getY();
			int cz = cur.getZ();
			for(int[] d : DIRS) {
				int ax = cx + d[0];
				int ay = cy + d[1];
				int az = cz + d[2];
				Block block = worldObj.getBlock(ax, ay, az);
				if(block.isAir(worldObj, ax, ay, az) || block.getMaterial().isLiquid()) continue;
				if(this.hasDroneNearby(ax, ay, az)) continue;
				if(this.isOre(block, worldObj.getBlockMetadata(ax, ay, az))) {
					this.oreX = ax;
					this.oreY = ay;
					this.oreZ = az;
					return true;
				}
			}
			for(int[] d : DIRS) {
				BlockPos n = new BlockPos(cx + d[0], cy + d[1], cz + d[2]);
				if(cameFrom.containsKey(n)) continue;
				Block nb = worldObj.getBlock(n.getX(), n.getY(), n.getZ());
				if(!nb.isAir(worldObj, n.getX(), n.getY(), n.getZ())) continue;
				cameFrom.put(n, cur);
				queue.add(n);
			}
		}

		return false;
	}

protected BlockPos pathfind(int tx, int ty, int tz) {

		int sx = MathHelper.floor_double(posX);
		int sy = MathHelper.floor_double(posY);
		int sz = MathHelper.floor_double(posZ);

		HashMap<BlockPos, BlockPos> cameFrom = new HashMap<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		BlockPos start = new BlockPos(sx, sy, sz);
		queue.add(start);
		cameFrom.put(start, null);

		BlockPos goal = null;
		int limit = 2000;
		while(!queue.isEmpty() && limit-- > 0) {
			BlockPos cur = queue.poll();
			int cx = cur.getX();
			int cy = cur.getY();
			int cz = cur.getZ();

			if(Math.abs(cx - tx) + Math.abs(cy - ty) + Math.abs(cz - tz) == 1) {
				goal = cur;
				break;
			}

			for(int[] d : DIRS) {
				BlockPos n = new BlockPos(cx + d[0], cy + d[1], cz + d[2]);
				if(cameFrom.containsKey(n)) continue;
				Block nb = worldObj.getBlock(n.getX(), n.getY(), n.getZ());
				if(!nb.isAir(worldObj, n.getX(), n.getY(), n.getZ()) && !nb.getMaterial().isLiquid()) continue;
				cameFrom.put(n, cur);
				queue.add(n);
			}
		}

		if(goal == null) return null;

	BlockPos step = goal;
		while(true) {
			BlockPos prev = cameFrom.get(step);
			if(prev == null) return null;
			if(prev.equals(start)) return step;
			step = prev;
		}
	}

	protected void mineBlock(int x, int y, int z) {

		Block block = worldObj.getBlock(x, y, z);
		int meta = worldObj.getBlockMetadata(x, y, z);

		if(block.isAir(worldObj, x, y, z) || block.getMaterial().isLiquid() || block == Blocks.bedrock) return;

		float hardness = block.getBlockHardness(worldObj, x, y, z);
		if(hardness < 0) return;

		if(x != mineX || y != mineY || z != mineZ) {
			mineX = x;
			mineY = y;
			mineZ = z;
			mineProgress = 0;
		}

		if(hardness == 0) {
			this.breakAndCollect(x, y, z, block, meta);
			this.resetMine();
			return;
		}

				this.mineProgress += getSpeed() / (hardness * 15D);

		if(this.mineProgress >= 1) {
			this.breakAndCollect(x, y, z, block, meta);
			this.resetMine();
		} else {
					worldObj.destroyBlockInWorldPartially(this.getEntityId(), x, y, z, (int) Math.floor(mineProgress * 10));
		}
	}

	protected void breakAndCollect(int x, int y, int z, Block block, int meta) {

		worldObj.playAuxSFXAtEntity(null, 2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
		worldObj.setBlockToAir(x, y, z);

		if(!this.isOre(block, meta)) return;

		for(ItemStack drop : this.getMinedDrops(block, x, y, z, meta)) {
			if(!this.addToStorage(this.slots, drop)) {
				this.entityDropItem(drop, 0F);
			}
		}

		if(!this.isEmpty()) this.setAppearance(1);
	}

	protected void resetMine() {
		mineX = 0;
		mineY = -1;
		mineZ = 0;
		mineProgress = 0;
	}

	protected boolean isFull() {
		for(ItemStack stack : slots) {
			if(stack == null) return false;
		}
		return true;
	}

	protected boolean isEmpty() {
		for(ItemStack stack : slots) {
			if(stack != null) return false;
		}
		return true;
	}

	protected void unloadToCrate(TileEntityDroneCrate crate) {

		if(crate.sendingMode || !crate.itemType) return;

		for(int i = 0; i < slots.length; i++) {
			ItemStack stack = slots[i];
			if(stack == null) continue;
			for(int j = 0; j < 18; j++) {
				if(crate.slots[j] != null && crate.slots[j].isItemEqual(stack) && ItemStack.areItemStackTagsEqual(crate.slots[j], stack)) {
					int add = Math.min(stack.stackSize, crate.slots[j].getMaxStackSize() - crate.slots[j].stackSize);
					if(add > 0) {
						crate.slots[j].stackSize += add;
						stack.stackSize -= add;
						if(stack.stackSize <= 0) {
							slots[i] = null;
							break;
						}
					}
				}
			}

			if(slots[i] == null) continue;

			for(int j = 0; j < 18; j++) {
				if(crate.slots[j] == null) {
					crate.slots[j] = stack.copy();
					slots[i] = null;
					break;
				}
			}
		}

		crate.markChanged();
		worldObj.playSoundEffect(baseX + 0.5, baseY + 0.5, baseZ + 0.5, "hbm:block.storageClose", 2.0F, 1.0F);
	}

	@Override
	public boolean hitByEntity(Entity attacker) {

		if(this.isDead) return false;

		if(attacker instanceof EntityPlayer && !worldObj.isRemote) {
			this.setDead();
			for(ItemStack stack : slots) {
				if(stack != null) this.entityDropItem(stack, 1F);
			}
			this.entityDropItem(new ItemStack(ModItems.drone, 1, EnumDroneType.SPELUNKER.ordinal()), 1F);
		}

		return false;
	}

	@Override
	public double getSpeed() {
		return 0.375D;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		nbt.setInteger("baseX", baseX);
		nbt.setInteger("baseY", baseY);
		nbt.setInteger("baseZ", baseZ);
		nbt.setByte("state", (byte) this.state.ordinal());
		nbt.setInteger("oreX", oreX);
		nbt.setInteger("oreY", oreY);
		nbt.setInteger("oreZ", oreZ);
		nbt.setDouble("mineProgress", mineProgress);

		NBTTagList list = new NBTTagList();
		for(int i = 0; i < slots.length; i++) {
			if(slots[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		this.baseX = nbt.getInteger("baseX");
		this.baseY = nbt.getInteger("baseY");
		this.baseZ = nbt.getInteger("baseZ");
		this.state = DroneState.values()[nbt.getByte("state")];
		this.oreX = nbt.getInteger("oreX");
		this.oreY = nbt.getInteger("oreY");
		this.oreZ = nbt.getInteger("oreZ");
		this.mineProgress = nbt.getDouble("mineProgress");

		NBTTagList list = nbt.getTagList("items", 10);
		this.slots = new ItemStack[this.getSizeInventory()];
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			int j = nbt1.getByte("slot") & 255;
			if(j < this.slots.length) {
				this.slots[j] = ItemStack.loadItemStackFromNBT(nbt1);
			}
		}
	}

	@Override
	public int getSizeInventory() { return 27; }
	@Override public ItemStack getStackInSlot(int slot) { return slots[slot]; }
	@Override public String getInventoryName() { return "container.drone"; }
	@Override public boolean hasCustomInventoryName() { return false; }
	@Override public int getInventoryStackLimit() { return 64; }
	@Override public void markDirty() { }
	@Override public boolean isUseableByPlayer(EntityPlayer player) { return false; }
	@Override public void openInventory() { }
	@Override public void closeInventory() { }

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(this.slots[slot] != null) {
			ItemStack itemstack;
			if(this.slots[slot].stackSize <= amount) {
				itemstack = this.slots[slot];
				this.slots[slot] = null;
			} else {
				itemstack = this.slots[slot].splitStack(amount);
				if(this.slots[slot].stackSize == 0) {
					this.slots[slot] = null;
				}
			}
			return itemstack;
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if(this.slots[slot] != null) {
			ItemStack itemstack = this.slots[slot];
			this.slots[slot] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.slots[slot] = stack;
		if(stack != null && stack.stackSize > this.getInventoryStackLimit()) {
			stack.stackSize = this.getInventoryStackLimit();
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) { return false; }
}
