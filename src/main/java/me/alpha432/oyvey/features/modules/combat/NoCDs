package me.alpha432.oyvey.features.modules.combat;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class NoCooldownModule extends Module {

    private final Setting<Boolean> tridentRiptide = new Setting<>("Riptide-Trident", true,
            "Rimuove cooldown del tridente con riptide");
    private final Setting<Boolean> windCharge = new Setting<>("WindCharge", true,
            "Rimuove cooldown della wind charge");
    private final Setting<Boolean> enderPearl = new Setting<>("EnderPearl", true,
            "Rimuove cooldown dell'ender pearl");
    private final Setting<Boolean> spearLunge = new Setting<>("Spear-Lunge", true,
            "Rimuove cooldown dello scatto della spear");
    private final Setting<Boolean> bowCrossbow = new Setting<>("Bow-Crossbow", true,
            "Rimuove cooldown di arco e balestra");

    public NoCooldownModule() {
        super("NoCooldown", "Removes cooldown for riptide, windcharge, enderpearl, spear and bow", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // Ender Pearl
        if (enderPearl.getValue()) {
            mc.player.getCooldowns().removeCooldown(Items.ENDER_PEARL);
        }

        // Wind Charge
        if (windCharge.getValue()) {
            mc.player.getCooldowns().removeCooldown(Items.WIND_CHARGE);
        }

        // Tridente con Riptide
        if (tridentRiptide.getValue()) {
            mc.player.getCooldowns().removeCooldown(Items.TRIDENT);
        }

        // Arco
        if (bowCrossbow.getValue()) {
            mc.player.getCooldowns().removeCooldown(Items.BOW);
            mc.player.getCooldowns().removeCooldown(Items.CROSSBOW);
        }

        // Spear custom (adatta il registry name al tuo item)
        if (spearLunge.getValue()) {
            removeSpearCooldown();
        }

        // Azzera il cooldown generico di attacco del giocatore
        resetAttackCooldown();
    }

    /**
     * Rimuove il cooldown della spear custom.
     * Adatta il nome al tuo item registrato.
     */
    private void removeSpearCooldown() {
        mc.player.getCooldowns().cooldowns.entrySet().removeIf(entry -> {
            String id = entry.getKey().getDescriptionId().toLowerCase();
            return id.contains("spear");
        });
    }

    /**
     * Resetta il cooldown di attacco corpo a corpo del giocatore a 0
     * usando reflection, così ogni arma può essere riusata immediatamente.
     */
    private void resetAttackCooldown() {
        try {
            java.lang.reflect.Field field = Player.class.getDeclaredField("attackStrengthTicker");
            field.setAccessible(true);
            // Valore altissimo = cooldown completato istantaneamente
            field.set(mc.player, Integer.MAX_VALUE);
        } catch (Exception e) {
            // Fallback: prova il nome obfuscato
            try {
                java.lang.reflect.Field field = Player.class.getDeclaredField("f_36095_");
                field.setAccessible(true);
                field.set(mc.player, Integer.MAX_VALUE);
            } catch (Exception ignored) {}
        }
    }
}
