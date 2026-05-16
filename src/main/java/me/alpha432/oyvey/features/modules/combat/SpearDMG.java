package me.alpha432.oyvey.features.modules.combat;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class SpearKillModule extends Module {

    private final Setting<Float> fakeSpeed = new Setting<>("FakeSpeed", 60.0f, 10.0f, 200.0f,
            "Altezza simulata della caduta (più alto = più danno)");
    private final Setting<Float> attackRange = new Setting<>("Range", 6.0f, 1.0f, 15.0f,
            "Raggio di ricerca nemici");
    private final Setting<Integer> packetCount = new Setting<>("Packets", 10, 1, 30,
            "Pacchetti falsi da mandare per simulare la velocità");
    private final Setting<Boolean> onlyCharging = new Setting<>("OnlyCharging", true,
            "Attiva solo quando tieni premuto il tasto sinistro");

    private int chargeTicks = 0;

    public SpearKillModule() {
        super("SpearKill", "Instant kill with spear using fake velocity packets", Category.COMBAT);
    }

    /**
     * Sostituisci questo metodo con il check del tuo item Spear custom.
     * Esempio: mc.player.getMainHandItem().is(ModItems.SPEAR)
     */
    private boolean isHoldingSpear() {
        Item mainHand = mc.player.getMainHandItem().getItem();
        Item offHand = mc.player.getOffhandItem().getItem();
        // Adatta "SPEAR" al registry name del tuo item custom
        String mainName = mainHand.getDescriptionId().toLowerCase();
        String offName  = offHand.getDescriptionId().toLowerCase();
        return mainName.contains("spear") || offName.contains("spear");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (!isHoldingSpear()) {
            chargeTicks = 0;
            return;
        }

        // Controlla se sta tenendo premuto tasto sinistro (carica)
        boolean isCharging = mc.player.isUsingItem();
        if (onlyCharging.getValue() && !isCharging) {
            chargeTicks = 0;
            return;
        }

        if (isCharging) chargeTicks++;
        else chargeTicks = 0;

        // Cerca nemici nel range
        AABB searchBox = mc.player.getBoundingBox().inflate(attackRange.getValue());
        List<LivingEntity> targets = mc.level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e != mc.player
                        && e.isAlive()
                        && !(e instanceof Player && isTeammate((Player) e))
        );
        if (targets.isEmpty()) return;

        // Nemico più vicino
        LivingEntity target = targets.stream()
                .min((a, b) -> Float.compare(
                        (float) a.distanceToSqr(mc.player),
                        (float) b.distanceToSqr(mc.player)
                ))
                .orElse(null);
        if (target == null) return;

        // Simula velocità e attacca
        simulateFakeVelocity(target);
        mc.player.attack(target);
        chargeTicks = 0;
    }

    /**
     * Manda pacchetti falsi che simulano una caduta da quota enorme.
     * Il server calcola il danno sulla base di questi pacchetti → morte istantanea.
     */
    private void simulateFakeVelocity(LivingEntity target) {
        Vec3 playerPos = mc.player.position();
        Vec3 targetPos  = target.position();
        Vec3 direction  = targetPos.subtract(playerPos).normalize();

        double fakeStartY = playerPos.y + fakeSpeed.getValue();
        int packets = packetCount.getValue();

        for (int i = 0; i < packets; i++) {
            double t = (double) i / packets;
            double fakeX = playerPos.x + direction.x * fakeSpeed.getValue() * t;
            double fakeY = fakeStartY - (fakeStartY - targetPos.y) * t;
            double fakeZ = playerPos.z + direction.z * fakeSpeed.getValue() * t;
            boolean isLast = (i == packets - 1);

            ServerboundMovePlayerPacket.PosRot pkt = new ServerboundMovePlayerPacket.PosRot(
                    fakeX, fakeY, fakeZ,
                    mc.player.getYRot(),
                    mc.player.getXRot(),
                    isLast,
                    mc.player.horizontalCollision
            );
            mc.player.connection.send(pkt);
        }

        // Pacchetto reset → riporta il client alla posizione reale
        ServerboundMovePlayerPacket.PosRot reset = new ServerboundMovePlayerPacket.PosRot(
                playerPos.x, playerPos.y, playerPos.z,
                mc.player.getYRot(),
                mc.player.getXRot(),
                mc.player.onGround(),
                mc.player.horizontalCollision
        );
        mc.player.connection.send(reset);
    }

    private boolean isTeammate(Player other) {
        if (mc.player.getTeam() == null) return false;
        return mc.player.getTeam().equals(other.getTeam());
    }
}
