package eu.usrv.gtsu.multiblock;

import net.minecraft.entity.player.EntityPlayer;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.BlockPosHelper.MB_BlockState;

public interface IMultiBlockComponent {
	public void updateMBStruct(boolean pStructValid, BlockPoswID pControllerBlock);
	public boolean onRightclick(EntityPlayer pPlayer, byte pSide, float par1, float par2, float par3);
	public boolean isUseableByPlayer(EntityPlayer pPlayer);
}
