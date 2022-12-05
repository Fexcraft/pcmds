package net.fexcraft.mod.pcmds;

import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class SignData {
	
	public String[] text;;
	
	public SignData(int length){
		text = new String[4];
	}

	public SignData load(NBTTagCompound com){
		
		return this;
	}
	
	public NBTTagCompound save(NBTTagCompound com){
		
		return com;
	}

	public void process(SignCapability cap, PlayerInteractEvent event, IBlockState state, TileEntitySign tile){
		//
	}

	public boolean valid(){
		//
		return false;
	}

}
