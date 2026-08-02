package org.crafterscr.crafterscobblemonnpc;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.crafterscr.crafterscobblemonnpc.command.CobbleNpcCommand;
import org.crafterscr.crafterscobblemonnpc.event.NpcDamageEvents;
import org.crafterscr.crafterscobblemonnpc.event.NpcInteractionEvents;
import org.crafterscr.crafterscobblemonnpc.event.NpcTickEvents;

@Mod(CraftersCobblemonNpc.MOD_ID)
public final class CraftersCobblemonNpc {

    public static final String MOD_ID = "crafterscobblemonnpc";

    public CraftersCobblemonNpc(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(CobbleNpcCommand.class);
        NeoForge.EVENT_BUS.register(NpcDamageEvents.class);
        NeoForge.EVENT_BUS.register(NpcInteractionEvents.class);
        NeoForge.EVENT_BUS.register(NpcTickEvents.class);
    }
}