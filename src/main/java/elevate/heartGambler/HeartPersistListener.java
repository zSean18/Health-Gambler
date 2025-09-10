package elevate.heartGambler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class HeartPersistListener implements Listener {

    private final HeartGambler plugin;

    public HeartPersistListener(HeartGambler plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Re-apply saved max-health delta as soon as the player logs in
        PlayerHealthUtil.applySavedDelta(event.getPlayer(), plugin.getPlayerDeltaKey());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // Re-apply on respawn (new player entity)
        // Run one tick later to ensure the attribute instance exists
        plugin.getServer().getScheduler().runTask(plugin, () ->
                PlayerHealthUtil.applySavedDelta(event.getPlayer(), plugin.getPlayerDeltaKey())
        );
    }
}
