package de.alexdigital.mcwi.listener;

import de.alexdigital.mcwi.McWebinterface;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerJoinLeaveListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(McWebinterface.getInstance(), this::sendPlayerList, 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(McWebinterface.getInstance(), this::sendPlayerList, 10L);
    }

    public void sendPlayerList() {
        List<String> users = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
        McWebinterface.getInstance().getWebServer()
                .getSocketIO().getServer().getBroadcastOperations().sendEvent("player-list", users);
    }

}
