package eu.usrv.gtsu.tileentity;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import eu.usrv.gtsu.TierHelper;
import static eu.usrv.gtsu.TierHelper.V;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IC2;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.init.MainConfig;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityGTSU extends TileEntityInventory implements INetworkClientTileEntityEventListener, IEnergySink, IEnergySource {
	public static final String NBTVAL_REDSTONE_MODE = "redstoneMode";
	public static final String NBTVAL_ACTIVE = "active";
	public static final String NBTVAL_ENERGY = "energy";
	public static final String NBTVAL_TIER = "tier";
	public int mTier = -1;
	public byte mRedstoneMode = 0;
	public static byte redstoneModes = 7;
	public InvSlotCharge mChargeSlot;
	public InvSlotDischarge mDischargeSlot;
	public long mEnergy;
	public long mMaxStorage;
	public int mOutput;
	public boolean mHasRedstone = false;
	private boolean mIsEmittingRedstone = false;
	private int mRedstoneUpdateInhibit = 5;
	public boolean mAddedToEnergyNet = false;

	public TileEntityGTSU()
	{
		mChargeSlot = new InvSlotCharge(this, 0, 5);
		mDischargeSlot = new InvSlotDischarge(this, 1, InvSlot.Access.IO, 5, InvSlot.InvSide.BOTTOM);		
	}
	
	public TileEntityGTSU(int pTier) {
		mTier = pTier;
		mMaxStorage = TierHelper.getMaxStorageForTier(mTier);
		mOutput = V[mTier];
		
		mChargeSlot = new InvSlotCharge(this, 0, 5);
		mDischargeSlot = new InvSlotDischarge(this, 1, InvSlot.Access.IO, 5, InvSlot.InvSide.BOTTOM);
	}

	public int getChargePercentage()
	{
		return new Double((100.0D / (double)mMaxStorage) * (double)mEnergy).intValue(); 
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();

		boolean needsInvUpdate = false;
		if (this.mEnergy >= 1L)
		{
			double sent = this.mChargeSlot.charge(this.mEnergy);

			this.mEnergy -= sent;
			needsInvUpdate = sent > 0L;
		}
		if ((getDemandedEnergy() > 0L) && (!this.mDischargeSlot.isEmpty()))
		{
			double gain = this.mDischargeSlot.discharge(this.mMaxStorage - this.mEnergy, false);

			this.mEnergy += gain;
			needsInvUpdate = gain > 0L;
		}
		if ((this.mRedstoneMode == 5) || (this.mRedstoneMode == 6)) {
			this.mHasRedstone = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
		}
		boolean shouldEmitRedstone = shouldEmitRedstone();
		if (shouldEmitRedstone != this.mIsEmittingRedstone)
		{
			this.mIsEmittingRedstone = shouldEmitRedstone;
			setActive(this.mIsEmittingRedstone);

			this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord));
		}
		if (needsInvUpdate) {
			markDirty();
		}
	}

	public boolean shouldEmitRedstone()
	{
		boolean shouldEmitRedstone = false;
		switch (this.mRedstoneMode)
		{
		case 1: 
			shouldEmitRedstone = this.mEnergy >= this.mMaxStorage - this.mOutput * 20;
			break;
		case 2: 
			shouldEmitRedstone = (this.mEnergy > this.mOutput) && (this.mEnergy < this.mMaxStorage);
			break;
		case 3: 
			shouldEmitRedstone = ((this.mEnergy > this.mOutput) && (this.mEnergy < this.mMaxStorage)) || (this.mEnergy < this.mOutput);
			break;
		case 4: 
			shouldEmitRedstone = this.mEnergy < this.mOutput;
		}
		if ((this.mIsEmittingRedstone == shouldEmitRedstone) || (this.mRedstoneUpdateInhibit == 0))
		{
			this.mRedstoneUpdateInhibit = 5;

			return shouldEmitRedstone;
		}
		this.mRedstoneUpdateInhibit -= 1;

		return this.mIsEmittingRedstone;
	}

	@Override
	public void onLoaded()
	{
		super.onLoaded();
		if (IC2.platform.isSimulating())
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			this.mAddedToEnergyNet = true;
		}
	}

	@Override
	public void onUnloaded()
	{
		if ((IC2.platform.isSimulating()) && (this.mAddedToEnergyNet))
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));

			this.mAddedToEnergyNet = false;
		}
		super.onUnloaded();
	}	
	
	@Override
	public boolean enableUpdateEntity()
	{
		return IC2.platform.isSimulating();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		
		mEnergy = nbttagcompound.getLong(NBTVAL_ENERGY);
		mRedstoneMode = nbttagcompound.getByte(NBTVAL_REDSTONE_MODE);
		mTier = nbttagcompound.getInteger(NBTVAL_TIER);
		mMaxStorage = TierHelper.getMaxStorageForTier(mTier);
		mOutput = V[mTier];

		//FMLLog.log(Level.INFO, "Data loaded from NBT: %d / T: %d", mEnergy, mTier);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setInteger(NBTVAL_TIER, mTier);
		nbttagcompound.setLong(NBTVAL_ENERGY, mEnergy);
		nbttagcompound.setBoolean(NBTVAL_ACTIVE, getActive());
		nbttagcompound.setByte(NBTVAL_REDSTONE_MODE, mRedstoneMode);
		//FMLLog.log(Level.INFO, "Data written to NBT: E: %d / T: %d", mEnergy, mTier);
	}
	
	public long getChargeLevel() {
		return mEnergy;
	}

	public String getredstoneMode()  {
		if ((mRedstoneMode > 6) || (mRedstoneMode < 0)) {
			return "";
		}
		return StatCollector.translateToLocal("ic2.EUStorage.gui.mod.redstone" + mRedstoneMode);
	}

	@Override
	public String getInventoryName() {
		return "GTSU";
	}

	public boolean isEmittingRedstone() {
		return false;
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		mRedstoneMode = ((byte)(mRedstoneMode + 1));
		if (mRedstoneMode >= redstoneModes) {
			mRedstoneMode = 0;
		}
		IC2.platform.messagePlayer(player, getredstoneMode(), new Object[0]);
	}

	@Override
	public ItemStack getWrenchDrop(EntityPlayer entityPlayer)
	{
		ItemStack ret = super.getWrenchDrop(entityPlayer);

		float energyRetainedInStorageBlockDrops = ConfigUtil.getFloat(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
		if (energyRetainedInStorageBlockDrops > 0.0F)
		{
			NBTTagCompound nbttagcompound = StackUtil.getOrCreateNbtData(ret);
			nbttagcompound.setDouble(NBTVAL_ENERGY, this.mEnergy * energyRetainedInStorageBlockDrops);
		}
		return ret;
	}
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return !facingMatchesDirection(direction);
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return facingMatchesDirection(direction);
	}

	public boolean facingMatchesDirection(ForgeDirection direction)
	{
		return direction.ordinal() == getFacing();
	}
	@Override
	public double getOfferedEnergy()
	{
		if ((this.mEnergy >= this.mOutput) && ((this.mRedstoneMode != 5) || (!this.mHasRedstone)) && ((this.mRedstoneMode != 6) || (!this.mHasRedstone) || (this.mEnergy >= this.mMaxStorage))) {
			return Math.min(this.mEnergy, this.mOutput);
		}
		return 0.0D;
	}

	@Override
	public void drawEnergy(double amount) {
		this.mEnergy -= amount;
	}

	@Override
	public int getSourceTier() {
		return 2 + mTier;
	}

	@Override
	public double getDemandedEnergy() {
		return this.mMaxStorage - this.mEnergy;
	}

	@Override
	public int getSinkTier() {
		return 2 + mTier;
	}

	@Override
	public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
		if (this.mEnergy >= this.mMaxStorage) {
			return amount;
		}
		this.mEnergy += amount;
		return 0.0D;
	}
	
	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return getFacing() != side;
	}

	@Override
	public void setFacing(short facing)
	{
		if (this.mAddedToEnergyNet) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		}
		super.setFacing(facing);
		if (this.mAddedToEnergyNet)
		{
			this.mAddedToEnergyNet = false;
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			this.mAddedToEnergyNet = true;
		}
	}
}
