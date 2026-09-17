package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.entity.item.EntityMovingItem;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.SpraypainterRecipes;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.fluid.IFluidStandardSender;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityConveyorSpraypainter extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardReceiver, IFluidStandardSender {

	public long power = 0;
	public static final long maxPower = 50000;
	public int usage = 100;

	public FluidTank tank;

	public TileEntityConveyorSpraypainter() {
		super(0);
		this.tank = new FluidTank(Fluids.NONE, 16_000);
	}

	@Override
	public String getName() {
		return "container.conveyor_spraypainter";
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			this.updateConnections();

			if(this.power >= usage) this.process();

			this.networkPackNT(50);
		}
	}

	protected void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			if(tank.getTankType() != Fluids.NONE) {
				this.trySubscribe(tank.getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				this.tryProvide(tank, worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}
		}
	}

	protected DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(xCoord + 1, yCoord, zCoord, Library.POS_X),
				new DirPos(xCoord - 1, yCoord, zCoord, Library.NEG_X),
				new DirPos(xCoord, yCoord, zCoord + 1, Library.POS_Z),
				new DirPos(xCoord, yCoord, zCoord - 1, Library.NEG_Z),
		};
	}

	public boolean process() {

		List<EntityMovingItem> items = worldObj.getEntitiesWithinAABB(EntityMovingItem.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 1.5, zCoord + 1));
		boolean did = false;

		for(EntityMovingItem item : items) {
			ItemStack stack = item.getItemStack();
			if(stack == null || stack.stackSize <= 0) continue;

			int count = stack.stackSize;
			int needed = SpraypainterRecipes.amount * count;
			if(this.tank.getFill() < needed) continue;

			ItemStack output = SpraypainterRecipes.getOutput(stack, tank.getTankType());
			if(output == null) continue;

			output.stackSize = count;

			item.setDead();
			EntityMovingItem out = new EntityMovingItem(worldObj);
			out.setPosition(item.posX, item.posY, item.posZ);
			out.setItemStack(output);
			worldObj.spawnEntityInWorld(out);

			this.tank.setFill(this.tank.getFill() - needed);
			did = true;
		}

		if(did) {
			this.power -= usage;
			this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "hbm:block.spraypaint", 1.0F, 1.0F);
		}

		return did;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.tank.readFromNBT(nbt, "tank");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		this.tank.writeToNBT(nbt, "tank");
	}

	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tank }; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tank }; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] { tank }; }

	@Override public boolean canConnect(ForgeDirection dir) { return dir != ForgeDirection.UP && dir != ForgeDirection.UNKNOWN; }
	@Override public boolean canConnect(FluidType type, ForgeDirection dir) { return dir != ForgeDirection.UP && dir != ForgeDirection.UNKNOWN; }

	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }
	@Override public void setPower(long power) { this.power = power; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					xCoord - 1,
					yCoord,
					zCoord - 1,
					xCoord + 2,
					yCoord + 2,
					zCoord + 2
					);
		}

		return bb;
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
