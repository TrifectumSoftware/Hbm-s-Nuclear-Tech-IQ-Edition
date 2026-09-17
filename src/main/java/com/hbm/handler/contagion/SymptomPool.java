package com.hbm.handler.contagion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.hbm.inventory.fluid.trait.FT_Consumable;
import com.hbm.potion.HbmPotion;

import net.minecraft.potion.Potion;

public class SymptomPool {

	public static class Symptom {
		public List<FT_Consumable.ConsumableEffect> effects = new ArrayList<>();
	}

	public static final HashMap<String, Symptom> SYMPTOMS = new HashMap<>();

	public static void registerSymptom(String key, FT_Consumable.ConsumableEffect... effects) {
		Symptom symptom = new Symptom();
		Collections.addAll(symptom.effects, effects);
		SYMPTOMS.put(key, symptom);
	}

	public static Symptom getSymptom(String key) {
		return SYMPTOMS.get(key);
	}

	public static void init() {

		registerSymptom("rash", new FT_Consumable.ConsumableEffect(HbmPotion.symptomRash.id, 0));
		registerSymptom("sneeze", new FT_Consumable.ConsumableEffect(HbmPotion.symptomSneeze.id, 0));
		registerSymptom("cough", new FT_Consumable.ConsumableEffect(HbmPotion.symptomCough.id, 0), new FT_Consumable.ConsumableEffect(Potion.digSlowdown.id, 1));
		registerSymptom("nausea", new FT_Consumable.ConsumableEffect(Potion.confusion.id, 0), new FT_Consumable.ConsumableEffect(Potion.hunger.id, 1));
		registerSymptom("fever", new FT_Consumable.ConsumableEffect(HbmPotion.symptomFever.id, 0), new FT_Consumable.ConsumableEffect(Potion.digSlowdown.id, 2));
		registerSymptom("vomit", new FT_Consumable.ConsumableEffect(HbmPotion.symptomVomit.id, 0), new FT_Consumable.ConsumableEffect(Potion.hunger.id, 2));
		registerSymptom("schizophrenia", new FT_Consumable.ConsumableEffect(HbmPotion.symptomSchizophrenia.id, 0), new FT_Consumable.ConsumableEffect(Potion.blindness.id, 0));
		registerSymptom("hemorrhage", new FT_Consumable.ConsumableEffect(HbmPotion.symptomHemorrhage.id, 0), new FT_Consumable.ConsumableEffect(Potion.weakness.id, 2));
		registerSymptom("paralysis", new FT_Consumable.ConsumableEffect(HbmPotion.symptomParalysis.id, 0), new FT_Consumable.ConsumableEffect(Potion.moveSlowdown.id, 4), new FT_Consumable.ConsumableEffect(Potion.digSlowdown.id, 4));
		registerSymptom("seizure", new FT_Consumable.ConsumableEffect(HbmPotion.symptomSeizure.id, 0), new FT_Consumable.ConsumableEffect(Potion.confusion.id, 2));
		registerSymptom("septic", new FT_Consumable.ConsumableEffect(HbmPotion.symptomSeptic.id, 0), new FT_Consumable.ConsumableEffect(Potion.moveSlowdown.id, 2));
		registerSymptom("coma", new FT_Consumable.ConsumableEffect(HbmPotion.symptomComa.id, 0), new FT_Consumable.ConsumableEffect(Potion.blindness.id, 0));
		registerSymptom("aortic", new FT_Consumable.ConsumableEffect(HbmPotion.symptomAortic.id, 0));
		registerSymptom("necrosis", new FT_Consumable.ConsumableEffect(HbmPotion.symptomNecrosis.id, 0), new FT_Consumable.ConsumableEffect(Potion.digSlowdown.id, 3));
		registerSymptom("cardiac", new FT_Consumable.ConsumableEffect(HbmPotion.symptomCardiac.id, 0), new FT_Consumable.ConsumableEffect(Potion.blindness.id, 0));

		// compat
		registerSymptom("harm", new FT_Consumable.ConsumableEffect(Potion.harm.id, 0));
		registerSymptom("confusion", new FT_Consumable.ConsumableEffect(Potion.confusion.id, 0));
		registerSymptom("wither", new FT_Consumable.ConsumableEffect(Potion.wither.id, 0));
		registerSymptom("hunger", new FT_Consumable.ConsumableEffect(Potion.hunger.id, 1));

		// covid 19 femboy theorem?
		registerSymptom("death", new FT_Consumable.ConsumableEffect(HbmPotion.death.id, 0));
	}
}
