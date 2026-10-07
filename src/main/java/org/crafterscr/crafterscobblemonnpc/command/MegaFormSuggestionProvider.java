package org.crafterscr.crafterscobblemonnpc.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.crafterscr.crafterscobblemonnpc.compat.MegaShowdownCompat;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class MegaFormSuggestionProvider {

    private MegaFormSuggestionProvider() {
    }

    public static CompletableFuture<Suggestions> suggest(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        if (!MegaShowdownCompat.isLoaded()) {
            return builder.buildFuture();
        }

        String remaining =
                builder.getRemaining()
                        .toLowerCase(Locale.ROOT);

        for (String form :
                MegaShowdownCompat.getMegaForms()) {

            if (form.startsWith(remaining)) {
                builder.suggest(form);
            }
        }

        return builder.buildFuture();
    }
}
