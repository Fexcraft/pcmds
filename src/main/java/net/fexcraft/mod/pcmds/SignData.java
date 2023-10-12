package net.fexcraft.mod.pcmds;

import static net.fexcraft.mod.pcmds.EditCmd.trs;
import static net.fexcraft.mod.pcmds.SignCapImpl.formattedComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.FCLCapabilities;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.pcmds.PayableCommandSigns.DimPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class SignData {

	public static String remaining = "{remaining}";
	public static final int MINUTE = 60000;
	public HashMap<String, String[]> ctext = new HashMap<>();
	public Type type = Type.BASIC;
	public Executor exec = Executor.SERVER;
	public UUID exid;
	public HashMap<String, ArrayList<String>> events = new HashMap<>();
	public Settings settings = new Settings();
	public long price = 10000;
	public boolean noplayer;
	public boolean showremaining;
	public DimPos pos;
	//
	public long refresh = 0, cooldown = 0;
	public int timer = 0, textlength;
	public HashMap<UUID, Integer> uses = new HashMap<>();
	
	public SignData(int length){
		textlength = length;
	}

	public SignData load(SignCapImpl cap, TileEntitySign tile, NBTTagCompound com){
		if(com.hasKey("type")) type = Type.valueOf(com.getString("type"));
		if(com.hasKey("exec")) exec = Executor.valueOf(com.getString("exec"));
		if(exec == Executor.OPERATOR && exid != null){
			com.setLong("exec0", exid.getMostSignificantBits());
			com.setLong("exec1", exid.getMostSignificantBits());
		}
		price = com.getLong("fee");
		settings.clear();
		events.clear();
		ctext.clear();
		uses.clear();
		for(String str : type.settings){
			if(com.hasKey("set:" + str)) settings.put(str, com.getInteger("set:" + str));
		}
		for(String event : type.cmd_events){
			if(com.hasKey("event:" + event)){
				NBTTagList list = (NBTTagList)com.getTag("event:" + event);
				ArrayList<String> alist = new ArrayList<>();
				for(NBTBase base : list){
					alist.add(((NBTTagString)base).getString());
				}
				events.put(event, alist);
			}
		}
		String[] txt = refresh == 0 ? type.cmd_events : type.cmdtexts();
		for(String event : txt){
			if(!com.hasKey("text:" + event)) continue;
			NBTTagCompound texts = com.getCompoundTag("text:" + event);
			if(!ctext.containsKey(event)) ctext.put(event, new String[textlength]);
			String[] txts = ctext.get(event);
			for(int i = 0; i < textlength; i++){
				String str = texts.getString("l" + i);
				txts[i] = str.length() == 0 ? null : str;
			}
		}
		if(com.hasKey("pos")) pos = new DimPos(com.getString("pos"));
		if(com.hasKey("refresh")){
			refresh = com.getLong("refresh");
			timer = (int)((refresh - Time.getDate()) / MINUTE);
			if(timer < 0) setupTimer(true);
		}
		if(com.hasKey("uses")){
			NBTTagCompound use = com.getCompoundTag("uses");
			for(String str : use.getKeySet()){
				uses.put(UUID.fromString(str), use.getInteger(str));
			}
		}
		if(com.hasKey("cooldown")) cooldown = com.getLong("cooldown");
		if(com.hasKey("noplayer")) noplayer = com.getBoolean("noplayer");
		if(com.hasKey("show_time")) showremaining = com.getBoolean("show_time");
		if(cap == null || tile == null) return this;
		try{
			String[] text = this.ctext.get(type == Type.RENT && uses.isEmpty() ? type.cmd_events[1] : type.cmd_events[0]);
			if(text != null){
				for(int i = 0; i < text.length; i++){
					if(i >= tile.signText.length) break;
					tile.signText[i] = formattedComponent(text[i]);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return this;
	}

	public NBTTagCompound save(NBTTagCompound com, boolean export){
		com.setString("type", type.toString());
		com.setString("exec", exec.toString());
		if(exec == Executor.OPERATOR && com.hasKey("exec0")){
			exid = new UUID(com.getLong("exec0"), com.getLong("exec1"));
		}
		com.setLong("fee", price);
		for(Entry<String, Integer> entry : settings.entrySet()){
			com.setInteger("set:" + entry.getKey(), entry.getValue());
		}
		for(Entry<String, ArrayList<String>> cmds : events.entrySet()){
			NBTTagList list = new NBTTagList();
			for(String str : cmds.getValue()) list.appendTag(new NBTTagString(str));
			com.setTag("event:" + cmds.getKey(), list);
		}
		for(Entry<String, String[]> entry : ctext.entrySet()){
			NBTTagCompound texts = new NBTTagCompound();
			String[] txts = entry.getValue();
			for(int i = 0; i < textlength; i++){
				if(i >= txts.length) break;
				if(txts[i] != null && txts[i].length() > 0) texts.setString("l" + i, txts[i]);
			}
			com.setTag("text:" + entry.getKey(), texts);
		}
		if(!export){
			if(pos != null) com.setString("pos", pos.toString());
			if(refresh > 0) com.setLong("refresh", refresh);
			if(uses.size() > 0){
				NBTTagCompound use = new NBTTagCompound();
				for(Entry<UUID, Integer> entry : uses.entrySet()){
					use.setInteger(entry.getKey().toString(), entry.getValue());
				}
				com.setTag("uses", use);
			}
			if(cooldown > 0) com.setLong("cooldown", cooldown);
			if(showremaining) com.setBoolean("show_time", showremaining);
		}
		com.setBoolean("noplayer", noplayer);
		return com;
	}

	public void process(SignCapImpl cap, PlayerInteractEvent event, IBlockState state, TileEntitySign tile){
		EntityPlayer player = event.getEntityPlayer();
		UUID uuid = player.getGameProfile().getId();
		if(type == Type.BASIC && settings.get("limit", 0) > 0){
			Integer l = uses.get(uuid);
			if(l != null && l >= settings.get("limit", 0)){
				boolean renew = settings.get("renew", 0) > 0;
				Print.chat(player, renew ? trs("reached_use_limit_wait", timer) : trs("reached_use_limit"));
				return;
			}
		}
		if(type == Type.RENT){
			if(uses.size() > 0){
				Print.chat(player, trs("sign_already_rented", timer + settings.get("cooldown", 0)));
				return;
			}
			else if(cooldown > 0 && Time.getDate() < cooldown){
				Print.chat(player, trs("sign_in_cooldown", (cooldown - Time.getDate()) / MINUTE));
				return;
			}
		}
		if(price > 0){
			Account account = DataManager.getAccount("player:" + uuid.toString(), false, false);
			if(account == null || account.getBalance() < price){
				Print.chat(player, trs("not_enough_money"));
				return;
			}
			account.setBalance(account.getBalance() - price);
		}
		ICommandManager cmdman = Static.getServer().commandManager;
		ArrayList<String> cmds = events.get(type.cmd_events[0]);
		if(cmds == null || cmds.isEmpty()) return;
		for(String cmd : cmds){
			cmd = format(cmd, tile, state, player, uuid);
			boolean p = cmd.startsWith("p!");
			boolean o = cmd.startsWith("o!");
			if(exec == Executor.PLAYER || p) cmdman.executeCommand(player, p ? cmd.substring(2) : cmd);
			else if(exec == Executor.OPERATOR || o) cmdman.executeCommand(PayableCommandSigns.getOpPlayer(pos.dim, exid), o ? cmd.substring(2) : cmd);
			else cmdman.executeCommand(new CommandSender(event.getWorld(), noplayer ? null : event.getEntityPlayer()), format(cmd, tile, state, player, uuid));
		}
		cooldown = 0;
		if(type == Type.BASIC && settings.get("limit", 0) > 0){
			Integer i = uses.get(uuid);
			uses.put(uuid, i == null ? 1 : i + 1);
			if(timer <= 0 && settings.get("renew", 0) > 0) setupTimer(false);
		}
		else if(type == Type.RENT){
			uses.put(uuid, -1);
			setupTimer(false);
		}
		String[] text = ctext.get(type.cmd_events[0]);
		showremaining = false;
		if(text != null){
			for(int i = 0; i < text.length; i++){
				if(i >= tile.signText.length) break;
				if(text[i] == null) continue;
				if(type == Type.RENT && text[i].contains(remaining)){
					showremaining = true;
					tile.signText[i] = formattedComponent(text[i].replace(remaining, remaining()));
					continue;
				}
				tile.signText[i] = formattedComponent(text[i].replace("{name}", player.getName()));
			}
			cap.sendUpdate(tile);
			tile.markDirty();
		}
	}

	private String format(String cmd, TileEntitySign tile, IBlockState state, EntityPlayer player, UUID uuid){
		if(player != null) cmd = cmd.replace("{name}", player.getName());
		else{
			String name = Static.getPlayerNameByUUID(uuid);
			if(name != null) cmd = cmd.replace("{name}", name);
		}
		return cmd.replace("{uuid}", uuid.toString());
	}

	public boolean valid(){
		if(type.cmd_events.length == 1){
			ArrayList<String> cmds = events.get(type.cmd_events[0]);
			return cmds != null && cmds.size() > 0;
		}
		else if(type == Type.RENT){
			ArrayList<String> cmds = events.get("start");
			if(cmds == null || cmds.size() == 0) return false;
			return (cmds = events.get("end")) != null && cmds.size() > 0;
		}
		return true;
	}

	public static enum Type {
		
		BASIC(0, "interact"), RENT(1, "start", "end");
		
		public String[] cmd_events;
		public String[] settings;
	
		Type(int sidx, String... cmds){
			cmd_events = cmds;
			settings = genset(sidx);
		}

		private String[] genset(int sidx){
			if(sidx == 0){
				return new String[]{ "fee", "limit", "renew" };
			}
			if(sidx == 1){
				return new String[]{ "fee", "duration", "cooldown" };
			}
			return new String[]{ "fee" };
		}

		public String durtag(){
			return this == BASIC ? settings[2] : settings[1];
		}

		public String initialtext(){
			return this == BASIC ? cmd_events[0] : cmd_events[1];
		}

		public String[] cmdtexts(){
			if(this == BASIC){
				return new String[]{ cmd_events[0], "predit" };
			}
			else{
				return new String[]{ cmd_events[0], cmd_events[1], "predit" };
			}
		}

	}

	public static enum Executor {

		SERVER, PLAYER, OPERATOR;

	}
	
	public static class Settings extends HashMap<String, Integer> {
		
		public int get(String key, int def){
			Integer entry = this.get(key);
			if(entry == null) return def;
			return entry;
		}
	}

	public boolean notext(){
		String[] text = ctext.get(type.initialtext());
		if(text == null || text.length == 0) return true;
		for(String str : text) if(str != null && str.length() > 0) return false;
		return true;
	}
	
	public void setupTimer(boolean min){
		if(min){
			refresh = Time.getDate() + MINUTE;
			timer = 1;
		}
		else{
			refresh = Time.getDate() + (settings.get(type.durtag(), 1) * MINUTE);
			timer = (int)((refresh - Time.getDate()) / MINUTE);
			if(timer < 1) timer = 1;
		}
		if(!PayableCommandSigns.FLOATING.containsKey(pos)) PayableCommandSigns.FLOATING.put(pos, this);
	}

	public void processTimed(){
		refresh = 0;
		if(type == Type.RENT){
			ctext.remove("predit");
			TileEntitySign tile = (TileEntitySign)Static.getServer().getWorld(pos.dim).getTileEntity(pos.pos);
			if(tile == null){
				ForcedChunks.requestLoad(pos.dim, pos.pos);
				tile = (TileEntitySign)Static.getServer().getWorld(pos.dim).getTileEntity(pos.pos);
			}
			if(tile == null){
				System.out.println("PCMDS: Sign not found (might be removed?), sign rent end event will be cancelled. DIM: " + pos.dim + ", POS:" + pos.pos.toString());
				ForcedChunks.requestUnload(pos.dim, pos.pos);
				return;
			}
			UUID uuid = uses.size() == 0 ? null : uses.keySet().toArray(new UUID[0])[0];
			if(uuid == null) return;
			ICommandManager cmdman = Static.getServer().getCommandManager();
			ArrayList<String> cmds = events.get(type.cmd_events[1]);
			if(cmds == null || cmds.isEmpty()) return;
			World world = Static.getServer().getWorld(pos.dim);
			for(String cmd : cmds){
				cmd = format(cmd, null, null, null, uuid);
				boolean o = cmd.startsWith("o!");
				if(exec == Executor.OPERATOR || o) cmdman.executeCommand(PayableCommandSigns.getOpPlayer(pos.dim, exid), o ? cmd.substring(2) : cmd);
				else cmdman.executeCommand(new CommandSender(world, null), format(cmd, null, null, null, uuid));
			}
			int cld = settings.get("cooldown", 0);
			if(cld > 0) cooldown = Time.getDate() + (cld * MINUTE);
			if(ctext.containsKey("end")){
				String[] text = ctext.get(type.cmd_events[1]);
				for(int i = 0; i < text.length; i++){
					if(i >= tile.signText.length) break;
					if(text[i] == null) continue;
					tile.signText[i] = formattedComponent(text[i]);
				}
				tile.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignCapImpl.class, SignCapImpl.REGNAME).sendUpdate(tile);
				tile.markDirty();
			}
			ForcedChunks.requestUnload(pos.dim, pos.pos);
		}
		uses.clear();
	}

	public void showRemaining(){
		TileEntitySign tile = (TileEntitySign)Static.getServer().getWorld(pos.dim).getTileEntity(pos.pos);
		if(tile != null){
			String[] text = ctext.get(type.cmd_events[0]);
			for(int i = 0; i < text.length; i++){
				if(i >= tile.signText.length) break;
				if(text[i] == null || !text[i].contains(remaining)) continue;
				tile.signText[i] = formattedComponent(text[i].replace(remaining, remaining()));
			}
			tile.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignCapImpl.class, SignCapImpl.REGNAME).sendUpdate(tile);
			tile.markDirty();
		}
	}

	private String remaining(){
		int mins = timer % 60;
		int hours = (timer % 1440) / 60;
		int days = timer / 1440;
		String res = days > 0 ? days + "d " : "";
		if(hours > 0) res += hours + "h ";
		return res + mins + "m";
	}

}
