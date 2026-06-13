/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

// Crelia: server-side stub of a NeoForge client-only type. The real class lives in NeoForge's
// client source, which a Folia/Paper (server-only) base does not have. Mods reference it from their
// client handlers; on a dedicated server it is either never loaded (Dist.CLIENT handlers) or only
// resolved as a bare type during subscriber registration, so an empty shell is enough. Never used at
// runtime on a server, so no behaviour and zero cost.
public final class ClientHooks {
    public static String forgeStatusLine;

    private ClientHooks() {}

    public static void registerSpriteSourceTypes() {}

    public static void reloadRenderer() {}

    public static boolean isBlockInSolidLayer(net.minecraft.world.level.block.state.BlockState state) {
        return true;
    }

    public static <T> T resolveLookup(net.minecraft.resources.ResourceKey<T> key) {
        return null;
    }

    public static java.util.List<net.minecraft.world.inventory.RecipeBookType> getFilteredRecipeBookTypeValues() {
        return java.util.List.of();
    }
}
