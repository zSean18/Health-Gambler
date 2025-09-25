package elevate.heartGambler;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

public class GambleAppleListener implements Listener {

    private final HeartGambler plugin;

    public GambleAppleListener(HeartGambler plugin) {
        this.plugin = plugin;
    }

    private boolean isRightClick(Action a) {
        return a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK;
    }

    @EventHandler
    public void onUsePhantomBlood(PlayerInteractEvent event) {
        if (!isRightClick(event.getAction())) return;

        ItemStack stack = event.getItem();
        if (stack == null) return;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        Byte marker = meta.getPersistentDataContainer().get(plugin.getAppleKey(), PersistentDataType.BYTE);

        if (marker == null || marker != (byte) 1) return;

        Player player = event.getPlayer();
        AttributeInstance maxAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (maxAttr != null && maxAttr.getBaseValue() <= 8.0) {
            player.sendMessage(ChatColor.RED + "You are too frail to handle this gamble (4 hearts total).");
            event.setCancelled(true);
            return;
        }

        stack.setAmount(stack.getAmount() - 1);
        event.setCancelled(true);

        //50/50 gamble logic
        double savedDelta = PlayerHealthUtil.getSavedDelta(player, plugin.getPlayerDeltaKey());
        boolean lucky = ThreadLocalRandom.current().nextBoolean();
        int heartsDelta;

        if (lucky) {
            int extraHearts = ThreadLocalRandom.current().nextInt(1, 4); // +1..+3 hearts
            savedDelta += extraHearts * 2.0;
            heartsDelta = extraHearts;
        } else {
            double removeHp = 4.0;
            double newTotal = 20.0 + savedDelta - removeHp;
            if (newTotal < 8.0) {
                removeHp = (20.0 + savedDelta) - 8.0;
            }
            removeHp = Math.max(0.0, removeHp);
            savedDelta -= removeHp;
            heartsDelta = -(int) Math.round(removeHp / 2.0);
        }

        PlayerHealthUtil.saveAndApplyDelta(player, plugin.getPlayerDeltaKey(), savedDelta);
        double newMax = 20.0 + savedDelta;
        if (player.getHealth() > newMax) player.setHealth(newMax);

        CreepyEffects.playBloodEffects(player, heartsDelta);
    }
}
