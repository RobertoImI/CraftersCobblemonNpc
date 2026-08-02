package org.crafterscr.crafterscobblemonnpc.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.crafterscr.crafterscobblemonnpc.manager.DecorativeNpcManager;

import java.util.concurrent.CompletableFuture;

public final class NpcIdSuggestionProvider {

    private NpcIdSuggestionProvider() {
    }

    public static CompletableFuture<Suggestions> suggest(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        String remaining =
                builder.getRemainingLowerCase();

        for (String id :
                DecorativeNpcManager.getAllIds(
                        context.getSource().getServer()
                )) {

            if (id.toLowerCase().startsWith(remaining)) {
                builder.suggest(id);
            }
        }

        return builder.buildFuture();
    }
}