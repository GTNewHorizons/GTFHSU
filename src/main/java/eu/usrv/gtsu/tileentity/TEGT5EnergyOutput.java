package eu.usrv.gtsu.tileentity;

import static eu.usrv.gtsu.TierHelper.V;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import eu.usrv.gtsu.helper.BlockPosition;
import eu.usrv.gtsu.helper.PlayerChatHelper;
import eu.usrv.gtsu.helper.EnergySystemConverter.PowerSystem;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import gregtech.api.metatileentity.BaseTileEntity;

public class TEGT5EnergyOutput extends GTSUMBSlaveBlockBase implements IEnergyConnected {
	private byte _mVoltageIdx;
	private long _mVoltageOut;
	private byte _mColor;
	private double _mEuLastTick;
	private long _mLastTickInjected;
	private boolean _mNeedsBlockUpdate = true;
	private int _mAmperageOut;

	private Map<ForgeDirection, IEnergyConnected> _adjacentTiles = new HashMap<ForgeDirection, IEnergyConnected>();
	private boolean _initialized;

	public TEGT5EnergyOutput() {
		setVoltageByIndex(0);
		setColorization((byte)-1);
	}

	private void setVoltageByIndex(int pVoltageIdx) {
		if(pVoltageIdx > V.length)
			pVoltageIdx = 0;

		_mVoltageIdx = (byte)pVoltageIdx;
		_mVoltageOut = V[_mVoltageIdx];
	}

	public void onNeighboorChanged() {
		Map<ForgeDirection, IEnergyConnected> adjacentTiles = new HashMap<ForgeDirection, IEnergyConnected>();

		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity te = BlockPosition.getAdjacentTileEntity(this, d);
			if (te != null && te instanceof IEnergyConnected) {
				adjacentTiles.put(d, (IEnergyConnected) te);
			}
		}

		_adjacentTiles = adjacentTiles;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (!_mStructureValid)
			return; // Don't do anything unless our multiblock Structure is complete
		
		if (!_initialized && !tileEntityInvalid) {
			onNeighboorChanged();
			_initialized = true;
		}

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
		_mAmperageOut = tag.getInteger("maxAmp");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		tag.setByte("voltageIndex", _mVoltageIdx);
		tag.setByte("gtTileColor", _mColor);
		tag.setInteger("maxAmp", _mAmperageOut);
	}

	public void produceEnergy() {
		long usedEU = 0;
		
		if (!_mStructureValid)
			return; // Don't do anything unless our multiblock Structure is complete
		
		TEMBControllerBlock tMaster = getMaster();
		if (tMaster == null)
			return;
		
		for (Entry<ForgeDirection, IEnergyConnected> it : _adjacentTiles.entrySet()) {
			IEnergyConnected t = it.getValue();

			if(getColorization() >= 0) {
				byte tColor = t.getColorization();
				if(tColor >= 0 && tColor != getColorization())
					continue;
			}

			long tEnergyPending = _mVoltageOut * _mAmperageOut;
			if(!tMaster.canDrainPower(PowerSystem.GT5, tEnergyPending))
				continue;
			
			long ampsUsed = t.injectEnergyUnits( (byte)it.getKey().getOpposite().ordinal(), _mVoltageOut, _mAmperageOut );
			long tEnergyUsed = ampsUsed * _mVoltageOut;
			tMaster.drainEnergy(PowerSystem.GT5, tEnergyUsed, false);
		}
	}



	/** GregTech API Part **/
	@Override
	public long injectEnergyUnits(byte aSide, long aVoltage, long aAmperage) {
		return 0;
	}

	@Override
	public boolean inputEnergyFrom(byte aSide) {
		return false;
	}

	@Override
	public boolean outputsEnergyTo(byte aSide) {
		return _mStructureValid;
	}

	@Override
	public byte getColorization() {
		return -1;
	}

	@Override
	public byte setColorization(byte aColor) {
		return -1;
	}
	
	@Override
	public boolean doScrewdriver(EntityPlayer pPlayer) 
	{
		if (pPlayer.isSneaking())
			_mAmperageOut++;
		else
			_mVoltageIdx++;
		
		if (_mVoltageIdx >= V.length)
			_mVoltageIdx = 0;
		if (_mAmperageOut > 16)
			_mAmperageOut = 1;
		
		
		setVoltageByIndex(_mVoltageIdx);
		PlayerChatHelper.SendInfo(pPlayer, String.format("Vout: %d EU/t @ %d Amp", _mVoltageOut, _mAmperageOut));
		
		return true;
	}
	
	@Override
	public boolean doWrench(EntityPlayer pPlayer) {
		return false;
	}

	@Override
	public void doBareHand(EntityPlayer pPlayer) {
		PlayerChatHelper.SendInfo(pPlayer, String.format("Vout: %d EU/t @ %d Amp", _mVoltageOut, _mAmperageOut));		
	}

	@Override
	public boolean doHardHammer(EntityPlayer pPlayer) {
		return false;
	}

	@Override
	public boolean doSoftHammer(EntityPlayer pPlayer) {
		PlayerChatHelper.SendInfo(pPlayer, String.format("Has masterBlock: %b", _mMasterBlock == null ? false : true));
		return false;
	}
}
