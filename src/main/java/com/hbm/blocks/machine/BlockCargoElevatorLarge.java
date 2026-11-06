package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ICustomBlockHighlight;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.TileEntityCargoElevatorLarge;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;

public class BlockCargoElevatorLarge extends BlockDummyable {

	public BlockCargoElevatorLarge() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityCargoElevatorLarge();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 2, 2, 2, 2};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) return true;
		if(player.isSneaking()) return false;
		int[] pos = ((BlockDummyable) ModBlocks.cargo_elevator_large).findCore(world, x, y, z);
		if(pos != null) {
			TileEntityCargoElevatorLarge elevator = (TileEntityCargoElevatorLarge) world.getTileEntity(pos[0], pos[1], pos[2]);

			x = pos[0];
			y = pos[1];
			z = pos[2];

			if(player.getHeldItem() != null && player.getHeldItem().getItem() == Item.getItemFromBlock(this)) {
				boolean replacable = true;
				for(int i = x - 2; i < x + 3; i++) for(int j = z - 2; j < z + 3; j++) {
					if(!world.getBlock(i, y + elevator.height + 1, j).isReplaceable(world, i, y + elevator.height + 1, j)) {
						replacable = false;
						break;
					}
				}

				if(replacable) {
					for(int i = x - 2; i < x + 3; i++) for(int j = z - 2; j < z + 3; j++) {
						world.setBlock(i, y + elevator.height + 1, j, ModBlocks.cargo_elevator_large, 1, 3);
					}
					elevator.height++;
					elevator.markDirty();
					if(!player.capabilities.isCreativeMode) player.getHeldItem().stackSize--;
				}
			} else {
				elevator.toggleElevator();
			}
		}
		return true;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		int[] pos = ((BlockDummyable) ModBlocks.cargo_elevator_large).findCore(world, x, y, z);
		if(pos != null) {
			TileEntityCargoElevatorLarge elevator = (TileEntityCargoElevatorLarge) world.getTileEntity(pos[0], pos[1], pos[2]);
			int toDrop = elevator.height + 1;
			ArrayList<ItemStack> drops = new ArrayList();
			while(toDrop > 0) {
				int perStack = Math.min(toDrop, 64);
				toDrop -= perStack;
				drops.add(new ItemStack(this, perStack));
			}
			return drops;
		}
		return super.getDrops(world, x, y, z, metadata, fortune);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.999F, 1.0F);
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB entityBounding, List list, Entity entity) {

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;

		x = pos[0];
		y = pos[1];
		z = pos[2];

		TileEntityCargoElevatorLarge elevator = (TileEntityCargoElevatorLarge) world.getTileEntity(x, y, z);
		if(elevator == null) return;

		for(AxisAlignedBB aabb : getAABBs(elevator, x, y, z)) {
			if(entityBounding.intersectsWith(aabb)) list.add(aabb);
		}
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return null;

		TileEntityCargoElevatorLarge elevator = (TileEntityCargoElevatorLarge) world.getTileEntity(pos[0], pos[1], pos[2]);
		if(elevator == null) return null;

		for(AxisAlignedBB aabb : getAABBs(elevator, pos[0], pos[1], pos[2])) {

			MovingObjectPosition intercept = aabb.calculateIntercept(startVec, endVec);
			if(intercept != null) {
				return new MovingObjectPosition(x, y, z, intercept.sideHit, intercept.hitVec);
			}
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event, World world, int x, int y, int z) {

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;

		x = pos[0];
		y = pos[1];
		z = pos[2];

		TileEntityCargoElevatorLarge elevator = (TileEntityCargoElevatorLarge) world.getTileEntity(x, y, z);
		if(elevator == null) return;

		EntityPlayer player = event.player;
		float interp = event.partialTicks;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;
		float exp = 0.002F;

		ICustomBlockHighlight.setup();
		for(AxisAlignedBB aabb : getAABBs(elevator, x, y, z)) RenderGlobal.drawOutlinedBoundingBox(aabb.expand(exp, exp, exp).getOffsetBoundingBox(-dX, -dY, -dZ), -1);
		ICustomBlockHighlight.cleanup();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldDrawHighlight(World world, int x, int y, int z) {
		return true;
	}

	public AxisAlignedBB[] getGuideAABBs(int x, int y, int z, int height) {
		return new AxisAlignedBB[] {
				AxisAlignedBB.getBoundingBox(x - 2, y, z - 2, x - 1.5, y + height, z - 1.5),
				AxisAlignedBB.getBoundingBox(x - 2, y, z + 2.5, x - 1.5, y + height, z + 3),
				AxisAlignedBB.getBoundingBox(x + 2.5, y, z - 2, x + 3, y + height, z - 1.5),
				AxisAlignedBB.getBoundingBox(x + 2.5, y, z + 2.5, x + 3, y + height, z + 3),
		};
	}

	public AxisAlignedBB[] getAABBs(TileEntityCargoElevatorLarge elevator, int x, int y, int z) {
		int height = elevator.height + 1;
		return new AxisAlignedBB[] {
				AxisAlignedBB.getBoundingBox(x - 2, y, z - 2, x - 1.5, y + height, z - 1.5),
				AxisAlignedBB.getBoundingBox(x - 2, y, z + 2.5, x - 1.5, y + height, z + 3),
				AxisAlignedBB.getBoundingBox(x + 2.5, y, z - 2, x + 3, y + height, z - 1.5),
				AxisAlignedBB.getBoundingBox(x + 2.5, y, z + 2.5, x + 3, y + height, z + 3),
				AxisAlignedBB.getBoundingBox(x - 2, y + 0.75 + elevator.extension, z - 2, x + 3, y + 1 + elevator.extension, z + 3),
		};
	}
}