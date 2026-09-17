package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerBloodCentrifuge;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIBloodCentrifuge;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemMedicalSyringe;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFillableItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityBloodCentrifuge extends TileEntityMachineBase implements IEnergyReceiverMK2, IGUIProvider {

	public long power;
	public static final long maxPower = 100000;
	public int progress;
	public static final int maxProgress = 200;
	public static final int BLOOD_PER_RUN = 1000;
	public static final int VIAL_FILL = 100;

	public FluidTank tank;

	public TileEntityBloodCentrifuge() {
		super(9);
		tank = new FluidTank(Fluids.HUMAN_BLOOD, 1000);
	}

	@Override
	public String getName() {
		return "container.bloodCentrifuge";
	}

	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			this.depositSyringe();
			power = Library.chargeTEFromItems(slots, 8, power, maxPower);

			if(worldObj.getTotalWorldTime() % 40 == 0) this.updateConnections();

			if(canProcess()) {
				progress++;
				power -= 50;

				if(progress >= maxProgress) {
					process();
				}
			} else {
				progress = 0;
			}

			networkPackNT(50);
		}
	}

	private void depositSyringe() {

		if(slots[0] == null) return;
		if(!(slots[0].getItem() instanceof IFillableItem)) return;

		IFillableItem fillable = (IFillableItem) slots[0].getItem();
		FluidType type = tank.getTankType();

		if(fillable.providesFluid(type, slots[0])) {
			int moved = fillable.tryEmpty(type, tank.getMaxFill() - tank.getFill(), slots[0]);
			if(moved > 0) tank.setFill(tank.getFill() + moved);
		}

		if(fillable.getFill(slots[0]) <= 0 && slots[1] == null) {
			slots[1] = slots[0];
			slots[0] = null;
		}
	}

	public boolean canProcess() {
		if(power < 50) return false;
		if(tank.getFill() < BLOOD_PER_RUN) return false;

		for(int i = 2; i <= 7; i++) {
			if(slots[i] != null) return false;
		}

		return true;
	}

	private void process() {

		tank.setFill(tank.getFill() - BLOOD_PER_RUN);

		NBTTagList pathogens = null;
		String ownerName = null;
		if(slots[1] != null && slots[1].hasTagCompound()) {
			if(slots[1].stackTagCompound.hasKey("pathogen")) {
				pathogens = slots[1].stackTagCompound.getTagList("pathogen", Constants.NBT.TAG_COMPOUND);
			}
			if(slots[1].stackTagCompound.hasKey(ItemMedicalSyringe.KEY_OWNER_NAME)) {
				ownerName = slots[1].stackTagCompound.getString(ItemMedicalSyringe.KEY_OWNER_NAME);
			}
		}

		int count = pathogens == null ? 0 : Math.min(pathogens.tagCount(), 6);

		for(int i = 0; i < count; i++) {
			NBTTagCompound entry = pathogens.getCompoundTagAt(i);

			float visibility = 1.0F;
			if(entry.hasKey("mut")) {
				com.hbm.handler.contagion.DiseaseInstance inst = com.hbm.handler.contagion.DiseaseInstance.readFromNBT(entry.getCompoundTag("mut"));
				visibility = inst.visibility;
			}
			if(worldObj.rand.nextFloat() >= visibility) continue;

			ItemStack vial = new ItemStack(ModItems.vial);
			NBTTagCompound payload = new NBTTagCompound();
			if(entry.hasKey("frame")) payload.setString("frame", entry.getString("frame"));
			if(entry.hasKey("amount")) payload.setFloat("amount", entry.getFloat("amount"));
			if(entry.hasKey("mut")) {
				payload.setTag("mut", entry.getCompoundTag("mut"));
				if(entry.getCompoundTag("mut").hasKey("genome")) payload.setString("genome", entry.getCompoundTag("mut").getString("genome"));
			}
			if(ownerName != null) payload.setString(ItemMedicalSyringe.KEY_OWNER_NAME, ownerName);
			if(!payload.hasNoTags()) vial.setTagCompound(payload);
			IFillableItem.setFluidFill(vial, Fluids.HUMAN_BLOOD, (short) VIAL_FILL);
			slots[2 + i] = vial;
		}

		progress = 0;
	}

	public int getProgressScaled(int i) {
		return (progress * i) / maxProgress;
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	private void updateConnections() {

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			this.trySubscribe(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		progress = nbt.getInteger("progress");
		tank.readFromNBT(nbt, "tank");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setInteger("progress", progress);
		tank.writeToNBT(nbt, "tank");
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerBloodCentrifuge(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIBloodCentrifuge(player.inventory, this);
	}
}
