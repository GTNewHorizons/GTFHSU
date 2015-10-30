package eu.usrv.gtsu.tileentity;

import static eu.usrv.gtsu.TierHelper.V;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import eu.usrv.gtsu.TierHelper;
import eu.usrv.gtsu.helper.PlayerChatHelper;
import eu.usrv.gtsu.helper.EnergySystemConverter.PowerSystem;
import eu.usrv.gtsu.helper.Enums.EN_EnergyType;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.BlockPosHelper.GTSU_BlockType;
import eu.usrv.gtsu.multiblock.BlockPosHelper.MB_BlockState;
import eu.usrv.gtsu.multiblock.IMultiBlockComponent;
import eu.usrv.gtsu.multiblock.TEMultiBlockBase;

// This is our main controller block
public class TEMBControllerBlock extends TEMultiBlockBase
{
	// Our stored energy. This is *not* EU, nor RF, nor anything else. It's a new PowerSystem, with blackjack, and hookers!
	private long _mEnergy;
	private long _mMaxEnergy;
	private final static double _mEnergyPerElement = 10000000.0D;
	//private HashMap<GTSU_BlockType, IMultiBlock> _mMultiBlockCompound;
	
	public static final String NBTVAL_ENERGY = "mEnergy";
	
	/** 
	 * More of an internal function to check if we have enough powerUnits stored to provide given EnergyUnits
	 * Note: TileEntities should call drainEnergy() or injectEnergy(), as those functions perform security checks on their own
	 * @param pEnergyType
	 * @param pUnits Total amount of units in pEnergyType. (Example: 5 Amps at GregTech5 IV Level would be 40960)
	 * @return
	 */
	public boolean canDrainPower(PowerSystem pEnergyType, long pUnits)
	{
		try
		{
			if (_mEnergy >= pEnergyType.getEnergyUnits(pUnits))
				return true;
			else
				return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/** 
	 * More of an internal function to check if we have enough free space to store given EnergyUnits
	 * Note: TileEntities should call drainEnergy() or injectEnergy(), as those functions perform security checks on their own
	 * @param pEnergyType
	 * @param pUnits Total amount of units in pEnergyType. (Example: 5 Amps at GregTech5 IV Level would be 40960)
	 * @return
	 */
	public boolean canInjectPower(PowerSystem pEnergyType, long pUnits)
	{
		try
		{
			// We can't check for _mEnergy + pEnergyType.getEnergyUnits(pUnits) here,
			// because this could result in an overflow of the operation itself.
			// So instead, we subtract our stored energy from the maximum possible amount,
			// and compare that to the amount to be injected. So we never leave our range of the Long
			// Datatype.
			
			if (Long.MAX_VALUE - _mEnergy >= pEnergyType.getEnergyUnits(pUnits))
				return true;
			else
				return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public long drainEnergy(PowerSystem pEnergyType, long pUnits)
	{
		return drainEnergy(pEnergyType, pUnits, false);
	}
	
	/**
	 * Main function to drain Energy from the Multiblock structure. The returnvalue is the amount of energy drained, and 
	 * should be used to calculate how much energy of the target system shall be emitted
	 * @param pEnergyType
	 * @param pUnits
	 * @param pDrainIncomplete if set to false, the controller will only allow *full* packets of energy
	 * @return
	 */
	public long drainEnergy(PowerSystem pEnergyType, long pUnits, boolean pDrainIncomplete)
	{
		long tReturnValue = 0;
		// Do we have enough energy stored to provide the full request?
		if (canDrainPower(pEnergyType, pUnits))
		{
			_mEnergy -= pEnergyType.getEnergyUnits(pUnits);
			tReturnValue = pUnits;
		}
		else
		{
			if (pDrainIncomplete && _mEnergy > 0)
			{
				// Drain all remaining energy, and return the amount of available power that can be used
				tReturnValue = _mEnergy * pEnergyType.getRatio();
				_mEnergy = 0;
			}
		}
		
		return tReturnValue;
	}
	
	public long injectEnergy(PowerSystem pEnergyType, long pUnits)
	{
		return injectEnergy(pEnergyType, pUnits, false);
	}
	
	/**
	 * Main function to inject Energy into the Multiblock structure. The returnvalue is the amount of energy accepted, and 
	 * should be used to calculate how much energy of the target system shall be subtracted
	 * @param pEnergyType
	 * @param pUnits
	 * @param pAcceptIncomplete if set to false, the controller will only accept *full* packets of energy
	 * @return
	 */
	public long injectEnergy(PowerSystem pEnergyType, long pUnits, boolean pAcceptIncomplete)
	{
		long tReturnValue = 0;
		// Do we have enough free space to accept the full offering?
		if (canInjectPower(pEnergyType, pUnits))
		{
			_mEnergy += pEnergyType.getEnergyUnits(pUnits);
			tReturnValue = pUnits;
		}
		else
		{
			if (pAcceptIncomplete && _mEnergy != Long.MAX_VALUE)
			{
				// Fill the buffer to max, and return the difference
				long tMissing = Long.MAX_VALUE - _mEnergy;
				_mEnergy = Long.MAX_VALUE;
				tReturnValue = tMissing;
			}
		}
		
		return tReturnValue;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		
		_mEnergy = nbttagcompound.getLong(NBTVAL_ENERGY);
		FMLLog.log(Level.INFO, "Data loaded from NBT: %d", _mEnergy);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setLong(NBTVAL_ENERGY, _mEnergy);
		FMLLog.log(Level.INFO, "Data written to NBT: E: %d", _mEnergy);
	}

	/**
	 * (Re-)Calculate maximum storage size based on the number of capacitor elements
	 * The possible stored amount of Energy grows with each 18. Block placed 
	 */
	private void calculateStorageSize()
	{
		double tCapacitorElements = 0.0D;
		for (Map.Entry<String, BlockPoswID> tElmt : scannedBlocks.entrySet())
		{
			if (tElmt.getValue().blockType == GTSU_BlockType.CAPACITORELEMENT || tElmt.getValue().blockType == GTSU_BlockType.CAPACITORELEMENT_VISIBLE)
				tCapacitorElements++;
		}
		
		double tMultiplier = Math.pow(Math.ceil(tCapacitorElements / 18.0D), 2.0D);
		if (tCapacitorElements > 66852)
			_mMaxEnergy = Long.MAX_VALUE;
		else
			_mMaxEnergy = new Double(_mEnergyPerElement * tCapacitorElements * tMultiplier).longValue();
		
		// Cut down energy to new storage size, if we have some energy left
		if (_mEnergy > _mMaxEnergy)
			_mEnergy = _mMaxEnergy;
		
		FMLLog.log(Level.INFO, "Found Capacitors: %f Total Possible Storage: %f", tCapacitorElements, _mMaxEnergy);
	}
	
	/**
	 * Calculate the percentage of power stored in the Multiblock
	 * @return
	 */
	private int calcFillLevel()
	{
		double tPerc = 100.0D / (double)_mMaxEnergy * (double)_mEnergy;
		return (int)tPerc;
	}
	
	/**
	 * Update the MetaID of the capacitor blocks, to reflect the current fill-level of
	 * the MultiBlock. While scanning the MB Structure, we already checked the visibility of the capacitor
	 * elements, so we don't have to do this here, which saves a lot of calculation time (And probably bandwith, for
	 * large Multiblocks)
	 */
	private void updateCapacitorBlocks(World pWorld)
	{
		int tMeta = new Double(15.0D / 100.0D * (double)calcFillLevel()).intValue();
		for (Map.Entry<String, BlockPoswID> tElmt : scannedBlocks.entrySet())
		{
			BlockPoswID tBlock = tElmt.getValue();
			if (tBlock.blockType == GTSU_BlockType.CAPACITORELEMENT_VISIBLE)
				pWorld.setBlockMetadataWithNotify(tBlock.x, tBlock.y, tBlock.z, tMeta, 2);
		}
	}

	@Override
	public void updateMBStruct(boolean pStructValid, BlockPoswID pControllerBlock) {
		// Nothing to do. We are a master block
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer pPlayer) {
		return true;
	}
	
	@Override
	public boolean onRightclick(EntityPlayer pPlayer, byte pSide, float par1, float par2, float par3) {
		// TODO: Open GUI
		PlayerChatHelper.SendInfo(pPlayer, String.format("Structure formed: %b", _mMultiblockIsValid));
		PlayerChatHelper.SendInfo(pPlayer, String.format("Energy Stored   : %d", _mEnergy));
		PlayerChatHelper.SendInfo(pPlayer, String.format("Max Energy      : %d", _mMaxEnergy));
		return true;
	}
}
