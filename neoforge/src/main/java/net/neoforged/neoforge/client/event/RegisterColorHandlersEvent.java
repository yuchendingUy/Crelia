/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

// Crelia: server-side stub of a NeoForge client-only type. The real class lives in NeoForge's
// client source, which a Folia/Paper (server-only) base does not have. Mods reference it from their
// client handlers; on a dedicated server it is either never loaded (Dist.CLIENT handlers) or only
// resolved as a bare type during subscriber registration, so an empty shell is enough. Never used at
// runtime on a server, so no behaviour and zero cost.
public class RegisterColorHandlersEvent extends Event implements IModBusEvent {
    public static class Item extends RegisterColorHandlersEvent {
        public void register(ItemColor color, net.minecraft.world.level.ItemLike... items) {}
    }

    public static class Block extends RegisterColorHandlersEvent {
        public void register(BlockColor color, net.minecraft.world.level.block.Block... blocks) {}
    }

    @FunctionalInterface
    public interface ItemColor {
        int getColor(net.minecraft.world.item.ItemStack stack, int layer);
    }

    @FunctionalInterface
    public interface BlockColor {
        int getColor(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.core.BlockPos pos, int tintIndex);
    }
}
