package eu.usrv.gtsu.multiblock;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import eu.usrv.gtsu.tileentity.TileEntityGTSU;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MultiBlocks extends Block {

	private static class AdjacentBlocks
	{
		UniqueIdentifier Me;
		UniqueIdentifier AdjacentTop;
		UniqueIdentifier AdjacentBottom;
		UniqueIdentifier AdjacentNorth;
		UniqueIdentifier AdjacentSouth;
		UniqueIdentifier AdjacentEast;
		UniqueIdentifier AdjacentWest;

		public AdjacentBlocks()
		{
			Me = null;
			AdjacentTop = null;
			AdjacentBottom = null;
			AdjacentNorth = null;
			AdjacentSouth = null;
			AdjacentEast = null;
			AdjacentWest = null;	
		}

		public boolean isEqualBlock(ForgeDirection pDirection)
		{
			boolean tResult = false;

			switch(pDirection)
			{
			case DOWN:
				tResult = (AdjacentBottom != null) ? (AdjacentBottom.equals(Me)) : false;
				//FMLLog.info("Adjacent result: %s (%s | %s)", tResult, AdjacentBottom, Me);
				break;
			case EAST:
				tResult = (AdjacentEast != null) ? (AdjacentEast.equals(Me)) : false;
				//FMLLog.info("Adjacent result: %s (%s | %s)", tResult, AdjacentEast, Me);
				break;
			case NORTH:
				tResult = (AdjacentNorth != null) ? (AdjacentNorth.equals(Me)) : false;
				//FMLLog.info("Adjacent result: %s (%s | %s)",tResult, AdjacentNorth, Me);
				break;
			case SOUTH:
				tResult = (AdjacentSouth != null) ? (AdjacentSouth.equals(Me)) : false;
				//FMLLog.info("Adjacent result: %s (%s | %s)", tResult, AdjacentSouth, Me);
				break;
			case UNKNOWN:
				tResult = false;
				break;
			case UP:
				tResult = (AdjacentTop != null) ? (AdjacentTop.equals(Me)) : false;
				//FMLLog.info("Adjacent result: %s (%s | %s)", tResult, AdjacentTop, Me);
				break;
			case WEST:
				tResult = (AdjacentWest != null) ? (AdjacentWest.equals(Me)) : false;
				//FMLLog.info("Adjacent result: %s (%s | %s)", tResult, AdjacentWest, Me);
				break;

			}

			
			
			return tResult;
		}

		public void setAdjacent(ForgeDirection pDirection, Block pBlock)
		{
			if (pBlock != null)
			{
				UniqueIdentifier tUID = GameRegistry.findUniqueIdentifierFor(pBlock);
				if (tUID != null)
				{
					switch (pDirection)
					{
					case DOWN:
						AdjacentBottom = tUID;
						break;
					case EAST:
						AdjacentEast = tUID;
						break;
					case NORTH:
						AdjacentNorth = tUID;
						break;
					case SOUTH:
						AdjacentSouth = tUID;
						break;
					case UNKNOWN:
						FMLLog.log(Level.WARN, "Something tried to set an adjacent block with unknown direction");
						break;
					case UP:
						AdjacentTop = tUID;
						break;
					case WEST:
						AdjacentWest = tUID;
						break;
					}
				}
			}
		}

	}

	private AdjacentBlocks _mAdjacent;
	public static final String[] _mMultiBlockNames = {"CapacitorHousing", "CapacitorGlass", "CapacitorCore"};
	public static final String[] _mSides = {"single", "singleleft", "singleright", "singlecenter", "singlebottom", "bottomleft", "bottomright", "bottomcenter", "singletop", "topleft", "topright", "topcenter", "singlecenter2", "centerleft", "centerright", "centercenter"};
	
	public MultiBlocks() {
		super(Material.iron);
		this.setBlockName(String.format("GTSU_MultiBlock_Frame"));
		this.setCreativeTab(IC2.tabIC2);
		this.setHardness(1.5F);
		this.setStepSound(soundTypeMetal);
		_mAdjacent = new AdjacentBlocks();
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	};

	@Override
	public int damageDropped(int par1)
	{
		return par1;
	}

	@SideOnly(Side.CLIENT)
	protected IIcon icCore;
	protected IIcon[] icHousing;
	protected IIcon[] icGlass;
	protected IIcon icGlassTransparent;
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister pIReg)
	{
		icHousing = new IIcon[_mSides.length];
		icGlass = new IIcon[_mSides.length];

		icGlassTransparent = pIReg.registerIcon("GTSU:multiblock/glass_transparent");
		
		for (int i = 0; i < _mSides.length; i++)
		{
			icHousing[i] = pIReg.registerIcon(String.format("GTSU:multiblock/housing/housing_%s", _mSides[i]));
			icGlass[i] = pIReg.registerIcon(String.format("GTSU:multiblock/glass/glass_%s", _mSides[i]));
		}

		icCore = pIReg.registerIcon("GTSU:multiblock_core");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess pBlockAccess, int pX, int pY, int pZ, int pSide)
	{
		IIcon tRetval = null;
		
		if (_mAdjacent.Me == null)
		{
			Block tMe = pBlockAccess.getBlock(pX,pY, pZ);
			_mAdjacent.Me = GameRegistry.findUniqueIdentifierFor(tMe);
		}

		// SOUTH   +Z-   NORTH
		// EAST    +X-   WEST
		_mAdjacent.setAdjacent(ForgeDirection.DOWN, pBlockAccess.getBlock(pX, pY-1, pZ));
		_mAdjacent.setAdjacent(ForgeDirection.UP, pBlockAccess.getBlock(pX, pY+1, pZ));
		_mAdjacent.setAdjacent(ForgeDirection.EAST, pBlockAccess.getBlock(pX+1, pY, pZ));
		_mAdjacent.setAdjacent(ForgeDirection.WEST, pBlockAccess.getBlock(pX-1, pY, pZ));
		_mAdjacent.setAdjacent(ForgeDirection.SOUTH, pBlockAccess.getBlock(pX, pY, pZ+1));
		_mAdjacent.setAdjacent(ForgeDirection.NORTH, pBlockAccess.getBlock(pX, pY, pZ-1));
		
		int pBlockMeta = pBlockAccess.getBlockMetadata(pX, pY, pZ);
		int tTexOffset = 0;

		ForgeDirection tDir = ForgeDirection.getOrientation(pSide);
		if (_mAdjacent.isEqualBlock(tDir))
			tTexOffset = -1;
		else
		{
			if (tDir == ForgeDirection.UP || tDir == ForgeDirection.DOWN)
			{
				if (_mAdjacent.isEqualBlock(ForgeDirection.EAST)) tTexOffset += 1;
				if (_mAdjacent.isEqualBlock(ForgeDirection.WEST)) tTexOffset += 2;
				if (_mAdjacent.isEqualBlock(ForgeDirection.NORTH)) tTexOffset += (tDir == ForgeDirection.DOWN) ? 8 : 4;
				if (_mAdjacent.isEqualBlock(ForgeDirection.SOUTH)) tTexOffset += (tDir == ForgeDirection.DOWN) ? 4 : 8;
			}
			else if (tDir == ForgeDirection.NORTH || tDir == ForgeDirection.SOUTH)
			{
				if (_mAdjacent.isEqualBlock(ForgeDirection.UP)) tTexOffset += 4;
				if (_mAdjacent.isEqualBlock(ForgeDirection.DOWN)) tTexOffset += 8;
				if (_mAdjacent.isEqualBlock(ForgeDirection.EAST)) tTexOffset += (tDir == ForgeDirection.NORTH) ? 2 : 1;
				if (_mAdjacent.isEqualBlock(ForgeDirection.WEST)) tTexOffset += (tDir == ForgeDirection.NORTH) ? 1 : 2;
			}
			else if (tDir == ForgeDirection.EAST || tDir == ForgeDirection.WEST)
			{
				if (_mAdjacent.isEqualBlock(ForgeDirection.UP)) tTexOffset += 4;
				if (_mAdjacent.isEqualBlock(ForgeDirection.DOWN)) tTexOffset += 8;
				if (_mAdjacent.isEqualBlock(ForgeDirection.NORTH)) tTexOffset += (tDir == ForgeDirection.EAST) ? 1 : 2;
				if (_mAdjacent.isEqualBlock(ForgeDirection.SOUTH)) tTexOffset += (tDir == ForgeDirection.EAST) ? 2 : 1;
			}
		}

		switch (pBlockMeta)
		{
		case 0:
			if (tTexOffset == -1)
				tTexOffset = 0;
			
			tRetval = icHousing[tTexOffset];
			break;
			
		case 1:
			if (tTexOffset == -1)
				tRetval = icGlassTransparent;
			else
				tRetval = icGlass[tTexOffset];
			break;
			
		case 2:
			tRetval =  icCore;
			break;
			
		default:
			System.out.println(String.format("Invalid metadata (%d) for %s", pBlockMeta, this.getUnlocalizedName()));
			tRetval = icHousing[0];
		}
		return tRetval;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List stackList) 
	{
		for (int i = 0; i < _mMultiBlockNames.length; i++)
		{
			ItemStack zeroStack = new ItemStack(this, 1, i);
			stackList.add(zeroStack);
		}
	}

}
