package eu.usrv.gtsu.multiblock;

import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.BlockPosHelper.MB_BlockState;

public interface IMultiBlock {
	public void updateMBStruct(boolean pStructValid, BlockPoswID pControllerBlock);
	
}
