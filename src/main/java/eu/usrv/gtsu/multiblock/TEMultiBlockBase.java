package eu.usrv.gtsu.multiblock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import eu.usrv.gtsu.GTSUMod;
import eu.usrv.gtsu.blocks.CoreBlock;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.BlockPosHelper.GTSU_BlockType;
import eu.usrv.gtsu.multiblock.BlockPosHelper.MB_BlockState;
import eu.usrv.gtsu.multiblock.BlockPosHelper.kvMinMax;

public abstract class TEMultiBlockBase extends TileEntity implements IMultiBlockComponent
{
	protected List<BlockPoswID> tBlocksToScan = new ArrayList<BlockPoswID>();
	protected List<BlockPoswID> newBlocksToScan = new ArrayList<BlockPoswID>();
	protected Map<String, BlockPoswID> scannedBlocks = new HashMap<String, BlockPoswID>();
	protected boolean _mMultiblockIsValid;
	private long _mLastRandomScan;
	private Random _mRnd;
	
	protected MB_BlockState _mMBState;
	protected int[][] offSets = new int[][]
			{
			{1, 0, 0},
			{-1, 0, 0},
			{0, 1, 0},
			{0, -1, 0},
			{0, 0, 1},
			{0, 0, -1} 
			};

	public TEMultiBlockBase()
	{
		_mRnd = new Random(System.currentTimeMillis());
		_mMultiblockIsValid = false;
	}

	/** Scan the given block and all his adjacent blocks; if they are valid, continue to scan those adjacent, 
	 * until no more valid blocks could be found
	 * @param pWorld
	 */
	public void scanMultiblockStructure(World pWorld)
	{
		BlockPoswID tMasterBlock = null;
		do
		{
			for (BlockPoswID b : tBlocksToScan)
			{
				for (int[] tNum : offSets)
				{
					int realX, realY, realZ;

					realX = b.x + tNum[0];
					realY = b.y + tNum[1];
					realZ = b.z + tNum[2];

					BlockPoswID tBlockPos = new BlockPoswID(realX, realY, realZ);

					Block tCurrentBlock = pWorld.getBlock(realX, realY, realZ);

					String tBlockID = String.format("%d-%d-%d-%d", pWorld.provider.dimensionId, realX, realY, realZ);
					tBlockPos.meta = tCurrentBlock.getDamageValue(pWorld, realX, realY, realZ);
					tBlockPos.blockType = getBlockTypeFromBlock(tCurrentBlock, tBlockPos);


					if (tBlockPos.blockType == GTSU_BlockType.INVALID)
						continue;

					// Do we have a CapacitorElement? Check if it is visible to the player
					if (tBlockPos.blockType == GTSU_BlockType.CAPACITORELEMENT)
					{
						for (ForgeDirection pDir : ForgeDirection.VALID_DIRECTIONS)
						{
							Block tAdjBlock = BlockPosHelper.getAdjacentBlockForPos(pWorld, tBlockPos.x, tBlockPos.y, tBlockPos.z, pDir);
							BlockPoswID tAdjBlockPos = new BlockPoswID(tBlockPos.x + pDir.offsetX, tBlockPos.y + pDir.offsetY, tBlockPos.z + pDir.offsetZ);
							tAdjBlockPos.meta = tAdjBlock.getDamageValue(pWorld, tAdjBlockPos.x, tAdjBlockPos.y, tAdjBlockPos.z);
							tAdjBlockPos.blockType = getBlockTypeFromBlock(tAdjBlock, tAdjBlockPos);
							
							if (tAdjBlockPos.blockType == GTSU_BlockType.GLASS)
							{
								// We found a GlassBlock. Change the BlockType to Visible
								tBlockPos.blockType = GTSU_BlockType.CAPACITORELEMENT_VISIBLE;
								break;
							}
						}
					}
					
					
					tBlockPos.validPosition = isBlockInValidPosition(pWorld, tBlockPos); 

					if (tBlockPos.blockType == GTSU_BlockType.CONTROLLER)
						tMasterBlock = tBlockPos;
					
					if (/*isValidMultiblockComponent(tCurrentBlock) && */!scannedBlocks.containsKey(tBlockID))
					{
						newBlocksToScan.add(tBlockPos);
					}
				}

				scannedBlocks.put(b.blockID, b);
			}
			tBlocksToScan.clear();
			tBlocksToScan = newBlocksToScan;
			newBlocksToScan = new ArrayList<BlockPoswID>();
		}
		while(tBlocksToScan.size() > 0);

		if (!checkForValidStructure(scannedBlocks))
			FMLLog.log(Level.INFO, "Multiblock structure is invalid");
		else
		{
			_mMultiblockIsValid = true;
			notifyTEComponents(pWorld, tMasterBlock);
			FMLLog.log(Level.INFO, "Multiblock structure is valid");
		}
	}

	
	/**
	 * Notify all TE components of our structure that we are the master block,
	 * and the structure is valid now, so they can begin to do their work 
	 */
	private void notifyTEComponents(World pWorld, BlockPoswID pMasterBlock)
	{
		for (Entry<String, BlockPoswID> tBlockMap : scannedBlocks.entrySet())
		{
			BlockPoswID tBlock = tBlockMap.getValue();
			
			switch (tBlock.blockType)
			{
				case AIR:
				case CAPACITORELEMENT:
				case CAPACITORELEMENT_VISIBLE:
				case FRAME:
				case GLASS:
				case CONTROLLER:
				case INVALID:
					break;
				default:
					break;
				
				case INPUT:
				case LASERLINK:
				case OUTPUT:
				case REDSTONE:
					TileEntity tTE = pWorld.getTileEntity(tBlock.x, tBlock.y, tBlock.z);
					if (tTE != null && tTE instanceof IMultiBlockComponent)
						((IMultiBlockComponent)tTE).updateMBStruct(true, pMasterBlock);
					break;
			}
		}
	}
	
	/**
	 * Read our MultiBlock state from NBT, so we skip the rescan of our structure
	 * @param pCompound
	 */
	@Override
	public void readFromNBT(NBTTagCompound pCompound)
	{
		super.readFromNBT(pCompound);
		
		scannedBlocks = new HashMap<String, BlockPosHelper.BlockPoswID>();
		
		NBTTagList tBlocks = pCompound.getTagList("scannedBlocks", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tBlocks.tagCount(); i++)
		{
			NBTTagCompound tSubComp = tBlocks.getCompoundTagAt(i);
			BlockPoswID tBlock = new BlockPoswID(tSubComp);
			scannedBlocks.put(tBlock.blockID, tBlock);
		}
			
		FMLLog.log(Level.INFO, "%d Blocks loaded from NBT", scannedBlocks.size());
	}
	
	/**
	 * Save all scanned blocks to NBT
	 * @param pCompound
	 */
	@Override
	public void writeToNBT(NBTTagCompound pCompound)
	{
		super.writeToNBT(pCompound);

		// Only store structure if it is valid
		if (!_mMultiblockIsValid)
			return;
		
		NBTTagList tBlocks = new NBTTagList();
		for (Entry<String, BlockPoswID> tBlockMap : scannedBlocks.entrySet())
			tBlocks.appendTag(tBlockMap.getValue().getTagCompound());

		pCompound.setTag("scannedBlocks", tBlocks);
		FMLLog.log(Level.INFO, "%d Blocks written to NBT", tBlocks.tagCount());
	}
	
	/**
	 * Perform random Block-Checks on the structure
	 * @param pWorld
	 * @return
	 */
	protected boolean randomCheckStructure(World pWorld)
	{
		long tCurrentMilis = System.currentTimeMillis();
		boolean tFlag = false;
		int tNumBlocks = scannedBlocks.size(); // Number of all scanned and validated blocks
		int randomAmount = (int) Math.min(20, Math.max(1, Math.floor(tNumBlocks / 4))); // check at least 1, max 20 blocks on a random scan

		ArrayList<Integer> idsToScan = new ArrayList<Integer>();

		// Do maximum 25 cycles. Should prevent problems with small builds
		int tLoopWatch = 0;
		do
		{
			int tRndID = _mRnd.nextInt(randomAmount);
			if (!idsToScan.contains(tRndID))
				idsToScan.add(tRndID);
			tLoopWatch++;
				
		} while(idsToScan.size() < randomAmount && tLoopWatch < 25);
		
		/*if (tCurrentMilis - _mLastRandomScan > 10000)
		{
			// Some time has passed; Maybe we need to do a full scan of all blocks?
			// All TE will invalidate the MB Struct if broken, so should not be necessary
		}
		else
		{
			// Do a quick scan of a few blocks and check if they are still there
			
		}*/
		for (int idx : idsToScan)
		{
			// The Block in our ScannedBlocks List to verify
			BlockPoswID tStoredPos = (BlockPoswID) scannedBlocks.values().toArray()[idx];
			BlockPoswID tPosToVerify = new BlockPoswID(tStoredPos.x, tStoredPos.y, tStoredPos.z);
			
			Block tWorldBlock = pWorld.getBlock(tStoredPos.x, tStoredPos.y, tStoredPos.z);
			tPosToVerify.meta = tWorldBlock.getDamageValue(pWorld, tStoredPos.x, tStoredPos.y, tStoredPos.z);
			
			if (getBlockTypeFromBlock(tWorldBlock, tPosToVerify) != tStoredPos.blockType)
			{
				tFlag = false; // Block at position has changed; Assume the MB is invalid
				break;
			}
			
		}

		return tFlag;
	}

	/**
	 * Check if the structure is valid by comparing min/max values with the found blocks
	 * @param pScannedBlocks
	 * @return
	 */
	protected boolean checkForValidStructure(Map<String, BlockPoswID> pScannedBlocks) {
		boolean tResult = true;
		BlockTypeCount bc = new BlockTypeCount<GTSU_BlockType>();

		for (Entry<String, BlockPoswID> tBlockMap : pScannedBlocks.entrySet())
			bc.Increment(tBlockMap.getValue().blockType);		

		for (GTSU_BlockType tbt: GTSU_BlockType.values())
		{
			kvMinMax mm = BlockPosHelper.getMinMaxValueForType(tbt);
			int num = bc.GetNumber(tbt);
			if (num >= mm.Min && num <= mm.Max)
				continue;
			else
			{
				tResult = false;
				FMLLog.log(Level.WARN, "Structure is invalid");
				break;
			}
		}

		return tResult;
	}

	/**
	 * Helper class to keep track of the amount of blocks based on an enum 
	 */
	private static class BlockTypeCount<T>
	{
		private Map<T, Integer> mElements = null;
		public BlockTypeCount()
		{
			mElements = new HashMap<T, Integer>();
		}

		public int GetNumber(T pType)
		{
			if (mElements.containsKey(pType))
				return mElements.get(pType);
			else
				return 0;
		}

		public void Increment(T pType)
		{
			int tOldVal = GetNumber(pType);
			mElements.put(pType, tOldVal + 1);
		}

		public void Decrement(T pType)
		{
			int tOldVal = GetNumber(pType);
			if (tOldVal <= 0)
				return;
			mElements.put(pType, tOldVal - 1);
		}
	}

	/**
	 * Check if the given pCurrentBlock is in a valid position for this MultiBlock structure
	 * @param pWorld
	 * @param pCurrentBlock
	 * @param pBlockType
	 * @return
	 */
	private boolean isBlockInValidPosition(World pWorld, BlockPoswID pBlockPos) 
	{
		boolean tResult = false;

		BlockTypeCount bc = new BlockTypeCount<GTSU_BlockType>();
		for (ForgeDirection pDir : ForgeDirection.VALID_DIRECTIONS)
		{
			Block tAdjBlock = BlockPosHelper.getAdjacentBlockForPos(pWorld, pBlockPos.x, pBlockPos.y, pBlockPos.z, pDir);
			BlockPoswID tAdjBlockPos = new BlockPoswID(pBlockPos.x + pDir.offsetX, pBlockPos.y + pDir.offsetY, pBlockPos.z + pDir.offsetZ);
			tAdjBlockPos.meta = tAdjBlock.getDamageValue(pWorld, tAdjBlockPos.x, tAdjBlockPos.y, tAdjBlockPos.z);
			tAdjBlockPos.blockType = getBlockTypeFromBlock(tAdjBlock, tAdjBlockPos);

			bc.Increment(tAdjBlockPos.blockType);
		}

		/*
		 *   GLASS: Not checked; Allowed to all blocks. Must have at least one AIR
			  FRAME: Connection allowed to all blocks except CAPACITORELEMENT. Must have at least two AIR
			  INPUT: Not checked; Allowed to all blocks,  Must have at least one AIR
			  OUTPUT: Not checked; Allowed to all blocks,  Must have at least one AIR
			  REDSTONE, Not checked; Allowed to all blocks,  Must have at least one AIR
			  CONTROLLER, Not checked; Allowed to all blocks,  Must have at least one AIR
			  LASERLINK, Not checked; Allowed to all blocks,  Must have at least one AIR
			  CAPACITORELEMENT Connection allowed to all blocks except FRAME, AIR
		 */


		switch (pBlockPos.blockType)
		{
		case CAPACITORELEMENT:
		case CAPACITORELEMENT_VISIBLE:
			if (bc.GetNumber(GTSU_BlockType.AIR) == 0 && bc.GetNumber(GTSU_BlockType.FRAME) == 0)
				tResult = true;
			break;

		case CONTROLLER:
			if (bc.GetNumber(GTSU_BlockType.AIR) > 0)
				tResult = true;
			break;

		case FRAME:
			if (bc.GetNumber(GTSU_BlockType.CAPACITORELEMENT) == 0 && bc.GetNumber(GTSU_BlockType.AIR) >= 2)
				return true;
			break;

		case GLASS:
			if (bc.GetNumber(GTSU_BlockType.AIR) > 0)
				tResult = true;
			break;

		case INPUT:
			if (bc.GetNumber(GTSU_BlockType.AIR) > 0)
				tResult = true;
			break;

		case INVALID:
			break;

		case LASERLINK:
			if (bc.GetNumber(GTSU_BlockType.AIR) > 0)
				tResult = true;
			break;

		case OUTPUT:
			if (bc.GetNumber(GTSU_BlockType.AIR) > 0)
				tResult = true;
			break;

		case REDSTONE:
			if (bc.GetNumber(GTSU_BlockType.AIR) > 0)
				tResult = true;
			break;

		case AIR:
			break;
		}
		return false;
	}

	/** Detect the type of the given Block
	 * @param pBlock
	 * @param pBlockMeta
	 * @return
	 */
	public GTSU_BlockType getBlockTypeFromBlock(Block pBlock, BlockPoswID pBlockPos)
	{
		GTSU_BlockType tRet = GTSU_BlockType.INVALID;

		if (pBlock instanceof MultiBlocks)
		{
			if (pBlockPos.meta == MultiBlocks.Meta_Glass)
				tRet = GTSU_BlockType.GLASS;
			else if (pBlockPos.meta == MultiBlocks.Meta_Housing)
				tRet = GTSU_BlockType.FRAME;
		}
		else if (pBlock instanceof CoreBlock)
			tRet = GTSU_BlockType.CAPACITORELEMENT;

		else if (pBlock instanceof ControllerBlock)
			tRet = GTSU_BlockType.CONTROLLER;


		return tRet;
	}
}
