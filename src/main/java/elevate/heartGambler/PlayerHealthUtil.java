package elevate.heartGambler;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public final class PlayerHealthUtil {

    private PlayerHealthUtil() {}

    public static double getSavedDelta(Player player, NamespacedKey key) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Double d = pdc.get(key, PersistentDataType.DOUBLE);
        return d == null ? 0.0 : d;
    }

    public static void saveAndApplyDelta(Player player, NamespacedKey key, double delta) {
        player.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, delta);
        applySavedDelta(player, key);
    }

    public static void applySavedDelta(Player player, NamespacedKey key) {
        AttributeInstance max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (max == null) return;
        double delta = getSavedDelta(player, key);
        double newBase = 20.0 + delta; // vanilla = 20.0 (10 hearts)
        if (newBase < 8.0) newBase = 8.0; // hard floor at 4 hearts total
        max.setBaseValue(newBase);
    }
}
