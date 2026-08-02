package org.crafterscr.crafterscobblemonnpc.cobblemon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.crafterscr.crafterscobblemonnpc.util.NpcDataKeys;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CobblemonNpcConfigurer {

    private static final String POKEMON_ENTITY_CLASS =
            "com.cobblemon.mod.common.entity.pokemon.PokemonEntity";

    private CobblemonNpcConfigurer() {
    }

    public static void configure(
            Entity entity,
            String npcId,
            String speciesText,
            boolean soundEnabled
    ) {
        CompoundTag data = entity.getPersistentData();

        data.putBoolean(NpcDataKeys.DECORATIVE, true);
        data.putString(NpcDataKeys.NPC_ID, npcId);
        data.putString(NpcDataKeys.SPECIES, speciesText);

        data.putDouble(NpcDataKeys.ANCHOR_X, entity.getX());
        data.putDouble(NpcDataKeys.ANCHOR_Y, entity.getY());
        data.putDouble(NpcDataKeys.ANCHOR_Z, entity.getZ());

        data.putFloat(
                NpcDataKeys.ANCHOR_YAW,
                entity.getYRot()
        );

        data.putFloat(
                NpcDataKeys.ANCHOR_PITCH,
                entity.getXRot()
        );

        data.putBoolean(
                NpcDataKeys.SOUND_ENABLED,
                soundEnabled
        );

        data.putLong(
                NpcDataKeys.NEXT_INTERACTION_TICK,
                0L
        );

        entity.setInvulnerable(true);
        entity.setNoGravity(true);
        entity.noPhysics = true;
        entity.setSilent(!soundEnabled);
        entity.setCustomNameVisible(false);
        entity.setDeltaMovement(Vec3.ZERO);

        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            mob.setTarget(null);
        }

        applyCobblemonFlags(entity);
    }

    public static boolean isCobblemonPokemon(Entity entity) {
        Class<?> current = entity.getClass();

        while (current != null) {
            if (current.getName().equals(POKEMON_ENTITY_CLASS)) {
                return true;
            }

            current = current.getSuperclass();
        }

        return false;
    }

    private static void applyCobblemonFlags(Entity entity) {
        trySetBooleanMethod(entity, "setUnbattleable", true);
        trySetBooleanMethod(entity, "setHideLabel", true);
        trySetBooleanMethod(entity, "hideNameRendering", true);
        trySetBooleanMethod(entity, "setCountsTowardsSpawnCap", false);
        trySetBooleanMethod(entity, "setUncatchable", true);

        trySetSyncedBoolean(
                entity,
                "UNBATTLEABLE",
                true
        );

        trySetSyncedBoolean(
                entity,
                "HIDE_LABEL",
                true
        );
    }

    private static void trySetBooleanMethod(
            Entity entity,
            String methodName,
            boolean value
    ) {
        try {
            Method method = entity.getClass().getMethod(
                    methodName,
                    boolean.class
            );

            method.invoke(entity, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void trySetSyncedBoolean(
            Entity entity,
            String fieldName,
            boolean value
    ) {
        try {
            Field field = findField(
                    entity.getClass(),
                    fieldName
            );

            if (field == null) {
                return;
            }

            field.setAccessible(true);

            Object accessor = field.get(null);

            for (Method method
                    : entity.getEntityData()
                    .getClass()
                    .getMethods()) {

                if (!method.getName().equals("set")) {
                    continue;
                }

                if (method.getParameterCount() != 2) {
                    continue;
                }

                try {
                    method.invoke(
                            entity.getEntityData(),
                            accessor,
                            value
                    );

                    return;
                } catch (ReflectiveOperationException ignored) {
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static Field findField(
            Class<?> type,
            String fieldName
    ) {
        Class<?> current = type;

        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }

        return null;
    }
}