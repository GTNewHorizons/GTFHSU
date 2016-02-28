package eu.usrv.gtsu.gui;

import ic2.core.GuiIconButton;
import ic2.core.IC2;
import ic2.core.util.GuiTooltipHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import eu.usrv.gtsu.NumberPrettifier;
import eu.usrv.gtsu.container.ContainerGTSU;

@SideOnly(Side.CLIENT)
public class GuiGTSU extends GuiContainer {

	private final ContainerGTSU container;
	private final String armorInv;
	private final String level;
	private final String name;
	private static final ResourceLocation BACKGROUND = new ResourceLocation("gtsu:textures/gui/gui_gtsu.png");

	public GuiGTSU(int ID, ContainerGTSU container) {
		super(container);
		
		this.xSize = 176;
		this.ySize = 166;
	    this.container = container;
	    this.armorInv = StatCollector.translateToLocal("ic2.EUStorage.gui.info.armor");
	    this.level = StatCollector.translateToLocal("ic2.EUStorage.gui.info.level");
		this.name = StatCollector.translateToLocal(String.format("tile.GTSU_Tier_%d.name", container.base.mTier));
	}

	@Override
	public void initGui(){
	    super.initGui();
	    this.buttonList.add(new GuiIconButton(0,
	    		(this.width - this.xSize) / 2 + 10, 
	    		(this.height - this.ySize) / 2 + 7, 
	    		20, 20, new ItemStack(Items.redstone), 
	    		true));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		fontRendererObj.drawString(this.name, ((this.xSize - this.fontRendererObj.getStringWidth(this.name))-20) / 2, 8, 16448255);

	    fontRendererObj.drawString(this.level, 16, 29, 16448255);
	    long energy = Math.min((this.container.base).mEnergy, (container.base).mMaxStorage);
	    fontRendererObj.drawString(" " + NumberPrettifier.getPrettifiedNumber(energy), 37, 39, 16448255);
	    fontRendererObj.drawString("/" + NumberPrettifier.getPrettifiedNumber((container.base).mMaxStorage), 37, 49, 16448255);

        String output = StatCollector.translateToLocalFormatted("ic2.EUStorage.gui.info.output", container.base.mOutput);
	    fontRendererObj.drawString(output, 26, 60, 16448255);
	    
	    GuiTooltipHelper.drawAreaTooltip(par1 - this.guiLeft, par2 - this.guiTop, (container.base).getredstoneMode(), 10, 7, 29, 26);
	  }

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y){
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	    this.mc.getTextureManager().bindTexture(GuiGTSU.BACKGROUND);
	    int j = (this.width - this.xSize) / 2; //Good here
	    int k = (this.height - this.ySize) / 2; //good here
	    this.drawTexturedModalRect(j, k, 0, 0, this.xSize, this.ySize);//Renders the actual gui texture.
	    if (container.base.mEnergy > 0L) {
	    	
	    	int i1 = container.base.getChargePercentage();
	    	
	    	int i2 = new Double((116.0D/100.0D) * (double)i1).intValue();
	    	//FMLLog.log(Level.INFO, "ChargePerc: %d | %d", i1, i2);
	     	drawTexturedModalRect(j + 8, k + 73, 0, 251, i2, 5);//Renders the blue energy bar.
	    }
    }

    @Override
	protected void actionPerformed(GuiButton guibutton){
		super.actionPerformed(guibutton);
        if (guibutton.id == 0) IC2.network.get().initiateClientTileEntityEvent(container.base, 0);
    }
}
