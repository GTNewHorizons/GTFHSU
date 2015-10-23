package eu.usrv.gtsu.tileentity;

import static eu.usrv.gtsu.TierHelper.V;

import java.util.HashMap;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import eu.usrv.gtsu.TierHelper;
import eu.usrv.gtsu.helper.EnergySystemConverter.PowerSystem;
import eu.usrv.gtsu.helper.Enums.EN_EnergyType;
import eu.usrv.gtsu.multiblock.BlockPosHelper.MB_BlockState;
import eu.usrv.gtsu.multiblock.IMultiBlock;
import eu.usrv.gtsu.multiblock.TEMultiBlockBase;

// This is our main controller block
public class TEMBControllerBlock extends TEMultiBlockBase
{
	// Our stored energy. This is *not* EU, nor RF, nor anything else. It's a new PowerSystem, with blackjack, and hookers!
	private long _mEnergy;
	private HashMap<MB_BlockState, IMultiBlock> _mMultiBlockCompound;
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
			// because this would result in an overflow of the operation itself.
			// So instead, we subtract our stored energy from the maximum possible amount,
			// and compare that to the amount to be injected. So we never leave our range of the Long
			// Datatype
			
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

	@Override
	public MB_BlockState getMultiblockBlockType()
	{
		return MB_BlockState.MASTER;
	}
	
}
