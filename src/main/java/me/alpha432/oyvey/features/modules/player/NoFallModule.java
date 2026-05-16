package me.alpha432.oyvey.features.modules.player;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.tags.FluidTags;

public class NoFallModule extends Module {
    public NoFallModule() {
        super("NoFall", "Removes fall damage", Category.PLAYER);
    }

    @Override
    public void onTick() {
        // Se il giocatore è in acqua, non fare nulla → caduta normale
        if (mc.player.isUnderWater() || mc.player.isInFluid(FluidTags.WATER)) {
            return;
        }

        if (!mc.player.onGround() && OyVey.positionManager.getFallDistance() > 3) {
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
    }
}
