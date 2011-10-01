package me.ahniolator.plugins.sunburn;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

public class BurningPlayerListener extends PlayerListener {
    private final Sunburn plugin;
    private final BurningConfig config;
    private boolean tellUpdate;
    
    public BurningPlayerListener(Sunburn plugin, boolean update, BurningConfig config) {
        this.plugin = plugin;
        this.tellUpdate = update;
        this.config = config;
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        tellUpdate = this.config.yml.getBoolean("sunburn.update.notifications", true);
        Player player = event.getPlayer();
        if ((!player.isOp() && !player.hasPermission("bcs.admin"))|| !tellUpdate) {
            return;
        }
        Sunburn.checkForUpdates(this.plugin, player, false);
        return;
    }
}
