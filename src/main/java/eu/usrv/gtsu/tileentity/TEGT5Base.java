package eu.usrv.gtsu.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;
import eu.usrv.gtsu.gregtech.GT5EnergyNetTEBase;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.IMultiBlockComponent;
import gregtech.api.GregTech_API;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;

public abstract class TEGT5Base extends GT5EnergyNetTEBase implements IMultiBlockComponent {
	protected boolean _mStructureValid; // Is the MultiBlock valid
	protected BlockPoswID _mMasterBlock; // Reference to our MultiBlock Master

	public static enum ToolModes
	{
		Wrench,
		Screwdriver,
		BareHand, Invalid
	}

	public abstract boolean doWrench(EntityPlayer pPlayer);
	public abstract boolean doScrewdriver(EntityPlayer pPlayer);
	public final boolean isUseableByPlayer(EntityPlayer aPlayer) {return aPlayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64; }

	public final boolean onRightclick(EntityPlayer pPlayer, byte pSide, float pX, float pY, float pZ) 
	{
		if (isServerSide())
		{
			ItemStack tCurrentItem = pPlayer.inventory.getCurrentItem();
			if (tCurrentItem != null)
			{
				if (GT_Utility.isStackInList(tCurrentItem, GregTech_API.sWrenchList))
				{
					if (doWrench(pPlayer))
					{
						GT_ModHandler.damageOrDechargeItem(tCurrentItem, 1, 1000, pPlayer);
						GT_Utility.sendSoundToPlayers(worldObj, GregTech_API.sSoundList.get(100), 1.0F, -1, xCoord, yCoord, zCoord);
					}						
					return true;
				}

				if (GT_Utility.isStackInList(tCurrentItem, GregTech_API.sScrewdriverList)) 
				{
					if (doScrewdriver(pPlayer))
					{
						GT_ModHandler.damageOrDechargeItem(tCurrentItem, 1, 200, pPlayer);
						GT_Utility.sendSoundToPlayers(worldObj, GregTech_API.sSoundList.get(100), 1.0F, -1, xCoord, yCoord, zCoord);
					}
					return true;
				}

			}
		}
		return true;
	}

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

