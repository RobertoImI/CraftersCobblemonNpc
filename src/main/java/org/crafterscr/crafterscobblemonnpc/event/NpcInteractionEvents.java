package org.crafterscr.crafterscobblemonnpc.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.crafterscr.crafterscobblemonnpc.manager.DecorativeNpcManager;
import org.crafterscr.crafterscobblemonnpc.util.NpcDataKeys;

import java.lang.reflect.Method;

public final class NpcInteractionEvents {

    private static final long INTERACTION_COOLDOWN_TICKS = 200L;

    private NpcInteractionEvents() {
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(
            PlayerInteractEvent.EntityInteractSpecific event
    ) {
        Entity target = event.getTarget();

        if (!DecorativeNpcManager.isDecorativeNpc(target)) {
            return;
        }

        /*
         * En el cliente no cancelamos, para permitir que Minecraft
         * envíe la interacción al servidor.
         */
        if (target.level().isClientSide()) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        handleInteraction(target, event.getHand());
    }

    @SubscribeEvent
    public static void onEntityInteract(
            PlayerInteractEvent.EntityInteract event
    ) {
        Entity target = event.getTarget();

        if (!DecorativeNpcManager.isDecorativeNpc(target)) {
            return;
        }

        if (target.level().isClientSide()) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        handleInteraction(target, event.getHand());
    }

    private static void handleInteraction(
            Entity target,
            InteractionHand hand
    ) {
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        CompoundTag data = target.getPersistentData();

        if (!data.getBoolean(NpcDataKeys.SOUND_ENABLED)) {
            return;
        }

        long currentTick = target.level().getGameTime();

        long nextAllowedTick = data.getLong(
                NpcDataKeys.NEXT_INTERACTION_TICK
        );

        if (currentTick < nextAllowedTick) {
            return;
        }

        data.putLong(
                NpcDataKeys.NEXT_INTERACTION_TICK,
                currentTick + INTERACTION_COOLDOWN_TICKS
        );

        /*
         * Sonido propio de la especie.
         */
        if (target instanceof Mob mob) {
            mob.playAmbientSound();
        }

        /*
         * También intenta reproducir la animación "cry"
         * que ofrece PokemonEntity.
         */
        try {
            Method cryMethod = target.getClass().getMethod("cry");
            cryMethod.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            /*
             * Si una versión de Cobblemon cambia este método,
             * el sonido seguirá funcionando.
             */
        }
    }
}