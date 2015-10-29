package eu.usrv.gtsu.tileentity;

import net.minecraft.tileentity.TileEntity;
import eu.usrv.gtsu.gregtech.GT5EnergyNetTEBase;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.IMultiBlock;

public abstract class TEGT5Base extends GT5EnergyNetTEBase implements IMultiBlock {
	protected boolean _mStructureValid; // Is the MultiBlock valid
	protected BlockPoswID _mMasterBlock; // Reference to our MultiBlock Master

	/**
	 * Get our Multiblock's MasterBlock. 
	 * Returns null on any error
	 * @param pWorld
	 * @return
	 */
	protected TEMBControllerBlock getMaster()
	{
		try
		{
			if (!_mStructureValid || _mMasterBlock == null) return null;
			TileEntity tTE = worldObj.getTileEntity(_mMasterBlock.x, _mMasterBlock.y, _mMasterBlock.z);

			if (tTE == null)
			{
				_mStructureValid = false;
				return null;
			}
			if (!(tTE instanceof TEMBControllerBlock))
			{
				_mStructureValid = false;
				return null;
			}

			return (TEMBControllerBlock)tTE;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public void updateMBStruct(boolean pStructValid, BlockPoswID pControllerBlock) {
		_mStructureValid = pStructValid;
		_mMasterBlock = pControllerBlock;
	}
}
