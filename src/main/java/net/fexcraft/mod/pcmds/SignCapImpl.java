package net.fexcraft.mod.pcmds;

import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.pcmds.PayableCommandSigns.EditMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class SignCapImpl implements SignCapability.Listener {
	
	private static final ResourceLocation REGNAME = new ResourceLocation("pcmds:sign");
	private SignData data;
	private boolean active;

	@Override
	public ResourceLocation getId(){
		return REGNAME;
	}

	@Override
	public boolean isActive(){
		return active;
	}

	@Override
	public boolean onPlayerInteract(SignCapability cap, PlayerInteractEvent event, IBlockState state,TileEntitySign tile){
		if(event.getWorld().isRemote) return false;
		if(data == null){
			if(!tile.signText[0].getUnformattedText().toLowerCase().equals("[pcmds]")) return false;
			if(!hasPerm(event.getEntityPlayer())){
				Print.chat(event.getEntityPlayer(), trs("no_permission"));
				return false;
			}
			PayableCommandSigns.SELSIGNS.get(event.getEntityPlayer().getGameProfile().getId()).pos = tile.getPos();
			Print.chat(event.getEntityPlayer(), trs("sign_registered"));
			tile.signText[0] = formattedComponent("&0[&6PcmdS&0]");
			tile.signText[1] = formattedComponent("&einactive");
			sendUpdate(tile);
			data = new SignData();
			cap.setActive();
			return true;
		}
		if(active){
			EditMode mode = PayableCommandSigns.SELSIGNS.get(event.getEntityPlayer().getGameProfile().getId());
			if(mode.set_edit){
				mode.pos = tile.getPos();
				active = false;
				tile.signText[0] = formattedComponent("&0[&6PcmdS&0]");
				tile.signText[1] = formattedComponent("&einactive");
				sendUpdate(tile);
				Print.chat(event.getEntityPlayer(), trs("sign_deactivated"));
				Print.chat(event.getEntityPlayer(), trs("sign_selected"));
			}
			else data.process(cap, event, state, tile);
		}
		else if(hasPerm(event.getEntityPlayer())){
			EditMode mode = PayableCommandSigns.SELSIGNS.get(event.getEntityPlayer().getGameProfile().getId());
			mode.pos = tile.getPos();
			Print.chat(event.getEntityPlayer(), trs("sign_selected"));
		}
		else return false;
		/*if(!active){
			if(tileentity.signText[0].getUnformattedText().toLowerCase().equals("[st-shop]")){
				if(!(tileentity.signText[3].getUnformattedText().toLowerCase().equals("buy") || tileentity.signText[3].getUnformattedText().toLowerCase().equals("sell"))){
					Print.chat(event.getEntityPlayer(), "Invalid type on line 4.");
					return false;
				}
				tileentity.signText[0] = Formatter.newTextComponentString("&0[&3St&8-&3Shop&0]");
				TileEntity te = event.getWorld().getTileEntity(getPosAtBack(state, tileentity));
				EnumFacing facing = state.getBlock() instanceof BlockWallSign ? EnumFacing.byIndex(tileentity.getBlockMetadata()) : null;
				if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing) && PlayerEvents.checkAccess(te.getWorld(), te.getPos(), te.getWorld().getBlockState(te.getPos()), event.getEntityPlayer(), true)){
					itemtype = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).getStackInSlot(0).copy();
					if(!tileentity.signText[1].getUnformattedText().equals("")){
						Chunk chunk = StateUtil.getChunk(te.getPos()); UUID uuid = event.getEntityPlayer().getGameProfile().getId();
						switch(tileentity.signText[1].getUnformattedText().toLowerCase()){
							case "district":
							case "municipality":{
								if(chunk.getMunicipality().isAuthorized(chunk.getMunicipality().r_CREATE_SIGN_SHOP.id, uuid).isTrue()){
									account = new ResourceLocation("municipality:" + chunk.getMunicipality().getId());
								}
								else{
									Print.chat(event.getEntityPlayer(), "&9No permission to Create Municipality Shops.");
									return true;
								}
								break;
							}
							case "state":{
								if(chunk.getState().isAuthorized(chunk.getState().r_CREATE_SIGN_SHOP.id, uuid).isTrue()){
									account = new ResourceLocation("state:" + chunk.getMunicipality().getId());
								}
								else{
									Print.chat(event.getEntityPlayer(), "&9No permission to Create State Shops.");
									return true;
								}
								break;
							}
							case "admin":
							case "server":{
								if(Perms.CREATE_SERVER_SIGN_SHOPS.has(event.getEntityPlayer())){
									account = States.SERVERACCOUNT.getAsResourceLocation();
									server = true;
								}
								else{
									Print.chat(event.getEntityPlayer(), "&9No permission to Create Server Shops.");
									return true;
								}
								break;
							}
							case "player":{
								account = null;
								break;
							}
						}
					}
					if(account == null){
						account = event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null).getAccount().getAsResourceLocation();
					}
					tileentity.signText[1] = new TextComponentString(itemtype.getDisplayName());
					try{
						long leng = Long.parseLong(tileentity.signText[2].getUnformattedText());
						tileentity.signText[2] = Formatter.newTextComponentString(Config.getWorthAsString(leng, true, leng < 10));
						price = leng;
					}
					catch(Exception e){
						e.printStackTrace();
						Print.chat(event.getEntityPlayer(), "Invalid Price. (1000 == 1" + Config.CURRENCY_SIGN + "!)");
						return false;
					}
					cap.setActive();
					active = true;
					this.sendUpdate(tileentity);
					return true;
				}
				else{
					Print.bar(event.getEntityPlayer(), "No ItemStack Container found.");
				}
			}
		}
		else{
			TileEntity te = event.getWorld().getTileEntity(getPosAtBack(state, tileentity));
			EnumFacing facing = state.getBlock() instanceof BlockWallSign ? EnumFacing.byIndex(tileentity.getBlockMetadata()) : null;
			if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)){
				if(event.getEntityPlayer().getHeldItemMainhand().isEmpty()){
					Account shop = DataManager.getAccount(account.toString(), true, false);
					if(shop == null){
						Print.chat(event.getEntityPlayer(), "Shop Account couldn't be loaded.");
						return true;
					}
					Account playeracc = event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null).getAccount();
					Bank playerbank = event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null).getBank();
					IItemHandler te_handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
					IItemHandler pl_handler = event.getEntityPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					if(tileentity.signText[3].getUnformattedText().toLowerCase().startsWith("buy")){
						if(hasStack(event.getEntityPlayer(), te_handler, false)){
							if(playerbank.processAction(Bank.Action.TRANSFER, event.getEntityPlayer(), playeracc, price, shop)){
								event.getEntityPlayer().addItemStackToInventory(getStackIfPossible(te_handler, false));
								Print.bar(event.getEntityPlayer(), "Items bought.");
							}
						}
					}
					else if(tileentity.signText[3].getUnformattedText().toLowerCase().startsWith("sell")){
						if(hasStack(event.getEntityPlayer(), pl_handler, true) && hasSpace(event.getEntityPlayer(), te_handler)){
							if(DataManager.getBank(shop.getBankId(), true, false).processAction(Bank.Action.TRANSFER, event.getEntityPlayer(), shop, price, playeracc)){
								addStack(te_handler, getStackIfPossible(pl_handler, true));
								Print.bar(event.getEntityPlayer(), "Items sold.");
							}
						}
					}
					else{
						Print.chat(event.getEntityPlayer(), "Invalid Mode at line 4.");
					}
				}
				else{
					Print.chat(event.getEntityPlayer(), "&9Shop Owner: &7" + account.toString());
					Print.chat(event.getEntityPlayer(), "&9Item: &7" + itemtype.getDisplayName());
					Print.chat(event.getEntityPlayer(), "&9Reg: &7" + itemtype.getItem().getRegistryName().toString());
					if(itemtype.getMetadata() > 0){
						Print.chat(event.getEntityPlayer(), "&9Meta: &8" + itemtype.getMetadata());
					}
					Print.chat(event.getEntityPlayer(), "&9Amount: &6" + itemtype.getCount());
					if(itemtype.hasTagCompound()){
						Print.chat(event.getEntityPlayer(), "&9NBT: &8" + itemtype.getTagCompound().toString());
					}
				}
				return true;
			}
			else{
				Print.bar(event.getEntityPlayer(), "No ItemStack Container linked.");
			}
		}*/
		return false;
	}

	private ITextComponent formattedComponent(String string){
		return new TextComponentString(Formatter.format(string));
	}

	@SuppressWarnings("deprecation")
	private String trs(String string){
		return I18n.translateToLocal("pcmds." + string);
	}

	private boolean hasPerm(EntityPlayer player){
		return PermissionAPI.hasPermission(player, PayableCommandSigns.EDIT_SIGN_PERM);
	}

	@Override
	public NBTBase writeToNBT(Capability<SignCapability> capability, EnumFacing side){
		if(!active){
			return null;
		}
		NBTTagCompound com = new NBTTagCompound();
		com.setBoolean("sign:active", active);
		if(data == null) return com;
		com.setTag("sign:data", data.save(new NBTTagCompound()));
		return com;
	}

	@Override
	public void readNBT(Capability<SignCapability> capability, EnumFacing side, NBTBase nbt){
		if(nbt == null || !(nbt instanceof NBTTagCompound)){
			data = null;
			return;
		}
		NBTTagCompound com = (NBTTagCompound)nbt;
		if(com.hasKey("sign:data")){
			data = new SignData().load(com.getCompoundTag("sign:data"));
		}
		active = com.getBoolean("sign:active");
	}
	
}