package de.alexdigital.mcwi.web;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import de.alexdigital.mcwi.McWebinterface;
import de.alexdigital.mcwi.web.obj.LoginCookieData;
import de.alexdigital.mcwi.web.obj.LoginData;
import de.alexdigital.mcwi.web.obj.LoginSuccessResponse;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SocketIO {

    @Getter
    private String hostname;

    @Getter
    private int port;

    private SocketIOServer server;
    private SessionStore sessionStore;

    public SocketIO(String hostname, int port) {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");
        Configuration configuration = new Configuration();
        configuration.setHostname(hostname);
        configuration.setPort(port);

        this.server = new SocketIOServer(configuration);
        this.setListeners();

        this.sessionStore = new SessionStore();
    }

    private void setListeners() {
        // Login with user data
        this.server.addEventListener("login-data", LoginData.class, (socketIOClient, loginData, ackRequest) -> {
            if (McWebinterface.getInstance().getLoginConfig().check(loginData.getUsername(), loginData.getPassword())) {
                LoginSuccessResponse response = new LoginSuccessResponse(loginData.getUsername(), sessionStore.getSession(loginData.getUsername()));
                socketIOClient.sendEvent("login-success", response);
            } else {
                socketIOClient.sendEvent("login-failed");
            }
        });

        // Login with cookie
        this.server.addEventListener("login-cookie", LoginCookieData.class, ((socketIOClient, loginCookieData, ackRequest) -> {
            if (this.sessionStore.getSession(loginCookieData.getUsername()).equals(loginCookieData.getCookie())) {
                LoginSuccessResponse response = new LoginSuccessResponse(loginCookieData.getUsername(), loginCookieData.getCookie());
                socketIOClient.sendEvent("login-success", response);
            } else {
                socketIOClient.sendEvent("login-failed");
            }
        }));

        // Logout
        this.server.addEventListener("logout", String.class, ((socketIOClient, s, ackRequest) -> {
            this.sessionStore.removeSession(s);
        }));

        // Dispatch console command
        this.server.addEventListener("console-command", String.class, (socketIOClient, s, ackRequest) -> {
            Bukkit.getScheduler().runTaskLater(McWebinterface.getInstance(), () -> {
                McWebinterface.getInstance().getServer().dispatchCommand(McWebinterface.getInstance().getServer().getConsoleSender(), s);
            }, 1L);
        });

        // Requests
        this.server.addEventListener("request", String.class, (socketIOClient, string, ackRequest) -> {
            switch (string) {
                case "player-list":
                    List<String> users = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
                    socketIOClient.sendEvent("player-list", users);
                    break;
            }
        });

    }

    public void start() {
        try {
            this.server.start();
        } catch (ExceptionInInitializerError error) {
            McWebinterface.getInstance().getLogger().severe("You mustn't reload the server because the plugin does not support it! Disabling...");
            Bukkit.getServer().getPluginManager().disablePlugin(McWebinterface.getInstance());
        }
    }

    public void stop() {
        this.server.stop();
    }

    public SocketIOServer getServer() {
        return this.server;
    }

}
