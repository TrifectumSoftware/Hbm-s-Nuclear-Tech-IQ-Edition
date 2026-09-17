package com.hbm.handler.contagion;

import java.util.Random;

import com.hbm.handler.contagion.DiseaseDefinition.PathogenType;
import com.hbm.handler.radiation.ChunkRadiationManager;

import net.minecraft.entity.EntityLivingBase;


public class Genome {

	public static final int LENGTH = 24;
	public static final String ALPHABET = "0123456789ABCDEF";

	// positions of the stats on the hexadecimal
	private static final int SEG_SEVERITY = 0;
	private static final int SEG_RESISTANCE = 8;
	private static final int SEG_TRANSMISSION = 16;
	private static final int SEG_SIZE = 8;

		public static String reference(String id) {
		Random rand = new Random(id.hashCode());
		StringBuilder sb = new StringBuilder(LENGTH);
		for(int i = 0; i < LENGTH; i++) sb.append(ALPHABET.charAt(rand.nextInt(ALPHABET.length())));
		return sb.toString();
	}


	public static String ensure(String genome, String id) {
		if(genome == null || genome.length() != LENGTH) return reference(id);
		return genome;
	}


	public static String mutate(String genome, int flips, Random rand) {
		char[] chars = genome.toCharArray();
		for(int f = 0; f < flips; f++) {
			int pos = rand.nextInt(LENGTH);
			chars[pos] = ALPHABET.charAt(rand.nextInt(ALPHABET.length()));
		}
		return new String(chars);
	}


	public static float distance(String a, String b) {
		if(a == null || b == null || a.length() != b.length()) return 1.0F;
		int diff = 0;
		for(int i = 0; i < a.length(); i++) if(a.charAt(i) != b.charAt(i)) diff++;
		return diff / (float) a.length();
	}

	public static float similarity(String a, String b) {
		return 1.0F - distance(a, b);
	}


	private static float segmentDrift(String genome, String ref, int start) {
		float sum = 0;
		for(int i = start; i < start + SEG_SIZE; i++) {
			sum += Math.abs(value(genome.charAt(i)) - value(ref.charAt(i))) / 15.0F;
		}
		return sum / SEG_SIZE;
	}

	private static int value(char c) {
		int v = Character.digit(c, 16);
		return Math.max(v, 0);
	}

	public static void derive(DiseaseInstance instance, DiseaseDefinition def) {
		String ref = reference(def.id);
		String genome = ensure(instance.genome, def.id);

		instance.currentSeverity = clamp((def.baseSeverity + segmentDrift(genome, ref, SEG_SEVERITY) * 0.4F) * instance.attenuation);
		instance.resistanceLevel = clamp(def.baseResistance + segmentDrift(genome, ref, SEG_RESISTANCE) * 0.6F);
		instance.visibility = clamp(1.0F - segmentDrift(genome, ref, SEG_TRANSMISSION) * (1.0F + def.antigenMutability * 2.0F));
	}

	private static float clamp(float f) {
		return Math.max(0.0F, Math.min(1.0F, f));
	}


	///  MUTATION SPEED MULTIPLIERS
	public static float getTypeMultiplier(PathogenType type) {
		if(type == null) return 1.0F;
		switch(type) {
			case VIRUS:     return 1.5F;
			case BACTERIA:  return 0.8F;
			case FUNGUS:    return 0.3F;
			case PARASITE:  return 0.5F;
			case PRION:     return 2.0F;
			default:        return 1.0F;
		}
	}


	public static float getRadBoost(EntityLivingBase entity) {
		try {
			float rads = ChunkRadiationManager.proxy.getRadiation(
				entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ);
			return 1.0F + Math.min(rads / 50.0F, 2.0F);
		} catch(Exception e) {
			return 1.0F;
		}
	}

	public static void tickMutation(DiseaseInstance instance, DiseaseDefinition def, EntityLivingBase entity) {
		if(entity == null || entity.worldObj.isRemote) return;
		if(instance.ticksRemaining <= 0) return;

		int interval = def.mutationIntervalTicks > 0 ? def.mutationIntervalTicks : 1;
		if(entity.worldObj.getTotalWorldTime() % interval != 0) return;

		float rate = def.baseMutationRate * getTypeMultiplier(def.type) * getRadBoost(entity);
		instance.mutationAccumulator += rate;

		while(instance.mutationAccumulator >= 1.0F) {
			instance.mutationAccumulator -= 1.0F;
			instance.genome = mutate(ensure(instance.genome, def.id), 1, entity.worldObj.rand);
			derive(instance, def);
		}
	}
}
