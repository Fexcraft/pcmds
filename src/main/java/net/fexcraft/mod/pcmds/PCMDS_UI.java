package net.fexcraft.mod.pcmds;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Print;
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
	private boolean edittext0 = true;
	private int currcom, days, hours, mins;

	public PCMDS_UI(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new PCMDS_CON(player, x, y, z), player);
		xSize = 256;
		ySize = 208;
	}
	
	@Override
	public void init(){
		int color = 0xdedede;
		int celor = 0x5d5d5d;
		texts.put("fee", new BasicText(guiLeft + 8, guiTop + 8, 68, color, "Use Fee:"));
		fields.put("fee", new NumberField(0, fontRenderer, guiLeft + 80, guiTop + 6, 170, 12));
		texts.put("type", new BasicText(guiLeft + 8, guiTop + 26, 68, color, "Type:"));
		buttons.put("type_basic", new BasicButton("basic", guiLeft + 80, guiTop + 24, 80, 24, 80, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				container.data.type = Type.BASIC;
				update(false);
				return true;
			}
		});
		texts.put("type_basic", new BasicText(guiLeft + 84, guiTop + 26, 74, color, "Basic"));
		buttons.put("type_rent", new BasicButton("rent", guiLeft + 80, guiTop + 38, 80, 38, 80, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				container.data.type = Type.RENT;
				update(false);
				return true;
			}
		});
		texts.put("type_rent", new BasicText(guiLeft + 84, guiTop + 40, 74, color, "Rent/Timed"));
		//
		buttons.put("time_days", new BasicButton("days", guiLeft + 162, guiTop + 31, 162, 31, 28, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				days += mb == 0 ? 1 : -1;
				if(days > 31) days = 0;
				if(days < 0) days = 31;
				update(false);
				return true;
			}
		});
		texts.put("time_days", new BasicText(guiLeft + 164, guiTop + 33, 24, color, "-d").autoscale().hoverable(true));
		buttons.put("time_hours", new BasicButton("hours", guiLeft + 192, guiTop + 31, 192, 31, 28, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				hours += mb == 0 ? 1 : -1;
				if(hours > 24) hours = 0;
				if(hours < 0) hours = 24;
				update(false);
				return true;
			}
		});
		texts.put("time_hours", new BasicText(guiLeft + 194, guiTop + 33, 24, color, "-h").autoscale().hoverable(true));
		buttons.put("time_mins", new BasicButton("mins", guiLeft + 222, guiTop + 31, 222, 31, 28, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				mins += mb == 0 ? 1 : -1;
				if(mins > 60) mins = 0;
				if(mins < 0) mins = 60;
				update(false);
				return true;
			}
		});
		texts.put("time_mins", new BasicText(guiLeft + 224, guiTop + 33, 24, color, "-m").autoscale().hoverable(true));
		//
		texts.put("executor", new BasicText(guiLeft + 8, guiTop + 58, 68, color, "Executor:"));
		buttons.put("exe_server", new BasicButton("server", guiLeft + 80, guiTop + 56, 80, 56, 54, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				container.data.exec = Executor.SERVER;
				update(false);
				return true;
			}
		});
		texts.put("exe_server", new BasicText(guiLeft + 82, guiTop + 58, 50, color, "Server"));
		buttons.put("exe_player", new BasicButton("player", guiLeft + 138, guiTop + 56, 138, 56, 54, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				container.data.exec = Executor.PLAYER;
				update(false);
				return true;
			}
		});
		texts.put("exe_player", new BasicText(guiLeft + 140, guiTop + 58, 50, color, "Player"));
		buttons.put("exe_operator", new BasicButton("operator", guiLeft + 196, guiTop + 56, 196, 56, 54, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				container.data.exec = Executor.OPERATOR;
				update(false);
				return true;
			}
		});
		texts.put("exe_operator", new BasicText(guiLeft + 198, guiTop + 58, 50, color, "Operator"));
		texts.put("operator", new BasicText(guiLeft + 8, guiTop + 72, 68, color, "Name/UUID"));
		fields.put("operator", new TextField(1, fontRenderer, guiLeft + 80, guiTop + 70, 170, 12).setMaxLength(64));
		//
		texts.put("signtext", new BasicText(guiLeft + 8, guiTop + 90, 68, celor, "Sign Text:"));
		texts.put("text_event_0", new BasicText(guiLeft + 106, guiTop + 90, 68, color, "event0"));
		texts.put("text_event_1", new BasicText(guiLeft + 180, guiTop + 90, 68, color, "event1"));
		buttons.put("text_event_0", new BasicButton("e0", guiLeft + 104, guiTop + 88, 182, 242, 72, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				edittext0 = true;
				update(false);
				return true;
			}
		});
		buttons.put("text_event_1", new BasicButton("e1", guiLeft + 178, guiTop + 88, 182, 242, 72, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				edittext0 = false;
				update(false);
				return true;
			}
		});
		for(int i = 0; i < 4; i++){
			fields.put("text" + i, new TextField(2 + i, fontRenderer, guiLeft + 7, guiTop + 103 + i * 14, 242, 10).setMaxLength(64));
		}
		//
		texts.put("commands", new BasicText(guiLeft + 8, guiTop + 164, 68, celor, "Commands:"));
		texts.put("cmd_event_0", new BasicText(guiLeft + 106, guiTop + 164, 68, color, "event0"));
		texts.put("cmd_event_1", new BasicText(guiLeft + 180, guiTop + 164, 68, color, "event1"));
		buttons.put("cmd_event_0", new BasicButton("c0", guiLeft + 104, guiTop + 162, 182, 242, 72, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				edittext0 = true;
				update(false);
				return true;
			}
		});
		buttons.put("cmd_event_1", new BasicButton("c1", guiLeft + 178, guiTop + 162, 182, 242, 72, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				edittext0 = false;
				update(false);
				return true;
			}
		});
		fields.put("command", new TextField(6, fontRenderer, guiLeft + 7, guiTop + 177, 242, 10).setMaxLength(1024));
		texts.put("status", new BasicText(guiLeft + 8, guiTop + 192, 152, color, "status").hoverable(true).autoscale());
		//
		buttons.put("cmd_add", new BasicButton("ca", guiLeft + 178, guiTop + 190, 178, 190, 12, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				String event = container.data.type == Type.BASIC ? Type.BASIC.cmd_events[0] : Type.RENT.cmd_events[edittext0 ? 0 : 1];
				if(!container.data.events.containsKey(event)){
					container.data.events.put(event, new ArrayList<>());
				}
				container.data.events.get(event).add("");
				currcom = container.data.events.get(event).size() - 1;
				update(false);
				return true;
			}
		});
		buttons.put("cmd_rem", new BasicButton("cr", guiLeft + 164, guiTop + 190, 164, 190, 12, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				String event = container.data.type == Type.BASIC ? Type.BASIC.cmd_events[0] : Type.RENT.cmd_events[edittext0 ? 0 : 1];
				if(!container.data.events.containsKey(event)) return true;
				container.data.events.get(event).remove(currcom);
				if(currcom >= container.data.events.get(event).size()) currcom = container.data.events.get(event).size() - 1;
				if(currcom < 0) currcom = 0;
				update(false);
				return true;
			}
		});
		buttons.put("cmd_prev", new BasicButton("cp", guiLeft + 192, guiTop + 190, 192, 190, 12, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				String event = container.data.type == Type.BASIC ? Type.BASIC.cmd_events[0] : Type.RENT.cmd_events[edittext0 ? 0 : 1];
				if(!container.data.events.containsKey(event)){
					currcom = 0;
				}
				else{
					currcom--;
					if(currcom < 0) currcom = container.data.events.get(event).size() - 1;
				}
				update(false);
				return true;
			}
		});
		buttons.put("cmd_next", new BasicButton("cn", guiLeft + 206, guiTop + 190, 206, 190, 12, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				savetext();
				savecmd();
				String event = container.data.type == Type.BASIC ? Type.BASIC.cmd_events[0] : Type.RENT.cmd_events[edittext0 ? 0 : 1];
				if(!container.data.events.containsKey(event)){
					currcom = 0;
				}
				else{
					currcom++;
					if(currcom >= container.data.events.get(event).size()) currcom = 0;
				}
				update(false);
				return true;
			}
		});
		buttons.put("save", new BasicButton("save", guiLeft + 224, guiTop + 190, 224, 190, 12, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				sendsave(false);
				return true;
			}
		});
		buttons.put("act", new BasicButton("act", guiLeft + 238, guiTop + 190, 238, 190, 12, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				sendsave(true);
				return true;
			}
		});
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
			int time = container.data.settings.get(container.data.type.durtag(), 1);
			mins = time % 60;
			hours = (time % 1440) / 60;
			days = time / 1440;
		}
		if(container.data.type == Type.BASIC){
			button = buttons.get("type_basic");
			button.tx = 2;
			button.ty = 226;
			button = buttons.get("type_rent");
			button.tx = 80;
			button.ty = 38;
			days = hours = mins = 0;
		}
		else{
			button = buttons.get("type_basic");
			button.tx = 80;
			button.ty = 24;
			button = buttons.get("type_rent");
			button.tx = 2;
			button.ty = 226;
		}
		texts.get("time_days").string = days + "d";
		texts.get("time_hours").string = hours + "h";
		texts.get("time_mins").string = mins + "m";
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
		if(container.data.type == Type.BASIC){
			texts.get("text_event_0").string = texts.get("cmd_event_0").string = Type.BASIC.cmd_events[0];
			texts.get("text_event_1").string = texts.get("cmd_event_1").string = "-";
			button = buttons.get("text_event_0");
			button.enabled = true;
			button.tx = 34;
			button = buttons.get("text_event_1");
			button.enabled = false;
			button.tx = 182;
			String[] text = container.data.ctext.get(Type.BASIC.cmd_events[0]);
			for(int i = 0; i < 4; i++){
				fields.get("text" + i).setText(text == null || text[i] == null? "" : text[i]);
			}
			button = buttons.get("cmd_event_0");
			button.enabled = true;
			button.tx = 34;
			button = buttons.get("cmd_event_1");
			button.enabled = false;
			button.tx = 182;
		}
		else{
			texts.get("text_event_0").string = texts.get("cmd_event_0").string = Type.RENT.cmd_events[0];
			texts.get("text_event_1").string = texts.get("cmd_event_1").string = Type.RENT.cmd_events[1];
			buttons.get("text_event_1").enabled = true;
			button = buttons.get("text_event_0");
			button.enabled = true;
			button.tx = edittext0 ? 34 : 108;
			button = buttons.get("text_event_1");
			button.enabled = true;
			button.tx = edittext0 ? 108 : 34;
			String[] text = container.data.ctext.get(Type.RENT.cmd_events[edittext0 ? 0 : 1]);
			for(int i = 0; i < 4; i++){
				fields.get("text" + i).setText(text == null || text[i] == null? "" : text[i]);
			}
			button = buttons.get("cmd_event_0");
			button.enabled = true;
			button.tx = edittext0 ? 34 : 108;
			button = buttons.get("cmd_event_1");
			button.enabled = true;
			button.tx = edittext0 ? 108 : 34;
		}
		String event = container.data.type == Type.BASIC ? Type.BASIC.cmd_events[0] : Type.RENT.cmd_events[edittext0 ? 0 : 1];
		if(!container.data.events.containsKey(event)){
			container.data.events.put(event, new ArrayList<>());
			container.data.events.get(event).add("");
		}
		int am = container.data.events.get(event).size();
		if(currcom >= am) currcom = 0;
		texts.get("status").string = "Command: " + (currcom  + 1) + " of " + am + "; Ev.: " + event;
		fields.get("command").setText(am == 0 ? "" : container.data.events.get(event).get(currcom));
	}

	public void savetext(){
		String entry = container.data.type == Type.BASIC ? Type.BASIC.cmd_events[0] : Type.RENT.cmd_events[edittext0 ? 0 : 1];
		String[] text = container.data.ctext.get(entry);
		for(int i = 0; i < 4; i++){
			String str = fields.get("text" + i).getText();
			if(str.length() <= 0) continue;
			if(text == null) text = new String[container.sign.signText.length];
			text[i] = str;
		}
		boolean empty = true;
		if(text != null){
			for(int i = 0; i < text.length ; i++){
				if(text[i] != null && text[i].length() > 0) empty = false;
			}
		}
		if(text != null && !empty){
			container.data.ctext.put(entry, text);
		}
	}

	public void savecmd(){
		String event = container.data.type == Type.BASIC ? Type.BASIC.cmd_events[0] : Type.RENT.cmd_events[edittext0 ? 0 : 1];
		if(!container.data.events.containsKey(event)) return;
		if(container.data.events.size() == 0) container.data.events.get(event).add("");
		container.data.events.get(event).set(currcom, fields.get("command").getText());
	}

	private static final DecimalFormat df = new DecimalFormat("#.000", new DecimalFormatSymbols(Locale.US));
	static { df.setRoundingMode(RoundingMode.DOWN); }

	public long parsefee(){
		try{
			String str = fields.get("fee").getText().replace(Config.getDot(), "").replace(",", ".");
			if(str.length() == 0) return 0;
			String format = df.format(Double.parseDouble(str));
			return Long.parseLong(format.replace(",", "").replace(".", ""));
		}
		catch(Exception e){
			Print.chat(player, EditCmd.trs("fee_invalid"));
			e.printStackTrace();
			return container.data.price;
		}
	}

	public int gettime(){
		return days * 1440 + hours * 60 + mins;
	}

	public void sendsave(boolean act){
		container.data.price = parsefee();
		container.data.settings.put(container.data.type.durtag(), gettime());
		savecmd();
		savetext();
		NBTTagCompound com = new NBTTagCompound();
		com.setTag("update", container.data.save(new NBTTagCompound(), false));
		com.setBoolean("activate", act);
		if(container.data.exec == Executor.OPERATOR){
			com.setString("exec", fields.get("operator").getText());
		}
		container.send(Side.SERVER, com);
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
		if(texts.get("status").hovered(mx, my)) info.add(texts.get("status").string);
		if(texts.get("time_days").hovered(mx, my)) addTimeInfo();
		if(texts.get("time_hours").hovered(mx, my)) addTimeInfo();
		if(texts.get("time_mins").hovered(mx, my)) addTimeInfo();
		if(buttons.get("cmd_add").hovered(mx, my)) info.add("Add a New Command");
		if(buttons.get("cmd_rem").hovered(mx, my)) info.add("Remove Current Command");
		if(buttons.get("cmd_prev").hovered(mx, my)) info.add("Previous Command");
		if(buttons.get("cmd_next").hovered(mx, my)) info.add("Next Command");
		if(buttons.get("save").hovered(mx, my)) info.add("Save and Exit");
		if(buttons.get("act").hovered(mx, my)) info.add("Save and Activate");
		if(info.size() > 0) drawHoveringText(info, mx, my);
	}

	private void addTimeInfo(){
		if(container.data.type == Type.BASIC){
			info.add("Set renew time for use limits.");
		}
		else{
			info.add("Set rent duration.");
		}
		info.add("Left click to increase.");
		info.add("Right click to decrease.");
	}

}