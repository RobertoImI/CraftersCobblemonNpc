package org.crafterscr.crafterscobblemonnpc.manager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.crafterscr.crafterscobblemonnpc.cobblemon.CobblemonNpcConfigurer;
import org.crafterscr.crafterscobblemonnpc.util.NpcDataKeys;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class DecorativeNpcManager {

    private DecorativeNpcManager() {
    }

    public static boolean isDecorativeNpc(Entity entity) {
        return CobblemonNpcConfigurer.isCobblemonPokemon(entity)
                && entity.getPersistentData()
                .getBoolean(NpcDataKeys.DECORATIVE);
    }

    public static Optional<Entity> findNearest(
            Entity origin,
            double radius
    ) {
        AABB searchArea =
                origin.getBoundingBox().inflate(radius);

        List<Entity> candidates =
                origin.level().getEntities(
                        origin,
                        searchArea,
                        DecorativeNpcManager::isDecorativeNpc
                );

        return candidates.stream()
                .min(
                        Comparator.comparingDouble(
                                origin::distanceToSqr
                        )
                );
    }

    public static Optional<Entity> findById(
            MinecraftServer server,
            String npcId
    ) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!isDecorativeNpc(entity)) {
                    continue;
                }

                String currentId =
                        entity.getPersistentData()
                                .getString(NpcDataKeys.NPC_ID);

                if (currentId.equalsIgnoreCase(npcId)) {
                    return Optional.of(entity);
                }
            }
        }

        return Optional.empty();
    }

    public static boolean idExists(
            MinecraftServer server,
            String npcId
    ) {
        return findById(server, npcId).isPresent();
    }

    public static List<String> getAllIds(
            MinecraftServer server
    ) {
        List<String> ids = new ArrayList<>();

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!isDecorativeNpc(entity)) {
                    continue;
                }

                String id =
                        entity.getPersistentData()
                                .getString(NpcDataKeys.NPC_ID);

                if (!id.isBlank()) {
                    ids.add(id);
                }
            }
        }

        ids.sort(
                Comparator.comparing(
                        value -> value.toLowerCase(Locale.ROOT)
                )
        );

        return ids;
    }

    public static List<Entity> findAll(
            ServerLevel level
    ) {
        List<Entity> result = new ArrayList<>();

        for (Entity entity : level.getAllEntities()) {
            if (isDecorativeNpc(entity)) {
                result.add(entity);
            }
        }

        return result;
    }

    public static void updateAnchor(Entity entity) {
        CompoundTag data = entity.getPersistentData();

        data.putDouble(
                NpcDataKeys.ANCHOR_X,
                entity.getX()
        );

        data.putDouble(
                NpcDataKeys.ANCHOR_Y,
                entity.getY()
        );

        data.putDouble(
                NpcDataKeys.ANCHOR_Z,
                entity.getZ()
        );

        data.putFloat(
                NpcDataKeys.ANCHOR_YAW,
                entity.getYRot()
        );

        data.putFloat(
                NpcDataKeys.ANCHOR_PITCH,
                entity.getXRot()
        );
    }
}