package com.hbm.handler;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

@SideOnly(Side.CLIENT)
public class FreeCursorHandler {

	private boolean cursorFree = false;
	private int cursorX, cursorY;

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {

		Minecraft mc = Minecraft.getMinecraft();

		if(!Display.isActive()) {
			cursorFree = false;
			HbmKeybinds.freeCursorKey.pressed = false;
			return;
		}

		boolean held = HbmKeybinds.freeCursorKey.getIsKeyPressed() && !Keyboard.isKeyDown(Keyboard.KEY_TAB);

		if(held && mc.currentScreen == null && mc.thePlayer != null && mc.inGameHasFocus) {
			cursorFree = true;
			mc.inGameHasFocus = false;
			Mouse.setGrabbed(false);
		} else if(cursorFree && !held) {
			cursorFree = false;

			if(!mc.inGameHasFocus && mc.currentScreen == null && mc.thePlayer != null) {
				mc.setIngameFocus();
			}
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		reassert();
	}

	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post event) {

		if(event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;
		reassert();
	}

	private void reassert() {

		if(!cursorFree) return;

		Minecraft mc = Minecraft.getMinecraft();
		if(!Display.isActive() || mc.currentScreen != null || mc.thePlayer == null) return;

		if(mc.inGameHasFocus) mc.inGameHasFocus = false;

		if(Mouse.isGrabbed()) {
			Mouse.setGrabbed(false);
			Mouse.setCursorPosition(cursorX, cursorY);
		} else {
			cursorX = Mouse.getX();
			cursorY = Mouse.getY();
		}
	}
}
