package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.hbm.handler.contagion.Genome;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityGenomeSequencer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class GUIScreenGenomeSequencer extends GuiScreen {

	protected static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/gui_seq.png");
	protected static final ResourceLocation[] TAB_ICONS = {
		new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/seq_genome.png"),
		new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/seq_stats.png"),
		new ResourceLocation(RefStrings.MODID + ":textures/gui/machine/seq_transmission.png")
	};
	protected static final int TAB_SIZE = 16;
	protected static final int TAB_Y = 11;
	protected static final RenderItem itemRender = RenderItem.getInstance();
	protected static final int LINE = 12;
	protected static final int NAME_LIMIT = 12;

	protected static final String[] PAGE_NAMES = { "GENOME", "STATS", "TRANSMISSION" };

	protected TileEntityGenomeSequencer seq;

	protected int xSize = 256;
	protected int ySize = 194;
	protected int guiLeft;
	protected int guiTop;

	protected GuiTextField nameField;
	protected ItemStack diskIcon = new ItemStack(ModItems.floppy_disk);
	protected String input = "";

	protected int page = 0;
	protected int revealTicks = 0;
	protected int lastState = -1;
	protected int lastPage = -1;

	protected List<String> genome = new ArrayList<String>();
	protected List<String> stats = new ArrayList<String>();
	protected List<String> transmission = new ArrayList<String>();
	protected String genomeHex = "";

	public GUIScreenGenomeSequencer(TileEntityGenomeSequencer seq) {
		this.seq = seq;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		this.nameField = new GuiTextField(this.fontRendererObj, guiLeft + 22, guiTop + 165, 111, 10);
		this.nameField.setTextColor(0x00ff00);
		this.nameField.setDisabledTextColour(0x00ff00);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(NAME_LIMIT);
		this.nameField.setFocused(false);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if(seq.state == TileEntityGenomeSequencer.STATE_DECODED && !this.nameField.isFocused()) {
			this.nameField.setText(seq.name);
		}

		if(seq.state != lastState || page != lastPage) {
			lastState = seq.state;
			lastPage = page;
			revealTicks = 0;
		} else {
			revealTicks++;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {

		if(this.seq == null || this.seq.isInvalid()) {
			Minecraft.getMinecraft().thePlayer.closeScreen();
			return;
		}

		this.drawDefaultBackground();
		this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawGuiContainerForegroundLayer();
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	private void drawGuiContainerForegroundLayer() {

		if(!seq.hasDisk) {
			this.fontRendererObj.drawString("INSERT FLOPPY DISK", guiLeft + 16, guiTop + 16, 0x00ff00);
			return;
		}

		if(seq.state == TileEntityGenomeSequencer.STATE_AWAIT) {
			List<String> boot = new ArrayList<String>();
			boot.add("SCANNING DISK..");
			boot.add("BOOTING SEQ-OS v2.13.7");
			boot.add("SEQUENCE DISK? (y/n)");

			int budget = revealTicks * 3;
			int y = 16;
			for(String line : boot) {
				if(budget <= 0) break;
				int n = Math.min(line.length(), budget);
				this.fontRendererObj.drawString(line.substring(0, n), guiLeft + 16, guiTop + y, 0x00ff00);
				budget -= line.length();
				y += LINE;
			}
			if(budget >= 0) {
				String prompt = "> " + input;
				if(System.currentTimeMillis() % 1000 < 500) prompt += "_";
				this.fontRendererObj.drawString(prompt, guiLeft + 16, guiTop + y, 0x00ff00);
			}
			return;
		}

		parse();

		drawTabs();

		int budget = revealTicks * 3;
		if(page == 0) {
			drawGenomePage(budget);
		} else if(page == 1) {
			drawTextPage(stats, budget);
		} else {
			drawTextPage(transmission, budget);
		}

		drawNameField();
	}

	private void drawTabs() {
		int section = 240 / PAGE_NAMES.length;
		for(int i = 0; i < PAGE_NAMES.length; i++) {
			int x = 8 + i * section + (section - TAB_SIZE) / 2;
			drawTabIcon(i, guiLeft + x, guiTop + TAB_Y, i == page ? 1.0F : 0.4F);
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GUIElements.drawDivider(guiLeft + 8, guiTop + TAB_Y + TAB_SIZE + 3, 240, 0xFF00ff00);
	}


	private void drawTabIcon(int index, int x, int y, float tint) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(TAB_ICONS[index]);
		GL11.glColor4f(tint, tint, tint, 1.0F);
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x, y + TAB_SIZE, this.zLevel, 0, 1);
		tess.addVertexWithUV(x + TAB_SIZE, y + TAB_SIZE, this.zLevel, 1, 1);
		tess.addVertexWithUV(x + TAB_SIZE, y, this.zLevel, 1, 0);
		tess.addVertexWithUV(x, y, this.zLevel, 0, 0);
		tess.draw();
	}

	private void drawGenomePage(int budget) {
		double spin = 1.0;
		double phase = System.currentTimeMillis() / 1000.0 * 0.5;
		GUIElements.drawHelix(guiLeft + 8, guiLeft + 248, guiTop + 70, 0, 24, 2, spin, phase, Genome.LENGTH, 0xFF00ff00);

		if(!genomeHex.isEmpty()) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			int half = (genomeHex.length() + 1) / 2;
			drawGenomeHalf(genomeHex.substring(0, half), 0, 112);
			if(genomeHex.length() > half) drawGenomeHalf(genomeHex.substring(half), half, 112 + LINE);
		}
	}

	private void drawGenomeHalf(String hex, int offset, int y) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < hex.length(); i++) {
			int bit = offset + i;
			EnumChatFormatting c = bit < 8 ? EnumChatFormatting.RED : bit < 16 ? EnumChatFormatting.AQUA : EnumChatFormatting.GREEN;
			sb.append(c).append(hex.charAt(i));
		}
		String s = sb.toString();
		int width = this.fontRendererObj.getStringWidth(s);
		this.fontRendererObj.drawString(s, guiLeft + 128 - width / 2, guiTop + y, 0x00ff00);
	}

	private void drawNameField() {
		boolean editable = page == 1;
		if(!editable) this.nameField.setFocused(false);
		this.nameField.setTextColor(editable ? 0x00ff00 : 0x555555);
		this.nameField.setDisabledTextColour(editable ? 0x00ff00 : 0x555555);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.nameField.drawTextBox();
	}

	private void drawTextPage(List<String> lines, int budget) {
		List<String> wrapped = GUIElements.wrapText(lines, 34);
		int y = TAB_Y + TAB_SIZE + 6;
		for(String line : wrapped) {
			if(budget <= 0) break;
			int n = Math.min(line.length(), budget);
			this.fontRendererObj.drawString(line.substring(0, n), guiLeft + 16, guiTop + y, 0x00ff00);
			budget -= line.length();
			y += LINE;
		}
	}

	private void parse() {
		genome.clear();
		stats.clear();
		transmission.clear();

		StringBuilder hex = new StringBuilder();
		boolean trans = false;
		for(int i = 0; i < seq.text.length; i++) {
			String line = seq.text[i];
			if(line == null || line.isEmpty()) continue;
			if(line.startsWith("GENOME:")) { genome.add(line); hex.append(line.substring(7).trim()); continue; }
			if(hex.length() > 0 && line.matches("\\s*[0-9A-Fa-f]+")) { genome.add(line); hex.append(line.trim()); continue; }
			if(line.equals("TRANSMISSION")) trans = true;
			if(trans) transmission.add(line); else stats.add(line);
		}
		genomeHex = hex.toString();
	}

	private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(seq.hasDisk) {
			itemRender.zLevel = 300.0F;
			itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), diskIcon, guiLeft + 198, guiTop + 162);
			itemRender.zLevel = 0.0F;
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	protected void mouseClicked(int x, int y, int i) {
		super.mouseClicked(x, y, i);

		if(seq.state == TileEntityGenomeSequencer.STATE_DECODED) {
			int section = 240 / PAGE_NAMES.length;
			for(int p = 0; p < PAGE_NAMES.length; p++) {
				int tx = 8 + p * section + (section - TAB_SIZE) / 2;
				if(x >= guiLeft + tx && x < guiLeft + tx + TAB_SIZE && y >= guiTop + TAB_Y && y < guiTop + TAB_Y + TAB_SIZE) {
					this.page = p;
					this.revealTicks = 0;
				}
			}
			if(page == 1) this.nameField.mouseClicked(x, y, i);
		}
	}

	@Override
	protected void keyTyped(char c, int i) {

		if(seq.state == TileEntityGenomeSequencer.STATE_DECODED) {
			if(this.nameField.isFocused() && page == 1) {
				if(i == 28) { // enter: save the name
					sendName();
					return;
				}
				if(this.nameField.textboxKeyTyped(c, i)) return;
			}
			if(i == Keyboard.KEY_LEFT) {
				this.page = (this.page + PAGE_NAMES.length - 1) % PAGE_NAMES.length;
				this.revealTicks = 0;
				return;
			}
			if(i == Keyboard.KEY_RIGHT) {
				this.page = (this.page + 1) % PAGE_NAMES.length;
				this.revealTicks = 0;
				return;
			}
		}

		if(i == 1 || i == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.thePlayer.closeScreen();
			return;
		}

		if(seq.state != TileEntityGenomeSequencer.STATE_AWAIT) return;

		if(i == 28) { // enter: run the command
			submitCommand();
			return;
		}

		if(i == 14) { // backspace
			if(input.length() > 0) input = input.substring(0, input.length() - 1);
			return;
		}

		if(ChatAllowedCharacters.isAllowedCharacter(c) && input.length() < 32) {
			input += c;
		}
	}

	private void submitCommand() {
		String cmd = input.trim().toLowerCase();
		input = "";

		NBTTagCompound data = new NBTTagCompound();
		if(cmd.equals("y") || cmd.equals("yes")) {
			data.setBoolean("anal", true);
		} else if(cmd.equals("n") || cmd.equals("no")) {
			data.setBoolean("eject", true);
		} else {
			return;
		}

		mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, seq.xCoord, seq.yCoord, seq.zCoord));
	}

	private void sendName() {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("name", this.nameField.getText());
		mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, seq.xCoord, seq.yCoord, seq.zCoord));
	}

	@Override public boolean doesGuiPauseGame() { return false; }
}
