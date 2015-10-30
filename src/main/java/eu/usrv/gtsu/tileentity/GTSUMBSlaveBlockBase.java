package eu.usrv.gtsu.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.IMultiBlockComponent;
import gregtech.api.GregTech_API;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;


/**
 * BaseClass for all Slave-Blocks of a Multiblock Structure
 *
 */
public abstract class GTSUMBSlaveBlockBase extends GTSUTileEntityBase implements IMultiBlockComponent {
	protected boolean _mStructureValid; // Is the MultiBlock valid
	protected BlockPoswID _mMasterBlock; // Reference to our MultiBlock Master

	/**
	 * Get our Multiblock's MasterBlock. 
	 * Returns null on any error
	 * @param pWorld
	 * @return
	 */
	protected final TEMBControllerBlock getMaster()
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
	public final void updateMBStruct(boolean pStructValid, BlockPoswID pControllerBlock) {
		_mStructureValid = pStructValid;
		_mMasterBlock = pControllerBlock;

	}

}

