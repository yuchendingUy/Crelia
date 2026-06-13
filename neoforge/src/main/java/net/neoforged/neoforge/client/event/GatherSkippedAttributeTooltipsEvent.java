/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

// Crelia: server-side stub of NeoForge's client attribute tooltip event.
public class GatherSkippedAttributeTooltipsEvent extends Event {
    private final Set<EquipmentSlotGroup> skipped = EnumSet.noneOf(EquipmentSlotGroup.class);

    public GatherSkippedAttributeTooltipsEvent(ItemStack stack, AttributeTooltipContext ctx) {}

    public boolean isSkippingAll() {
        return false;
    }

    public boolean isSkipped(EquipmentSlotGroup group) {
        return skipped.contains(group);
    }

    public boolean isSkipped(net.minecraft.resources.ResourceLocation id) {
        return false;
    }
}
