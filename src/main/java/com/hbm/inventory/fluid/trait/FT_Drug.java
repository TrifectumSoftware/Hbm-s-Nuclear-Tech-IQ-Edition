package com.hbm.inventory.fluid.trait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.util.i18n.I18nUtil;
import com.hbm.potion.HbmPotion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class FT_Drug extends FluidTrait {

	public int consumption = 1;
	public float threshold;
	public float dissipationRate = 3.4F;
	private List<FT_Consumable.ConsumableEffect> effects = new ArrayList();
	private List<String> specialEffects = new ArrayList();
	private List<String> sideEffects = new ArrayList();

	public FT_Drug setConsumption(int rate) {
		this.consumption = rate;
		return this;
	}

	public FT_Drug setThreshold(float threshold) {
		this.threshold = threshold;
		return this;
	}

	public FT_Drug setDissipationRate(float rate) {
		this.dissipationRate = rate;
		return this;
	}

	public float getDecayFactor() {
		return (float) Math.pow(0.5, 1.0 / (this.dissipationRate * 20));
	}

	public float getDecayFactor(float concentration) {
		float hl = this.dissipationRate / (1F + concentration * 2F);
		return (float) Math.pow(0.5, 1.0 / (hl * 20));
	}

	public int getEffectDuration() {
		float dose = Math.max(10, consumption * 10F);
		float factor = getDecayFactor(0F);
		double halfLives = Math.log(0.5 / dose) / Math.log(factor);
		return (int) (halfLives / 20F);
	}

	public FT_Drug addEffect(int potionId, int amplifier) {
		effects.add(new FT_Consumable.ConsumableEffect(potionId, amplifier));
		return this;
	}

	public FT_Drug addSpecialEffect(String langKey) {
		specialEffects.add(langKey);
		return this;
	}

	public FT_Drug addSideEffect(String symptomKey) {
		sideEffects.add(symptomKey);
		return this;
	}

	public boolean hasSpecialEffect(String key) {
		return specialEffects.contains(key);
	}

	public List<FT_Consumable.ConsumableEffect> getEffects() {
		return effects;
	}

	public List<String> getSideEffects() {
		return sideEffects;
	}

	@Override
	public void addInfoHidden(List<String> info) {
		info.add(EnumChatFormatting.AQUA + "[" + I18nUtil.resolveKey("hbmfluid.trait.drug") + "]");

		if(effects.isEmpty() && specialEffects.isEmpty()) {
			info.add(EnumChatFormatting.YELLOW + "   - " + I18nUtil.resolveKey("hbmfluid.trait.noEffects"));
		}

		for(FT_Consumable.ConsumableEffect effect : effects) {
			String name = Potion.potionTypes[effect.potionId] != null ? StatCollector.translateToLocal(Potion.potionTypes[effect.potionId].getName()) : "Unknown";
			String amp = effect.amplifier > 0 ? " " + StatCollector.translateToLocal("potion.potency." + effect.amplifier).trim() : "";
			info.add(EnumChatFormatting.YELLOW + "   - " + name + amp);
		}

		for(String effect : specialEffects) {
			info.add(EnumChatFormatting.YELLOW + "   - " + I18nUtil.resolveKey(effect));
		}

		info.add(EnumChatFormatting.YELLOW + "   " + I18nUtil.resolveKey("hbmfluid.trait.consumption", consumption));

		if(threshold > 0) {
			info.add(EnumChatFormatting.RED + "   " + I18nUtil.resolveKey("hbmfluid.trait.whenInjected"));
			info.add(EnumChatFormatting.RED + "   " + I18nUtil.resolveKey("hbmfluid.trait.threshold", (int) (threshold * 100) + "%"));
		}

		if(!sideEffects.isEmpty()) {
			info.add(EnumChatFormatting.RED + "   " + I18nUtil.resolveKey("hbmfluid.trait.sideEffects"));
			for(String key : sideEffects) {
				com.hbm.handler.contagion.SymptomPool.Symptom symptom = com.hbm.handler.contagion.SymptomPool.getSymptom(key);
				String name = symptom != null && !symptom.effects.isEmpty() && Potion.potionTypes[symptom.effects.get(0).potionId] != null ? StatCollector.translateToLocal(Potion.potionTypes[symptom.effects.get(0).potionId].getName()) : key;
				info.add(EnumChatFormatting.RED + "   - " + name);
			}
		}

		if(dissipationRate > 0) {
			info.add(EnumChatFormatting.GRAY + "   " + I18nUtil.resolveKey("hbmfluid.trait.dissipation", getEffectDuration()));
		}
	}

	public void apply(EntityLivingBase entity, double intensity) {
		if(entity == null || !entity.isEntityAlive()) return;

		if(this.getFluidType() != null) com.hbm.extprop.HbmBloodstreamProps.getData(entity).addDrug(this.getFluidType().getID(), (int) Math.max(1, intensity * this.consumption));
		for(String special : specialEffects) {
			if("clear_effects".equals(special)) {
				entity.clearActivePotions();
			}
			if("clear_bad_effects".equals(special)) {
				List<Integer> toRemove = new ArrayList();
				for(Object o : entity.getActivePotionEffects()) {
					PotionEffect pe = (PotionEffect) o;
					if(HbmPotion.getIsBadEffect(Potion.potionTypes[pe.getPotionID()])) {
						toRemove.add(pe.getPotionID());
					}
				}
				for(Integer id : toRemove) {
					entity.removePotionEffect(id);
				}
			}
		}
	}

	@Override public void serializeJSON(JsonWriter writer) throws IOException {
		writer.name("consumption").value(consumption);
		writer.name("threshold").value(threshold);
		writer.name("dissipationRate").value(dissipationRate);
		writer.name("effects").beginArray();
		for(FT_Consumable.ConsumableEffect effect : effects) {
			writer.beginArray();
			writer.value(effect.potionId).value(effect.amplifier);
			writer.endArray();
		}
		writer.endArray();
		writer.name("specialEffects").beginArray();
		for(String effect : specialEffects) {
			writer.value(effect);
		}
		writer.endArray();
		writer.name("sideEffects").beginArray();
		for(String effect : sideEffects) {
			writer.value(effect);
		}
		writer.endArray();
	}

	@Override public void deserializeJSON(JsonObject obj) {
		if(obj.has("consumption")) this.consumption = obj.get("consumption").getAsInt();
		if(obj.has("threshold")) this.threshold = obj.get("threshold").getAsFloat();
		if(obj.has("dissipationRate")) this.dissipationRate = obj.get("dissipationRate").getAsFloat();
		JsonArray array = obj.get("effects").getAsJsonArray();
		for(int i = 0; i < array.size(); i++) {
			JsonArray entry = array.get(i).getAsJsonArray();
			FT_Consumable.ConsumableEffect effect = new FT_Consumable.ConsumableEffect(entry.get(0).getAsInt(), entry.get(1).getAsInt());
			this.effects.add(effect);
		}
		if(obj.has("specialEffects")) {
			JsonArray special = obj.get("specialEffects").getAsJsonArray();
			for(int i = 0; i < special.size(); i++) {
				this.specialEffects.add(special.get(i).getAsString());
			}
		}
		if(obj.has("sideEffects")) {
			JsonArray side = obj.get("sideEffects").getAsJsonArray();
			for(int i = 0; i < side.size(); i++) {
				this.sideEffects.add(side.get(i).getAsString());
			}
		}
	}
}
