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
        PlayerHealthUtil.applySavedDelta(event.getPlayer(), plugin.getPlayerDeltaKey());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () ->
                PlayerHealthUtil.applySavedDelta(event.getPlayer(), plugin.getPlayerDeltaKey())
        );
    }
}
