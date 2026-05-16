package me.alpha432.oyvey.features.modules.combat;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.player.NoFallModule;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class MaceDMGModule extends Module {

    // Impostazioni configurabili
    private final Setting<Float> bonusMultiplier = new Setting<>("Multiplier", 1.5f, 0.5f, 5.0f,
            "Moltiplicatore del danno bonus dalla caduta");
    private final Setting<Float> attackRange = new Setting<>("Range", 4.0f, 1.0f, 8.0f,
            "Raggio di attacco della mazza");
    private final Setting<Float> minFallDistance = new Setting<>("MinFall", 3.0f, 1.0f, 20.0f,
            "Distanza minima di caduta per attivare il bonus");

    public MaceDMGModule() {
        super("MaceDMG", "Boosts mace damage using NoFall accumulated distance", Category.COMBAT);
    }

    /**
     * Calcola il danno bonus della mazza basato sulla fall distance accumulata da NoFall.
     * Formula vanilla: 0.5 * fallDistance (semplificata)
     */
    public float getBonusDamage() {
        NoFallModule noFall = OyVey.moduleManager.getModule(NoFallModule.class);
        if (noFall == null || !noFall.isEnabled()) return 0f;

        float fallDist = noFall.accumulatedFallDistance;
        if (fallDist < minFallDistance.getValue()) return 0f;

        return fallDist * 0.5f * bonusMultiplier.getValue();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        // Controlla che il giocatore tenga la mazza in mano
        boolean holdingMace = mc.player.getMainHandItem().is(Items.MACE)
                || mc.player.getOffhandItem().is(Items.MACE);
        if (!holdingMace) return;

        float bonus = getBonusDamage();
        if (bonus <= 0f) return;

        // Cerca nemici nel range
        AABB searchBox = mc.player.getBoundingBox().inflate(attackRange.getValue());
        List<LivingEntity> targets = mc.level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e != mc.player && e.isAlive() && !(e instanceof Player && isTeammate((Player) e))
        );

        if (targets.isEmpty()) return;

        // Prende il nemico più vicino
        LivingEntity target = targets.stream()
                .min((a, b) -> Float.compare(
                        (float) a.distanceToSqr(mc.player),
                        (float) b.distanceToSqr(mc.player)
                ))
                .orElse(null);

        if (target == null) return;

        // Attacca il bersaglio se il cooldown è pronto
        if (mc.player.getCurrentItemAttackStrengthDelay() <= 0) {
            mc.player.attack(target);

            // Log nel display per debug
            OyVey.LOGGER.info("[MaceDMG] Attacco! Bonus danno: +" + String.format("%.1f", bonus));

            // Resetta la fall distance dopo l'attacco
            NoFallModule noFall = OyVey.moduleManager.getModule(NoFallModule.class);
            if (noFall != null) {
                noFall.accumulatedFallDistance = 0f;
            }
        }
    }

    /**
     * Controlla se un altro giocatore è nella stessa squadra (per non attaccarlo).
     */
    private boolean isTeammate(Player other) {
        if (mc.player.getTeam() == null) return false;
        return mc.player.getTeam().equals(other.getTeam());
    }
}
