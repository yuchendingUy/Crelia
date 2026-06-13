/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask.Type;
import net.minecraft.world.entity.player.Player;

// Crelia: server-side stub of NeoForge's client payload context.
public record ClientPayloadContext(ClientCommonPacketListener listener, ResourceLocation payloadId) implements IPayloadContext {
    @Override
    public void handle(CustomPacketPayload payload) {
        handle(new ClientboundCustomPayloadPacket(payload));
    }

    @Override
    public CompletableFuture<Void> enqueueWork(Runnable task) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void finishCurrentTask(Type type) {
        throw new UnsupportedOperationException("Attempted to complete a configuration task on the client.");
    }

    @Override
    public PacketFlow flow() {
        return PacketFlow.CLIENTBOUND;
    }

    @Override
    public Player player() {
        throw new UnsupportedOperationException("No client player on a server-only build.");
    }
}
