package com.hbm.handler.contagion;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

public class BloodEntry {

	public int fluidId;
	public float amount;
	public String frameId;
	public NBTTagCompound mutationNBT;

	public BloodEntry(int fluidId, float amount, String frameId, NBTTagCompound mutationNBT) {
		this.fluidId = fluidId;
		this.amount = amount;
		this.frameId = frameId;
		this.mutationNBT = mutationNBT;
	}

	public BloodEntry(int fluidId, float amount) {
		this(fluidId, amount, null, null);
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("fluid", fluidId);
		nbt.setFloat("amount", amount);
		if (frameId != null) {
			nbt.setString("frame", frameId);
		}
		if (mutationNBT != null) {
			nbt.setTag("mut", mutationNBT);
		}
	}

	public static BloodEntry readFromNBT(NBTTagCompound nbt) {
		BloodEntry entry = new BloodEntry(0, 0F);

		if (nbt.hasKey("fluid")) {
			entry.fluidId = nbt.getInteger("fluid");
		}
		if (nbt.hasKey("amount")) {
			entry.amount = nbt.getFloat("amount");
		}
		if (nbt.hasKey("frame")) {
			entry.frameId = nbt.getString("frame");
		} else {
			entry.frameId = null;
		}

		try {
			if (nbt.hasKey("mut", Constants.NBT.TAG_COMPOUND)) {
				entry.mutationNBT = nbt.getCompoundTag("mut");
			} else {
				entry.mutationNBT = null;
			}
		} catch (Exception e) {
			entry.mutationNBT = null;
		}

		return entry;
	}

	public boolean matches(int fluidId, String frameId) {
		return this.fluidId == fluidId && (frameId == null || frameId.equals(this.frameId));
	}
}
