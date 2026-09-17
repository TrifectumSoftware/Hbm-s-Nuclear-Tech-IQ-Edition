package com.hbm.tileentity.machine;

import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.fluidmk2.IFluidReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockPlushie.PlushieType;
import com.hbm.blocks.generic.BlockPlushie.TileEntityPlushie;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.inventory.container.ContainerMachinePress;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachinePress;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import com.hbm.util.BufferUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMachinePress extends TileEntityMachineBase implements IGUIProvider, IFluidStandardTransceiver {
	public static final int maxSpeed = 200; // max speed ticks for acceleration
	public static final int consumption = 100; // steam mb/t

	public int press; // extension of the press, operation is completed if maxPress is reached
	public double renderPress; // client-side version of the press var, a double for smoother rendering
	public double lastPress; // for interp
	private int syncPress; // for interp
	private int turnProgress; // for interp 3: revenge of the sith
	public final static int maxPress = 200; // max tick count per operation assuming speed is 1
	boolean isRetracting = false; // direction the press is currently going
	private int delay; // delay between direction changes to look a bit more appealing

	public FluidTank[] tanks; //hhhhhh

	public ItemStack syncStack;

	public TileEntityMachinePress() {
		super(13);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.STEAM, 8000);
		tanks[1] = new FluidTank(Fluids.SPENTSTEAM, 1000);
	}

	@Override
	public String getName() {
		return "container.press";
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			// Triggers the legacy monoblock fix
			if (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) < 12) {
				worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord), 1);
			}

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				trySubscribe(tanks[0].getTankType(), worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
				sendFluid(tanks[1], worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
			}

			boolean canProcess = this.canProcess();

			if (canProcess) {
				this.tanks[0].setFill(this.tanks[0].getFill() - consumption);
				if ((this.tanks[1].getFill() + 20) < this.tanks[1].getMaxFill()) {
					this.tanks[1].setFill(this.tanks[1].getFill() + (consumption / 100));
				}
			}

			if(delay <= 0) {

				int stampSpeed = maxSpeed;

				if(this.isRetracting) {
					this.press -= stampSpeed;

					if(this.press <= 0) {
						this.isRetracting = false;
						this.delay = 5;
					}
				} else if(canProcess) {
					this.press += stampSpeed;

					if(this.press >= this.maxPress) {
						String squish = "hbm:block.pressOperate";
						TileEntity tile = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
						if(tile instanceof TileEntityPlushie) {
							TileEntityPlushie plushie = (TileEntityPlushie) tile;
							squish = "hbm:block.squeakyPain";

							if(plushie.type == PlushieType.HUNDUN) {
								worldObj.func_147480_a(xCoord, yCoord, zCoord, false);
								ExplosionVNT vnt = new ExplosionVNT(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 5);
								vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 50));
								vnt.setPlayerProcessor(new PlayerProcessorStandard());
								vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
								vnt.explode();
							}
						}
						this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, squish, getVolume(1.5F), 1.0F);
						ItemStack output = PressRecipes.getOutput(slots[2], slots[1]);
						if(slots[3] == null) {
							slots[3] = output.copy();
						} else {
							slots[3].stackSize += output.stackSize;
						}
						this.decrStackSize(2, 1);

						if(slots[1].getMaxDamage() != 0) {
							slots[1].setItemDamage(slots[1].getItemDamage() + 1);
							if(slots[1].getItemDamage() >= slots[1].getMaxDamage()) {
								slots[1] = null;
							}
						}

						this.isRetracting = true;
						this.delay = 5;

						this.markDirty();
					}
				} else if(this.press > 0){
					this.isRetracting = true;
				}
			} else {
				delay--;
			}

			this.networkPackNT(50);

		} else {

			// approach-based interpolation, GO!
			this.lastPress = this.renderPress;

			if(this.turnProgress > 0) {
				this.renderPress = this.renderPress + ((this.syncPress - this.renderPress) / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.renderPress = this.syncPress;
			}

			TileEntity te = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
			if(te instanceof TileEntityPlushie) {
				TileEntityPlushie abuseTarget = (TileEntityPlushie) te;
				abuseTarget.miseryFactor = MathHelper.clamp_float(((float)syncPress - 100.0F) / 100.0F, 0, 1);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.press);
		BufferUtil.writeItemStack(buf, slots[2]);
		for(FluidTank tank : tanks) tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.syncPress = buf.readInt();
		this.syncStack = BufferUtil.readItemStack(buf);

		this.turnProgress = 2;
		for(FluidTank tank : tanks) tank.deserialize(buf);
	}

	public boolean canProcess() {
		if(slots[1] == null || slots[2] == null) return false;
		if(tanks[0].getFill() < consumption) return false;

		ItemStack output = PressRecipes.getOutput(slots[2], slots[1]);

		if(output == null) return false;

		if(slots[3] == null) return true;
		if(slots[3].stackSize + output.stackSize <= slots[3].getMaxStackSize() && slots[3].getItem() == output.getItem() && slots[3].getItemDamage() == output.getItemDamage()) return true;
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {

		if(stack.getItem() instanceof ItemStamp)
			return i == 1;

		if(TileEntityFurnace.getItemBurnTime(stack) > 0 && i == 0)
			return true;

		return i == 2;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i == 3;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		press = nbt.getInteger("press");
		isRetracting = nbt.getBoolean("ret");
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("press", press);
		nbt.setBoolean("ret", isRetracting);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
	}

	AxisAlignedBB aabb;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(aabb != null) return aabb;
		aabb = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 3, zCoord + 1);
		return aabb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachinePress(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachinePress(player.inventory, this);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[1] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[0] };
	}
}
