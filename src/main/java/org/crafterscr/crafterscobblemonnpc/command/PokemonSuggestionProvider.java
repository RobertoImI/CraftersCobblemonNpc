package org.crafterscr.crafterscobblemonnpc.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class PokemonSuggestionProvider {

    private static final String SPECIES_CLASS =
            "com.cobblemon.mod.common.api.pokemon.PokemonSpecies";

    private PokemonSuggestionProvider() {
    }

    public static CompletableFuture<Suggestions> suggest(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        String remaining =
                builder.getRemaining().toLowerCase();

        for (String species : getImplementedSpecies()) {
            if (species.toLowerCase().startsWith(remaining)) {
                builder.suggest(species);
            }
        }

        if ("shiny".startsWith(remaining)) {
            builder.suggest("shiny");
        }

        return builder.buildFuture();
    }

    public static List<String> getImplementedSpecies() {
        List<String> result = new ArrayList<>();

        try {
            Class<?> pokemonSpeciesClass =
                    Class.forName(SPECIES_CLASS);

            Object registryObject =
                    getRegistryObject(pokemonSpeciesClass);

            Collection<?> implemented =
                    findImplementedCollection(
                            pokemonSpeciesClass,
                            registryObject
                    );

            if (implemented == null) {
                return result;
            }

            for (Object species : implemented) {
                String identifier =
                        readSpeciesIdentifier(species);

                if (identifier != null
                        && !identifier.isBlank()) {

                    result.add(identifier);
                }
            }
        } catch (ReflectiveOperationException ignored) {
            return result;
        }

        result.sort(
                Comparator.comparing(String::toLowerCase)
        );

        return result;
    }

    private static Object getRegistryObject(
            Class<?> pokemonSpeciesClass
    ) throws ReflectiveOperationException {

        try {
            Field instance =
                    pokemonSpeciesClass.getField("INSTANCE");

            return instance.get(null);
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private static Collection<?> findImplementedCollection(
            Class<?> pokemonSpeciesClass,
            Object registryObject
    ) throws ReflectiveOperationException {

        if (registryObject != null) {
            for (Method method :
                    registryObject.getClass().getMethods()) {

                if (!method.getName().equals("getImplemented")) {
                    continue;
                }

                Object value =
                        method.invoke(registryObject);

                if (value instanceof Collection<?> collection) {
                    return collection;
                }
            }
        }

        for (Method method :
                pokemonSpeciesClass.getMethods()) {

            if (!method.getName().equals("getImplemented")) {
                continue;
            }

            Object value = method.invoke(null);

            if (value instanceof Collection<?> collection) {
                return collection;
            }
        }

        return null;
    }

    private static String readSpeciesIdentifier(
            Object species
    ) {
        try {
            Method method =
                    species.getClass().getMethod(
                            "getResourceIdentifier"
                    );

            Object identifier = method.invoke(species);

            if (identifier != null) {
                String value = identifier.toString();

                if (value.startsWith("cobblemon:")) {
                    return value.substring(
                            "cobblemon:".length()
                    );
                }

                return value;
            }
        } catch (ReflectiveOperationException ignored) {
            // Probar con el nombre interno.
        }

        try {
            Method method =
                    species.getClass().getMethod("getName");

            Object name = method.invoke(species);

            if (name != null) {
                return name.toString()
                        .toLowerCase()
                        .replace(" ", "_");
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }

        return null;
    }
}