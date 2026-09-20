package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseRegistry;
import com.hbm.handler.contagion.Genome;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerIncubator;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIIncubator;
import com.hbm.items.ItemVial;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityIncubator extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IGUIProvider, IUpgradeInfoProvider {

	public long power;
	public long maxPower = 2_000;
	public long consumption = 100;

	public int progress;
	public int processTime = 200;

	private ItemStack pendingOutput;

	public FluidTank inputTank;
	public FluidTank outputTank;

	public static final int COOLANT_PER_RUN = 100;
	public static final float CROSSBREED_CHANCE = 0.5F;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT();

	public TileEntityIncubator() {
		super(8);
		this.inputTank = new FluidTank(Fluids.PERFLUOROMETHYL_COLD, 16_000);
		this.outputTank = new FluidTank(Fluids.PERFLUOROMETHYL, 16_000);
	}

	@Override
	public String getName() {
		return "container.incubator";
	}

	public static boolean isCoolant(FluidType type) {
		return type == Fluids.PERFLUOROMETHYL_COLD;
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			this.power = Library.chargeTEFromItems(slots, 4, this.getPower(), this.getMaxPower());
			this.inputTank.setType(5, slots);

			if(worldObj.getTotalWorldTime() % 20 == 0) {
				for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					this.trySubscribe(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
					if(inputTank.getTankType() != Fluids.NONE) this.trySubscribe(inputTank.getTankType(), worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
					this.tryProvide(outputTank, worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
				}
			}

			upgradeManager.checkSlots(this, slots, 6, 7);
			int speed = upgradeManager.getLevel(UpgradeType.SPEED);
			int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);

			int baseTime = getIncubationTime();
			this.processTime = baseTime > 0 ? baseTime - (baseTime * speed / 6) + (baseTime * powerLevel / 3) : 0;
			this.consumption = 100 + (100 * speed) - (100 * powerLevel / 6);
			if(this.processTime < 1) this.processTime = 0;
			if(this.consumption < 1) this.consumption = 1;

			if(canProcess()) {
				if(this.pendingOutput == null) this.pendingOutput = rollOutput();
				if(this.pendingOutput != null && canAccept(this.pendingOutput)) {
					this.progress++;
					this.power -= this.consumption;

					if(this.progress >= this.processTime) {
						this.progress = 0;
						this.commitOutput(this.pendingOutput);
						this.pendingOutput = null;
					}
				}
			} else if(getVials().isEmpty()) {
				this.progress = 0;
				this.pendingOutput = null;
			}

			this.maxPower = Math.max(this.consumption * 20, this.power);
			this.networkPackNT(25);
		}
	}

	private int getIncubationTime() {
		for(ItemStack vial : getVials()) {
			DiseaseDefinition def = getDef(vial);
			if(def != null && def.incubationTicks > 0) return def.incubationTicks;
		}
		return 0;
	}

	private List<ItemStack> getVials() {
		List<ItemStack> vials = new ArrayList<ItemStack>();
		for(int i = 0; i <= 2; i++) {
			if(slots[i] != null && slots[i].getItem() == ModItems.vial && ItemVial.readFrame(slots[i]) != null) vials.add(slots[i]);
		}
		return vials;
	}

	private DiseaseDefinition getDef(ItemStack vial) {
		String frame = ItemVial.readFrame(vial);
		if(frame == null) return null;
		NBTTagCompound mut = vial.hasTagCompound() && vial.stackTagCompound.hasKey("mut") ? vial.stackTagCompound.getCompoundTag("mut") : null;
		DiseaseDefinition def = DiseaseRegistry.get(frame);
		if(def == null) def = DiseaseRegistry.restore(frame, mut);
		return def;
	}

	private String getGenome(ItemStack vial) {
		if(vial == null || !vial.hasTagCompound()) return null;
		if(vial.stackTagCompound.hasKey("genome")) return vial.stackTagCompound.getString("genome");
		if(vial.stackTagCompound.hasKey("mut") && vial.stackTagCompound.getCompoundTag("mut").hasKey("genome")) {
			return vial.stackTagCompound.getCompoundTag("mut").getString("genome");
		}
		return null;
	}

	private boolean canCrossbreed(List<ItemStack> vials) {
		if(vials.size() < 2) return false;
		DiseaseDefinition first = getDef(vials.get(0));
		if(first == null || first.type == null) return false;
		for(ItemStack vial : vials) {
			DiseaseDefinition def = getDef(vial);
			if(def == null || def.type != first.type) return false;
		}
		return true;
	}

	public boolean canProcess() {
		if(this.power < this.consumption) return false;
		if(!isCoolant(this.inputTank.getTankType()) || this.inputTank.getFill() < COOLANT_PER_RUN) return false;
		if(this.outputTank.getTankType() != Fluids.PERFLUOROMETHYL) return false;
		if(this.outputTank.getFill() + COOLANT_PER_RUN > this.outputTank.getMaxFill()) return false;
		if(getVials().isEmpty()) return false;

		if(slots[3] != null) {
			if(slots[3].getItem() != ModItems.vial) return false;
			if(slots[3].stackSize >= slots[3].getMaxStackSize()) return false;
		}

		return true;
	}

	private ItemStack rollOutput() {

		List<ItemStack> vials = getVials();
		if(vials.isEmpty()) return null;

		ItemStack base = vials.get(worldObj.rand.nextInt(vials.size()));
		DiseaseDefinition def = getDef(base);
		String genome = getGenome(base);
		if(genome == null && def != null) genome = def.getReferenceGenome();

		if(canCrossbreed(vials)) {
			String otherGenome = getGenome(vials.get(worldObj.rand.nextInt(vials.size())));
			if(otherGenome != null && genome != null && otherGenome.length() == genome.length() && worldObj.rand.nextFloat() < CROSSBREED_CHANCE) {
				genome = crossbreed(genome, otherGenome);
			}
		}

		if(genome != null && def != null) {
			float rate = def.baseMutationRate * Genome.getTypeMultiplier(def.type);
			while(rate >= 1F) {
				genome = Genome.mutate(genome, 1, worldObj.rand);
				rate -= 1F;
			}
			if(worldObj.rand.nextFloat() < rate) genome = Genome.mutate(genome, 1, worldObj.rand);
		}

		return makeVial(base, genome);
	}

	
	private boolean canAccept(ItemStack output) {
		if(output == null) return false;
		if(slots[3] == null) return true;
		if(slots[3].getItem() != ModItems.vial) return false;
		if(!ItemStack.areItemStackTagsEqual(slots[3], output)) return false;
		return slots[3].stackSize + output.stackSize <= slots[3].getMaxStackSize();
	}

	private void commitOutput(ItemStack output) {
		if(slots[3] == null) {
			slots[3] = output.copy();
		} else {
			slots[3].stackSize += output.stackSize;
		}

		this.inputTank.setFill(this.inputTank.getFill() - COOLANT_PER_RUN);
		this.outputTank.setFill(this.outputTank.getFill() + COOLANT_PER_RUN);
		this.markDirty();
	}

	private String crossbreed(String a, String b) {
		StringBuilder sb = new StringBuilder(a.length());
		for(int seg = 0; seg * Genome.LENGTH / 3 < a.length(); seg++) {
			int start = seg * Genome.LENGTH / 3;
			String pick = worldObj.rand.nextBoolean() ? a : b;
			sb.append(pick.substring(start, start + Genome.LENGTH / 3));
		}
		return sb.toString();
	}

	private ItemStack makeVial(ItemStack source, String genome) {
		ItemStack out = source.copy();
		out.stackSize = 1;
		if(!out.hasTagCompound()) out.stackTagCompound = new NBTTagCompound();
		if(genome != null) {
			out.stackTagCompound.setString("genome", genome);
			if(out.stackTagCompound.hasKey("mut")) out.stackTagCompound.getCompoundTag("mut").setString("genome", genome);
		}
		return out;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeLong(consumption);
		buf.writeInt(progress);
		buf.writeInt(processTime);
		inputTank.serialize(buf);
		outputTank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		maxPower = buf.readLong();
		consumption = buf.readLong();
		progress = buf.readInt();
		processTime = buf.readInt();
		inputTank.deserialize(buf);
		outputTank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.progress = nbt.getInteger("progress");
		this.processTime = nbt.getInteger("processTime");
		inputTank.readFromNBT(nbt, "in");
		outputTank.readFromNBT(nbt, "out");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setLong("maxPower", maxPower);
		nbt.setInteger("progress", progress);
		nbt.setInteger("processTime", processTime);
		inputTank.writeToNBT(nbt, "in");
		outputTank.writeToNBT(nbt, "out");
	}

	@Override public long getPower() { return Math.max(Math.min(power, maxPower), 0); }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {inputTank, outputTank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {inputTank}; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {outputTank}; }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot <= 2) return stack.getItem() == ModItems.vial;
		if(slot == 5) return stack.getItem() instanceof IItemFluidIdentifier;
		if(slot >= 6) return stack.getItem() instanceof ItemMachineUpgrade;
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot <= 3;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerIncubator(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIIncubator(player.inventory, this);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_incubator));
		if(type == UpgradeType.SPEED) {
			info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (level * 100 / 6) + "%"));
			info.add(EnumChatFormatting.RED + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(EnumChatFormatting.GREEN + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "-" + (level * 100 / 6) + "%"));
			info.add(EnumChatFormatting.RED + I18nUtil.resolveKey(this.KEY_DELAY, "+" + (level * 100 / 3) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.POWER, 3);
		return upgrades;
	}
}
