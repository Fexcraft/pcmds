package net.fexcraft.mod.pcmds;

import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.pcmds.SignData.Executor;
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
				update(false);
				return true;
			}
		});
		texts.put("type_basic", new BasicText(guiLeft + 84, guiTop + 26, 74, color, "Basic"));
		buttons.put("type_rent", new BasicButton("rent", guiLeft + 80, guiTop + 38, 80, 38, 80, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				container.data.type = Type.RENT;
				update(false);
				return true;
			}
		});
		texts.put("type_rent", new BasicText(guiLeft + 84, guiTop + 40, 74, color, "Rent/Timed"));
		//
		buttons.put("time_days", new BasicButton("days", guiLeft + 162, guiTop + 38, 162, 38, 28, 12, true));
		texts.put("time_days", new BasicText(guiLeft + 164, guiTop + 40, 76, color, "-d").autoscale().hoverable(true));
		buttons.put("time_hours", new BasicButton("hours", guiLeft + 192, guiTop + 38, 192, 38, 28, 12, true));
		texts.put("time_hours", new BasicText(guiLeft + 194, guiTop + 40, 76, color, "-h").autoscale().hoverable(true));
		buttons.put("time_mins", new BasicButton("mins", guiLeft + 222, guiTop + 38, 222, 38, 28, 12, true));
		texts.put("time_mins", new BasicText(guiLeft + 224, guiTop + 40, 76, color, "-m").autoscale().hoverable(true));
		//
		texts.put("executor", new BasicText(guiLeft + 8, guiTop + 58, 68, color, "Executor:"));
		buttons.put("exe_server", new BasicButton("server", guiLeft + 80, guiTop + 56, 80, 56, 54, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				container.data.exec = Executor.SERVER;
				update(false);
				return true;
			}
		});
		texts.put("exe_server", new BasicText(guiLeft + 82, guiTop + 58, 50, color, "Server"));
		buttons.put("exe_player", new BasicButton("player", guiLeft + 138, guiTop + 56, 138, 56, 54, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				container.data.exec = Executor.PLAYER;
				update(false);
				return true;
			}
		});
		texts.put("exe_player", new BasicText(guiLeft + 140, guiTop + 58, 50, color, "Player"));
		buttons.put("exe_operator", new BasicButton("operator", guiLeft + 196, guiTop + 56, 196, 56, 54, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				container.data.exec = Executor.OPERATOR;
				update(false);
				return true;
			}
		});
		texts.put("exe_operator", new BasicText(guiLeft + 198, guiTop + 58, 50, color, "Operator"));
		texts.put("operator", new BasicText(guiLeft + 8, guiTop + 72, 68, color, "Name/UUID"));
		fields.put("operator", new TextField(1, fontRenderer, guiLeft + 80, guiTop + 70, 170, 12));
		//
		container.gui = this;
		NBTTagCompound com = new NBTTagCompound();
		com.setBoolean("sync", true);
		container.send(Side.SERVER, com);
	}

	public void update(boolean packet){
		BasicButton button;
		if(packet){
			fields.get("fee").setText(Config.getWorthAsString(container.data.price, false));
			fields.get("operator").setText(container.opname);
		}
		if(container.data.type == Type.BASIC){
			button = buttons.get("type_basic");
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
			button = buttons.get("type_basic");
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
		if(container.data.exec == Executor.OPERATOR){
			button = buttons.get("exe_operator");
			button.tx = 200;
			button.ty = 226;
			button = buttons.get("exe_player");
			button.tx = 138;
			button.ty = 56;
			button = buttons.get("exe_server");
			button.tx = 80;
			button.ty = 56;
			fields.get("operator").setVisible(true);
			texts.get("operator").visible = true;
		}
		else{
			button = buttons.get("exe_operator");
			button.tx = 196;
			button.ty = 56;
			if(container.data.exec == Executor.PLAYER){
				button = buttons.get("exe_player");
				button.tx = 142;
				button.ty = 226;
				button = buttons.get("exe_server");
				button.tx = 80;
				button.ty = 56;
			}
			else{
				button = buttons.get("exe_player");
				button.tx = 138;
				button.ty = 56;
				button = buttons.get("exe_server");
				button.tx = 84;
				button.ty = 226;
			}
			fields.get("operator").setVisible(false);
			texts.get("operator").visible = false;
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