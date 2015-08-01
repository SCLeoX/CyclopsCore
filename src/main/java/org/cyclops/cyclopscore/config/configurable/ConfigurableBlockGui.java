package org.cyclops.cyclopscore.config.configurable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;

/**
 * Block without a tile entity with a GUI that can hold ExtendedConfigs.
 * The container and GUI must be set inside the constructor of the extension.
 * @author rubensworks
 *
 */
public abstract class ConfigurableBlockGui extends ConfigurableBlock implements IGuiContainerProvider {

    private int guiID;

    /**
     * Make a new block instance.
     * @param eConfig Config for this blockState.
     * @param material Material of this blockState.
     */
    @SuppressWarnings({ "rawtypes" })
    public ConfigurableBlockGui(ExtendedConfig eConfig, Material material) {
        super(eConfig, material);
        this.hasGui = true;
        if(hasGui()) {
            this.guiID = Helpers.getNewId(eConfig.getMod(), Helpers.IDType.GUI);
        }
    }
    
    @Override
    public int getGuiID() {
        return this.guiID;
    }

    @Override
    public ModBase getMod() {
        return getConfig().getMod();
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityplayer, EnumFacing side, float par7, float par8, float par9) {
        super.onBlockActivated(world, blockPos, blockState, entityplayer, side, par7, par8, par9);

        // Drop through if the player is sneaking
        if (entityplayer.isSneaking()) {
            return false;
        }

        if (!world.isRemote && hasGui()) {
            entityplayer.openGui(getConfig().getMod(), getGuiID(), world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        return true;
    }

}