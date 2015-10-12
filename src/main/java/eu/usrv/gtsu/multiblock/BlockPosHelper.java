package eu.usrv.gtsu.multiblock;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPosHelper {
	public enum MB_BlockState
	{
		INVALID,
		SLAVE,
		MASTER
	}

	public enum GTSU_BlockType
	{
		INVALID,
		GLASS,
		FRAME,
		INPUT,
		OUTPUT,
		REDSTONE,
		CONTROLLER,
		LASERLINK,
		CAPACITORELEMENT,
		AIR
	}

	public static class BlockPoswID
	{
		public int x;
		public int y;
		public int z;
		public int meta;
		public String blockID;
		public GTSU_BlockType blockType;
		public boolean validPosition;

		public BlockPoswID(int px, int py, int pz)
		{
			x=px;
			y=py;
			z=pz;
			blockID= "";
			meta = 0;
			validPosition = false;
			blockType = GTSU_BlockType.INVALID;	
		}
	
		public BlockPoswID(int px, int py, int pz, String pblockID, GTSU_BlockType pblockType, boolean pValidPos)
		{
			x=px;
			y=py;
			z=pz;
			blockID=pblockID;
			meta = 0;
			validPosition = pValidPos;
			blockType = pblockType;	
		}
	}
	
	public static Block getAdjacentBlockForPos(World pWorld, int pX, int pY, int pZ, ForgeDirection pDirection)
	{
		return pWorld.getBlock(pX + pDirection.offsetX, pY + pDirection.offsetY, pZ + pDirection.offsetZ);
	}
	
	public static class kvMinMax
	{
		public int Min = 0;
		public int Max = 0;
		public kvMinMax(int pMin, int pMax)
		{
			Min = pMin;
			Max = pMax;
		}
	}
	
	private static Map<GTSU_BlockType, kvMinMax> GTSU_MinMaxValues = null;
	
	public static kvMinMax getMinMaxValueForType(GTSU_BlockType pType)
	{
		kvMinMax tRet = new kvMinMax(0, 0);
		
		if (GTSU_MinMaxValues == null)
			InitMinMax();
		
		if (!GTSU_MinMaxValues.containsKey(pType))
			FMLLog.warning("MinMaxValue container does not contain any definition for GTSUBlockType %s", pType.toString());
		else
			tRet = GTSU_MinMaxValues.get(pType);
		
		return tRet;
	}
	
	private static void InitMinMax()
	{
		GTSU_MinMaxValues = new HashMap<GTSU_BlockType, kvMinMax>();
		GTSU_MinMaxValues.put(GTSU_BlockType.GLASS, new kvMinMax(1,-1));
		GTSU_MinMaxValues.put(GTSU_BlockType.FRAME, new kvMinMax(1,-1));
		GTSU_MinMaxValues.put(GTSU_BlockType.INPUT, new kvMinMax(1,-1));
		GTSU_MinMaxValues.put(GTSU_BlockType.OUTPUT, new kvMinMax(1,-1));
		GTSU_MinMaxValues.put(GTSU_BlockType.REDSTONE, new kvMinMax(0,-1));
		GTSU_MinMaxValues.put(GTSU_BlockType.CONTROLLER, new kvMinMax(1,1));
		GTSU_MinMaxValues.put(GTSU_BlockType.LASERLINK, new kvMinMax(0,-1));
		GTSU_MinMaxValues.put(GTSU_BlockType.CAPACITORELEMENT, new kvMinMax(1,-1));
	}
}
