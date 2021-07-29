package io.github.noeppi_noeppi.mods.bongo.task;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.render.ClientTickHandler;
import io.github.noeppi_noeppi.libx.render.RenderHelperItem;
import io.github.noeppi_noeppi.libx.util.Misc;
import io.github.noeppi_noeppi.mods.bongo.util.TagWithCount;
import io.github.noeppi_noeppi.mods.bongo.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TaskTypeTag implements TaskTypeSimple<TagWithCount> {

    public static final TaskTypeTag INSTANCE = new TaskTypeTag();

    private TaskTypeTag() {

    }

    @Override
    public Class<TagWithCount> getTaskClass() {
        return TagWithCount.class;
    }

    @Override
    public String getId() {
        return "bongo.tag";
    }

    @Override
    public String getTranslationKey() {
        return "bongo.task.item.name";
    }

    @Override
    public void renderSlot(Minecraft mc, PoseStack poseStack, MultiBufferSource buffer) {
        GuiComponent.blit(poseStack, 0, 0, 0, 0, 18, 18, 256, 256);
    }

    @Override
    public void renderSlotContent(Minecraft mc, TagWithCount content, PoseStack poseStack, MultiBufferSource buffer, boolean bigBongo) {
        ItemStack stack = cycle(content);
        RenderHelperItem.renderItemGui(poseStack, buffer, stack == null ? new ItemStack(Items.BARRIER) : stack, 0, 0, 16, !bigBongo);
    }

    @Override
    public String getTranslatedContentName(TagWithCount content) {
        ItemStack stack = cycle(content);
        if (stack == null) {
            return I18n.get("bongo.task.tag.empty");
        } else {
            String text = stack.getHoverName().getString(16);
            if (content.getCount() > 1) {
                text += (" x " + content.getCount());
            }
            return text;
        }
    }

    @Override
    public Component getContentName(TagWithCount content, MinecraftServer server) {
        return new TextComponent(content.getId().toString());
    }

    @Override
    public boolean shouldComplete(TagWithCount element, Player player, TagWithCount compare) {
        return element.getId().equals(compare.getId()) && element.getCount() <= compare.getCount();
    }

    @Override
    public void consumeItem(TagWithCount element, Player player) {
        Util.removeItems(player, element.getCount(), stack -> element.getTag().contains(stack.getItem()));
    }

    @Override
    public Predicate<ItemStack> bongoTooltipStack(TagWithCount element) {
        return stack -> element.getTag().contains(stack.getItem());
    }

    @Override
    public Set<ItemStack> bookmarkStacks(TagWithCount element) {
        //noinspection UnstableApiUsage
        return element.getTag().getValues().stream().map(ItemStack::new).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public CompoundTag serializeNBT(TagWithCount element) {
        return element.serialize();
    }

    @Override
    public TagWithCount deserializeNBT(CompoundTag nbt) {
        return TagWithCount.deserialize(nbt);
    }

    @Override
    public void validate(TagWithCount element, MinecraftServer server) {
        if (!element.getId().equals(Misc.MISSIGNO) && element.getTag().getValues().isEmpty()) {
            throw new IllegalStateException("Empty or unknown tag: " + element.getId());
        }
    }

    @Override
    public TagWithCount copy(TagWithCount element) {
        return element.copy();
    }

    @Nullable
    @Override
    public Comparator<TagWithCount> getSorting() {
        return Comparator.comparing(TagWithCount::getId, Util.COMPARE_RESOURCE)
                .thenComparingInt(TagWithCount::getCount);
    }

    @Override
    public Stream<TagWithCount> getAllElements(MinecraftServer server, @Nullable ServerPlayer player) {
        if (player == null) {
            return ItemTags.getAllTags().getAvailableTags().stream()
                    .map(id -> new TagWithCount(id, 1));
        } else {
            return player.getInventory().items.stream()
                    .filter(stack -> !stack.isEmpty())
                    .flatMap(stack -> ItemTags.getAllTags().getMatchingTags(stack.getItem()).stream()
                            .map(rl -> new TagWithCount(rl, stack.getCount()))
                    );
        }
    }

    @Nullable
    private static ItemStack cycle(TagWithCount element) {
        List<Item> items = element.getTag().getValues();
        if (items.isEmpty()) {
            return null;
        } else {
            ItemStack stack = new ItemStack(items.get((ClientTickHandler.ticksInGame / 20) % items.size()));
            stack.setCount(element.getCount());
            return stack;
        }
    }
}