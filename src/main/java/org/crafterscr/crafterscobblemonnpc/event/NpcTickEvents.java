package org.crafterscr.crafterscobblemonnpc.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.crafterscr.crafterscobblemonnpc.manager.DecorativeNpcManager;
import org.crafterscr.crafterscobblemonnpc.util.NpcDataKeys;

public final class NpcTickEvents {

    private NpcTickEvents() {
    }

    @SubscribeEvent
    public static void onEntityTick(
            EntityTickEvent.Post event
    ) {
        Entity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        if (!DecorativeNpcManager.isDecorativeNpc(entity)) {
            return;
        }

        CompoundTag data = entity.getPersistentData();

        double x = data.getDouble(NpcDataKeys.ANCHOR_X);
        double y = data.getDouble(NpcDataKeys.ANCHOR_Y);
        double z = data.getDouble(NpcDataKeys.ANCHOR_Z);

        float yaw = data.getFloat(NpcDataKeys.ANCHOR_YAW);
        float pitch =
                data.getFloat(NpcDataKeys.ANCHOR_PITCH);

        entity.setDeltaMovement(Vec3.ZERO);
        entity.setNoGravity(true);
        entity.setInvulnerable(true);
        entity.noPhysics = true;

        entity.moveTo(
                x,
                y,
                z,
                yaw,
                pitch
        );

        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.setYHeadRot(yaw);
        entity.setYBodyRot(yaw);

        boolean soundEnabled =
                data.getBoolean(
                        NpcDataKeys.SOUND_ENABLED
                );

        entity.setSilent(!soundEnabled);

        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
    }
}