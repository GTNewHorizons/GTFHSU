package eu.usrv.gtsu.gui;

import ic2.core.block.wiring.TileEntityElectricBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import eu.usrv.gtsu.container.ContainerGTSU;
import eu.usrv.gtsu.tileentity.TileEntityGTSU;
public class GuiHandler implements IGuiHandler{

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity entity = world.getTileEntity(x, y, z);
		if(entity instanceof TileEntityGTSU) {
			return new ContainerGTSU(player, (TileEntityGTSU)entity);
		}
		return null;
	}

	@Override
    @SideOnly(Side.CLIENT)
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity entity = world.getTileEntity(x, y, z);
		if(entity instanceof TileEntityGTSU) {return new GuiGTSU(ID, new ContainerGTSU(player, (TileEntityGTSU)entity));}
		return null;
	}

}
