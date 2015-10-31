package eu.usrv.gtsu.multiblock.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import eu.usrv.gtsu.blocks.BlockGT5EnergyUnit;
import eu.usrv.gtsu.blocks.BlockMBController;
import eu.usrv.gtsu.blocks.CoreBlock;
import eu.usrv.gtsu.multiblock.BlockPosHelper;
import eu.usrv.gtsu.multiblock.BlockPosHelper.BlockPoswID;
import eu.usrv.gtsu.multiblock.BlockPosHelper.GTSU_BlockType;
import eu.usrv.gtsu.multiblock.BlockPosHelper.MB_BlockState;
import eu.usrv.gtsu.multiblock.BlockPosHelper.kvMinMax;
import eu.usrv.gtsu.multiblock.IMultiBlockComponent;
import eu.usrv.gtsu.multiblock.MultiBlocks;

public class MultiBlockStructManager
{
	private Map<String, BlockPoswID> tBlocksToScan = new HashMap<String, BlockPoswID>();
	private Map<String, BlockPoswID> newBlocksToScan = new HashMap<String, BlockPoswID>();
	private Map<String, BlockPoswID> scannedBlocks = new HashMap<String, BlockPoswID>();
	private boolean _mMultiblockIsValid;
	private long _mLastRandomScan;
	private Random _mRnd;
	private long lastMBScan;
	private int mbInstanceID;

	private MB_BlockState _mMBState;
	private int[][] offSets = new int[][]
			{
			{1, 0, 0},
			{-1, 0, 0},
			{0, 1, 0},
			{0, -1, 0},
			{0, 0, 1},
			{0, 0, -1} 
			};

	public MultiBlockStructManager(int pX, int pY, int pZ)
	{
		tBlocksToScan.put("notset", new BlockPoswID(pX, pY, pZ));

		_mRnd = new Random(System.currentTimeMillis());
		mbInstanceID = _mRnd.nextInt(Integer.MAX_VALUE);
		_mMultiblockIsValid = false;
	}

	public Map<String, BlockPoswID> getMBStruct()
	{
		return Collections.unmodifiableMap(scannedBlocks);
	}


	/** Scan the given block and all his adjacent blocks; if they are valid, continue to scan those adjacent, 
	 * until no more valid blocks could be found
	 * TODO: Make this a runnable
	 * @param pWorld
	 */
	public boolean scanMultiblockStructure(World pWorld)
	{
		long start = System.currentTimeMillis();
		if (start - lastMBScan < 1000) // Prevent MultiBlock scan-spam
			return false;

		lastMBScan = start;
		BlockPoswID tMasterBlock = null;
		int tBlockScanCycle = 0;
		do
		{
			tBlockScanCycle++;
			GTSUMod.Logger.info("Multiblock scan; Run %d; Blocks to scan: %d", tBlockScanCycle, tBlocksToScan.size());
			for (Entry<String, BlockPoswID> bEs : tBlocksToScan.entrySet())
			{
				BlockPoswID b = bEs.getValue();
				// Get the Block in the world and its meta
				Block tCurrentBlock = pWorld.getBlock(b.x, b.y, b.z);

				// Probably an external injected Block, or our initial Block. Populate ID, Meta Values and blocktype now
				// For any other found block, these values are checked and set while scanning all adjacent blocks
				if (b.blockID.equals("notset"))
				{
					GTSUMod.Logger.info("Block info not set, probably our initial block");
					int tBlockMeta = tCurrentBlock.getDamageValue(pWorld, b.x, b.y, b.z);

					// Assign the Unique BlockID to this block, so not scan blocks twice.
					// This doesn't need the MetaVal, as it will only keep track of the position, not the blocktype
					String tBlockID = String.format("%d-%d-%d-%d", pWorld.provider.dimensionId, b.x, b.y, b.z);
					GTSUMod.Logger.info("BlockID is %s", tBlockID);

					b.blockID = tBlockID;
					b.meta = tBlockMeta;
					b.blockType = getBlockTypeFromBlock(tCurrentBlock, b);
					// TODO: What happens if this block is type invalid?
				}
				GTSUMod.Logger.info("Current Block is: %s", b.blockType);

				// Do we have a CapacitorElement? Check if it is visible to the player
				if (b.blockType == GTSU_BlockType.CAPACITORELEMENT)
				{
					GTSUMod.Logger.info("Checking if capacitor element is visible...");
					for (ForgeDirection pDir : ForgeDirection.VALID_DIRECTIONS)
					{
						Block tAdjBlock = BlockPosHelper.getAdjacentBlockForPos(pWorld, b.x, b.y, b.z, pDir);
						BlockPoswID tAdjBlockPos = new BlockPoswID(b.x + pDir.offsetX, b.y + pDir.offsetY, b.z + pDir.offsetZ);
						tAdjBlockPos.meta = tAdjBlock.getDamageValue(pWorld, tAdjBlockPos.x, tAdjBlockPos.y, tAdjBlockPos.z);
						tAdjBlockPos.blockType = getBlockTypeFromBlock(tAdjBlock, tAdjBlockPos);

						if (tAdjBlockPos.blockType == GTSU_BlockType.GLASS)
						{
							// We found a GlassBlock. Change the BlockType to Visible
							b.blockType = GTSU_BlockType.CAPACITORELEMENT_VISIBLE;
							GTSUMod.Logger.info("...it is");
							break;
						}
					}
				}
				b.validPosition = isBlockInValidPosition(pWorld, b); 
				GTSUMod.Logger.info("Block has a valid position: %b", b.validPosition);

				if (b.blockType == GTSU_BlockType.CONTROLLER)
				{
					GTSUMod.Logger.info("Block is our MasterBlock");
					tMasterBlock = b;
				}

				// Now we loop all offsets (adjacent blocks of our block "b") and check if that block needs to be scanned next turn
				for (int[] tNum : offSets)
				{
					int realX, realY, realZ;

					realX = b.x + tNum[0];
					realY = b.y + tNum[1];
					realZ = b.z + tNum[2];
					GTSUMod.Logger.info("Analysing adjacent Block at %d %d %d", realX, realY, realZ);
					Block tBlockInQuestion = pWorld.getBlock(realX, realY, realZ);

					BlockPoswID tBlockPos = new BlockPoswID(realX, realY, realZ);
					tBlockPos.meta = tBlockInQuestion.getDamageValue(pWorld, realX, realY, realZ);
					tBlockPos.blockID = String.format("%d-%d-%d-%d", pWorld.provider.dimensionId, tBlockPos.x, tBlockPos.y, tBlockPos.z);
					tBlockPos.blockType = getBlockTypeFromBlock(tBlockInQuestion, tBlockPos);

					if (tBlockPos.blockType == GTSU_BlockType.INVALID)
					{
						GTSUMod.Logger.info("Ignoring Block as its type is invalid");
						continue;
					}
					else
					{
						GTSUMod.Logger.info("Found a Multiblock component: %s", tBlockPos.blockType);
						// Make sure we only add those components to the scanlist we
						// haven't analyzed yet, or we found in this loop already, or are already set for a upcomming scan cycle
						if (!scannedBlocks.containsKey(tBlockPos.blockID) && !newBlocksToScan.containsKey(tBlockPos.blockID) && !tBlocksToScan.containsKey(tBlockPos.blockID))
						{
							//GTSUMod.Logger.info("Block enqueued for next scan cycle");	
							newBlocksToScan.put(tBlockPos.blockID, tBlockPos);
						}
						else
						{
							/*
							GTSUMod.Logger.info("Block not enqueued");
							GTSUMod.Logger.info("scannedBlocks   : %b", scannedBlocks.containsKey(tBlockPos.blockID));
							GTSUMod.Logger.info("newBlocksToScan : %b", newBlocksToScan.containsKey(tBlockPos.blockID));
							GTSUMod.Logger.info("tBlocksToScan   : %b", tBlocksToScan.containsKey(tBlockPos.blockID));
							 */
						}
					}
				}
				scannedBlocks.put(b.blockID, b);
			}

			// Now clear the main list and copy over the new populated list with blocks that need to be analyzed
			tBlocksToScan.clear();
			tBlocksToScan = newBlocksToScan;

			// Also prepare an empty list to add more "new blocks to scan"
			newBlocksToScan = new HashMap<String, BlockPoswID>();
		}
		while(tBlocksToScan.size() > 0); // Will loop as long as the List of found adjacent blocks contains at least one block

		long stop = System.currentTimeMillis();
		long totalTime = stop - start;

		GTSUMod.Logger.info("All Blocks have been analyzed; Found %d so far. Took %d ms", scannedBlocks.size(), totalTime);

		if (checkForValidStructure(scannedBlocks))
		{
			_mMultiblockIsValid = true;
			notifyTEComponents(pWorld, tMasterBlock);
			return true;
		}
		else
			return false;
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
	public void loadFromNBT(NBTTagCompound pCompound, World pWorldObject)
	{
		Map<String, BlockPosHelper.BlockPoswID> tLoadedBlocks = new HashMap<String, BlockPosHelper.BlockPoswID>();
		BlockPoswID tMasterBlock = null;

		NBTTagList tBlocks = pCompound.getTagList("scannedBlocks", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tBlocks.tagCount(); i++)
		{
			NBTTagCompound tSubComp = tBlocks.getCompoundTagAt(i);
			BlockPoswID tBlock = new BlockPoswID(tSubComp);
			if (tBlock.blockType == GTSU_BlockType.CONTROLLER)
			{
				if (tMasterBlock == null)
					tMasterBlock = tBlock;
				else
				{
					//Something weird happend. Don't continue to load as there can never be 2 masters
					tLoadedBlocks = null;
					break;
				}
			}
			tLoadedBlocks.put(tBlock.blockID, tBlock);
		}

		// Only continue if the loaded blocks didn't cancel, and we have a master block
		if (tLoadedBlocks != null && tMasterBlock != null)
		{
			// Now validate the structure we just loaded
			if (checkForValidStructure(tLoadedBlocks))
			{
				// The List from NBT is valid, now set the actual list of blocks to the one
				// we've loaded and trigger the validate process
				_mMultiblockIsValid = true;
				scannedBlocks = tLoadedBlocks;
				//notifyTEComponents(pWorldObject, tMasterBlock); // Does not work as the worldObject seems to be empty. Need to save the TE's on their own
			}
			// Something went wrong, the structure from NBT seems to be invalid.
			// Either something went wrong while saving, or we have somekind of world corruption
			// TODO: Check if we need to do something here
		}
	}

	/**
	 * Save all scanned blocks to NBT
	 * @param pCompound
	 */
	public void saveToNBT(NBTTagCompound pCompound)
	{
		// Only store structure if it is valid
		if (!_mMultiblockIsValid)
		{
			GTSUMod.Logger.info("Not saving structure, as it is invalid");
			return;
		}

		NBTTagList tBlocks = new NBTTagList();
		for (Entry<String, BlockPoswID> tBlockMap : scannedBlocks.entrySet())
			tBlocks.appendTag(tBlockMap.getValue().getTagCompound());

		pCompound.setTag("scannedBlocks", tBlocks);
		GTSUMod.Logger.info("%d Blocks written to NBT", tBlocks.tagCount());
	}

	/**
	 * Perform random Block-Checks on the structure
	 * @param pWorld
	 * @return
	 */
	private boolean randomCheckStructure(World pWorld)
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
	private static boolean checkForValidStructure(Map<String, BlockPoswID> pScannedBlocks) {
		boolean tResult = true;
		BlockTypeCount bc = new BlockTypeCount<GTSU_BlockType>();

		for (Entry<String, BlockPoswID> tBlockMap : pScannedBlocks.entrySet())
		{
			//FMLLog.info("= DUMP =: B[%s] X[%d] Y[%d] Z[%d]", tBlockMap.getValue().blockType, tBlockMap.getValue().x, tBlockMap.getValue().y, tBlockMap.getValue().z);
			bc.Increment(tBlockMap.getValue().blockType);
		}

		for (GTSU_BlockType tbt: GTSU_BlockType.values())
		{
			kvMinMax mm = BlockPosHelper.getMinMaxValueForType(tbt);
			int num = bc.GetNumber(tbt);

			// Check if we have at least .min. and .max. of that block.
			// Or if we have .min. and .max. is unlimited
			if ((num >= mm.Min && num <= mm.Max) || (num >= mm.Min && mm.Max == -1))
				continue;
			else
			{
				tResult = false;
				GTSUMod.Logger.info("Structure is invalid. Type %s: Found %d min %d max %d", tbt, num, mm.Min, mm.Max);

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
	private static boolean isBlockInValidPosition(World pWorld, BlockPoswID pBlockPos) 
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
	public static GTSU_BlockType getBlockTypeFromBlock(Block pBlock, BlockPoswID pBlockPos)
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

		else if (pBlock instanceof BlockMBController)
			tRet = GTSU_BlockType.CONTROLLER;

		else if (pBlock instanceof BlockGT5EnergyUnit)
		{
			if (pBlockPos.meta == BlockGT5EnergyUnit.ID_Acceptor)
				tRet = GTSU_BlockType.INPUT;
			else if (pBlockPos.meta == BlockGT5EnergyUnit.ID_Producer)
				tRet = GTSU_BlockType.OUTPUT;
		}

		return tRet;
	}

	public boolean isValidMultiBlock() {
		return _mMultiblockIsValid;
	}
}
