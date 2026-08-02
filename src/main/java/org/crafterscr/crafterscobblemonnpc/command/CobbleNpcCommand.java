package org.crafterscr.crafterscobblemonnpc.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.crafterscr.crafterscobblemonnpc.cobblemon.CobblemonNpcFactory;
import org.crafterscr.crafterscobblemonnpc.manager.DecorativeNpcManager;
import org.crafterscr.crafterscobblemonnpc.util.NpcDataKeys;
import org.crafterscr.crafterscobblemonnpc.util.NpcMessages;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public final class CobbleNpcCommand {

    private static final double TARGET_RADIUS = 8.0D;

    private static final Pattern VALID_ID =
            Pattern.compile("^[a-zA-Z0-9_-]{1,40}$");

    private CobbleNpcCommand() {
    }

    @SubscribeEvent
    public static void registerCommands(
            RegisterCommandsEvent event
    ) {
        register(event.getDispatcher());
    }

    private static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("cobblenpc")
                        .requires(source ->
                                source.hasPermission(2)
                        )

                        /*
                         * /cobblenpc create <id> <pokemon>
                         */
                        .then(
                                Commands.literal("create")
                                        .then(
                                                Commands.argument(
                                                                "id",
                                                                StringArgumentType.word()
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                                "pokemon",
                                                                                StringArgumentType.greedyString()
                                                                        )
                                                                        .suggests(
                                                                                PokemonSuggestionProvider::suggest
                                                                        )
                                                                        .executes(context ->
                                                                                createNpc(
                                                                                        context.getSource(),
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "id"
                                                                                        ),
                                                                                        StringArgumentType.getString(
                                                                                                context,
                                                                                                "pokemon"
                                                                                        )
                                                                                )
                                                                        )
                                                        )
                                        )
                        )

                        /*
                         * /cobblenpc remove <id>
                         */
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument(
                                                                "id",
                                                                StringArgumentType.word()
                                                        )
                                                        .suggests(
                                                                NpcIdSuggestionProvider::suggest
                                                        )
                                                        .executes(context ->
                                                                removeById(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "id"
                                                                        )
                                                                )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("nearest")
                                                        .executes(context ->
                                                                removeNearest(
                                                                        context.getSource()
                                                                )
                                                        )
                                        )
                        )

                        /*
                         * /cobblenpc movehere <id>
                         */
                        .then(
                                Commands.literal("movehere")
                                        .then(
                                                Commands.argument(
                                                                "id",
                                                                StringArgumentType.word()
                                                        )
                                                        .suggests(
                                                                NpcIdSuggestionProvider::suggest
                                                        )
                                                        .executes(context ->
                                                                moveByIdHere(
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "id"
                                                                        )
                                                                )
                                                        )
                                        )
                        )

                        /*
                         * Los siguientes comandos usan el NPC cercano.
                         */
                        .then(
                                Commands.literal("rotate")
                                        .then(
                                                Commands.argument(
                                                                "degrees",
                                                                FloatArgumentType.floatArg(
                                                                        -360.0F,
                                                                        360.0F
                                                                )
                                                        )
                                                        .executes(context ->
                                                                rotateNearest(
                                                                        context.getSource(),
                                                                        FloatArgumentType.getFloat(
                                                                                context,
                                                                                "degrees"
                                                                        )
                                                                )
                                                        )
                                        )
                        )

                        .then(
                                Commands.literal("sound")
                                        .then(
                                                Commands.argument(
                                                                "enabled",
                                                                BoolArgumentType.bool()
                                                        )
                                                        .executes(context ->
                                                                changeNearestSound(
                                                                        context.getSource(),
                                                                        BoolArgumentType.getBool(
                                                                                context,
                                                                                "enabled"
                                                                        )
                                                                )
                                                        )
                                        )
                        )

                        .then(
                                Commands.literal("info")
                                        .executes(context ->
                                                showNearestInfo(
                                                        context.getSource()
                                                )
                                        )
                        )

                        .then(
                                Commands.literal("list")
                                        .executes(context ->
                                                listNpcs(
                                                        context.getSource()
                                                )
                                        )
                        )
        );
    }

    private static int createNpc(
            CommandSourceStack source,
            String npcId,
            String properties
    ) {
        try {
            ServerPlayer player =
                    source.getPlayerOrException();

            MinecraftServer server =
                    source.getServer();

            if (!VALID_ID.matcher(npcId).matches()) {
                source.sendFailure(
                        NpcMessages.error(
                                "El ID solo puede contener letras, "
                                        + "números, guion y guion bajo. "
                                        + "Máximo 40 caracteres."
                        )
                );

                return 0;
            }

            if (DecorativeNpcManager.idExists(
                    server,
                    npcId
            )) {
                source.sendFailure(
                        NpcMessages.error(
                                "Ya existe un NPC con el ID: "
                                        + npcId
                        )
                );

                return 0;
            }

            ServerLevel level =
                    player.serverLevel();

            Vec3 look =
                    player.getLookAngle()
                            .normalize()
                            .scale(2.0D);

            double x = player.getX() + look.x;
            double y = player.getY();
            double z = player.getZ() + look.z;

            float yaw =
                    normalizeYaw(
                            player.getYRot() + 180.0F
                    );

            CobblemonNpcFactory.create(
                    level,
                    npcId,
                    properties,
                    x,
                    y,
                    z,
                    yaw
            );

            source.sendSuccess(
                    () -> NpcMessages.success(
                            "NPC creado | ID: "
                                    + npcId
                                    + " | Pokémon: "
                                    + properties
                    ),
                    true
            );

            return 1;
        } catch (Exception exception) {
            source.sendFailure(
                    NpcMessages.error(
                            "No se pudo crear el NPC: "
                                    + rootMessage(exception)
                    )
            );

            exception.printStackTrace();
            return 0;
        }
    }

    private static int removeById(
            CommandSourceStack source,
            String npcId
    ) {
        Optional<Entity> npc =
                DecorativeNpcManager.findById(
                        source.getServer(),
                        npcId
                );

        if (npc.isEmpty()) {
            source.sendFailure(
                    NpcMessages.error(
                            "No existe un NPC cargado con el ID: "
                                    + npcId
                    )
            );

            return 0;
        }

        Entity entity = npc.get();

        String species =
                entity.getPersistentData()
                        .getString(NpcDataKeys.SPECIES);

        entity.discard();

        source.sendSuccess(
                () -> NpcMessages.success(
                        "NPC eliminado | ID: "
                                + npcId
                                + " | Pokémon: "
                                + species
                ),
                true
        );

        return 1;
    }

    private static int removeNearest(
            CommandSourceStack source
    ) {
        try {
            ServerPlayer player =
                    source.getPlayerOrException();

            Optional<Entity> nearest =
                    DecorativeNpcManager.findNearest(
                            player,
                            TARGET_RADIUS
                    );

            if (nearest.isEmpty()) {
                source.sendFailure(
                        NpcMessages.error(
                                "No hay un NPC decorativo cercano."
                        )
                );

                return 0;
            }

            Entity entity = nearest.get();

            String id =
                    entity.getPersistentData()
                            .getString(NpcDataKeys.NPC_ID);

            entity.discard();

            source.sendSuccess(
                    () -> NpcMessages.success(
                            "NPC eliminado | ID: " + id
                    ),
                    true
            );

            return 1;
        } catch (Exception exception) {
            source.sendFailure(
                    NpcMessages.error(
                            rootMessage(exception)
                    )
            );

            return 0;
        }
    }

    private static int moveByIdHere(
            CommandSourceStack source,
            String npcId
    ) {
        try {
            ServerPlayer player =
                    source.getPlayerOrException();

            Optional<Entity> npc =
                    DecorativeNpcManager.findById(
                            source.getServer(),
                            npcId
                    );

            if (npc.isEmpty()) {
                source.sendFailure(
                        NpcMessages.error(
                                "No existe un NPC cargado con el ID: "
                                        + npcId
                        )
                );

                return 0;
            }

            Entity entity = npc.get();

            if (entity.level() != player.level()) {
                source.sendFailure(
                        NpcMessages.error(
                                "El NPC está en otra dimensión. "
                                        + "Primero debes ir a esa dimensión."
                        )
                );

                return 0;
            }

            Vec3 look =
                    player.getLookAngle()
                            .normalize()
                            .scale(2.0D);

            double x = player.getX() + look.x;
            double y = player.getY();
            double z = player.getZ() + look.z;

            float yaw =
                    normalizeYaw(
                            player.getYRot() + 180.0F
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

            DecorativeNpcManager.updateAnchor(entity);

            source.sendSuccess(
                    () -> NpcMessages.success(
                            "NPC movido | ID: " + npcId
                    ),
                    true
            );

            return 1;
        } catch (Exception exception) {
            source.sendFailure(
                    NpcMessages.error(
                            rootMessage(exception)
                    )
            );

            return 0;
        }
    }

    private static int rotateNearest(
            CommandSourceStack source,
            float degrees
    ) {
        try {
            ServerPlayer player =
                    source.getPlayerOrException();

            Optional<Entity> nearest =
                    DecorativeNpcManager.findNearest(
                            player,
                            TARGET_RADIUS
                    );

            if (nearest.isEmpty()) {
                source.sendFailure(
                        NpcMessages.error(
                                "No hay un NPC cercano."
                        )
                );

                return 0;
            }

            Entity entity = nearest.get();

            float yaw = normalizeYaw(degrees);

            entity.setYRot(yaw);
            entity.setYHeadRot(yaw);
            entity.setYBodyRot(yaw);

            entity.getPersistentData().putFloat(
                    NpcDataKeys.ANCHOR_YAW,
                    yaw
            );

            String id =
                    entity.getPersistentData()
                            .getString(NpcDataKeys.NPC_ID);

            source.sendSuccess(
                    () -> NpcMessages.success(
                            "NPC "
                                    + id
                                    + " rotado a "
                                    + yaw
                                    + " grados."
                    ),
                    true
            );

            return 1;
        } catch (Exception exception) {
            source.sendFailure(
                    NpcMessages.error(
                            rootMessage(exception)
                    )
            );

            return 0;
        }
    }

    private static int changeNearestSound(
            CommandSourceStack source,
            boolean enabled
    ) {
        try {
            ServerPlayer player =
                    source.getPlayerOrException();

            Optional<Entity> nearest =
                    DecorativeNpcManager.findNearest(
                            player,
                            TARGET_RADIUS
                    );

            if (nearest.isEmpty()) {
                source.sendFailure(
                        NpcMessages.error(
                                "No hay un NPC cercano."
                        )
                );

                return 0;
            }

            Entity entity = nearest.get();

            entity.getPersistentData().putBoolean(
                    NpcDataKeys.SOUND_ENABLED,
                    enabled
            );

            entity.setSilent(!enabled);

            String id =
                    entity.getPersistentData()
                            .getString(NpcDataKeys.NPC_ID);

            source.sendSuccess(
                    () -> NpcMessages.success(
                            "Sonido de "
                                    + id
                                    + ": "
                                    + (enabled
                                    ? "activado"
                                    : "desactivado")
                    ),
                    true
            );

            return 1;
        } catch (Exception exception) {
            source.sendFailure(
                    NpcMessages.error(
                            rootMessage(exception)
                    )
            );

            return 0;
        }
    }

    private static int showNearestInfo(
            CommandSourceStack source
    ) {
        try {
            ServerPlayer player =
                    source.getPlayerOrException();

            Optional<Entity> nearest =
                    DecorativeNpcManager.findNearest(
                            player,
                            TARGET_RADIUS
                    );

            if (nearest.isEmpty()) {
                source.sendFailure(
                        NpcMessages.error(
                                "No hay un NPC cercano."
                        )
                );

                return 0;
            }

            Entity entity = nearest.get();

            String id =
                    entity.getPersistentData()
                            .getString(NpcDataKeys.NPC_ID);

            String species =
                    entity.getPersistentData()
                            .getString(NpcDataKeys.SPECIES);

            boolean sound =
                    entity.getPersistentData()
                            .getBoolean(
                                    NpcDataKeys.SOUND_ENABLED
                            );

            source.sendSuccess(
                    () -> NpcMessages.info(
                            "ID: " + id
                    ),
                    false
            );

            source.sendSuccess(
                    () -> NpcMessages.info(
                            "Pokémon: " + species
                    ),
                    false
            );

            source.sendSuccess(
                    () -> NpcMessages.info(
                            "Posición: "
                                    + format(entity.getX())
                                    + ", "
                                    + format(entity.getY())
                                    + ", "
                                    + format(entity.getZ())
                    ),
                    false
            );

            source.sendSuccess(
                    () -> NpcMessages.info(
                            "Rotación: "
                                    + entity.getYRot()
                    ),
                    false
            );

            source.sendSuccess(
                    () -> NpcMessages.info(
                            "Sonido: "
                                    + (sound
                                    ? "activado"
                                    : "desactivado")
                    ),
                    false
            );

            return 1;
        } catch (Exception exception) {
            source.sendFailure(
                    NpcMessages.error(
                            rootMessage(exception)
                    )
            );

            return 0;
        }
    }

    private static int listNpcs(
            CommandSourceStack source
    ) {
        List<Entity> npcs =
                DecorativeNpcManager.findAll(
                        source.getLevel()
                );

        source.sendSuccess(
                () -> NpcMessages.info(
                        "NPC cargados en esta dimensión: "
                                + npcs.size()
                ),
                false
        );

        int index = 1;

        for (Entity entity : npcs) {
            String id =
                    entity.getPersistentData()
                            .getString(NpcDataKeys.NPC_ID);

            String species =
                    entity.getPersistentData()
                            .getString(NpcDataKeys.SPECIES);

            final int currentIndex = index++;

            source.sendSuccess(
                    () -> NpcMessages.info(
                            currentIndex
                                    + ". "
                                    + id
                                    + " | "
                                    + species
                                    + " | "
                                    + format(entity.getX())
                                    + ", "
                                    + format(entity.getY())
                                    + ", "
                                    + format(entity.getZ())
                    ),
                    false
            );
        }

        return npcs.size();
    }

    private static float normalizeYaw(float yaw) {
        float result = yaw % 360.0F;

        if (result < 0.0F) {
            result += 360.0F;
        }

        return result;
    }

    private static String format(double value) {
        return String.format("%.2f", value);
    }

    private static String rootMessage(
            Throwable throwable
    ) {
        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        String message = current.getMessage();

        if (message == null || message.isBlank()) {
            return current.getClass().getSimpleName();
        }

        return message;
    }
}