package io.github.noeppi_noeppi.mods.bongo.compat;

import io.github.noeppi_noeppi.mods.bongo.Bongo;
import io.github.noeppi_noeppi.mods.bongo.BongoMod;
import io.github.noeppi_noeppi.mods.bongo.util.ItemRenderUtil;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

@WailaPlugin
public class JadeIntegration implements IWailaPlugin, IComponentProvider {

    private static final JadeIntegration INSTANCE = new JadeIntegration();
    private static final ResourceLocation BONGO = BongoMod.getInstance().resource("bingo_items");

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(INSTANCE, TooltipPosition.TAIL, Block.class);
        registrar.addConfig(BONGO, true);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (config.get(BONGO)) {
            //noinspection ConstantConditions
            Bongo bongo = Bongo.get(Minecraft.getInstance().level);
            if (!bongo.active()) {
                return;
            }

            Block block = accessor.getBlock();
            if (bongo.isTooltipStack(new ItemStack(block))) {
                tooltip.add(ItemRenderUtil.REQUIRED_ITEM);
            }
        }
    }
}
