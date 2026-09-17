package com.hbm.handler.contagion;

import net.minecraft.nbt.NBTTagCompound;

public class DiseaseInstance {

	public String frameId;
	public long infectedAtTick;
	public float currentSeverity;
	public float resistanceLevel;
	public float mutationAccumulator;
	public long ticksRemaining;
	public String genome;
	public float visibility = 1.0F;
	public float attenuation = 1.0F;
	public NBTTagCompound frameDef;

	public DiseaseInstance() { }

	public DiseaseInstance(String frameId, long infectedAtTick, long ticksRemaining) {
		this.frameId = frameId;
		this.infectedAtTick = infectedAtTick;
		this.ticksRemaining = ticksRemaining;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("frame", frameId == null ? "" : frameId);
		nbt.setLong("infTick", infectedAtTick);
		nbt.setFloat("severity", currentSeverity);
		nbt.setFloat("resist", resistanceLevel);
		nbt.setFloat("mutAcc", mutationAccumulator);
		nbt.setLong("ticks", ticksRemaining);
		if(genome != null) nbt.setString("genome", genome);
		nbt.setFloat("vis", visibility);
		nbt.setFloat("att", attenuation);
		if(frameDef != null) nbt.setTag("def", frameDef);
	}

	public static DiseaseInstance readFromNBT(NBTTagCompound nbt) {
		DiseaseInstance instance = new DiseaseInstance();

		if(nbt.hasKey("frame")) instance.frameId = nbt.getString("frame");
		if(nbt.hasKey("infTick")) instance.infectedAtTick = nbt.getLong("infTick");
		if(nbt.hasKey("severity")) instance.currentSeverity = nbt.getFloat("severity");
		if(nbt.hasKey("resist")) instance.resistanceLevel = nbt.getFloat("resist");
		if(nbt.hasKey("mutAcc")) instance.mutationAccumulator = nbt.getFloat("mutAcc");
		if(nbt.hasKey("ticks")) instance.ticksRemaining = nbt.getLong("ticks");
		if(nbt.hasKey("genome")) instance.genome = nbt.getString("genome");
		if(nbt.hasKey("vis")) instance.visibility = nbt.getFloat("vis");
		if(nbt.hasKey("att")) instance.attenuation = nbt.getFloat("att");
		if(nbt.hasKey("def")) instance.frameDef = nbt.getCompoundTag("def");

		return instance;
	}
}
