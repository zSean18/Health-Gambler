package elevate.heartGambler;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

public class PhantomStewListener implements Listener {

    private final HeartGambler plugin;

    public PhantomStewListener(HeartGambler plugin) {
        this.plugin = plugin;
    }

    private boolean isRightClick(Action a) {
        return a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK;
    }

    @EventHandler
    public void onUseStew(PlayerInteractEvent event) {
        if (!isRightClick(event.getAction())) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Byte mark = meta.getPersistentDataContainer().get(plugin.getStewKey(), PersistentDataType.BYTE);
        if (mark == null || mark != (byte)1) return;

        Player p = event.getPlayer();
        AttributeInstance maxAttr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxAttr == null) return;

        double delta = PlayerHealthUtil.getSavedDelta(p, plugin.getPlayerDeltaKey());
        double totalMax = 20.0 + delta;

        //block use if already at or above 10 hearts
        if (totalMax >= 20.0) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.YELLOW + "You don't need this stew. You're already at 10 hearts.");
            return;
        }

        int missingHearts = (int) Math.max(0, Math.round((20.0 - totalMax) / 2.0));
        int roll = ThreadLocalRandom.current().nextInt(1, 3);
        int restore = Math.min(roll, missingHearts);

        if (restore <= 0) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.YELLOW + "You don't need this stew right now.");
            return;
        }

        //consume one stew manually regardless of hunger
        item.setAmount(item.getAmount() - 1);
        event.setCancelled(true);

        double addHp = restore * 2.0;
        double newDelta = Math.min(0.0, delta + addHp);
        PlayerHealthUtil.saveAndApplyDelta(p, plugin.getPlayerDeltaKey(), newDelta);
        double newMax = 20.0 + newDelta;
        if (p.getHealth() > newMax) p.setHealth(newMax);
        p.sendMessage(ChatColor.GREEN + "Phantom Stew restored +" + restore + " heart(s).");
    }
}
