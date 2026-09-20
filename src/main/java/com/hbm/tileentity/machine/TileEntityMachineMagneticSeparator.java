package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineMagneticSeparator;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineMagneticSeparator;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMagneticDisc;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.module.machine.ModuleMachineMagneticSeparator;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityMachineMagneticSeparator extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IControlReceiver, IGUIProvider {

	public FluidTank inputTank;
	public FluidTank outputTank;

	public long power;
	public long maxPower = 1_000_000;
	public boolean didProcess = false;

	public static final int[] OPENING_ANIMATION_TICKS = {144, 43, 93};
	public static final int[] CLOSING_ANIMATION_TICKS = {65, 64, 231};
	public static final float ROTATION_MULTIPLIER = 8.0F;

	public int animationTicks = 0;
	public int animAcceleration = 0;
	public int animRotation = 0;
	public int animPause = 0;
	public int rotation = 0;
	public boolean open = true;
	public int idleTicks = 1000;
	public AudioWrapper audio;

	public ModuleMachineMagneticSeparator module;

	public TileEntityMachineMagneticSeparator() {
		super(9);

		this.inputTank = new FluidTank(Fluids.NONE, 24_000);
		this.outputTank = new FluidTank(Fluids.NONE, 24_000);

		this.module = new ModuleMachineMagneticSeparator(0, this, slots)
				.itemInput(1).itemOutput(3)
				.fluidInput(inputTank).fluidOutput(outputTank);
	}

	@Override
	public String getName() {
		return "container.machineMagneticSeparator";
	}

	public int getParallels() {
		ItemStack disc = slots[2];
		if(disc != null && disc.getItem() instanceof ItemMagneticDisc) return ItemMagneticDisc.getType(disc).parallels;
		return 0;
	}

	@Override
	public void updateEntity() {

		if(maxPower <= 0) this.maxPower = 1_000_000;

		if(!worldObj.isRemote) {

			com.hbm.inventory.recipes.loader.GenericRecipe recipe = module.getRecipe();
			if(recipe != null) this.maxPower = recipe.power * 100;
			this.maxPower = BobMathUtil.max(this.power, this.maxPower, 1_000_000);

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(worldObj, pos);
				if(inputTank.getTankType() != Fluids.NONE) this.trySubscribe(inputTank.getTankType(), worldObj, pos);
				if(outputTank.getFill() > 0) this.tryProvide(outputTank, worldObj, pos);
			}

			int parallels = getParallels();
			this.module.parallels = parallels > 0 ? parallels : 1;

			this.module.update(1D, this.module.parallels / 2D, parallels > 0, null);
			this.didProcess = this.module.didProcess;

			if(this.didProcess) {
				this.damageDisc();
				this.idleTicks = 0;
			} else if(this.idleTicks < 1000) {
				this.idleTicks++;
			}

			boolean shouldOpen = this.idleTicks > 40;
			if(this.open != shouldOpen) this.setState(shouldOpen);

			if(this.module.markDirty) this.markDirty();
			this.networkPackNT(50);

		} else {
			this.tickAnimation();
		}
	}

	private void tickAnimation() {
		if(this.animationTicks > 0) {
			this.animationTicks--;
			if(this.open) {
				if (this.audio != null) {
					this.audio.stopSound();
					this.audio = null;
				}
				if(this.animAcceleration < OPENING_ANIMATION_TICKS[0]) this.animAcceleration++;
				else if(this.animPause < OPENING_ANIMATION_TICKS[1]) this.animPause++;
				else if(this.animRotation < OPENING_ANIMATION_TICKS[2]) this.animRotation++;
			} else {
				if(this.animPause < CLOSING_ANIMATION_TICKS[0]) this.animPause++;
				else if(this.animRotation < CLOSING_ANIMATION_TICKS[1]) this.animRotation++;
				else if(this.animAcceleration < CLOSING_ANIMATION_TICKS[2]) this.animAcceleration++;
				else this.rotation++;
			}
		} else {
			if(!this.open) {
				this.rotation++;
				if (this.audio == null) {
					this.audio = this.createAudioLoop();
					this.audio.startSound();
				} else if (!this.audio.isPlaying()) {
					this.audio = this.rebootAudio(this.audio);
				}
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (this.audio != null) {
			this.audio.stopSound();
			this.audio = null;
		}
	}

	public void setState(boolean state) {
		if(this.open == state) return;
		if (this.audio != null) {
			this.audio.stopSound();
			this.audio = null;
		}
		this.open = state;
		this.animAcceleration = 0;
		this.animPause = 0;
		this.animRotation = 0;
		if(state) {
			this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, "hbm:machine.magnetic_open", 1.0F, 1.0F);
			this.animationTicks = 20 * 20;
		} else {
			this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, "hbm:machine.magnetic_close", 1.0F, 1.0F);
			this.animationTicks = 21 * 20;
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound("hbm:machine.magnetic_loop", this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, 1.0F, 16.0F, 1.0F);
	}

	private void damageDisc() {
		ItemStack disc = slots[2];
		if(disc == null || !(disc.getItem() instanceof ItemMagneticDisc)) return;
		if(worldObj.getTotalWorldTime() % 20 != 0) return;
		int dur = ItemMagneticDisc.getDurability(disc) - 1;
		if(dur <= 0) {
			slots[2] = null;
			worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "random.break", 1.0F, 1.0F);
		} else {
			ItemMagneticDisc.setDurability(disc, dur);
		}
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(xCoord + 3, yCoord + 1, zCoord - 2, Library.POS_X),
				new DirPos(xCoord + 3, yCoord + 1, zCoord - 1, Library.POS_X),
				new DirPos(xCoord + 3, yCoord + 1, zCoord + 0, Library.POS_X),
				new DirPos(xCoord + 3, yCoord + 1, zCoord + 1, Library.POS_X),
				new DirPos(xCoord + 3, yCoord + 1, zCoord + 2, Library.POS_X),
				new DirPos(xCoord - 3, yCoord + 1, zCoord - 2, Library.NEG_X),
				new DirPos(xCoord - 3, yCoord + 1, zCoord - 1, Library.NEG_X),
				new DirPos(xCoord - 3, yCoord + 1, zCoord + 0, Library.NEG_X),
				new DirPos(xCoord - 3, yCoord + 1, zCoord + 1, Library.NEG_X),
				new DirPos(xCoord - 3, yCoord + 1, zCoord + 2, Library.NEG_X),
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		inputTank.serialize(buf);
		outputTank.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeBoolean(didProcess);
		buf.writeBoolean(open);
		this.module.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		inputTank.deserialize(buf);
		outputTank.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		this.didProcess = buf.readBoolean();
		boolean prevOpen = this.open;
		this.open = buf.readBoolean();
		if(this.open != prevOpen) {
			this.animAcceleration = 0;
			this.animPause = 0;
			this.animRotation = 0;
			this.animationTicks = this.open ? 20 * 20 : 21 * 20;
		}
		this.module.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.inputTank.readFromNBT(nbt, "i0");
		this.outputTank.readFromNBT(nbt, "o0");
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.open = nbt.getBoolean("open");
		this.module.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.inputTank.writeToNBT(nbt, "i0");
		this.outputTank.writeToNBT(nbt, "o0");
		nbt.setLong("power", power);
		nbt.setLong("maxPower", maxPower);
		nbt.setBoolean("open", open);
		this.module.writeToNBT(nbt);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return true;
		if(slot == 2) return stack.getItem() instanceof ItemMagneticDisc;
		if(slot >= 3 && slot <= 8) return false;
		if(this.module.isItemValid(slot, stack)) return true;
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i >= 3 && i <= 8;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] {1, 3, 4, 5, 6, 7, 8};
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {inputTank}; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {outputTank}; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {inputTank, outputTank}; }

	@Override
	public boolean canConnect(FluidType type, net.minecraftforge.common.util.ForgeDirection dir) {
		return dir != net.minecraftforge.common.util.ForgeDirection.UNKNOWN;
	}

	@Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachineMagneticSeparator(player.inventory, this); }
	@Override @SideOnly(Side.CLIENT) public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachineMagneticSeparator(player.inventory, this); }

	@Override public boolean hasPermission(EntityPlayer player) { return this.isUseableByPlayer(player); }

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("index") && data.hasKey("selection")) {
			if(data.getInteger("index") == 0) {
				this.module.setRecipe(data.getString("selection"), false);
				this.markChanged();
			}
		}
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = AxisAlignedBB.getBoundingBox(xCoord - 2, yCoord, zCoord - 2, xCoord + 3, yCoord + 3, zCoord + 3);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
