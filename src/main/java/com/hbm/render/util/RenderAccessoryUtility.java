package com.hbm.render.util;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.ModelArmorTailPeep;
import com.hbm.render.model.ModelArmorWings;
import com.hbm.render.model.ModelArmorWingsPheo;
import com.hbm.util.SONUtil;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class RenderAccessoryUtility {

	private static ResourceLocation hbm = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeHbm3.png");
	private static ResourceLocation hbm2 = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeHbm2.png");
	private static ResourceLocation drillgon = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeDrillgon.png");
	private static ResourceLocation dafnik = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeDafnik.png");
	private static ResourceLocation lpkukin = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeShield.png");
	private static ResourceLocation vertice = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeVertice_2.png");
	private static ResourceLocation red = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeRed.png");
	private static ResourceLocation ayy = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeAyy.png");
	private static ResourceLocation nostalgia2 = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeNostalgia2.png");
	private static ResourceLocation sam = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeSam.png");
	private static ResourceLocation hoboy = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeHoboy_mk3.png");
	private static ResourceLocation master = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeMaster.png");
	private static ResourceLocation mek = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeMek.png");
	private static ResourceLocation zippy = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeZippySqrl.png");
	private static ResourceLocation test = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeTest.png");
	private static ResourceLocation schrabby = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeSchrabbyAlt.png");
	private static ResourceLocation swiggs = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeSweatySwiggs.png");
	private static ResourceLocation doctor17 = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeDoctor17.png");
	private static ResourceLocation shimmeringblaze = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeBlaze.png");
	private static ResourceLocation blaze2 = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeBlaze2.png");
	private static ResourceLocation wiki = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeWiki.png");
	private static ResourceLocation leftnugget = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeLeftNugget.png");
	private static ResourceLocation rightnugget = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeRightNugget.png");
	private static ResourceLocation tankish = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeTankish.png");
	private static ResourceLocation frizzlefrazzle = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeFrizzleFrazzle.png");
	//private static ResourceLocation pheo = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapePheo.png");
	private static ResourceLocation vaer = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeVaer.png");
	private static ResourceLocation adam = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeAdam.png");
	private static ResourceLocation gwen = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeGwen.png");
	private static ResourceLocation mlow = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeMellow.png");

	private static ResourceLocation alcater = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeAlcater.png");
	private static ResourceLocation jame = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeJame.png");
	private static ResourceLocation iris = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeIris.png");
	private static ResourceLocation xenon = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeXenon.png");
	private static ResourceLocation jenga = new ResourceLocation(RefStrings.MODID + ":textures/models/capes/CapeJenga.png");






	public static ResourceLocation getCloakFromPlayer(EntityPlayer player) {

		String uuid = player.getUniqueID().toString();
		String name = player.getDisplayName();

		if(uuid.equals(SONUtil.HbMinecraft)) {
			return (MainRegistry.polaroidID == 11 ? hbm2 : hbm);
		}

		if(uuid.equals(SONUtil.Drillgon)) {
			return drillgon;
		}
		if(uuid.equals(SONUtil.Dafnik)) {
			return dafnik;
		}
		if(uuid.equals(SONUtil.LPkukin)) {
			return lpkukin;
		}
		if(uuid.equals(SONUtil.LordVertice)) {
			return vertice;
		}
		if(uuid.equals(SONUtil.CodeRed_)) {
			return red;
		}
		if(uuid.equals(SONUtil.dxmaster769)) {
			return ayy;
		}
		if(uuid.equals(SONUtil.Dr_Nostalgia)) {
			return nostalgia2;
		}
		if(uuid.equals(SONUtil.Samino2)) {
			return sam;
		}
		if(uuid.equals(SONUtil.Hoboy03new)) {
			return hoboy;
		}
		if(uuid.equals(SONUtil.Dragon59MC)) {
			return master;
		}
		if(uuid.equals(SONUtil.Steelcourage)) {
			return mek;
		}
		if(uuid.equals(SONUtil.ZippySqrl)) {
			return zippy;
		}
		if(uuid.equals(SONUtil.Schrabby)) {
			return schrabby;
		}
		if(uuid.equals(SONUtil.SweatySwiggs)) {
			return swiggs;
		}
		if(uuid.equals(SONUtil.Doctor17) || uuid.equals(SONUtil.Doctor17PH)) {
			return doctor17;
		}
		if(uuid.equals(SONUtil.ShimmeringBlaze)) {
			return (MainRegistry.polaroidID == 11 ? blaze2 : shimmeringblaze);
		}
		if(uuid.equals(SONUtil.FifeMiner)) {
			return leftnugget;
		}
		if(uuid.equals(SONUtil.lag_add)) {
			return rightnugget;
		}
		if(uuid.equals(SONUtil.Tankish)) {
			return tankish;
		}
		if(uuid.equals(SONUtil.FrizzleFrazzle)) {
			return frizzlefrazzle;
		}
		/*if(uuid.equals(SONUtil.Barnaby99_x)) {
			return pheo;
		}*/
		if(uuid.equals(SONUtil.Ma118)) {
			return vaer;
		}
		if(uuid.equals(SONUtil.Adam29Adam29)) {
			return adam;
		}
		if(uuid.equals(SONUtil.Alcater)) {
			return alcater;
		}
		if(uuid.equals(SONUtil.ege444)) {
			return jame;
		}
		if(SONUtil.contributors.contains(uuid)) {
			return wiki;
		}
		if(uuid.equals(SONUtil.DUODEC_)) {
			return gwen;
		}
		if(uuid.equals(SONUtil.MellowRPG8)) {
			return mlow;
		}
		// iq edition contributors
		if(uuid.equals(SONUtil.Iristhepianist)) {
			return iris;
		}
        // xenon birthday
		if(uuid.equals(SONUtil.xenonio)) {
			return xenon;
		}

		if(uuid.equals(SONUtil.jengatower)) {
			return jenga;
		}




		return null;
	}

	private static ModelBiped[] wingModels = new ModelBiped[10];
	public static void renderWings(RenderPlayerEvent.SetArmorModel event, int mode) {

		if(wingModels[mode] == null)
			wingModels[mode] = new ModelArmorWings(mode);

		RenderPlayer renderer = event.renderer;
		ModelBiped model = renderer.modelArmor;
		EntityPlayer player = event.entityPlayer;

		wingModels[mode].isSneak = model.isSneak;

		float interp = event.partialRenderTick;
		float yawHead = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * interp;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * interp;
		float yaw = yawHead - yawOffset;
		float yawWrapped = MathHelper.wrapAngleTo180_float(yawHead - yawOffset);
		float pitch = player.rotationPitch;

		wingModels[mode].render(event.entityPlayer, 0.0F, 0.0F, yawWrapped, yaw, pitch, 0.0625F);
	}

	private static ModelBiped axePackModel;
	public static void renderAxePack(RenderPlayerEvent.SetArmorModel event) {

		if(axePackModel == null)
			axePackModel = new ModelArmorWingsPheo();

		RenderPlayer renderer = event.renderer;
		ModelBiped model = renderer.modelArmor;
		EntityPlayer player = event.entityPlayer;

		axePackModel.isSneak = model.isSneak;

		float interp = event.partialRenderTick;
		float yawHead = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * interp;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * interp;
		float yaw = yawHead - yawOffset;
		float yawWrapped = MathHelper.wrapAngleTo180_float(yawHead - yawOffset);
		float pitch = player.rotationPitch;

		axePackModel.render(event.entityPlayer, 0.0F, 0.0F, yawWrapped, yaw, pitch, 0.0625F);
	}

	private static ModelBiped tailModel;
	public static void renderFaggot(RenderPlayerEvent.SetArmorModel event) {

		if(tailModel == null)
			tailModel = new ModelArmorTailPeep();

		RenderPlayer renderer = event.renderer;
		ModelBiped model = renderer.modelArmor;
		EntityPlayer player = event.entityPlayer;

		tailModel.isSneak = model.isSneak;

		float interp = event.partialRenderTick;
		float yawHead = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * interp;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * interp;
		float yaw = yawHead - yawOffset;
		float yawWrapped = MathHelper.wrapAngleTo180_float(yawHead - yawOffset);
		float pitch = player.rotationPitch;

		tailModel.render(event.entityPlayer, 0.0F, 0.0F, yawWrapped, yaw, pitch, 0.0625F);
	}
}
