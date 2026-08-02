package org.crafterscr.crafterscobblemonnpc.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class NpcMessages {

    private NpcMessages() {
    }

    public static Component success(String message) {
        return Component.literal("[CobbleNPC] ")
                .withStyle(ChatFormatting.GOLD)
                .append(
                        Component.literal(message)
                                .withStyle(ChatFormatting.GREEN)
                );
    }

    public static Component error(String message) {
        return Component.literal("[CobbleNPC] ")
                .withStyle(ChatFormatting.GOLD)
                .append(
                        Component.literal(message)
                                .withStyle(ChatFormatting.RED)
                );
    }

    public static Component info(String message) {
        return Component.literal("[CobbleNPC] ")
                .withStyle(ChatFormatting.GOLD)
                .append(
                        Component.literal(message)
                                .withStyle(ChatFormatting.YELLOW)
                );
    }
}