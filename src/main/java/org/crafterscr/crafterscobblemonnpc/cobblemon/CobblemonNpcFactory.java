package org.crafterscr.crafterscobblemonnpc.cobblemon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class CobblemonNpcFactory {

    private CobblemonNpcFactory() {
    }

    public static Entity create(
            ServerLevel level,
            String npcId,
            String properties,
            double x,
            double y,
            double z,
            float yaw
    ) throws ReflectiveOperationException {

        String finalProperties =
                addRequiredProperties(properties);

        Entity entity =
                CobblemonPropertiesHelper.createPokemonEntity(
                        level,
                        finalProperties
                );

        entity.moveTo(
                x,
                y,
                z,
                yaw,
                0.0F
        );

        entity.setYHeadRot(yaw);
        entity.setYBodyRot(yaw);

        CobblemonNpcConfigurer.configure(
                entity,
                npcId,
                properties,
                true
        );

        if (!level.addFreshEntity(entity)) {
            throw new IllegalStateException(
                    "Minecraft rechazó la entidad."
            );
        }

        return entity;
    }

    private static String addRequiredProperties(
            String properties
    ) {
        String result = properties.trim();

        if (!containsProperty(result, "uncatchable")) {
            result += " uncatchable";
        }

        return result.trim();
    }

    private static boolean containsProperty(
            String properties,
            String property
    ) {
        return properties
                .toLowerCase()
                .contains(property.toLowerCase());
    }
}