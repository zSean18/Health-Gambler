package elevate.heartGambler;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public final class PlayerHealthUtil {

    private PlayerHealthUtil() {}
    private static final double MIN_TOTAL = 8.0;
    private static final double MAX_TOTAL = 40.0;

    public static double getSavedDelta(Player player, NamespacedKey key) {
        var pdc = player.getPersistentDataContainer();
        Double d = pdc.get(key, PersistentDataType.DOUBLE);
        return d == null ? 0.0 : d;
    }

    private static double clampDelta(double delta) {
        double minDelta = MIN_TOTAL - 20.0;
        double maxDelta = MAX_TOTAL - 20.0;
        return Math.max(minDelta, Math.min(maxDelta, delta));
    }

    public static void saveAndApplyDelta(Player player, NamespacedKey key, double delta) {
        delta = clampDelta(delta);
        player.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, delta);
        var max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (max == null) return;

        double newBase = 20.0 + delta;

        if (newBase < MIN_TOTAL) newBase = MIN_TOTAL;
        if (newBase > MAX_TOTAL) newBase = MAX_TOTAL;

        max.setBaseValue(newBase);

        if (player.getHealth() > newBase) {
            player.setHealth(newBase);
        }
    }

    public static void applySavedDelta(Player player, NamespacedKey key) {
        double delta = clampDelta(getSavedDelta(player, key));
        saveAndApplyDelta(player, key, delta);
    }
}