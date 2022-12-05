package net.fexcraft.mod.pcmds;

import static net.fexcraft.mod.pcmds.PayableCommandSigns.SIGNCAP;

import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.pcmds.PayableCommandSigns.EditMode;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
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
		    		impl.setActive(cap.getTileEntity());
		    		Print.chat(sender, trs("sign_incomplete"));
	    		}
	    		else{
		    		impl.setActive(cap.getTileEntity());
		    		Print.chat(sender, trs("sign_activated"));
	    		}
	    	}
    		return;
		}
		case "help":
    		Print.chat(sender, "&0[&6PcmdS&0]&e>>&2===========");
    		ITextComponent comp0 = new TextComponentString(Formatter.format("&2WIKI: "));
    		ITextComponent comp1 = new TextComponentString(Formatter.format("&7&nhttps://fexcraft.net/wiki/mod/pcmds"));
    		comp1.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://fexcraft.net/wiki/mod/pcmds"));
    		comp0.appendSibling(comp1);
    		sender.sendMessage(comp0);
    		Print.chat(sender, "/pmcds editmode");
    		Print.chat(sender, "/pmcds activate");
    		Print.chat(sender, "/pmcds status");
    		Print.chat(sender, "/pmcds cmd <cmd + args>");
    		Print.chat(sender, "/pmcds text <line> <text>");
    		Print.chat(sender, "/pmcds type <process-type>");
    		Print.chat(sender, "/pmcds set <setting> <value>");
    		Print.chat(sender, "/pmcds save <id>");
    		Print.chat(sender, "/pmcds load <id> <args>");
    		Print.chat(sender, "/pmcds export");
    		Print.chat(sender, "/pmcds import <args>");
			return;
		default:
    		Print.chat(sender, trs("cmd.unknown_arg"));
			return;
		}
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

}
