package org.crafterscr.crafterscobblemonnpc;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CraftersCobblemonNpc.MOD_ID)
public final class CraftersCobblemonNpc {

    public static final String MOD_ID = "crafterscobblemonnpc";

    public static final Logger LOGGER = LoggerFactory.getLogger("Crafters Cobblemon Npc");

    public CraftersCobblemonNpc() {
        LOGGER.info("Crafters Cobblemon Npc ha sido cargado.");
    }
}