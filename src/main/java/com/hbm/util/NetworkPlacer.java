package com.hbm.util;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.tileentity.network.TileEntityPipelineBase;
import com.hbm.tileentity.network.TileEntityPylonBase;
import com.hbm.util.fauxpointtwelve.BlockPos;

import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NetworkPlacer {

	private static final String SET = "nw_set";

	public static boolean handle(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, Block block, int meta, int fluid) {

		boolean span = isSpan(block);
		if(!span && !isLine(block)) return false;

		if(player.isSneaking()) {
			if(hasPending(stack)) {
				stack.stackTagCompound = null;
				if(world.isRemote) player.addChatMessage(new ChatComponentText("Selection cleared"));
				return true;
			}
			return false;
		}

		if(!hasPending(stack)) {
			NBTTagCompound nbt = stack.stackTagCompound = new NBTTagCompound();
			nbt.setBoolean(SET, true);
			nbt.setInteger("nw_side", side);
			nbt.setInteger("nw_count", count(player, stack));

			BlockPos pos = placePos(world, block, x, y, z, side);
			nbt.setInteger("nw_x", pos.getX());
			nbt.setInteger("nw_y", pos.getY());
			nbt.setInteger("nw_z", pos.getZ());

			TileEntity te = world.getTileEntity(x, y, z);
			if(te instanceof TileEntityPipeBaseNT) nbt.setInteger("nw_fluid", ((TileEntityPipeBaseNT) te).getType().getID());
			nbt.setInteger("nw_itemFluid", fluid);
			nbt.setInteger("nw_meta", meta);

			if(world.isRemote) player.addChatMessage(new ChatComponentText("Start set"));
			return true;
		}

		NBTTagCompound nbt = stack.stackTagCompound;
		int sx = nbt.getInteger("nw_x");
		int sy = nbt.getInteger("nw_y");
		int sz = nbt.getInteger("nw_z");
		int sSide = nbt.getInteger("nw_side");
		int max = nbt.getInteger("nw_count");
		int startFluid = nbt.getInteger("nw_fluid");
		stack.stackTagCompound = null;

		if(!world.isRemote) {
			BlockPos end = placePos(world, block, x, y, z, side);
			TileEntity endTe = world.getTileEntity(x, y, z);
			int type = fluid > 0 ? fluid : startFluid;
			if(type <= 0 && endTe instanceof TileEntityPipeBaseNT) type = ((TileEntityPipeBaseNT) endTe).getType().getID();
			int placed = 0;

			if(span && !isNodeAt(world, sx, sy, sz)) {
				if(!world.getBlock(sx, sy, sz).isReplaceable(world, sx, sy, sz)) {
					player.addChatMessage(new ChatComponentText("Obstructed, build cancelled"));
					return true;
				}
				place(world, player, block, meta, sx, sy, sz, sSide);
				placed++;
			}

			double range = span ? rangeAt(world, sx, sy, sz, block) : 0;
			List<BlockPos> path = calcRoute(world, sx, sy, sz, end.getX(), end.getY(), end.getZ(), range);

			if(path == null) {
				if(placed > 0) consume(player, stack, placed);
				player.addChatMessage(new ChatComponentText("Cannot build here" + (placed > 0 ? " (start placed)" : "")));
				return true;
			}

			int needed = 0;

			for(BlockPos pos : path) {
				if(isNodeAt(world, pos.getX(), pos.getY(), pos.getZ())) continue;
				if(!world.getBlock(pos.getX(), pos.getY(), pos.getZ()).isReplaceable(world, pos.getX(), pos.getY(), pos.getZ())) {
					if(placed > 0) consume(player, stack, placed);
					player.addChatMessage(new ChatComponentText("Obstructed, build cancelled" + (placed > 0 ? " (start placed)" : "")));
					return true;
				}
				needed++;
			}

			if(needed > max - placed) {
				if(placed > 0) consume(player, stack, placed);
				player.addChatMessage(new ChatComponentText("Not enough blocks, build cancelled" + (placed > 0 ? " (start placed)" : "")));
				return true;
			}

			for(BlockPos pos : path) {
				if(isNodeAt(world, pos.getX(), pos.getY(), pos.getZ())) continue;
				place(world, player, block, meta, pos.getX(), pos.getY(), pos.getZ(), sSide);

				if(type > 0) {
					TileEntity te = world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
					if(te instanceof TileEntityPipeBaseNT) ((TileEntityPipeBaseNT) te).setType(Fluids.fromID(type));
				}
			}

			TileEntity prev = null;

			for(BlockPos pos : path) {
				TileEntity cur = nodeAt(world, pos.getX(), pos.getY(), pos.getZ());
				if(cur == null) { prev = null; continue; }
				if(prev != null) link(prev, cur);
				prev = cur;
			}

			if(placed + needed > 0) {
				consume(player, stack, placed + needed);
				player.addChatMessage(new ChatComponentText("Placed " + (placed + needed) + " block(s)"));
			}
		}

		return true;
	}

	public static void update(ItemStack stack, EntityPlayer player, boolean inHand) {

		if(!inHand && hasPending(stack)) {
			ItemStack held = player.getHeldItem();

			if(held == null || held.getItem() != stack.getItem() || held.getItemDamage() != stack.getItemDamage()) {
				stack.stackTagCompound = null;

				if(player.worldObj.isRemote) NetworkPlacerClient.clear();
			}
		}

		if(player.worldObj.isRemote && inHand) NetworkPlacerClient.preview(stack, player);
	}

	public static boolean hasPending(ItemStack stack) {
		return stack.stackTagCompound != null && stack.stackTagCompound.getBoolean(SET);
	}

	public static Block blockOf(ItemStack stack) {
		Block block = Block.getBlockFromItem(stack.getItem());
		return block != null ? block : ModBlocks.fluid_duct_neo;
	}

	public static BlockPos placePos(World world, Block block, int x, int y, int z, int side) {

		if(isSpan(block) && isNodeAt(world, x, y, z)) return new BlockPos(x, y, z);

		return new BlockPos(x + off(side, 0), y + off(side, 1), z + off(side, 2));
	}

	public static List<BlockPos> calcPath(int x1, int y1, int z1, int x2, int y2, int z2, double range) {

		List<BlockPos> path = new ArrayList<>();

		if(range > 0) {
			double dx = x2 - x1;
			double dy = y2 - y1;
			double dz = z2 - z1;
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if(dist < 0.5D) return null;

			int links = (int) Math.ceil(dist / range);
			if(links < 1) links = 1;

			for(int i = 0; i <= links; i++) {
				path.add(new BlockPos((int) Math.round(x1 + dx * i / links), (int) Math.round(y1 + dy * i / links), (int) Math.round(z1 + dz * i / links)));
			}
		} else {
			int x = x1, y = y1, z = z1;
			path.add(new BlockPos(x, y, z));

			while(x != x2) { x += x2 > x ? 1 : -1; path.add(new BlockPos(x, y, z)); }
			while(z != z2) { z += z2 > z ? 1 : -1; path.add(new BlockPos(x, y, z)); }
			while(y != y2) { y += y2 > y ? 1 : -1; path.add(new BlockPos(x, y, z)); }
		}

		return path;
	}

	public static List<BlockPos> calcRoute(World world, int x1, int y1, int z1, int x2, int y2, int z2, double range) {

		List<BlockPos> path = calcPath(x1, y1, z1, x2, y2, z2, range);
		if(path == null) return null;

		List<BlockPos> route = new ArrayList<>();
		route.add(path.get(0));

		if(range > 0) {
			for(int i = 1; i < path.size(); i++) {
				BlockPos pos = resolve(world, route, path.get(i), i < path.size() - 1);
				if(pos == null) return null;

				for(int j = 0; j < 8; j++) {
					BlockPos prev = route.get(route.size() - 1);
					if(within(prev, pos, range)) break;

					BlockPos mid = resolve(world, route, new BlockPos((prev.getX() + pos.getX()) / 2, (prev.getY() + pos.getY()) / 2, (prev.getZ() + pos.getZ()) / 2), true);
					if(mid == null || mid.equals(prev) || mid.equals(pos)) break;
					route.add(mid);
				}

				route.add(pos);
			}
			return route;
		}

		BlockPos cur = path.get(0);
		int elev = 0;

		for(int i = 1; i < path.size(); i++) {
			BlockPos ideal = path.get(i);
			int next = elev;

			while(!isFree(world, ideal.add(0, next, 0))) {
				if(++next > 16) return null;
			}

			while(elev < next) {
				cur = cur.add(0, 1, 0);
				if(!isFree(world, cur)) return null;
				route.add(cur);
				elev++;
			}

			cur = ideal.add(0, elev, 0);
			route.add(cur);

			while(elev > 0 && isFree(world, ideal.add(0, elev - 1, 0))) {
				elev--;
				cur = ideal.add(0, elev, 0);
				route.add(cur);
			}
		}

		return route;
	}

	private static BlockPos resolve(World world, List<BlockPos> route, BlockPos pos, boolean descend) {

		for(int i = 0; i < 16 && !isFree(world, pos); i++) pos = pos.add(0, 1, 0);
		if(!isFree(world, pos)) return null;

		if(descend) {
			for(int i = 0; i < 64 && pos.getY() > 0; i++) {
				BlockPos below = pos.add(0, -1, 0);
				if(!world.isAirBlock(below.getX(), below.getY(), below.getZ()) || route.contains(below)) break;
				pos = below;
			}
		}

		return pos;
	}

	private static boolean within(BlockPos first, BlockPos second, double range) {
		double dx = first.getX() - second.getX();
		double dy = first.getY() - second.getY();
		double dz = first.getZ() - second.getZ();
		return dx * dx + dy * dy + dz * dz <= range * range;
	}

	public static boolean isFree(World world, BlockPos pos) {
		return isNodeAt(world, pos.getX(), pos.getY(), pos.getZ()) || world.getBlock(pos.getX(), pos.getY(), pos.getZ()).isReplaceable(world, pos.getX(), pos.getY(), pos.getZ());
	}

	public static int placedMeta(World world, EntityPlayer player, Block block, int meta, int x, int y, int z, int side) {

		if(!(block instanceof BlockDummyable)) return block.onBlockPlaced(world, x, y, z, side, 0.5F, 0.5F, 0.5F, meta);

		ForgeDirection placed = ForgeDirection.getOrientation(side);
		if(placed != ForgeDirection.UP && placed != ForgeDirection.DOWN) return BlockDummyable.offset + placed.ordinal();

		int i = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		return BlockDummyable.offset + (i == 0 ? 2 : i == 1 ? 5 : i == 2 ? 3 : 4);
	}

	static void linkPreview(TileEntity first, TileEntity second) {

		if(first == null || second == null || first == second) return;

		if(first instanceof TileEntityPylonBase && second instanceof TileEntityPylonBase) {
			if(TileEntityPylonBase.canConnect((TileEntityPylonBase) first, (TileEntityPylonBase) second) == 0) {
				((TileEntityPylonBase) first).getConnected().add(new int[] { second.xCoord, second.yCoord, second.zCoord });
				((TileEntityPylonBase) second).getConnected().add(new int[] { first.xCoord, first.yCoord, first.zCoord });
			}
		} else if(first instanceof TileEntityPipelineBase && second instanceof TileEntityPipelineBase) {
			if(TileEntityPipelineBase.canConnect((TileEntityPipelineBase) first, (TileEntityPipelineBase) second) == 0) {
				((TileEntityPipelineBase) first).getConnected().add(new int[] { second.xCoord, second.yCoord, second.zCoord });
				((TileEntityPipelineBase) second).getConnected().add(new int[] { first.xCoord, first.yCoord, first.zCoord });
			}
		}
	}

	public static boolean isLine(Block block) {
		return block == ModBlocks.fluid_duct_neo || block == ModBlocks.fluid_duct_box || block == ModBlocks.fluid_duct_exhaust || block == ModBlocks.fluid_duct_paintable || block == ModBlocks.fluid_duct_gauge || block == ModBlocks.fluid_duct_paintable_block_exhaust
				|| block == ModBlocks.red_cable || block == ModBlocks.red_cable_classic || block == ModBlocks.red_cable_paintable || block == ModBlocks.red_cable_gauge || block == ModBlocks.red_cable_box || block == ModBlocks.red_wire_coated;
	}

	public static boolean isSpan(Block block) {
		return block == ModBlocks.pipe_anchor || block == ModBlocks.pipe_anchor_industrial
				|| block == ModBlocks.red_pylon || block == ModBlocks.red_pylon_steel || block == ModBlocks.red_pylon_medium_wood || block == ModBlocks.red_pylon_medium_wood_transformer
				|| block == ModBlocks.red_pylon_medium_steel || block == ModBlocks.red_pylon_medium_steel_transformer || block == ModBlocks.red_pylon_large
				|| block == ModBlocks.red_connector || block == ModBlocks.red_connector_super || block == ModBlocks.substation;
	}

	public static boolean isNodeAt(World world, int x, int y, int z) {
		return isNode(world.getTileEntity(x, y, z));
	}

	public static boolean isNode(TileEntity te) {
		return te instanceof TileEntityPylonBase || te instanceof TileEntityPipelineBase;
	}

	public static double rangeAt(World world, int x, int y, int z, Block block) {

		TileEntity te = nodeAt(world, x, y, z);
		double range = rangeOf(te);

		if(range > 0) return range;

		if(block == ModBlocks.pipe_anchor) return 10;
		if(block == ModBlocks.pipe_anchor_industrial) return 100;
		if(block == ModBlocks.red_pylon || block == ModBlocks.red_pylon_steel) return 25;
		if(block == ModBlocks.red_pylon_medium_wood || block == ModBlocks.red_pylon_medium_steel) return 45;
		if(block == ModBlocks.red_pylon_medium_wood_transformer || block == ModBlocks.red_pylon_medium_steel_transformer) return 45;
		if(block == ModBlocks.red_pylon_large) return 100;
		if(block == ModBlocks.red_connector) return 10;
		if(block == ModBlocks.red_connector_super) return 100;
		if(block == ModBlocks.substation) return 20;

		return 0;
	}

	private static double rangeOf(TileEntity te) {
		if(te instanceof TileEntityPylonBase) return ((TileEntityPylonBase) te).getMaxWireLength();
		if(te instanceof TileEntityPipelineBase) return ((TileEntityPipelineBase) te).getMaxPipeLength();
		return 0;
	}

	private static TileEntity nodeAt(World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(x, y, z);
		if(isNode(te)) return te;

		for(int i = -2; i <= 2; i++) {
			for(int j = -2; j <= 2; j++) {
				for(int k = -2; k <= 2; k++) {
					TileEntity near = world.getTileEntity(x + i, y + j, z + k);
					if(isNode(near)) return near;
				}
			}
		}

		return null;
	}

	private static void link(TileEntity first, TileEntity second) {

		if(first == null || second == null || first == second) return;

		if(first instanceof TileEntityPylonBase && second instanceof TileEntityPylonBase) {
			if(TileEntityPylonBase.canConnect((TileEntityPylonBase) first, (TileEntityPylonBase) second) == 0) {
				preparePylon((TileEntityPylonBase) first);
				preparePylon((TileEntityPylonBase) second);
				((TileEntityPylonBase) first).addConnection(second.xCoord, second.yCoord, second.zCoord);
				((TileEntityPylonBase) second).addConnection(first.xCoord, first.yCoord, first.zCoord);
			}
		} else if(first instanceof TileEntityPipelineBase && second instanceof TileEntityPipelineBase) {
			if(TileEntityPipelineBase.canConnect((TileEntityPipelineBase) first, (TileEntityPipelineBase) second) == 0) {
				((TileEntityPipelineBase) first).addConnection(second.xCoord, second.yCoord, second.zCoord);
				((TileEntityPipelineBase) second).addConnection(first.xCoord, first.yCoord, first.zCoord);
			}
		}
	}

	private static void preparePylon(TileEntityPylonBase pylon) {

		PowerNode node = Nodespace.getNode(pylon.getWorldObj(), pylon.xCoord, pylon.yCoord, pylon.zCoord);

		if(node == null || node.expired) Nodespace.createNode(pylon.getWorldObj(), pylon.createNode());
	}

	private static void place(World world, EntityPlayer player, Block block, int meta, int x, int y, int z, int side) {

		world.setBlock(x, y, z, block);
		int placedMeta = block.onBlockPlaced(world, x, y, z, side, 0.5F, 0.5F, 0.5F, meta);
		world.setBlockMetadataWithNotify(x, y, z, placedMeta, 3);
		block.onBlockPlacedBy(world, x, y, z, player, new ItemStack(block, 1, meta));
	}

	private static int off(int side, int axis) {
		switch(side) {
		case 0: return axis == 1 ? -1 : 0;
		case 1: return axis == 1 ? 1 : 0;
		case 2: return axis == 2 ? -1 : 0;
		case 3: return axis == 2 ? 1 : 0;
		case 4: return axis == 0 ? -1 : 0;
		case 5: return axis == 0 ? 1 : 0;
		default: return 0;
		}
	}

	private static int lerp(int a, int b, int i, int steps) {
		return steps <= 1 ? a : (int) Math.round(a + (b - a) * (i / (double) (steps - 1)));
	}

	private static int count(EntityPlayer player, ItemStack stack) {

		if(player.capabilities.isCreativeMode) return 256;

		int count = 0;

		for(ItemStack inventoryStack : player.inventory.mainInventory) {
			if(inventoryStack != null && inventoryStack.getItem() == stack.getItem() && inventoryStack.getItemDamage() == stack.getItemDamage()) count += inventoryStack.stackSize;
		}

		return count;
	}

	private static void consume(EntityPlayer player, ItemStack stack, int amount) {

		if(player.capabilities.isCreativeMode) return;

		for(ItemStack inventoryStack : player.inventory.mainInventory) {
			if(inventoryStack == null || inventoryStack.getItem() != stack.getItem() || inventoryStack.getItemDamage() != stack.getItemDamage()) continue;

			int remove = Math.min(amount, inventoryStack.stackSize);
			inventoryStack.stackSize -= remove;
			amount -= remove;

			if(amount <= 0) break;
		}

		player.inventoryContainer.detectAndSendChanges();
	}
}
