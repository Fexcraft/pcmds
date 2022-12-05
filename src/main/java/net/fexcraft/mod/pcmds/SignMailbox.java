package net.fexcraft.mod.pcmds;

import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class SignMailbox implements SignCapability.Listener {
	
	public static final ResourceLocation RESLOC = new ResourceLocation("states:mailbox");
	private NonNullList<ItemStack> mails = NonNullList.<ItemStack>create();
	private boolean active;
	private String reci;
	private String type;

	@Override
	public ResourceLocation getId(){
		return RESLOC;
	}

	@Override
	public boolean isActive(){
		return active;
	}

	@Override
	public boolean onPlayerInteract(SignCapability cap, PlayerInteractEvent event, IBlockState state, TileEntitySign tileentity){
		if(event.getWorld().isRemote) return false;
		/*boolean admin = StateUtil.isAdmin(event.getEntityPlayer());
		if(!active){
			if(tileentity.signText[0].getUnformattedText().toLowerCase().equals("[st-mailbox]")){
				BlockPos back = getPosAtBack(state, tileentity);
				if(!PlayerEvents.checkAccess(event.getWorld(), back, event.getWorld().getBlockState(back), event.getEntityPlayer(), true)){
					Print.chat(event.getEntityPlayer(), "Block/TileEntity behind sign cannot be accessed."); return false;
				}
				Chunk chunk = StateUtil.getChunk(tileentity.getPos());
				String type = tileentity.signText[1].getUnformattedText().toLowerCase();
				switch(type){
					case "state":{
						if(!admin && !chunk.getState().isAuthorized(chunk.getState().r_SET_MAILBOX.id, event.getEntityPlayer().getGameProfile().getId()).isTrue()){
							Print.chat(event.getEntityPlayer(), "No permission to set the State Mailbox."); return false;
						} reci = chunk.getState().getId() + "";
					}
					case "municipality":{
						if(!admin && !chunk.getMunicipality().isAuthorized(chunk.getMunicipality().r_SET_MAILBOX.id, event.getEntityPlayer().getGameProfile().getId()).isTrue()){
							Print.chat(event.getEntityPlayer(), "No permission to set the Municipality Mailbox."); return false;
						} reci = chunk.getMunicipality().getId() + "";
					}
					case "district":{
						if(!admin && !chunk.getDistrict().isAuthorized(chunk.getDistrict().r_SET_MAILBOX.id, event.getEntityPlayer().getGameProfile().getId()).isTrue()){
							Print.chat(event.getEntityPlayer(), "No permission to set the District Mailbox."); return false;
						} reci = chunk.getDistrict().getId() + "";
					}
					case "company": break;//TODO
					case "player":{
						String rec = tileentity.signText[2].getUnformattedText().toLowerCase();
						if(rec == null || rec.length() == 0){
							Print.chat(event.getEntityPlayer(), "No player name on 3rd line.");
							return false;
						}
						com.mojang.authlib.GameProfile prof = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(rec);
						if(prof == null){
							Print.chat(event.getEntityPlayer(), "Couldn't find player UUID in cache.");
							return false;
						}
						if(prof.getId().equals(event.getEntityPlayer().getGameProfile().getId()) || admin){
							this.reci = prof.getId().toString();
							tileentity.signText[1] = Formatter.newTextComponentString(prof.getName());
							tileentity.signText[2] = Formatter.newTextComponentString("");
						}//TODO municipality check
						else{
							Print.chat(event.getEntityPlayer(), "No permission to set mailbox of that player.");
						}
						break;
					}
					case "central": case "fallback":{
						if(!admin){
							Print.chat(event.getEntityPlayer(), "No permission to set the Central/Fallback Mailbox."); return false;
						} reci = "-1";
						break;
					}
					default:{
						Print.chat(event.getEntityPlayer(), "Invalid mailbox type.");
						return false;
					}
				}
				tileentity.signText[0] = Formatter.newTextComponentString("&0[&3Mailbox&0]");
				try{
					switch(type){
						case "state": chunk.getState().setMailbox(tileentity.getPos()); break;
						case "municipality": chunk.getMunicipality().setMailbox(tileentity.getPos()); break;
						case "district": chunk.getDistrict().setMailbox(tileentity.getPos()); break;
						case "company": break;//TODO
						case "player":{
							if(event.getEntityPlayer().getGameProfile().getId().toString().equals(reci)){
								event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null).setMailbox(tileentity.getPos());
							}
							else{
								StateUtil.getPlayer(reci, true).setMailbox(tileentity.getPos());
							}
							break;
						}
						case "central": case "fallback":{
							StateUtil.getState(-1).setMailbox(tileentity.getPos());
							break;
						}
					}
					this.type = type.equals("fallback") || type.equals("central") ? "state" : type;
					cap.setActive(); this.active = true; this.sendUpdate(tileentity);
				}
				catch(Exception e){
					e.printStackTrace();
					Print.chat(event.getEntityPlayer(), "Error occured, check log for info.");
				}
				return true;
			}
			else return false;
		}
		else{
			Chunk chunk = StateUtil.getChunk(tileentity.getPos());
			switch(type){
				case "state":{
					if(!admin && !chunk.getState().isAuthorized(chunk.getState().r_OPEN_MAILBOX.id, event.getEntityPlayer().getGameProfile().getId()).isTrue()){
						Print.chat(event.getEntityPlayer(), "No permission to open the State Mailbox."); return false;
					}
				}
				case "municipality":{
					if(!admin && !chunk.getMunicipality().isAuthorized(chunk.getMunicipality().r_OPEN_MAILBOX.id, event.getEntityPlayer().getGameProfile().getId()).isTrue()){
						Print.chat(event.getEntityPlayer(), "No permission to open the Municipality Mailbox."); return false;
					}
				}
				case "district":{
					if(!admin && !chunk.getDistrict().isAuthorized(chunk.getDistrict().r_OPEN_MAILBOX.id, event.getEntityPlayer().getGameProfile().getId()).isTrue()){
						Print.chat(event.getEntityPlayer(), "No permission to open the District Mailbox."); return false;
					}
				}
				case "company": break;//TODO
				case "player":{
					if(!admin && !event.getEntityPlayer().getGameProfile().getId().toString().equals(reci)){
						Print.chat(event.getEntityPlayer(), "No permission to open mailbox of that player."); return false;
					}
					break;
				}
				case "central": case "fallback":{
					if(!admin){
						Print.chat(event.getEntityPlayer(), "No permission to set the Central/Fallback Mailbox."); return false;
					}
					break;
				}
				default:{
					Print.chat(event.getEntityPlayer(), "Invalid mailbox type.");
					return false;
				}
			}
			if(mails.isEmpty()){
				Print.chat(event.getEntity(), "&aThere is no new mail!"); 
				return true;
			}
			openGui(event.getEntityPlayer(), MAILBOX, tileentity.getPos());
			//Print.chat(event.getEntityPlayer(), "&k!000-000!000-000!");
        	Print.chat(event.getEntity(), "&7&oOpening Mailbox UI."); return true;
		}*/
		return false;
	}

	@Override
	public NBTBase writeToNBT(Capability<SignCapability> capability, EnumFacing side){
		NBTTagCompound compound = new NBTTagCompound();
		return compound;
	}

	@Override
	public void readNBT(Capability<SignCapability> capability, EnumFacing side, NBTBase nbt){
		//
	}
	
}