
package elevate.heartGambler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;

public class HeartGambler extends JavaPlugin {

    public static final String PLAYER_HEALTH_DELTA_KEY = "health_delta"; // stored as double (extra max health)
    public static final String APPLE_KEY = "phantom_apple"; // marker on the item

    private NamespacedKey playerDeltaKey;
    private NamespacedKey appleKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        playerDeltaKey = new NamespacedKey(this, PLAYER_HEALTH_DELTA_KEY);
        appleKey = new NamespacedKey(this, APPLE_KEY);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new PhantomDropListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GambleAppleListener(this), this);

        // Command
        getCommand("phantomapple").setExecutor(new elevate.healthgambler.PhantomAppleCommand(this));
        getCommand("hearts").setExecutor(new HeartsCommand(this));


        // Re-apply saved max health deltas to online players (e.g., /reload)
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerHealthUtil.applySavedDelta(p, getPlayerDeltaKey());
        }

        // Re-apply for players already online (e.g., /reload)
        for (org.bukkit.entity.Player p : getServer().getOnlinePlayers()) {
            PlayerHealthUtil.applySavedDelta(p, getPlayerDeltaKey());
        }
        // NEW: keep health active across joins/respawns
        getServer().getPluginManager().registerEvents(new HeartPersistListener(this), this);
    }

    public ItemStack createGambleApple() {
        ItemStack apple = new ItemStack(Material.APPLE, 1);
        ItemMeta meta = apple.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Wanna play a game?");
        meta.setLore(java.util.List.of(
                ChatColor.GRAY + "50%: +" + ChatColor.RED + "1-3" + ChatColor.GRAY + " hearts permanently",
                ChatColor.GRAY + "50%: -" + ChatColor.RED + "2" + ChatColor.GRAY + " hearts permanently",
                ChatColor.DARK_GRAY + "Dropped by a Phantom"
        ));
        // tag the item so we can detect it later
        meta.getPersistentDataContainer().set(getAppleKey(), PersistentDataType.BYTE, (byte)1);
        apple.setItemMeta(meta);
        return apple;
    }

    public NamespacedKey getPlayerDeltaKey() { return playerDeltaKey; }
    public NamespacedKey getAppleKey() { return appleKey; }
}
