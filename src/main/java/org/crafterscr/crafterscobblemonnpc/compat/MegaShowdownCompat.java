package org.crafterscr.crafterscobblemonnpc.compat;

import net.neoforged.fml.ModList;

import java.util.List;
import java.util.Locale;

public final class MegaShowdownCompat {

    public static final String MOD_ID = "mega_showdown";

    private static final List<String> MEGA_FORMS =
            List.of(
                    "mega",
                    "mega_x",
                    "mega_y",
                    "mega_z"
            );

    private MegaShowdownCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static List<String> getMegaForms() {
        return MEGA_FORMS;
    }

    public static String buildPokemonProperties(
            String pokemon,
            String megaForm,
            boolean shiny
    ) {
        String species = pokemon == null
                ? ""
                : pokemon.trim();

        if (species.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes indicar un Pokémon."
            );
        }

        String normalizedForm = normalizeMegaForm(megaForm);

        StringBuilder properties =
                new StringBuilder(species);

        if (shiny) {
            properties.append(" shiny");
        }

        properties
                .append(" mega_evolution=")
                .append(normalizedForm);

        return properties.toString();
    }

    private static String normalizeMegaForm(
            String megaForm
    ) {
        String normalized =
                megaForm == null
                        ? ""
                        : megaForm
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (!MEGA_FORMS.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Forma Mega inválida. Usa: "
                            + String.join(", ", MEGA_FORMS)
            );
        }

        return normalized;
    }
}
