package org.crafterscr.crafterscobblemonnpc.cobblemon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class CobblemonPropertiesHelper {

    private static final String PROPERTIES_CLASS =
            "com.cobblemon.mod.common.api.pokemon.PokemonProperties";

    private CobblemonPropertiesHelper() {
    }

    public static Entity createPokemonEntity(
            ServerLevel level,
            String propertiesText
    ) throws ReflectiveOperationException {

        Class<?> propertiesClass = Class.forName(PROPERTIES_CLASS);

        Object properties = parseProperties(
                propertiesClass,
                propertiesText
        );

        if (properties == null) {
            throw new IllegalArgumentException(
                    "Cobblemon no pudo interpretar las propiedades."
            );
        }

        Entity entity = invokeCreateEntity(
                propertiesClass,
                properties,
                level
        );

        if (entity == null) {
            throw new IllegalStateException(
                    "Cobblemon no devolvió una entidad Pokémon."
            );
        }

        return entity;
    }

    private static Object parseProperties(
            Class<?> propertiesClass,
            String propertiesText
    ) throws ReflectiveOperationException {

        Object companion = getCompanion(propertiesClass);

        for (Method method : companion.getClass().getMethods()) {
            if (!method.getName().equals("parse")) {
                continue;
            }

            Class<?>[] parameters = method.getParameterTypes();

            if (parameters.length == 1
                    && parameters[0] == String.class) {

                return method.invoke(
                        companion,
                        propertiesText
                );
            }

            if (parameters.length == 2
                    && parameters[0] == String.class
                    && parameters[1] == String.class) {

                return method.invoke(
                        companion,
                        propertiesText,
                        " "
                );
            }
        }

        for (Method method : propertiesClass.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (!method.getName().equals("parse")) {
                continue;
            }

            Class<?>[] parameters = method.getParameterTypes();

            if (parameters.length == 1
                    && parameters[0] == String.class) {

                return method.invoke(
                        null,
                        propertiesText
                );
            }

            if (parameters.length == 2
                    && parameters[0] == String.class
                    && parameters[1] == String.class) {

                return method.invoke(
                        null,
                        propertiesText,
                        " "
                );
            }
        }

        throw new NoSuchMethodException(
                "No se encontró PokemonProperties.parse."
        );
    }

    private static Entity invokeCreateEntity(
            Class<?> propertiesClass,
            Object properties,
            ServerLevel level
    ) throws ReflectiveOperationException {

        for (Method method : propertiesClass.getMethods()) {
            if (!method.getName().equals("createEntity")) {
                continue;
            }

            Class<?>[] parameters = method.getParameterTypes();

            if (parameters.length == 1
                    && parameters[0].isAssignableFrom(level.getClass())) {

                Object result = method.invoke(
                        properties,
                        level
                );

                if (result instanceof Entity entity) {
                    return entity;
                }
            }

            if (parameters.length == 1
                    && parameters[0].isInstance(level)) {

                Object result = method.invoke(
                        properties,
                        level
                );

                if (result instanceof Entity entity) {
                    return entity;
                }
            }
        }

        /*
         * Algunas compilaciones de Kotlin exponen el método con
         * parámetros predeterminados mediante createEntity$default.
         */
        for (Method method : propertiesClass.getDeclaredMethods()) {
            if (!method.getName().equals("createEntity$default")) {
                continue;
            }

            method.setAccessible(true);

            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];

            for (int index = 0; index < parameterTypes.length; index++) {
                Class<?> type = parameterTypes[index];

                if (type.isInstance(properties)) {
                    arguments[index] = properties;
                } else if (type.isInstance(level)) {
                    arguments[index] = level;
                } else if (type == int.class) {
                    arguments[index] = 2;
                } else if (type == boolean.class) {
                    arguments[index] = false;
                } else {
                    arguments[index] = null;
                }
            }

            Object result = method.invoke(null, arguments);

            if (result instanceof Entity entity) {
                return entity;
            }
        }

        throw new NoSuchMethodException(
                "No se encontró PokemonProperties.createEntity."
        );
    }

    private static Object getCompanion(
            Class<?> propertiesClass
    ) throws ReflectiveOperationException {

        Field companionField =
                propertiesClass.getField("Companion");

        return companionField.get(null);
    }
}