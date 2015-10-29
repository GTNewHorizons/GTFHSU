package eu.usrv.gtsu.multiblock;

import net.minecraft.entity.player.EntityPlayer;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.BlockPosHelper.MB_BlockState;

public interface IMultiBlock {
	public void updateMBStruct(boolean pStructValid, BlockPoswID pControllerBlock);
	public void onPlayerClicked(EntityPlayer pPlayer);
}
