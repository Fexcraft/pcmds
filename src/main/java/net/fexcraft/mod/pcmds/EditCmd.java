package net.fexcraft.mod.pcmds;

import static net.fexcraft.mod.pcmds.PayableCommandSigns.SIGNCAP;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.pcmds.PayableCommandSigns.EditMode;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.server.permission.PermissionAPI;

@fCommand
public class EditCmd extends CommandBase {
	
    public EditCmd(){}
    
	@Override
	public String getName(){
		return "pcmds";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return sender.getCommandSenderEntity() instanceof EntityPlayer;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
    	if(!PermissionAPI.hasPermission(player, PayableCommandSigns.EDIT_SIGN_PERM)){
			Print.chat(player, trs("no_permission"));
    		return;
    	}
    	if(args.length < 1){
    		Print.chat(sender, "/pcmds help");
    		return;
    	}
    	EditMode mode = PayableCommandSigns.SELSIGNS.get(player.getGameProfile().getId());
    	SignCapability cap = getCap(sender.getEntityWorld(), mode);
    	SignCapImpl impl = cap == null ? null : cap.getListener(SignCapImpl.class, SignCapImpl.REGNAME);
    	SignData data = impl == null ? null : impl.data;
		SignData fldt = data == null ? null : PayableCommandSigns.FLOATING.get(data.pos);
		if(fldt != null && impl != null && data != fldt) data = impl.data = fldt;
		switch(args[0]){
		case "editmode":{
			mode.set_edit = !mode.set_edit;
			Print.chat(sender, trs("cmd.editmode_" + (mode.set_edit ? "on" : "off")));
    		return;
		}
		case "activate":{
	    	if(impl == null){
	    		Print.chat(sender, trs("sign_none_selected"));
	    	}
	    	else{
	    		if(!impl.data.valid()){
		    		Print.chat(sender, trs("sign_incomplete"));
	    		}
	    		else{
		    		impl.setActive(cap.getTileEntity());
		    		Print.chat(sender, trs("sign_activated"));
	    		}
	    	}
    		return;
		}
		case "status":{
    		Print.chat(sender, "&0[&6PcmdS&0]&e&2==== === == =");
    		if(err(sender, impl, data)) return;
    		Print.chat(sender, trs("cmd.status.type", data.type.name().toLowerCase()));
    		for(String str : data.type.cmd_events){
        		Print.chat(sender, trs("cmd.status.event", str));
        		ArrayList<String> list = data.events.get(str);
        		if(list == null || list.isEmpty()){
            		Print.chat(sender, trs("cmd.status.event.none"));
        		}
        		else{
        			for(int i = 0; i < list.size(); i++){
                		Print.chat(sender, i + " " + trs("cmd.status.event.cmd", list.get(i)));
        			}
        		}
    		}
    		Print.chat(sender, trs("cmd.status.price", Config.getWorthAsString(data.price)));
    		Print.chat(sender, trs("cmd.status.settings"));
    		if(data.settings.isEmpty()){
        		Print.chat(sender, trs("cmd.status.settings.none"));
        		Print.chat(sender, trs("cmd.status.settings.none_info"));
    		}
    		else{
    			for(Entry<String, Integer> entry : data.settings.entrySet()){
            		Print.chat(sender, trs("cmd.status.settings.entry", entry.getKey(), entry.getValue()));
    			}
    		}
    		if(data.ctext.size() > 0){
        		Print.chat(sender, trs("cmd.status.text"));
        		for(Entry<String, String[]> entry : data.ctext.entrySet()){
            		Print.chat(sender, trs("cmd.status.text.event", entry.getKey()));
            		String[] text = entry.getValue();
            		for(int i = 0; i < text.length; i++){
                		Print.chat(sender, i + " " + trs("cmd.status.text.entry", text[i] == null ? "" : text[i]));
            		}
        		}
    		}
			return;
		}
		case "text":{
			if(args.length < 4){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds text <event> <line> <text>");
	    		Print.chat(sender, "/pcmds text interact 0 example text");
				return;
			}
			String event = findEvent(sender, data, args[1]);
			if(event == null) return;
			int idx = Integer.parseInt(args[2]);
			String rest = args[3];
			if(args.length > 4) for(int i = 4; i < args.length; i++) rest += " " + args[i];
			if(!data.ctext.containsKey(event)) data.ctext.put(event, new String[data.textlength]);
			data.ctext.get(event)[idx] = rest;
			Print.chat(sender, trs("cmd.text.updated", idx, event));
			cap.getTileEntity().markDirty();
			return;
		}
		case "type":{
			if(args.length < 2){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds type <process-type>");
	    		Print.chat(sender, "/pcmds type list");
	    		return;
			}
			if(args[1].equals("list")){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, trs("cmd.type.available"));
	    		for(SignData.Type type : SignData.Type.values()){
	    			Print.chat(sender, "&a- " + type.name().toLowerCase());
	    			Print.chat(sender, trs("cmd.type." + type.name().toLowerCase()));
	    		}
				return;
			}
			data.type = SignData.Type.valueOf(args[1].toUpperCase());
			Print.chat(sender, trs("cmd.type.updated"));
			cap.getTileEntity().markDirty();
			return;
		}
		case "set":{
			if(args.length < 2){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds set <setting> <value>");
	    		Print.chat(sender, "/pcmds set list");
	    		return;
			}
			if(args[1].equals("list")){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, trs("cmd.set.available"));
	    		for(String set : data.type.settings){
	    			Print.chat(sender, "&a- " + set);
	    			Print.chat(sender, trs("cmd.set." + set));
	    		}
				return;
			}
			if(args.length < 3){
				Print.chat(sender, trs("cmd.set.missing_value"));
				return;
			}
			String set = args[1];
			boolean found = false;
			for(String str : data.type.settings){
				if(str.equals(set)){
					found = true;
					break;
				}
			}
			if(!found){
				Print.chat(sender, trs("cmd.set.not_found", set));
				return;
			}
			String val = args[2];
			if(set.equals("fee")) data.price = Long.parseLong(val);
			else data.settings.put(set, Integer.parseInt(val));
			Print.chat(sender, trs("cmd.set.updated", set, val));
			cap.getTileEntity().markDirty();
			return;
		}
		case "iknow":{
			mode.knows = true;
			Print.chat(sender, trs("cmd.knows"));
			return;
		}
		case "add":{
			if(args.length < 3){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds add <event> <cmd + args>");
	    		return;
			}
			String event = findEvent(sender, data, args[1]);
			if(event == null) return;
			if(!mode.knows){
				Print.chat(sender, trs("cmd.warning"));
				Print.chat(sender, trs("cmd.iknow"));
			}
			String rest = args[2];
			if(args.length > 3) for(int i = 3; i < args.length; i++) rest += " " + args[i];
			if(!data.events.containsKey(event)) data.events.put(event, new ArrayList<>());
			data.events.get(event).add(rest);
			Print.chat(sender, trs("cmd.cmd.added", event));
			cap.getTileEntity().markDirty();
			return;
		}
		case "rem": case "remove":{
			if(args.length < 3){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds remove <event> <index>");
	    		return;
			}
			String event = findEvent(sender, data, args[1]);
			if(event == null) return;
			int idx = Integer.parseInt(args[2]);
			ArrayList<String> list = data.events.get(event);
			if(list == null || idx < 0 || idx >= list.size()) return;
			list.remove(idx);
			Print.chat(sender, trs("cmd.cmd.removed", event, idx));
			cap.getTileEntity().markDirty();
			return;
		}
		case "clear":{
			if(args.length < 2){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds clear <event>");
	    		return;
			}
			String event = findEvent(sender, data, args[1]);
			if(event == null) return;
			data.events.remove(event);
			Print.chat(sender, trs("cmd.cmd.cleared", event));
			cap.getTileEntity().markDirty();
			return;
		}
		case "export":{
    		StringSelection strsel = new StringSelection(data.save(new NBTTagCompound()).toString());
    		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(strsel, null);
			Print.chat(sender, trs("cmd.export"));
			return;
		}
		case "import":{
			Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable trs = cp.getContents(null);
			if(!trs.isDataFlavorSupported(DataFlavor.stringFlavor)) return;
			try{
				data.load(null, null, JsonToNBT.getTagFromJson(trs.getTransferData(DataFlavor.stringFlavor).toString()));
			}
			catch(Exception e){
				e.printStackTrace();
			}
			Print.chat(sender, trs("cmd.import"));
			cap.getTileEntity().markDirty();
			return;
		}
		case "save":{
			if(args.length < 2){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds save <id>");
	    		return;
			}
			File file = new File(PayableCommandSigns.CFGPATH, "/" + args[1] + ".nbt");
			try{
				CompressedStreamTools.write(data.save(new NBTTagCompound()), file);
			}
			catch (IOException e){
				e.printStackTrace();
			}
			Print.chat(sender, trs("cmd.save", args[1]));
			return;
		}
		case "load":{
			if(args.length < 2){
	    		Print.chat(sender, "&a---- --- -- -");
	    		Print.chat(sender, "/pcmds load <id>");
	    		return;
			}
			File file = new File(PayableCommandSigns.CFGPATH, "/" + args[1] + ".nbt");
			try{
				data.load(null, null, CompressedStreamTools.read(file));
			}
			catch (IOException e){
				e.printStackTrace();
			}
			Print.chat(sender, trs("cmd.load", args[1]));
			cap.getTileEntity().markDirty();
			return;
		}
		case "noplayer":{
			data.noplayer = !data.noplayer;
			Print.chat(sender, trs(data.noplayer ? "cmd.noplayer.on" : "cmd.noplayer.off"));
			return;
		}
		case "help":
    		Print.chat(sender, "&0[&6PcmdS&0]&2==== === == =");
    		ITextComponent comp0 = new TextComponentString(Formatter.format("&2WIKI: "));
    		ITextComponent comp1 = new TextComponentString(Formatter.format("&7&nhttps://fexcraft.net/wiki/mod/pcmds"));
    		comp1.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://fexcraft.net/wiki/mod/pcmds"));
    		comp0.appendSibling(comp1);
    		sender.sendMessage(comp0);
    		Print.chat(sender, "/pcmds editmode");
    		Print.chat(sender, "/pcmds activate");
    		Print.chat(sender, "/pcmds status");
    		Print.chat(sender, "&2---- --- -- -");
    		Print.chat(sender, "/pcmds add <event> <cmd + args>");
    		Print.chat(sender, "/pcmds remove <event> <index>");
    		Print.chat(sender, "/pcmds clear <event>");
    		Print.chat(sender, "/pcmds text <line> <text>");
    		Print.chat(sender, "/pcmds type <process-type>");
    		Print.chat(sender, "/pcmds type list");
    		Print.chat(sender, "/pcmds set <setting> <value>");
    		Print.chat(sender, "/pcmds set list");
    		Print.chat(sender, "/pcmds noplayer");
    		Print.chat(sender, "&2---- --- -- -");
    		Print.chat(sender, "/pcmds save <id>");
    		Print.chat(sender, "/pcmds load <id>");
    		Print.chat(sender, "/pcmds export");
    		Print.chat(sender, "/pcmds import");
			return;
		default:
    		Print.chat(sender, trs("cmd.unknown_arg"));
			return;
		}
    }

	private String findEvent(ICommandSender sender, SignData data, String string){
		boolean found = false;
		for(String str : data.type.cmd_events){
			if(str.equals(string)){
				found = true;
				break;
			}
		}
		if(!found){
			Print.chat(sender, "cmd.cmd.event_not_found");
			return null;
		}
		return string;
	}

	private boolean err(ICommandSender sender, SignCapImpl impl, SignData data){
		if(impl == null){
			Print.chat(sender, "error, no cap impl");
			return true;
		}
		if(data == null){
			Print.chat(sender, "error, no sign data");
			return true;
		}
		return false;
	}

	private SignCapability getCap(World world, EditMode mode){
		if(mode.pos == null) return null;
		TileEntity tile = world.getTileEntity(mode.pos);
		if(tile == null) return null;
		return tile.getCapability(SIGNCAP, null);
	}

	@SuppressWarnings("deprecation")
	public static String trs(String string){
		return I18n.translateToLocal("pcmds." + string);
	}

	@SuppressWarnings("deprecation")
	public static String trs(String string, Object... strs){
		return I18n.translateToLocalFormatted("pcmds." + string, strs);
	}

}
