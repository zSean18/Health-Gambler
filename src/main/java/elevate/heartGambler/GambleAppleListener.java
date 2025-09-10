package elevate.heartGambler;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

public class GambleAppleListener implements Listener {

    private final HeartGambler plugin;

    public GambleAppleListener(HeartGambler plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        if (event.getItem() == null || event.getItem().getItemMeta() == null) return;

        ItemMeta meta = event.getItem().getItemMeta();
        Byte marker = meta.getPersistentDataContainer().get(plugin.getAppleKey(), PersistentDataType.BYTE);
        if (marker == null || marker != (byte)1) return; // not our special apple

        // Prevent eating if player only has 4 hearts total (8.0 max health)
        AttributeInstance maxHealthAttr = event.getPlayer().getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr == null) return;

        double currentMax = maxHealthAttr.getBaseValue();
        if (currentMax <= 8.0) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You are too frail to handle this gamble (4 hearts total).");
            return;
        }

        // Load saved delta and decide outcome
        double savedDelta = PlayerHealthUtil.getSavedDelta(event.getPlayer(), plugin.getPlayerDeltaKey());
        boolean win = ThreadLocalRandom.current().nextBoolean();

        if (win) {
            int extraHearts = ThreadLocalRandom.current().nextInt(1, 4); // 1-3
            double add = extraHearts * 2.0;
            savedDelta += add;
            event.getPlayer().sendMessage(ChatColor.GREEN + "Lucky! +" + extraHearts + " heart(s) permanently.");
        } else {
            double remove = 4.0; // 2 hearts
            // Don’t go below 8.0 max health total
            double newTotal = 20.0 + savedDelta - remove;
            if (newTotal < 8.0) {
                remove = (20.0 + savedDelta) - 8.0; // clamp so we stop at 8.0
            }
            savedDelta -= remove;
            event.getPlayer().sendMessage(ChatColor.RED + "Unlucky! -2 hearts permanently.");
        }

        // Save & apply
        PlayerHealthUtil.saveAndApplyDelta(event.getPlayer(), plugin.getPlayerDeltaKey(), savedDelta);

        // If current health exceeds new max (after loss), clamp current health
        double newMax = 20.0 + savedDelta;
        if (event.getPlayer().getHealth() > newMax) {
            event.getPlayer().setHealth(newMax);
        }
    }
    // Helper to build the tagged, named apple used everywhere
    public org.bukkit.inventory.ItemStack createGambleApple() {
        org.bukkit.inventory.ItemStack apple = new org.bukkit.inventory.ItemStack(org.bukkit.Material.APPLE, 1);
        org.bukkit.inventory.meta.ItemMeta meta = apple.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.LIGHT_PURPLE + "Wanna play a game?");
        meta.setLore(java.util.List.of(
                org.bukkit.ChatColor.GRAY + "50%: +" + org.bukkit.ChatColor.RED + "1-3" + org.bukkit.ChatColor.GRAY + " hearts permanently",
                org.bukkit.ChatColor.GRAY + "50%: -" + org.bukkit.ChatColor.RED + "2" + org.bukkit.ChatColor.GRAY + " hearts permanently",
                org.bukkit.ChatColor.DARK_GRAY + "Dropped by a Phantom"
        ));
        meta.getPersistentDataContainer().set(plugin.getAppleKey(), org.bukkit.persistence.PersistentDataType.BYTE, (byte)1);
        apple.setItemMeta(meta);
        return apple;
    }

}
