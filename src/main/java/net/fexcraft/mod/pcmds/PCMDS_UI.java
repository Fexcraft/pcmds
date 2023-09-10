package net.fexcraft.mod.pcmds;

import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.mod.pcmds.SignData.Type;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class PCMDS_UI extends GenericGui<PCMDS_CON> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("pcmds:textures/gui/main.png");
	private static ArrayList<String> info = new ArrayList<>();

	public PCMDS_UI(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new PCMDS_CON(player, x, y, z), player);
		xSize = 256;
		ySize = 208;
	}
	
	@Override
	public void init(){
		int color = 0xdedede;
		texts.put("fee", new BasicText(guiLeft + 8, guiTop + 8, 68, color, "Use Fee:"));
		fields.put("fee", new NumberField(0, fontRenderer, guiLeft + 80, guiTop + 6, 170, 12));
		texts.put("type", new BasicText(guiLeft + 8, guiTop + 26, 68, color, "Type:"));
		buttons.put("type_basic", new BasicButton("basic", guiLeft + 80, guiTop + 24, 80, 24, 80, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				container.data.type = Type.BASIC;
				update();
				return true;
			}
		});
		texts.put("type_basic", new BasicText(guiLeft + 84, guiTop + 26, 74, color, "Basic"));
		buttons.put("type_rent", new BasicButton("rent", guiLeft + 80, guiTop + 38, 80, 38, 80, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				container.data.type = Type.RENT;
				update();
				return true;
			}
		});
		texts.put("type_rent", new BasicText(guiLeft + 84, guiTop + 40, 74, color, "Rent/Timed"));
		buttons.put("time_days", new BasicButton("days", guiLeft + 162, guiTop + 38, 162, 38, 28, 12, true));
		texts.put("time_days", new BasicText(guiLeft + 164, guiTop + 40, 76, color, "-d").autoscale().hoverable(true));
		buttons.put("time_hours", new BasicButton("hours", guiLeft + 192, guiTop + 38, 192, 38, 28, 12, true));
		texts.put("time_hours", new BasicText(guiLeft + 194, guiTop + 40, 76, color, "-h").autoscale().hoverable(true));
		buttons.put("time_mins", new BasicButton("mins", guiLeft + 222, guiTop + 38, 222, 38, 28, 12, true));
		texts.put("time_mins", new BasicText(guiLeft + 224, guiTop + 40, 76, color, "-m").autoscale().hoverable(true));
		container.gui = this;
		NBTTagCompound com = new NBTTagCompound();
		com.setBoolean("sync", true);
		container.send(Side.SERVER, com);
	}

	public void update(){
		if(container.data.type == Type.BASIC){
			BasicButton button = buttons.get("type_basic");
			button.tx = 2;
			button.ty = 226;
			button = buttons.get("type_rent");
			button.tx = 80;
			button.ty = 38;
			texts.get("time_days").string = "-";
			texts.get("time_hours").string = "-";
			texts.get("time_mins").string = "-";
		}
		else{
			BasicButton button = buttons.get("type_basic");
			button.tx = 80;
			button.ty = 24;
			button = buttons.get("type_rent");
			button.tx = 2;
			button.ty = 226;
			buttons.get("time_days").enabled = true;
			buttons.get("time_hours").enabled = true;
			buttons.get("time_mins").enabled = true;
			texts.get("time_days").string = "0d";
			texts.get("time_hours").string = "0h";
			texts.get("time_mins").string = "0m";
		}
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		//
	}
	
	@Override
	public void drawbackground(float ticks, int mx, int my){
		//
	}
	
	@Override
	protected void drawlast(float pticks, int mx, int my){
		info.clear();
		//
		if(info.size() > 0) drawHoveringText(info, mx, my);
	}
	
}