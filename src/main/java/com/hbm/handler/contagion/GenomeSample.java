package com.hbm.handler.contagion;

import java.util.Map;

import com.hbm.handler.contagion.DiseaseDefinition.PathogenType;
import com.hbm.handler.contagion.DiseaseDefinition.TransmissionAxis;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemVial;
import com.hbm.items.ModItems;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class GenomeSample {

	public static final String KEY_SAMPLE = "gsample";
	public static final String KEY_VALUE = "gsvalue";

	public static final int GENOME_SEGMENT = 8;

	public static final String[] KEYS = {
			"severity", "resistance", "transmission",
			"id", "name", "type",
			"incubation", "duration",
			"baseSeverity", "baseResistance",
			"mutationRate", "mutationInterval", "antigenMutability",
			"transmissionInterval", "aerosol", "touch", "hit", "proximity", "injected",
			"range", "wetBoost", "fomite",
			"uncurable"
	};

	public static ItemStack make(String key, String value) {
		ItemStack vial = new ItemStack(ModItems.vial);
		IFillableItem.setFluidFill(vial, Fluids.DNA, (short) ItemVial.MAX_FLUID);
		if(!vial.hasTagCompound()) vial.stackTagCompound = new NBTTagCompound();
		vial.stackTagCompound.setString(KEY_SAMPLE, key);
		vial.stackTagCompound.setString(KEY_VALUE, value);
		return vial;
	}

	public static String getKey(ItemStack vial) {
		return vial != null && vial.hasTagCompound() && vial.stackTagCompound.hasKey(KEY_SAMPLE) ? vial.stackTagCompound.getString(KEY_SAMPLE) : null;
	}

	public static String getValue(ItemStack vial) {
		return vial != null && vial.hasTagCompound() && vial.stackTagCompound.hasKey(KEY_VALUE) ? vial.stackTagCompound.getString(KEY_VALUE) : null;
	}

	public static String valueOf(String key, String genome, DiseaseDefinition def) {
		if(def == null) return "";
		String g = Genome.ensure(genome, def.id);

		if("severity".equals(key)) return g.substring(0, GENOME_SEGMENT);
		if("resistance".equals(key)) return g.substring(GENOME_SEGMENT, GENOME_SEGMENT * 2);
		if("transmission".equals(key)) return g.substring(GENOME_SEGMENT * 2, GENOME_SEGMENT * 3);

		if("id".equals(key)) return def.id == null ? "" : def.id;
		if("name".equals(key)) return def.displayName == null ? "" : def.displayName;
		if("type".equals(key)) return def.type == null ? "" : def.type.name();
		if("incubation".equals(key)) return String.valueOf(def.incubationTicks);
		if("duration".equals(key)) return String.valueOf(def.durationTicks);
		if("baseSeverity".equals(key)) return String.valueOf(def.baseSeverity);
		if("baseResistance".equals(key)) return String.valueOf(def.baseResistance);
		if("mutationRate".equals(key)) return String.valueOf(def.baseMutationRate);
		if("mutationInterval".equals(key)) return String.valueOf(def.mutationIntervalTicks);
		if("antigenMutability".equals(key)) return String.valueOf(def.antigenMutability);
		if("transmissionInterval".equals(key)) return String.valueOf(def.transmissionIntervalTicks);
		if("aerosol".equals(key)) return axis(def, TransmissionAxis.AEROSOL);
		if("touch".equals(key)) return axis(def, TransmissionAxis.TOUCH);
		if("hit".equals(key)) return axis(def, TransmissionAxis.HIT);
		if("proximity".equals(key)) return axis(def, TransmissionAxis.PROXIMITY);
		if("injected".equals(key)) return axis(def, TransmissionAxis.INJECTED);
		if("range".equals(key)) return String.valueOf(def.range);
		if("wetBoost".equals(key)) return String.valueOf(def.wetBoost);
		if("fomite".equals(key)) return String.valueOf(def.fomite);
		if("uncurable".equals(key)) return String.valueOf(def.uncurable);

		return "";
	}

	private static String axis(DiseaseDefinition def, TransmissionAxis axis) {
		Float value = def.transmission == null ? null : def.transmission.get(axis);
		return String.valueOf(value == null ? 0F : value);
	}

	// unnused, to be used in the gene splcier

	public static String buildGenome(Map<String, String> pieces) {
		return get(pieces, "severity", "00000000") + get(pieces, "resistance", "00000000") + get(pieces, "transmission", "00000000");
	}

		public static DiseaseDefinition buildDefinition(Map<String, String> pieces) {
		DiseaseDefinition def = new DiseaseDefinition();
		def.id = get(pieces, "id", "");
		def.displayName = get(pieces, "name", def.id);
		try {
			String type = get(pieces, "type", "");
			if(!type.isEmpty()) def.type = PathogenType.valueOf(type);
		} catch(IllegalArgumentException e) { }
		def.incubationTicks = intVal(pieces, "incubation", def.incubationTicks);
		def.durationTicks = intVal(pieces, "duration", def.durationTicks);
		def.baseSeverity = floatVal(pieces, "baseSeverity", def.baseSeverity);
		def.baseResistance = floatVal(pieces, "baseResistance", def.baseResistance);
		def.baseMutationRate = floatVal(pieces, "mutationRate", def.baseMutationRate);
		def.mutationIntervalTicks = intVal(pieces, "mutationInterval", def.mutationIntervalTicks);
		def.antigenMutability = floatVal(pieces, "antigenMutability", def.antigenMutability);
		def.transmissionIntervalTicks = intVal(pieces, "transmissionInterval", def.transmissionIntervalTicks);
		def.range = floatVal(pieces, "range", def.range);
		def.wetBoost = floatVal(pieces, "wetBoost", def.wetBoost);
		def.fomite = boolVal(pieces, "fomite", def.fomite);
		def.uncurable = boolVal(pieces, "uncurable", def.uncurable);
		def.transmission.put(TransmissionAxis.AEROSOL, floatVal(pieces, "aerosol", 0F));
		def.transmission.put(TransmissionAxis.TOUCH, floatVal(pieces, "touch", 0F));
		def.transmission.put(TransmissionAxis.HIT, floatVal(pieces, "hit", 0F));
		def.transmission.put(TransmissionAxis.PROXIMITY, floatVal(pieces, "proximity", 0F));
		def.transmission.put(TransmissionAxis.INJECTED, floatVal(pieces, "injected", 0F));
		return def;
	}

	private static String get(Map<String, String> pieces, String key, String fallback) {
		String value = pieces == null ? null : pieces.get(key);
		return value == null || value.isEmpty() ? fallback : value;
	}

	private static int intVal(Map<String, String> pieces, String key, int fallback) {
		try { return Integer.parseInt(get(pieces, key, null)); } catch(Exception e) { return fallback; }
	}

	private static float floatVal(Map<String, String> pieces, String key, float fallback) {
		try { return Float.parseFloat(get(pieces, key, null)); } catch(Exception e) { return fallback; }
	}

	private static boolean boolVal(Map<String, String> pieces, String key, boolean fallback) {
		String value = pieces == null ? null : pieces.get(key);
		return value == null || value.isEmpty() ? fallback : Boolean.parseBoolean(value);
	}
}
