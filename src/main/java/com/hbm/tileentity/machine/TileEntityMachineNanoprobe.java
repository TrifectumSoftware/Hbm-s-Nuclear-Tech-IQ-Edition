package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.Map;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.inventory.container.ContainerMachineNanoprobe;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineNanoprobe;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.energymk2.IBatteryItem;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.redstoneoverradio.IRORInteractive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class TileEntityMachineNanoprobe extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IGUIProvider, IRORInteractive {
    // S.O.N
	public static final int GOO_CAPACITY = 16_000_000;
	public static final int OUTPUT_CAPACITY = 256_000;
	public static final int OUTPUT_SLOTS = 3;
	public static final int MIN_DEPLOY = 1_000;

	public static final int DEPLOY_BASE = 400;
	public static final int DEPLOY_RATE = 2_000;

	public static final double LOSS_PER_MIN = 0.005D;
	public static final double LOSS_MAX = 0.75D;

	public static final double YIELD_PER_MB = 0.001D;
	public static final double E_MAX = 100_000D;

	public static final long POWER_BASE = 1_000;
	public static final double POWER_PER_MB = 0.01D;

	public static final double[] OUTPUT_PEAK = { 6D, 22D, 50D };
	public static final double[] OUTPUT_WIDTH = { 6D, 10D, 16D };

	private static final Map<SolarSystem.Body, FluidType[]> OUTPUT_TABLE = new HashMap<SolarSystem.Body, FluidType[]>();
	static {
		OUTPUT_TABLE.put(SolarSystem.Body.DROSS, new FluidType[] { Fluids.COLOSTAMINE, Fluids.MALAISE, Fluids.PUTRYIN });
	}

	public static enum ProbeState {
		IDLE, DEPLOYING, EXTRACTING, RECALLING
	}

	public long power;
	public long maxPower = 4_000_000;

	public ProbeState state = ProbeState.IDLE;
	public int deployed;
	public int deployTime;
	public int recallTime;
	public int stateTicks;
	public int elapsedTicks;
	public int missionTicks;
	public int recallStart;
	public float lossFraction;
	public int returnedGoo;
	public double[] harvest = new double[OUTPUT_SLOTS];

	public FluidTank gooTank;
	public FluidTank dormantGooTank;
	public FluidTank[] outputTanks;

	private boolean prevRedstone;
	private AudioWrapper audio;

	public TileEntityMachineNanoprobe() {
		super(1);
		this.gooTank = new FluidTank(Fluids.GRAY_GOO, GOO_CAPACITY);
		this.dormantGooTank = new FluidTank(Fluids.DORMANT_GRAY_GOO, GOO_CAPACITY);
		this.outputTanks = new FluidTank[OUTPUT_SLOTS];
		for(int i = 0; i < OUTPUT_SLOTS; i++) this.outputTanks[i] = new FluidTank(Fluids.NONE, OUTPUT_CAPACITY);
	}

	@Override
	public String getName() {
		return "container.nanomachineProbe";
	}

	public SolarSystem.Body getBody() {
		if(worldObj == null) return null;
		try {
			return CelestialBody.getEnum(worldObj);
		} catch(Exception ex) {
			return null;
		}
	}

	public FluidType[] getOutputs() {
		SolarSystem.Body body = getBody();
		return body == null ? null : OUTPUT_TABLE.get(body);
	}

	public int outputCount() {
		FluidType[] outputs = getOutputs();
		return outputs == null ? 0 : Math.min(outputs.length, OUTPUT_SLOTS);
	}

	public boolean hasOutputs() {
		return outputCount() > 0;
	}

	private DirPos[] getConPos() {
		return new DirPos[] {
			new DirPos(xCoord + 5, yCoord, zCoord, Library.POS_X),
			new DirPos(xCoord - 5, yCoord, zCoord, Library.NEG_X),
			new DirPos(xCoord, yCoord, zCoord + 5, Library.POS_Z),
			new DirPos(xCoord, yCoord, zCoord - 5, Library.NEG_Z),
		};
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {

			FluidType[] outputs = getOutputs();
			for(int i = 0; i < OUTPUT_SLOTS; i++) {
				FluidType type = outputs != null && i < outputs.length ? outputs[i] : Fluids.NONE;
				if(this.outputTanks[i].getTankType() != type) this.outputTanks[i].setTankType(type);
			}

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(worldObj, pos);
				this.trySubscribe(this.gooTank.getTankType(), worldObj, pos);
				this.tryProvide(this.gooTank, worldObj, pos);
				for(FluidTank tank : this.outputTanks) if(tank.getFill() > 0) this.tryProvide(tank, worldObj, pos);
			}

			boolean prev = this.prevRedstone;
			this.prevRedstone = this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
			boolean recallSignal = !prev && this.prevRedstone;

			tick(recallSignal);
			this.networkPackNT(20);
		} else {
			this.updateAudio();
		}
	}
   // :tesla:
	private void tick(boolean recallSignal) {
		switch(this.state) {
		case IDLE:
			this.flushOutputs();
			break;

		case DEPLOYING:
			if(recallSignal) {
				this.recall();
				break;
			}
			if(!consumePower()) {
				this.missionTicks++;
				if(++this.stateTicks >= this.deployTime) {
					this.state = ProbeState.EXTRACTING;
					this.elapsedTicks = 0;
				}
			}
			break;

		case EXTRACTING:
			if(recallSignal) {
				this.recall();
				break;
			}
			if(!consumePower()) {
				this.missionTicks++;
				this.elapsedTicks++;
				this.lossFraction = (float) getLoss(this.elapsedTicks);
				this.accumulateOutputs();
			}
			break;

		case RECALLING:
			if(!consumePower()) {
				if(++this.stateTicks >= this.recallTime) {
					this.returnedGoo = (int) (this.deployed * (1D - this.lossFraction));
					this.state = ProbeState.IDLE;
					this.deployed = 0;
					this.stateTicks = 0;
					this.elapsedTicks = 0;
					this.missionTicks = 0;
					this.lossFraction = 0F;
					this.flushOutputs();
					this.markDirty();
				} else {
					this.missionTicks = Math.max(0, (int) (this.recallStart * (1D - (double) this.stateTicks / this.recallTime)));
				}
			}
			break;
		}
	}

	public void startDeploy() {
		if(this.state != ProbeState.IDLE) return;
		if(!hasOutputs()) return;

		int amount = this.gooTank.getFill();
		if(amount < MIN_DEPLOY) return;
		if(this.power < POWER_BASE + (long) (amount * POWER_PER_MB)) return;

		this.deployed = amount;
		this.deployTime = DEPLOY_BASE + this.deployed / DEPLOY_RATE;
		this.gooTank.setFill(this.gooTank.getFill() - this.deployed);
		this.stateTicks = 0;
		this.elapsedTicks = 0;
		this.missionTicks = 0;
		this.recallStart = 0;
		this.lossFraction = 0F;
		this.returnedGoo = 0;
		for(int i = 0; i < OUTPUT_SLOTS; i++) this.harvest[i] = 0D;
		this.state = ProbeState.DEPLOYING;
		this.markDirty();
	}

	public void recall() {
		if(this.state == ProbeState.DEPLOYING || this.state == ProbeState.EXTRACTING) {
			this.recallTime = Math.max(20, Math.max(this.elapsedTicks, this.stateTicks));
			this.recallStart = this.missionTicks;
			this.state = ProbeState.RECALLING;
			this.stateTicks = 0;
			this.markDirty();
		}
	}

	private void flushOutputs() {
		if(this.returnedGoo > 0) {
			int room = this.dormantGooTank.getMaxFill() - this.dormantGooTank.getFill();
			int add = Math.min(room, this.returnedGoo);
			this.dormantGooTank.setFill(this.dormantGooTank.getFill() + add);
			this.returnedGoo -= add;
		}

		for(int i = 0; i < outputCount(); i++) {
			if(this.harvest[i] >= 1D) {
				int room = this.outputTanks[i].getMaxFill() - this.outputTanks[i].getFill();
				int add = (int) Math.min(room, this.harvest[i]);
				if(add > 0) {
					this.outputTanks[i].setFill(this.outputTanks[i].getFill() + add);
					this.harvest[i] -= add;
				}
			}
		}
	}

	private void accumulateOutputs() {
		int count = outputCount();
		if(count <= 0) return;

		double produced = getYield(this.deployed, this.elapsedTicks) - getYield(this.deployed, this.elapsedTicks - 1);
		if(produced <= 0) return;

		double[] w = getComposition(this.elapsedTicks / 1200D, count);
		for(int i = 0; i < count; i++) this.harvest[i] += produced * w[i];
	}

	private long consumption() {
		return POWER_BASE + (long) (this.deployed * POWER_PER_MB);
	}

	private boolean consumePower() {
		long use = consumption();
		if(this.power < use) return true;
		this.power -= use;
		return false;
	}

	public boolean isActive() {
		return this.state != ProbeState.IDLE;
	}

	public static double getLoss(int ticks) {
		return Math.min(LOSS_MAX, (ticks / 1200D) * LOSS_PER_MIN);
	}

	public static double getYield(int deployed, int ticks) {
		if(deployed <= 0 || ticks <= 0) return 0D;
		double x = deployed * YIELD_PER_MB * (ticks / 1200D) / E_MAX;
		return E_MAX * (1D - Math.exp(-x));
	}

	public static double getEfficiency(int deployed, int ticks) {
		if(deployed <= 0 || ticks <= 0) return 0D;
		double progress = getYield(deployed, ticks) / E_MAX;
		double value = progress * (1D - getLoss(ticks));
		return value < 0D ? 0D : value > 1D ? 1D : value;
	}

	public static double[] getComposition(double t, int count) {
		double[] w = new double[count];
		double sum = 0D;

		for(int i = 0; i < count; i++) {
			double peak = OUTPUT_PEAK[Math.min(i, OUTPUT_PEAK.length - 1)];
			double width = OUTPUT_WIDTH[Math.min(i, OUTPUT_WIDTH.length - 1)];
			double d = (t - peak) / width;
			w[i] = Math.exp(-0.5D * d * d);
			sum += w[i];
		}

		if(sum <= 0D) {
			if(count > 0) w[0] = 1D;
			return w;
		}

		for(int i = 0; i < count; i++) w[i] /= sum;
		return w;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot == 0 && stack != null && stack.getItem() instanceof IBatteryItem;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] { 0 };
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		buf.writeLong(this.maxPower);
		buf.writeByte(this.state.ordinal());
		buf.writeInt(this.deployed);
		buf.writeInt(this.deployTime);
		buf.writeInt(this.recallTime);
		buf.writeInt(this.stateTicks);
		buf.writeInt(this.elapsedTicks);
		buf.writeInt(this.missionTicks);
		buf.writeInt(this.recallStart);
		buf.writeFloat(this.lossFraction);
		buf.writeInt(this.returnedGoo);
		for(int i = 0; i < OUTPUT_SLOTS; i++) buf.writeDouble(this.harvest[i]);
		this.gooTank.serialize(buf);
		this.dormantGooTank.serialize(buf);
		for(FluidTank tank : this.outputTanks) tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		this.state = ProbeState.values()[buf.readByte() % ProbeState.values().length];
		this.deployed = buf.readInt();
		this.deployTime = buf.readInt();
		this.recallTime = buf.readInt();
		this.stateTicks = buf.readInt();
		this.elapsedTicks = buf.readInt();
		this.missionTicks = buf.readInt();
		this.recallStart = buf.readInt();
		this.lossFraction = buf.readFloat();
		this.returnedGoo = buf.readInt();
		for(int i = 0; i < OUTPUT_SLOTS; i++) this.harvest[i] = buf.readDouble();
		this.gooTank.deserialize(buf);
		this.dormantGooTank.deserialize(buf);
		for(FluidTank tank : this.outputTanks) tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.state = ProbeState.values()[nbt.getByte("state") % ProbeState.values().length];
		this.deployed = nbt.getInteger("deployed");
		this.deployTime = nbt.getInteger("deployTime");
		this.recallTime = nbt.getInteger("recallTime");
		this.stateTicks = nbt.getInteger("stateTicks");
		this.elapsedTicks = nbt.getInteger("elapsedTicks");
		this.missionTicks = nbt.getInteger("missionTicks");
		this.recallStart = nbt.getInteger("recallStart");
		this.lossFraction = nbt.getFloat("lossFraction");
		this.returnedGoo = nbt.getInteger("returnedGoo");
		for(int i = 0; i < OUTPUT_SLOTS; i++) this.harvest[i] = nbt.getDouble("harvest" + i);
		this.gooTank.readFromNBT(nbt, "goo");
		this.dormantGooTank.readFromNBT(nbt, "dormantGoo");
		for(int i = 0; i < OUTPUT_SLOTS; i++) this.outputTanks[i].readFromNBT(nbt, "outputTank" + i);
		this.prevRedstone = false;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", this.power);
		nbt.setLong("maxPower", this.maxPower);
		nbt.setByte("state", (byte) this.state.ordinal());
		nbt.setInteger("deployed", this.deployed);
		nbt.setInteger("deployTime", this.deployTime);
		nbt.setInteger("recallTime", this.recallTime);
		nbt.setInteger("stateTicks", this.stateTicks);
		nbt.setInteger("elapsedTicks", this.elapsedTicks);
		nbt.setInteger("missionTicks", this.missionTicks);
		nbt.setInteger("recallStart", this.recallStart);
		nbt.setFloat("lossFraction", this.lossFraction);
		nbt.setInteger("returnedGoo", this.returnedGoo);
		for(int i = 0; i < OUTPUT_SLOTS; i++) nbt.setDouble("harvest" + i, this.harvest[i]);
		this.gooTank.writeToNBT(nbt, "goo");
		this.dormantGooTank.writeToNBT(nbt, "dormantGoo");
		for(int i = 0; i < OUTPUT_SLOTS; i++) this.outputTanks[i].writeToNBT(nbt, "outputTank" + i);
	}

	@Override public long getPower() { return this.power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return this.maxPower; }

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { this.gooTank };
	}

	@Override
	public FluidTank[] getSendingTanks() {
		FluidTank[] tanks = new FluidTank[2 + this.outputTanks.length];
		tanks[0] = this.gooTank;
		tanks[1] = this.dormantGooTank;
		for(int i = 0; i < this.outputTanks.length; i++) tanks[2 + i] = this.outputTanks[i];
		return tanks;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return getSendingTanks();
	}

	@Override
	public void handleButtonPacket(int value, int meta) {
		if(meta != 0) return;
		if(this.state == ProbeState.IDLE) this.startDeploy();
		else this.recall();
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineNanoprobe(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineNanoprobe(player.inventory, this);
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] { PREFIX_FUNCTION + "recall" };
	}

	@Override
	public String runRORFunction(String name, String[] params) {
		if("recall".equalsIgnoreCase(name)) this.recall();
		return null;
	}

	private void updateAudio() {
		boolean running = this.isActive() && MainRegistry.proxy.me() != null
				&& MainRegistry.proxy.me().getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) < 35D * 35D;

		if(running) {
			if(this.audio == null) {
				this.audio = this.createAudioLoop();
				this.audio.startSound();
			} else if(!this.audio.isPlaying()) {
				this.audio = this.rebootAudio(this.audio);
			}

			this.audio.keepAlive();
			this.audio.updateVolume(this.getVolume(1F));

		} else if(this.audio != null) {
			this.audio.stopSound();
			this.audio = null;
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(NTMSounds.ELECTRIC_MOTOR_LOOP, xCoord, yCoord, zCoord, 1F, 10F, 1F, 20);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(this.audio != null) {
			this.audio.stopSound();
			this.audio = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if(this.audio != null) {
			this.audio.stopSound();
			this.audio = null;
		}
	}
}
