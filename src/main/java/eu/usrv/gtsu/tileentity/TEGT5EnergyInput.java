package eu.usrv.gtsu.tileentity;

import java.util.HashMap;
import java.util.Map;

import eu.usrv.gtsu.TierHelper;
import eu.usrv.gtsu.helper.BlockPosition;
import eu.usrv.gtsu.helper.EnergySystemConverter.PowerSystem;
import eu.usrv.gtsu.helper.PlayerChatHelper;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.IMultiBlockComponent;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import gregtech.api.metatileentity.BaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import static eu.usrv.gtsu.TierHelper.V;

public class TEGT5EnergyInput extends GTSUMBSlaveBlockBase implements IEnergyConnected {
	private byte _mVoltageIdx;
	private long _mMaxSafeVoltage;
	private byte _mColor;
	private double _mEuLastTick;
	private long _mLastTickInjected;
	private boolean _mNeedsBlockUpdate = true;
	private int _mMaxAmperage;
	
	private Map<ForgeDirection, IEnergyConnected> _adjacentTiles = new HashMap<ForgeDirection, IEnergyConnected>();
	private boolean _initialized;
	
	public TEGT5EnergyInput() {
		setVoltageByIndex(0);
		setColorization((byte)-1);
	}

	private void setVoltageByIndex(int pVoltageIdx) {
		if(pVoltageIdx > V.length)
			pVoltageIdx = 0;

		_mVoltageIdx = (byte)pVoltageIdx;
		_mMaxSafeVoltage = V[_mVoltageIdx];
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
		_mMaxAmperage = tag.getInteger("maxAmp");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		tag.setByte("voltageIndex", _mVoltageIdx);
		tag.setByte("gtTileColor", _mColor);
		tag.setInteger("maxAmp", _mMaxAmperage);
	}

	private void onOvervoltage() {
		Block b = worldObj.getBlock(xCoord, yCoord, zCoord);

		b.dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, worldObj.getBlockMetadata(xCoord, yCoord, zCoord), 0);
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);    		
	}

	/** GregTech API Part **/
	@Override
	public long injectEnergyUnits(byte aSide, long aVoltage, long aAmperage) {
		if (!_mStructureValid || getMaster() == null)
			return 0;
		
		TEMBControllerBlock tMaster = getMaster();
		
		double dEU = (double)aVoltage;
		double dAmperage = (double)aAmperage;
		long usedAmps;
		//boolean powered = getWorldObj().getStrongestIndirectPower(xCoord, yCoord, zCoord) > 0;

		if(aVoltage > _mMaxSafeVoltage)
		{
			onOvervoltage();
			return 0;
		}
		
		long tMaxAmps = Math.max(aAmperage, _mMaxAmperage);
		long tTotalEnergy = aVoltage * tMaxAmps;
		
		// Charge/Store ->
		// Due to the "dont accept incomplete" flag, this will either return tTotalEnergy on success, or 0 on failure.
		long tUnusedEnergy = tMaster.injectEnergy(PowerSystem.GT5, tTotalEnergy, false);
		if (tUnusedEnergy == 0)
			return 0;

		// Update Stat Counters
		if (_mLastTickInjected == worldObj.getTotalWorldTime())
			_mEuLastTick += tTotalEnergy;
		else
			_mEuLastTick = tTotalEnergy;
		_mLastTickInjected = worldObj.getTotalWorldTime();

		return tMaxAmps;
	}

	@Override
	public boolean inputEnergyFrom(byte aSide) {
		return _mStructureValid;
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

	@Override
	public boolean doScrewdriver(EntityPlayer pPlayer) 
	{
		if (pPlayer.isSneaking())
			_mMaxAmperage++;
		else
			_mVoltageIdx++;
		
		if (_mVoltageIdx >= V.length)
			_mVoltageIdx = 0;
		if (_mMaxAmperage > 16)
			_mMaxAmperage = 1;
		
		setVoltageByIndex(_mVoltageIdx);
		PlayerChatHelper.SendInfo(pPlayer, String.format("Vin: %d EU/t @ %d Amp", _mMaxSafeVoltage, _mMaxAmperage));
		
		return true;
	}
	
	@Override
	public boolean doWrench(EntityPlayer pPlayer) {
		return false;
	}

	@Override
	public void doBareHand(EntityPlayer pPlayer) {
		// TODO: Add a fancy GUI later
		PlayerChatHelper.SendInfo(pPlayer, String.format("Vin: %d EU/t @ %d Amp", _mMaxSafeVoltage, _mMaxAmperage));
	}

	@Override
	public boolean doHardHammer(EntityPlayer pPlayer) {
		return false;
	}

	@Override
	public boolean doSoftHammer(EntityPlayer pPlayer) {
		return false;
	}
}
