package eu.usrv.gtsu.tileentity;

import eu.usrv.gtsu.TierHelper;
import eu.usrv.gtsu.gregtech.GT5EnergyNetTEBase;
import eu.usrv.gtsu.helper.BlockPosition;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import gregtech.api.metatileentity.BaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import static eu.usrv.gtsu.TierHelper.V;

// This code is based on PowerCrystals PowerConverters
public class TEGT5EnergyInput extends GT5EnergyNetTEBase implements IEnergyConnected {
	private byte _mVoltageIdx;
	private long _mMaxSafeVoltage;
	private byte _mColor;   
	private double _mEuLastTick;
	private long _mLastTickInjected;
	private boolean _mNeedsBlockUpdate = true;

	public TEGT5EnergyInput(int voltageIndex) {
		setVoltageByIndex(voltageIndex);
		setColorization((byte)-1);
	}

	private void setVoltageByIndex(int pVoltageIdx) {
		if(pVoltageIdx > V.length)
			pVoltageIdx = 0;

		_mVoltageIdx = (byte)pVoltageIdx;
		_mMaxSafeVoltage = V[_mVoltageIdx];
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!worldObj.isRemote){

			if (worldObj.getTotalWorldTime() - _mLastTickInjected > 2) {
				_mEuLastTick = 0;
			}

			if(_mNeedsBlockUpdate) {

				// GT's TE caches which surrounding TE's are present
				// so this is required, otherwise a Consumer placed next to
				// an already existing Cable would'nt be provided with energy
				// as the Cable's TE would assume we're not present at all.
				for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {

					TileEntity te = BlockPosition.getAdjacentTileEntity(this, d);
					if(te instanceof BaseTileEntity){
						((BaseTileEntity)te).onAdjacentBlockChange(xCoord, yCoord, zCoord);
					}
				}

				_mNeedsBlockUpdate = false;
			}
		}			                            
	}

	@Override
	public void validate() {
		super.validate();
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		setVoltageByIndex( tag.getByte("voltageIndex") );
		setColorization( tag.getByte("gtTileColor") );
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		tag.setByte("voltageIndex", _mVoltageIdx);
		tag.setByte("gtTileColor", _mColor);
	}

	private void onOvervoltage() {
		Block b = worldObj.getBlock(xCoord, yCoord, zCoord);

		b.dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, worldObj.getBlockMetadata(xCoord, yCoord, zCoord), 0);
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);    		
	}

	/** GregTech API Part **/
	@Override
	public long injectEnergyUnits(byte aSide, long aVoltage, long aAmperage) {
		double dInternalFactor = getPowerSystem().getInternalEnergyPerInput(0);
		double dEU = (double)aVoltage;
		double dAmperage = (double)aAmperage;
		long usedAmps;
		boolean powered = getWorldObj().getStrongestIndirectPower(xCoord, yCoord, zCoord) > 0;
		if(powered)
		{
			return 0;
		}

		if(aVoltage > _mMaxSafeVoltage)
		{
			onOvervoltage();
			return 0;
		}


		// Note about behavior:
		//  We're not going to waste 'half amps' in order to fill the bridge up to 100%.
		//  Only a multiple of 1-Amp gets consumed.
		//  If there's not enough free capacity to store (voltage*amperage)*InternalEnergyPerInput 
		//  it will just not store it.
		//

		// Determine how much Amps we need
		double dDemandInEU = ((double)getTotalEnergyDemand()) / dInternalFactor;
		if( dDemandInEU < dEU ) {
			return 0; // as we can't even store 1 Amp.
		}

		// Determine the Demand in Amperes at the given current
		double dDemandInAmps = dDemandInEU / dEU;
		if( dDemandInAmps < 1.0 ) { // should'nt happen but better to be paranoid :)
			return 0;
		}

		// Limit the Demand to the provided max. Energy
		if( dDemandInAmps > dAmperage ){
			dDemandInAmps = dAmperage;	
		}


		// Charge/Store ->
		double dUnusedAmps = storeEnergy( ((dDemandInAmps * dEU) * dInternalFactor), false );
		dUnusedAmps /= dInternalFactor;
		dUnusedAmps /= dEU; // divide with current to get the unused Amperage.

		if(dUnusedAmps > 0)
			usedAmps = (long)dDemandInAmps - (long)Math.floor(dUnusedAmps); 
		else
			usedAmps = (long)dDemandInAmps;


		// Update Stat Counters
		if (_mLastTickInjected == worldObj.getTotalWorldTime())
			_mEuLastTick += dEU * usedAmps;
		else
			_mEuLastTick = dEU * usedAmps;
			_mLastTickInjected = worldObj.getTotalWorldTime();

		return usedAmps;
	}

	@Override
	public boolean inputEnergyFrom(byte aSide) {
		return true;
	}

	@Override
	public boolean outputsEnergyTo(byte aSide) {
		return false;
	}

	@Override
	public byte getColorization() {
		return -1;
	}

	@Override
	public byte setColorization(byte aColor) {
		return -1;
	}
}
