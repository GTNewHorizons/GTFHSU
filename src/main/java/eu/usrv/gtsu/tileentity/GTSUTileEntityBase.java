package eu.usrv.gtsu.tileentity;

import java.util.Map;

import cpw.mods.fml.common.FMLLog;
import gregtech.api.GregTech_API;
import static gregtech.api.enums.GT_Values.GT;
import static gregtech.api.enums.GT_Values.NW;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IHasWorldObjectAndCoords;
import gregtech.api.net.GT_Packet_Block_Event;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Main TileEntity BaseClass for all TileEntities.
 * Initially, required because the GT EnergyNet is a pain to use (GT5 that is), since it requires IHasWorldObjectAndCoords
 * But after a while, more functions where added to it, and after several refractorings, i decided to use this as my main TE-Base
 */
public abstract class GTSUTileEntityBase extends TileEntity implements IHasWorldObjectAndCoords
{
	/**
	 * Player tries to perform a Wrench-Action on the TE
	 * You should return false if no action was performed, as the tool won't loose charge/durability then
	 * @param pPlayer
	 * @return true if any action was performed, false if not
	 */
	public abstract boolean doWrench(EntityPlayer pPlayer);

	/**
	 * Player tries to perform a Screwdriver-Action on the TE
	 * You should return false if no action was performed, as the tool won't loose charge/durability then
	 * @param pPlayer
	 * @return true if any action was performed, false if not
	 */
	public abstract boolean doScrewdriver(EntityPlayer pPlayer);
	
	/**
	 * Player just right-clicked the TE
	 * @param pPlayer
	 */
	public abstract void doBareHand(EntityPlayer pPlayer);
	
	
	/**
	 * Player tries to perform a Hammer-Action on the TE
	 * You should return false if no action was performed, as the tool won't loose charge/durability then
	 * @param pPlayer
	 * @return true if any action was performed, false if not
	 */
	public abstract boolean doHardHammer(EntityPlayer pPlayer);
	
	/**
	 * Player tries to perform a SoftHammer-Action on the TE
	 * You should return false if no action was performed, as the tool won't loose charge/durability then
	 * @param pPlayer
	 * @return true if any action was performed, false if not
	 */
	public abstract boolean doSoftHammer(EntityPlayer pPlayer);
	
	/**
	 * A check if the player is in valid range of the TE.
	 * Can be overwritten to perform additional security checks
	 * @param aPlayer
	 * @return
	 */
	public boolean isUseableByPlayer(EntityPlayer aPlayer) {return aPlayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64; }

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
				
				if (GT_Utility.isStackInList(tCurrentItem, GregTech_API.sSoftHammerList)) 
				{
					if (doSoftHammer(pPlayer))
					{
						GT_ModHandler.damageOrDechargeItem(tCurrentItem, 1, 1000, pPlayer);
						GT_Utility.sendSoundToPlayers(worldObj, GregTech_API.sSoundList.get(101), 1.0F, -1, xCoord, yCoord, zCoord);
					}
					return true;
				}
				
				if (GT_Utility.isStackInList(tCurrentItem, GregTech_API.sHardHammerList)) 
				{
					if (doHardHammer(pPlayer))
					{
						GT_ModHandler.damageOrDechargeItem(tCurrentItem, 1, 1000, pPlayer);
						GT_Utility.sendSoundToPlayers(worldObj, GregTech_API.sSoundList.get(1), 1.0F, -1, xCoord, yCoord, zCoord);
					}
					return true;
				}

			}
			else
				doBareHand(pPlayer);
		}
		return true;
	}
	
	
	/**
	 * If this TileEntity checks for the Chunk to be loaded before returning World based values.
	 * The AdvPump hacks this to false to ensure everything runs properly even when far Chunks are not actively loaded.
	 * But anything else should not cause worfin' Chunks, uhh I mean orphan Chunks.
	 */
	public boolean ignoreUnloadedChunks = true;

	/**
	 * This Variable checks if this TileEntity is dead, because Minecraft is too stupid to have proper TileEntity unloading.
	 */
	public boolean isDead = false;

	/**
	 * Buffers adjacent TileEntities for faster access
	 * 
	 * "this" means that there is no TileEntity, while "null" means that it doesn't know if there is even a TileEntity and still needs to check that if needed.
	 */
	private final TileEntity[] mBufferedTileEntities = new TileEntity[6];

	private final void clearNullMarkersFromTileEntityBuffer() {
		for (int i = 0; i < mBufferedTileEntities.length; i++) if (mBufferedTileEntities[i] == this) mBufferedTileEntities[i] = null;
	}

	/**
	 * Called automatically when the Coordinates of this TileEntity have been changed
	 */
	protected final void clearTileEntityBuffer() {
		for (int i = 0; i < mBufferedTileEntities.length; i++) mBufferedTileEntities[i] = null;
	}

	@Override public final World getWorld () {return      worldObj;}
	@Override public final int   getXCoord() {return        xCoord;}
	@Override public final short getYCoord() {return (short)yCoord;}
	@Override public final int   getZCoord() {return        zCoord;}
	@Override public final int   getOffsetX(byte aSide, int aMultiplier) {return         xCoord + ForgeDirection.getOrientation(aSide).offsetX * aMultiplier ;}
	@Override public final short getOffsetY(byte aSide, int aMultiplier) {return (short)(yCoord + ForgeDirection.getOrientation(aSide).offsetY * aMultiplier);}
	@Override public final int   getOffsetZ(byte aSide, int aMultiplier) {return         zCoord + ForgeDirection.getOrientation(aSide).offsetZ * aMultiplier ;}
	@Override public final boolean isServerSide() {return !worldObj.isRemote;}
	@Override public final boolean isClientSide() {return  worldObj.isRemote;}
	@Override public final boolean openGUI(EntityPlayer aPlayer) {return openGUI(aPlayer, 0);}
	@Override public final boolean openGUI(EntityPlayer aPlayer, int aID) {if (aPlayer == null) return false; aPlayer.openGui(GT, aID, worldObj, xCoord, yCoord, zCoord); return true;}
	@Override public final int getRandomNumber(int aRange) {return worldObj.rand.nextInt(aRange);}
	@Override public final BiomeGenBase getBiome(int aX, int aZ) {return worldObj.getBiomeGenForCoords(aX, aZ);}
	@Override public final BiomeGenBase getBiome() {return getBiome(xCoord, zCoord);}
	@Override public final Block getBlockOffset(int aX, int aY, int aZ) {return getBlock(xCoord+aX, yCoord+aY, zCoord+aZ);}
	@Override public final Block getBlockAtSide(byte aSide) {return getBlockAtSideAndDistance(aSide, 1);}
	@Override public final Block getBlockAtSideAndDistance(byte aSide, int aDistance) {return getBlock(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));}
	@Override public final byte getMetaIDOffset(int aX, int aY, int aZ) {return getMetaID(xCoord+aX, yCoord+aY, zCoord+aZ);}
	@Override public final byte getMetaIDAtSide(byte aSide) {return getMetaIDAtSideAndDistance(aSide, 1);}
	@Override public final byte getMetaIDAtSideAndDistance(byte aSide, int aDistance) {return getMetaID(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));}
	@Override public final byte getLightLevelOffset(int aX, int aY, int aZ) {return getLightLevel(xCoord+aX, yCoord+aY, zCoord+aZ);}
	@Override public final byte getLightLevelAtSide(byte aSide) {return getLightLevelAtSideAndDistance(aSide, 1);}
	@Override public final byte getLightLevelAtSideAndDistance(byte aSide, int aDistance) {return getLightLevel(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));}
	@Override public final boolean getOpacityOffset(int aX, int aY, int aZ) {return getOpacity(xCoord+aX, yCoord+aY, zCoord+aZ);}
	@Override public final boolean getOpacityAtSide(byte aSide) {return getOpacityAtSideAndDistance(aSide, 1);}
	@Override public final boolean getOpacityAtSideAndDistance(byte aSide, int aDistance) {return getOpacity(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));}
	@Override public final boolean getSkyOffset(int aX, int aY, int aZ) {return getSky(xCoord+aX, yCoord+aY, zCoord+aZ);}
	@Override public final boolean getSkyAtSide(byte aSide) {return getSkyAtSideAndDistance(aSide, 1);}
	@Override public final boolean getSkyAtSideAndDistance(byte aSide, int aDistance) {return getSky(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));}
	@Override public final boolean getAirOffset(int aX, int aY, int aZ) {return getAir(xCoord+aX, yCoord+aY, zCoord+aZ);}
	@Override public final boolean getAirAtSide(byte aSide) {return getAirAtSideAndDistance(aSide, 1);}
	@Override public final boolean getAirAtSideAndDistance(byte aSide, int aDistance) {return getAir(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));}
	@Override public final TileEntity getTileEntityOffset(int aX, int aY, int aZ) {return getTileEntity(xCoord+aX, yCoord+aY, zCoord+aZ);}
	@Override public final TileEntity getTileEntityAtSideAndDistance(byte aSide, int aDistance) {if (aDistance == 1) return getTileEntityAtSide(aSide); return getTileEntity(getOffsetX(aSide, aDistance), getOffsetY(aSide, aDistance), getOffsetZ(aSide, aDistance));}
	@Override public final IInventory getIInventory(int aX, int aY, int aZ) {TileEntity tTileEntity = getTileEntity(aX, aY, aZ); if (tTileEntity instanceof IInventory) return (IInventory)tTileEntity; return null;}
	@Override public final IInventory getIInventoryOffset(int aX, int aY, int aZ) {TileEntity tTileEntity = getTileEntityOffset(aX, aY, aZ); if (tTileEntity instanceof IInventory) return (IInventory)tTileEntity; return null;}
	@Override public final IInventory getIInventoryAtSide(byte aSide) {TileEntity tTileEntity = getTileEntityAtSide(aSide); if (tTileEntity instanceof IInventory) return (IInventory)tTileEntity; return null;}
	@Override public final IInventory getIInventoryAtSideAndDistance(byte aSide, int aDistance) {TileEntity tTileEntity = getTileEntityAtSideAndDistance(aSide, aDistance); if (tTileEntity instanceof IInventory) return (IInventory)tTileEntity; return null;}
	@Override public final IFluidHandler getITankContainer(int aX, int aY, int aZ) {TileEntity tTileEntity = getTileEntity(aX, aY, aZ); if (tTileEntity instanceof IFluidHandler) return (IFluidHandler)tTileEntity; return null;}
	@Override public final IFluidHandler getITankContainerOffset(int aX, int aY, int aZ) {TileEntity tTileEntity = getTileEntityOffset(aX, aY, aZ); if (tTileEntity instanceof IFluidHandler) return (IFluidHandler)tTileEntity; return null;}
	@Override public final IFluidHandler getITankContainerAtSide(byte aSide) {TileEntity tTileEntity = getTileEntityAtSide(aSide); if (tTileEntity instanceof IFluidHandler) return (IFluidHandler)tTileEntity; return null;}
	@Override public final IFluidHandler getITankContainerAtSideAndDistance(byte aSide, int aDistance) {TileEntity tTileEntity = getTileEntityAtSideAndDistance(aSide, aDistance); if (tTileEntity instanceof IFluidHandler) return (IFluidHandler)tTileEntity; return null;}
	@Override public final IGregTechTileEntity getIGregTechTileEntity(int aX, int aY, int aZ) {TileEntity tTileEntity = getTileEntity(aX, aY, aZ); if (tTileEntity instanceof IGregTechTileEntity) return (IGregTechTileEntity)tTileEntity; return null;}
	@Override public final IGregTechTileEntity getIGregTechTileEntityOffset(int aX, int aY, int aZ) {TileEntity tTileEntity = getTileEntityOffset(aX, aY, aZ); if (tTileEntity instanceof IGregTechTileEntity) return (IGregTechTileEntity)tTileEntity; return null;}
	@Override public final IGregTechTileEntity getIGregTechTileEntityAtSide(byte aSide) {TileEntity tTileEntity = getTileEntityAtSide(aSide); if (tTileEntity instanceof IGregTechTileEntity) return (IGregTechTileEntity)tTileEntity; return null;}
	@Override public final IGregTechTileEntity getIGregTechTileEntityAtSideAndDistance(byte aSide, int aDistance) {TileEntity tTileEntity = getTileEntityAtSideAndDistance(aSide, aDistance); if (tTileEntity instanceof IGregTechTileEntity) return (IGregTechTileEntity)tTileEntity; return null;}

	@Override
	public final Block getBlock(int aX, int aY, int aZ) {
		if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return Blocks.air;
		return worldObj.getBlock(aX, aY, aZ);
	}

	@Override
	public final byte getMetaID(int aX, int aY, int aZ) {
		if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return 0;
		return (byte)worldObj.getBlockMetadata(aX, aY, aZ);
	}

	@Override
	public final byte getLightLevel(int aX, int aY, int aZ) {
		if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return 0;
		return (byte)(worldObj.getLightBrightness(aX, aY, aZ)*15);
	}

	@Override
	public final boolean getSky(int aX, int aY, int aZ) {
		if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return true;
		return worldObj.canBlockSeeTheSky(aX, aY, aZ);
	}

	@Override
	public final boolean getOpacity(int aX, int aY, int aZ) {
		if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return false;
		return GT_Utility.isOpaqueBlock(worldObj, aX, aY, aZ);
	}

	@Override
	public final boolean getAir(int aX, int aY, int aZ) {
		if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return true;
		return GT_Utility.isBlockAir(worldObj, aX, aY, aZ);
	}

	@Override
	public final TileEntity getTileEntity(int aX, int aY, int aZ) {
		if (ignoreUnloadedChunks && crossedChunkBorder(aX, aZ) && !worldObj.blockExists(aX, aY, aZ)) return null;
		return worldObj.getTileEntity(aX, aY, aZ);
	}

	@Override
	public final TileEntity getTileEntityAtSide(byte aSide) {
		if (aSide < 0 || aSide >= 6 || mBufferedTileEntities[aSide] == this) return null;
		int tX = getOffsetX(aSide, 1), tY = getOffsetY(aSide, 1), tZ = getOffsetZ(aSide, 1);
		if (crossedChunkBorder(tX, tZ)) {
			mBufferedTileEntities[aSide] = null;
			if (ignoreUnloadedChunks && !worldObj.blockExists(tX, tY, tZ)) return null;
		}
		if (mBufferedTileEntities[aSide] == null) {
			mBufferedTileEntities[aSide] = worldObj.getTileEntity(tX, tY, tZ);
			if (mBufferedTileEntities[aSide] == null) {
				mBufferedTileEntities[aSide] = this;
				return null;
			}
			return mBufferedTileEntities[aSide];
		}
		if (mBufferedTileEntities[aSide].isInvalid()) {
			mBufferedTileEntities[aSide] = null;
			return getTileEntityAtSide(aSide);
		}
		if (mBufferedTileEntities[aSide].xCoord == tX && mBufferedTileEntities[aSide].yCoord == tY && mBufferedTileEntities[aSide].zCoord == tZ) {
			return mBufferedTileEntities[aSide];
		}
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound aNBT) {
		super.writeToNBT(aNBT);
		//isDead = true;
	}

	@Override
	public boolean isDead() {
		return isDead || isInvalidTileEntity();
	}

	@Override
	public void validate() {
		clearNullMarkersFromTileEntityBuffer();
		super.validate();
	}

	@Override
	public void invalidate() {
		clearNullMarkersFromTileEntityBuffer();
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		clearNullMarkersFromTileEntityBuffer();
		super.onChunkUnload();
		isDead = true;
	}

	@Override
	public void updateEntity() {
		// Well if the TileEntity gets ticked it is alive.
		isDead = false;
	}

	public final void onAdjacentBlockChange(int aX, int aY, int aZ) {
		clearNullMarkersFromTileEntityBuffer();
	}

	@Override
	public final void sendBlockEvent(byte aID, byte aValue) {
		NW.sendPacketToAllPlayersInRange(worldObj, new GT_Packet_Block_Event(xCoord, (short)yCoord, zCoord, aID, aValue), xCoord, zCoord);
	}

	private boolean crossedChunkBorder(int aX, int aZ) {
		return aX >> 4 != xCoord >> 4 || aZ >> 4 != zCoord >> 4;
	}

	@Override public void setLightValue(byte aLightValue) {};
	@Override public boolean isInvalidTileEntity() { return true; }
	@Override public long getTimer() { return 0; }
}
