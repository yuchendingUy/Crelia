/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

// Crelia: server-side stub of NeoForge's client attribute tooltip event.
public class AddAttributeTooltipsEvent extends Event {
    public AddAttributeTooltipsEvent(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx) {}
}
