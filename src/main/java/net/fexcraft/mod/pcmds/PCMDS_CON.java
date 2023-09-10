package net.fexcraft.mod.pcmds;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PCMDS_CON extends GenericContainer {

	public final int x, y, z;
	@SideOnly(Side.CLIENT)
	public PCMDS_UI gui;

	public PCMDS_CON(EntityPlayer entity, int x, int y, int z){
		super(entity);
		this.x = x;
		this.y = y;
		this.z = z;
		if(entity.world.isRemote) return;
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(side.isServer()){
			if(packet.getBoolean("sync")){
				sendSync();
				return;
			}
		}
		else{
			//
		}
	}

	private void sendSync(){
		NBTTagCompound sync = new NBTTagCompound();
		//
		send(Side.CLIENT, sync);
	}

}