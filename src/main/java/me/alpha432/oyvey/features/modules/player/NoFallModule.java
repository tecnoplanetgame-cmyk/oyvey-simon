package me.alpha432.oyvey.features.modules.player;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.tags.FluidTags;

public class NoFallModule extends Module {
    public NoFallModule() {
        super("NoFall", "Removes fall damage", Category.PLAYER);
    }

    // Fall distance accumulata, letta da MaceDMG
    public float accumulatedFallDistance = 0f;

    @Override
    public void onTick() {
        if (mc.player.isUnderWater() || mc.player.isInFluid(FluidTags.WATER)) {
            accumulatedFallDistance = 0f;
            return;
        }

        if (!mc.player.onGround()) {
            // Accumula la distanza di caduta ogni tick
            accumulatedFallDistance += mc.player.getDeltaMovement().y < 0
                    ? Math.abs(mc.player.getDeltaMovement().y)
                    : 0;

            if (OyVey.positionManager.getFallDistance() > 3) {
                boolean bl = mc.player.horizontalCollision;
                ServerboundMovePlayerPacket.PosRot packet = new ServerboundMovePlayerPacket.PosRot(
                        mc.player.getX(),
                        mc.player.getY() + 0.000000001,
                        mc.player.getZ(),
                        mc.player.getYRot(),
                        mc.player.getXRot(),
                        false,
                        bl
                );
                mc.player.connection.send(packet);
            }
        } else {
            // Appena tocca terra, resetta
            accumulatedFallDistance = 0f;
        }
    }
}
