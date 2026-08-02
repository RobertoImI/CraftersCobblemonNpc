package org.crafterscr.crafterscobblemonnpc.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.crafterscr.crafterscobblemonnpc.manager.DecorativeNpcManager;

public final class NpcDamageEvents {

    private NpcDamageEvents() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(
            LivingIncomingDamageEvent event
    ) {
        if (DecorativeNpcManager.isDecorativeNpc(
                event.getEntity()
        )) {
            event.setCanceled(true);
        }
    }
}