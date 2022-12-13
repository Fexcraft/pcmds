package net.fexcraft.mod.pcmds;

import java.util.TimerTask;

import net.fexcraft.lib.mc.utils.Static;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class ScheduledCheck extends TimerTask {

	@Override
	public void run(){
		Static.getServer().addScheduledTask(() -> {
			PayableCommandSigns.FLOATING.values().removeIf(data -> {
				try{
					if(data.timer <= 0){
						data.processTimed();
						return true;
					}
					else data.timer--;
				}
				catch(Throwable e){
					e.printStackTrace();
				}
				return false;
			});
		});
	}

}
