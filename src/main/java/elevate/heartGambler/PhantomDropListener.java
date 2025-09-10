package elevate.heartGambler;

import elevate.heartGambler.HeartGambler;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class PhantomDropListener implements Listener {

    private final HeartGambler plugin;

    public PhantomDropListener(HeartGambler plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPhantomDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.PHANTOM) return;

        int chance = plugin.getConfig().getInt("drop_chance", 10);
        int roll = ThreadLocalRandom.current().nextInt(100); // 0..99

        if (roll < chance) {
            ItemStack apple = plugin.createGambleApple();
            event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), apple);
        }
    }
}
