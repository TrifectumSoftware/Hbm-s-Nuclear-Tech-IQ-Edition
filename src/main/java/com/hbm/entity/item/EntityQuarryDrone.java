package com.hbm.entity.item;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemDrone.EnumDroneType;
import com.hbm.tileentity.network.TileEntityDroneCrate;
import com.hbm.tileentity.network.TileEntityQuarryMarker;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityQuarryDrone extends EntityDroneBase {

	public static final double mineRate = 3.0D;
	public static final int quarryDepth = 32;

	public ItemStack[] slots = new ItemStack[27];

	protected int baseX, baseY, baseZ;

	protected int minX, maxX, minZ, maxZ;
	protected int topY, bottomY;
	protected boolean boundsFound = false;

	protected int curX, curZ, curY;
	protected int dirX = 1;

	protected HashMap<BlockPos, Double> mineProgress = new HashMap<>();

	protected boolean quarryComplete = false;

	protected DroneState state = DroneState.WORKING;

	public enum DroneState {
		WORKING, RETURNING, HOMED
	}

	public EntityQuarryDrone(World world) {
		super(world);
		this.setSize(1.5F, 1.5F);
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
		case WORKING:
			this.doWork();
			break;
		case RETURNING:
			this.doReturn();
			break;
		case HOMED:
			break;
		}
	}

	protected void doWork() {

		if(!this.boundsFound) {
			if(!this.findMarkers()) {
				this.state = DroneState.HOMED;
				return;
			}
		}

		if(this.isFull()) {
			this.state = DroneState.RETURNING;
			return;
		}

		double dist = MathHelper.sqrt_double((curX + 0.5 - posX) * (curX + 0.5 - posX) + (curY + 1 - posY) * (curY + 1 - posY) + (curZ + 0.5 - posZ) * (curZ + 0.5 - posZ));
		if(dist > 1) {
			this.setTarget(curX + 0.5, curY + 1, curZ + 0.5);
			if(posY > topY + 2 || isCollidedHorizontally || isCollidedVertically) this.mineBlockAhead();
			return;
		}

		this.minePatch();

		if(this.isPatchClear()) {
		if(!this.jumpToWork()) this.advance();
		}
	}

	protected boolean jumpToWork() {

		int bx = curX, bz = curZ;
		int best = Integer.MAX_VALUE;

		for(int z = minZ; z <= maxZ; z++) {
			for(int x = minX; x <= maxX; x++) {
				Block block = worldObj.getBlock(x, curY, z);
				if(block.isAir(worldObj, x, curY, z) || this.isUnmineable(block, x, curY, z)) continue;
				int d = (x - curX) * (x - curX) + (z - curZ) * (z - curZ);
				if(d < best) {
					best = d;
					bx = x;
					bz = z;
				}
			}
		}

		if(best == Integer.MAX_VALUE) return false;

		this.curX = bx;
		this.curZ = bz;
		return true;
	}

	protected void minePatch() {

		for(int dx = -1; dx <= 1; dx++) {
			for(int dz = -1; dz <= 1; dz++) {
				int x = curX + dx;
				int z = curZ + dz;
				if(x < minX || x > maxX || z < minZ || z > maxZ) continue;
				this.mineBlock(x, curY, z);
			}
		}
	}

	protected boolean isPatchClear() {

		for(int dx = -1; dx <= 1; dx++) {
			for(int dz = -1; dz <= 1; dz++) {
				int x = curX + dx;
				int z = curZ + dz;
				if(x < minX || x > maxX || z < minZ || z > maxZ) continue;
				Block block = worldObj.getBlock(x, curY, z);
				if(block.isAir(worldObj, x, curY, z) || this.isUnmineable(block, x, curY, z)) continue;
				return false;
			}
		}
		return true;
	}

		protected void advance() {

		int nextX = curX + dirX;
		if(nextX >= minX && nextX <= maxX) {
			curX = nextX;
			return;
		}

		if(curZ >= maxZ) {
			if(curY > bottomY) {
				curY--;
				curX = minX;
				curZ = minZ;
				dirX = 1;
			} else {
				this.quarryComplete = true;
				this.state = DroneState.RETURNING;
			}
			return;
		}

		curZ++;
		dirX = -dirX;
		curX = dirX == 1 ? minX : maxX;
	}


	protected boolean findMarkers() {

		int radius = 64;
		BlockPos nearest = null;
		int best = Integer.MAX_VALUE;


		// directions to find the marker
		for(int dy = -4; dy <= 4; dy++) {
			for(int dx = -radius; dx <= radius; dx++) {
				for(int dz = -radius; dz <= radius; dz++) {
					int x = baseX + dx;
					int y = baseY + dy;
					int z = baseZ + dz;
					if(worldObj.getBlock(x, y, z) == ModBlocks.quarry_marker) {
						int d = dx * dx + dz * dz + dy * dy * 4;
						if(d < best) {
							best = d;
							nearest = new BlockPos(x, y, z);
						}
					}
				}
			}
		}

		if(nearest == null) return false;
        // BFS baby
		HashSet<BlockPos> markers = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		markers.add(nearest);
		queue.add(nearest);

		while(!queue.isEmpty()) {
			BlockPos current = queue.poll();
			TileEntity te = worldObj.getTileEntity(current.getX(), current.getY(), current.getZ());
			if(!(te instanceof TileEntityQuarryMarker)) continue;
			TileEntityQuarryMarker marker = (TileEntityQuarryMarker) te;
			for(int i = 0; i < 3; i++) {
				if(marker.links[i] == null) continue;
				BlockPos n = new BlockPos(marker.links[i][0], marker.links[i][1], marker.links[i][2]);
				if(markers.add(n)) queue.add(n);
			}
		}

		if(markers.size() < 2) return false;

		int loX = Integer.MAX_VALUE, loY = Integer.MAX_VALUE, loZ = Integer.MAX_VALUE;
		int hiX = Integer.MIN_VALUE, hiY = Integer.MIN_VALUE, hiZ = Integer.MIN_VALUE;
		for(BlockPos pos : markers) {
			if(pos.getX() < loX) loX = pos.getX();
			if(pos.getX() > hiX) hiX = pos.getX();
			if(pos.getY() < loY) loY = pos.getY();
			if(pos.getY() > hiY) hiY = pos.getY();
			if(pos.getZ() < loZ) loZ = pos.getZ();
			if(pos.getZ() > hiZ) hiZ = pos.getZ();
		}

		this.minX = loX + 1;
		this.maxX = hiX - 1;
		this.minZ = loZ + 1;
		this.maxZ = hiZ - 1;

		if(minX > maxX || minZ > maxZ) return false;

		this.topY = hiY - 1;

		if(hiY > loY) {
			this.bottomY = loY + 1;
		} else {
			this.bottomY = Math.max(1, topY - quarryDepth);
		}

		if(topY < bottomY) return false;

		int workY = -1;
		outer: for(int y = topY; y >= bottomY; y--) {
			for(int x = minX; x <= maxX; x++) {
				for(int z = minZ; z <= maxZ; z++) {
					Block block = worldObj.getBlock(x, y, z);
					if(!block.isAir(worldObj, x, y, z) && !this.isUnmineable(block, x, y, z)) {
						workY = y;
						break outer;
					}
				}
			}
		}
		if(workY == -1) return false;


		int[][] corners = { {minX, minZ}, {maxX, minZ}, {minX, maxZ}, {maxX, maxZ} };
		int corner = Math.abs(this.getEntityId()) % 4;
		this.curX = corners[corner][0];
		this.curZ = corners[corner][1];
		this.curY = workY;
		this.dirX = 1;
		this.boundsFound = true;
		return true;
	}

	protected void doReturn() {

		Vec3 dist = Vec3.createVectorHelper(posX - (baseX + 0.5), posY - (baseY + 1), posZ - (baseZ + 0.5));

		if(dist.lengthVector() < 1.5) {

			TileEntity te = worldObj.getTileEntity(baseX, baseY, baseZ);

			if(!(te instanceof TileEntityDroneCrate)) {
				for(ItemStack stack : slots) {
					if(stack != null) this.entityDropItem(stack, 0F);
				}
				this.entityDropItem(new ItemStack(ModItems.drone, 1, EnumDroneType.QUARRY.ordinal()), 0F);
				this.setDead();
				return;
			}

			this.unloadToCrate((TileEntityDroneCrate) te);

			if(this.isEmpty()) {
				this.setAppearance(0);
				this.state = this.quarryComplete ? DroneState.HOMED : DroneState.WORKING;
			}

		} else {
			this.setTarget(baseX + 0.5, baseY + 1, baseZ + 0.5);
		}
	}

	protected void mineBlockAhead() {

		if(this.targetY == -1) return;

		Vec3 dir = Vec3.createVectorHelper(targetX - posX, targetY - posY, targetZ - posZ);
		if(dir.lengthVector() < 0.01) return;
		dir = dir.normalize();

		int cx = MathHelper.floor_double(posX + dir.xCoord);
		int cy = MathHelper.floor_double(posY + 0.375 + dir.yCoord);
		int cz = MathHelper.floor_double(posZ + dir.zCoord);


		for(int dx = -1; dx <= 1; dx++) {
			for(int dz = -1; dz <= 1; dz++) {
				int x = cx + dx;
				int z = cz + dz;
				if(x < minX || x > maxX || z < minZ || z > maxZ) continue;
				this.mineBlock(x, cy, z);
			}
		}
	}

	protected void mineBlock(int x, int y, int z) {

		Block block = worldObj.getBlock(x, y, z);
		int meta = worldObj.getBlockMetadata(x, y, z);

		if(this.isUnmineable(block, x, y, z)) return;

		float hardness = block.getBlockHardness(worldObj, x, y, z);
		if(hardness < 0) return;

		BlockPos key = new BlockPos(x, y, z);
		double progress = this.mineProgress.getOrDefault(key, 0D);

		if(hardness == 0) {
			this.breakBlock(x, y, z, block, meta);
			this.mineProgress.remove(key);
			return;
		}

		progress += mineRate / (hardness * 15D);

		if(progress >= 1) {
			this.breakBlock(x, y, z, block, meta);
			this.mineProgress.remove(key);
		} else {
			this.mineProgress.put(key, progress);
			worldObj.destroyBlockInWorldPartially(breakerId(x, y, z), x, y, z, (int) Math.floor(progress * 10));
		}
	}

	protected void breakBlock(int x, int y, int z, Block block, int meta) {

		worldObj.playAuxSFXAtEntity(null, 2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
		worldObj.setBlockToAir(x, y, z);
		if(this.isOre(block, meta)) return;
		for(ItemStack drop : this.getMinedDrops(block, x, y, z, meta)) {
			if(!this.addToStorage(this.slots, drop)) {
				this.entityDropItem(drop, 0F);
			}
		}

		if(!this.isEmpty()) this.setAppearance(1);
	}

	protected boolean isUnmineable(Block block, int x, int y, int z) {
		if(block.isAir(worldObj, x, y, z) || block.getMaterial().isLiquid()) return true;
		if(block == Blocks.bedrock) return true;
		if(block == ModBlocks.quarry_marker) return true;
		if(block == ModBlocks.drone_crate || block == ModBlocks.drone_dock) return true;
		if(block == ModBlocks.drone_crate_provider || block == ModBlocks.drone_crate_requester) return true;
		if(block == ModBlocks.drone_waypoint || block == ModBlocks.drone_waypoint_request) return true;
		return x == baseX && z == baseZ;
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

	protected int breakerId(int x, int y, int z) {
		return this.getEntityId() * 1000003 + x * 31 + y * 17 + z;
	}

	@Override
	public boolean hitByEntity(Entity attacker) {

		if(this.isDead) return false;

		if(attacker instanceof EntityPlayer && !worldObj.isRemote) {
			this.setDead();
			for(ItemStack stack : slots) {
				if(stack != null) this.entityDropItem(stack, 1F);
			}
			this.entityDropItem(new ItemStack(ModItems.drone, 1, EnumDroneType.QUARRY.ordinal()), 1F);
		}

		return false;
	}

	@Override
	protected boolean canClimbWalls() {
		return false;
	}

	@Override
	public double getSpeed() {
		return 0.25D; // slower than a regular transport drone
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		nbt.setInteger("baseX", baseX);
		nbt.setInteger("baseY", baseY);
		nbt.setInteger("baseZ", baseZ);
		nbt.setByte("state", (byte) this.state.ordinal());
		nbt.setBoolean("boundsFound", boundsFound);
		nbt.setBoolean("quarryComplete", quarryComplete);
		nbt.setInteger("minX", minX);
		nbt.setInteger("maxX", maxX);
		nbt.setInteger("topY", topY);
		nbt.setInteger("bottomY", bottomY);
		nbt.setInteger("minZ", minZ);
		nbt.setInteger("maxZ", maxZ);
		nbt.setInteger("curX", curX);
		nbt.setInteger("curY", curY);
		nbt.setInteger("curZ", curZ);
		nbt.setInteger("dirX", dirX);

		NBTTagCompound mine = new NBTTagCompound();
		int i = 0;
		for(Entry<BlockPos, Double> entry : mineProgress.entrySet()) {
			NBTTagCompound nbt1 = new NBTTagCompound();
			BlockPos pos = entry.getKey();
			nbt1.setIntArray("pos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
			nbt1.setDouble("progress", entry.getValue());
			mine.setTag("entry" + i, nbt1);
			i++;
		}
		mine.setInteger("size", i);
		nbt.setTag("mine", mine);

		NBTTagList items = new NBTTagList();
		for(i = 0; i < slots.length; i++) {
			if(slots[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				slots[i].writeToNBT(nbt1);
				items.appendTag(nbt1);
			}
		}
		nbt.setTag("items", items);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		this.baseX = nbt.getInteger("baseX");
		this.baseY = nbt.getInteger("baseY");
		this.baseZ = nbt.getInteger("baseZ");
		this.state = DroneState.values()[nbt.getByte("state")];
		this.boundsFound = nbt.getBoolean("boundsFound");
		this.quarryComplete = nbt.getBoolean("quarryComplete");
		this.minX = nbt.getInteger("minX");
		this.maxX = nbt.getInteger("maxX");
		this.topY = nbt.getInteger("topY");
		this.bottomY = nbt.getInteger("bottomY");
		this.minZ = nbt.getInteger("minZ");
		this.maxZ = nbt.getInteger("maxZ");
		this.curX = nbt.getInteger("curX");
		this.curY = nbt.getInteger("curY");
		this.curZ = nbt.getInteger("curZ");
		this.dirX = nbt.getInteger("dirX");

		this.mineProgress.clear();
		NBTTagCompound mine = nbt.getCompoundTag("mine");
		int size = mine.getInteger("size");
		for(int i = 0; i < size; i++) {
			NBTTagCompound nbt1 = mine.getCompoundTag("entry" + i);
			int[] pos = nbt1.getIntArray("pos");
			this.mineProgress.put(new BlockPos(pos[0], pos[1], pos[2]), nbt1.getDouble("progress"));
		}

		this.slots = new ItemStack[27];
		NBTTagList items = nbt.getTagList("items", 10);
		for(int i = 0; i < items.tagCount(); i++) {
			NBTTagCompound nbt1 = items.getCompoundTagAt(i);
			int j = nbt1.getByte("slot") & 255;
			if(j < this.slots.length) {
				this.slots[j] = ItemStack.loadItemStackFromNBT(nbt1);
			}
		}
	}
}
