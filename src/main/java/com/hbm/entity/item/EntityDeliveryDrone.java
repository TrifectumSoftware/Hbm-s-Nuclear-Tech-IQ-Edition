package com.hbm.entity.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableSet;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import com.hbm.util.ChunkShapeHelper;
import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public class EntityDeliveryDrone extends EntityDroneBase implements IInventory, IChunkLoader {

	protected ItemStack[] slots = new ItemStack[this.getSizeInventory()];
	public FluidStack fluid;

	protected boolean chunkLoading = false;
	private Ticket loaderTicket;

	protected boolean demolisher = false;
	protected HashMap<BlockPos, Double> mineProgressMap = new HashMap();

	private static final ImmutableSet<Block> protectedBlocks = ImmutableSet.of(
			ModBlocks.drone_waypoint, ModBlocks.drone_crate, ModBlocks.drone_waypoint_request,
			ModBlocks.drone_dock, ModBlocks.drone_crate_provider, ModBlocks.drone_crate_requester);

	public EntityDeliveryDrone(World world) {
		super(world);
	}

	@Override
	public boolean hitByEntity(Entity attacker) {

		if(this.isDead) return false;

		if(attacker instanceof EntityPlayer && !worldObj.isRemote) {
			this.setDead();
			for (ItemStack stack : slots) {
				if(stack != null)
					this.entityDropItem(stack, 1F);
			}
			int meta = 0;

			//demolishers use base 5 so the express and chunkloading bits line up with the patrol variants
			if(demolisher)
				meta += 5;

			//whether it is an express drone
			if(this.dataWatcher.getWatchableObjectByte(11) == 1)
				meta += 2;

			if(chunkLoading)
				meta += 1;

			this.entityDropItem(new ItemStack(ModItems.drone, 1, meta), 1F);
		}

		return false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(11, new Byte((byte) 0));
		this.dataWatcher.addObject(12, new Byte((byte) 0));
	}

	public void setChunkLoading() {
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, worldObj, Type.ENTITY));
		this.chunkLoading = true;
	}

	public void setDemolisher() {
		this.demolisher = true;
		this.dataWatcher.updateObject(12, (byte) 1);
	}

	public boolean isDemolisher() {
		return this.dataWatcher.getWatchableObjectByte(12) == 1;
	}


	@Override
	public double getSpeed() {
		return this.dataWatcher.getWatchableObjectByte(11) == 1 ? 0.375 * 3 : 0.375;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!worldObj.isRemote && demolisher) {
			this.tryMine();
		}
	}

	@Override
	protected boolean canClimbWalls() {
		return !demolisher;
	}

	protected void tryMine() {

		if(targetY == -1) return;

		Vec3 dir = Vec3.createVectorHelper(targetX - posX, targetY - posY, targetZ - posZ);

		if(dir.lengthVector() < 0.01) return;

		dir = dir.normalize();

		boolean travelX = Math.abs(dir.xCoord) > Math.abs(dir.zCoord);
		int cy = MathHelper.floor_double(posY + 0.375);
		int hx, hz;
		if(travelX) {
			hx = MathHelper.floor_double(posX + Math.signum(dir.xCoord));
			hz = MathHelper.floor_double(posZ);
		} else {
			hx = MathHelper.floor_double(posX);
			hz = MathHelper.floor_double(posZ + Math.signum(dir.zCoord));
		}

		for(int dy = -1; dy <= 1; dy++) {
			for(int dw = -1; dw <= 1; dw++) {
				int x, y, z;
				if(travelX) {
					x = hx;
					y = cy + dy;
					z = hz + dw;
				} else {
					x = hx + dw;
					y = cy + dy;
					z = hz;
				}
				mineBlock(new BlockPos(x, y, z));
			}
		}

		Iterator<BlockPos> it = mineProgressMap.keySet().iterator();
		while(it.hasNext()) {
			BlockPos pos = it.next();
			boolean inHead;
			if(travelX) {
				inHead = pos.getX() == hx && Math.abs(pos.getY() - cy) <= 1 && Math.abs(pos.getZ() - hz) <= 1;
			} else {
				inHead = pos.getZ() == hz && Math.abs(pos.getY() - cy) <= 1 && Math.abs(pos.getX() - hx) <= 1;
			}
			if(!inHead) {
				clearCrack(pos);
				it.remove();
			}
		}
	}

	protected void mineBlock(BlockPos pos) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		Block block = worldObj.getBlock(x, y, z);
		float hardness = block.getBlockHardness(worldObj, x, y, z);

		if(block.isAir(worldObj, x, y, z) || hardness < 0 || block == Blocks.bedrock || block.getMaterial().isLiquid() || protectedBlocks.contains(block)) {
			clearCrack(pos);
			mineProgressMap.remove(pos);
			return;
		}

		if(hardness == 0) {
			breakBlock(x, y, z);
			clearCrack(pos);
			mineProgressMap.remove(pos);
			return;
		}

		double progress = mineProgressMap.containsKey(pos) ? mineProgressMap.get(pos) : 0D;
		progress += getSpeed() / (hardness * 15D);

		if(progress >= 1) {
			breakBlock(x, y, z);
			clearCrack(pos);
			mineProgressMap.remove(pos);
		} else {
			mineProgressMap.put(pos, progress);
			worldObj.destroyBlockInWorldPartially(breakerId(x, y, z), x, y, z, (int) Math.floor(progress * 10));
		}
	}

	protected void breakBlock(int x, int y, int z) {
		Block block = worldObj.getBlock(x, y, z);
		int meta = worldObj.getBlockMetadata(x, y, z);
		worldObj.playAuxSFXAtEntity(null, 2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
		worldObj.setBlockToAir(x, y, z);
	}
	protected void clearCrack(BlockPos pos) {
		worldObj.destroyBlockInWorldPartially(breakerId(pos.getX(), pos.getY(), pos.getZ()), pos.getX(), pos.getY(), pos.getZ(), -1);
	}


	protected int breakerId(int x, int y, int z) {
		return this.getEntityId() * 1000003 + x * 31 + y * 17 + z;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		NBTTagList nbttaglist = new NBTTagList();

		for(int i = 0; i < this.slots.length; ++i) {
			if(this.slots[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.slots[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbt.setTag("Items", nbttaglist);

		if(fluid != null) {
			nbt.setInteger("fluidType", fluid.type.getID());
			nbt.setInteger("fluidAmount", fluid.fill);
		}

		nbt.setByte("load", this.dataWatcher.getWatchableObjectByte(11));
		nbt.setBoolean("chunkLoading", chunkLoading);
		nbt.setBoolean("demolisher", demolisher);

		NBTTagList mineGrid = new NBTTagList();
		for(Entry<BlockPos, Double> entry : mineProgressMap.entrySet()) {
			NBTTagCompound mineNBT = new NBTTagCompound();
			BlockPos pos = entry.getKey();
			mineNBT.setIntArray("pos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
			mineNBT.setDouble("progress", entry.getValue());
			mineGrid.appendTag(mineNBT);
		}
		nbt.setTag("mineGrid", mineGrid);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		NBTTagList nbttaglist = nbt.getTagList("Items", 10);
		this.slots = new ItemStack[this.getSizeInventory()];

		for(int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;

			if(j >= 0 && j < this.slots.length) {
				this.slots[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}

		if(nbt.hasKey("fluidType")) {
			FluidType type = Fluids.fromNameCompat(nbt.getString("fluidType"));
			if(type != Fluids.NONE) {
				nbt.removeTag(nbt.getString("fluidType"));
			} else
				type = Fluids.fromID(nbt.getInteger("fluidType"));

			this.fluid = new FluidStack(type, nbt.getInteger("fluidAmount"));
		}

		this.dataWatcher.updateObject(11, nbt.getByte("load"));
		if(nbt.getBoolean("chunkLoading")) this.setChunkLoading();
		if(nbt.getBoolean("demolisher")) this.setDemolisher();

		mineProgressMap.clear();
		if(nbt.hasKey("mineGrid")) {
			NBTTagList mineGrid = nbt.getTagList("mineGrid", 10);
			for(int i = 0; i < mineGrid.tagCount(); i++) {
				NBTTagCompound mineNBT = mineGrid.getCompoundTagAt(i);
				int[] pos = mineNBT.getIntArray("pos");
				mineProgressMap.put(new BlockPos(pos[0], pos[1], pos[2]), mineNBT.getDouble("progress"));
			}
		}
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slots[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(this.slots[slot] != null) {
			ItemStack itemstack;

			if(this.slots[slot].stackSize <= amount) {
				itemstack = this.slots[slot];
				this.slots[slot] = null;
				return itemstack;
			} else {
				itemstack = this.slots[slot].splitStack(amount);

				if(this.slots[slot].stackSize == 0) {
					this.slots[slot] = null;
				}

				return itemstack;
			}
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

	@Override public int getSizeInventory() { return 18; }
	@Override public String getInventoryName() { return "container.drone"; }
	@Override public int getInventoryStackLimit() { return 64; }
	@Override public boolean hasCustomInventoryName() { return false; }
	@Override public boolean isUseableByPlayer(EntityPlayer player) { return false; }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return false; }

	@Override public void markDirty() { }
	@Override public void openInventory() { }
	@Override public void closeInventory() { }

	@Override
	protected void loadNeighboringChunks() {
		if(!worldObj.isRemote && loaderTicket != null) {

			for(ChunkCoordIntPair chunk : ImmutableSet.copyOf(loaderTicket.getChunkList())) {
				ForgeChunkManager.unforceChunk(loaderTicket, chunk);
			}

			// This is the lowest padding that worked with my drone waypoint path. if they stop getting loaded crank up paddingSize
			for (ChunkCoordIntPair chunk : ChunkShapeHelper.getChunksAlongLineSegment((int) Math.floor(this.posX), (int) Math.floor(this.posZ), (int) Math.floor(this.posX + this.motionX), (int) Math.floor(this.posZ + this.motionZ), 8)){
				ForgeChunkManager.forceChunk(loaderTicket, chunk);
			}
		}
	}

	@Override
	public void setDead() {
		super.setDead();
		this.clearChunkLoader();
	}

	public void clearChunkLoader() {
		if(!worldObj.isRemote && loaderTicket != null) {
			ForgeChunkManager.releaseTicket(loaderTicket);
			this.loaderTicket = null;
		}
	}

	@Override
	public void init(Ticket ticket) {
		if(!worldObj.isRemote && ticket != null) {
			if(loaderTicket == null) {
				loaderTicket = ticket;
				loaderTicket.bindEntity(this);
				loaderTicket.getModData();
			}
			this.loadNeighboringChunks();
		}
	}
}
