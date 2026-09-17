package com.hbm.handler.contagion;

import net.minecraft.nbt.NBTTagCompound;



public class PharmaProfile {

	public String target;
	public String targetGenome;

	public PharmaProfile() {
		this.target = null;
		this.targetGenome = null;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("target", target == null ? "" : target);
		if(targetGenome != null) nbt.setString("targetGenome", targetGenome);
	}

	public static PharmaProfile readFromNBT(NBTTagCompound nbt) {
		PharmaProfile profile = new PharmaProfile();

		if(nbt.hasKey("target")) profile.target = nbt.getString("target");
		if(nbt.hasKey("targetGenome")) profile.targetGenome = nbt.getString("targetGenome");

		return profile;
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return nbt;
	}
}
