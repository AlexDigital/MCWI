package de.alexdigital.mcwi.web;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.common.base.Joiner;
import de.alexdigital.mcwi.McWebinterface;
import de.alexdigital.mcwi.util.FileCheck;
import de.alexdigital.mcwi.web.obj.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SocketIO {

    @Getter
    private String hostname;

    @Getter
    private int port;

    private SocketIOServer server;
    private SessionStore sessionStore;

    private List<UUID> acceptedClients = new ArrayList<>();

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

        this.server.addConnectListener(socketIOClient -> {

        });

        // Login with user data
        this.server.addEventListener("login-data", LoginData.class, (socketIOClient, loginData, ackRequest) -> {
            if (McWebinterface.getInstance().getLoginConfig().check(loginData.getUsername(), loginData.getPassword())) {
                LoginSuccessResponse response = new LoginSuccessResponse(loginData.getUsername(), sessionStore.getSession(loginData.getUsername()));
                socketIOClient.sendEvent("login-success", response);
                this.acceptedClients.add(socketIOClient.getSessionId());
            } else {
                socketIOClient.sendEvent("login-failed");
            }
        });

        // Login with cookie
        this.server.addEventListener("login-cookie", LoginCookieData.class, ((socketIOClient, loginCookieData, ackRequest) -> {
            if (this.sessionStore.getSession(loginCookieData.getUsername()).equals(loginCookieData.getCookie())) {
                LoginSuccessResponse response = new LoginSuccessResponse(loginCookieData.getUsername(), loginCookieData.getCookie());
                socketIOClient.sendEvent("login-success", response);
                this.acceptedClients.add(socketIOClient.getSessionId());
            } else {
                socketIOClient.sendEvent("login-failed");
            }
        }));

        // Logout
        this.server.addEventListener("logout", String.class, ((socketIOClient, s, ackRequest) -> {
            if (this.acceptedClients.contains(socketIOClient.getSessionId())) {
                this.sessionStore.removeSession(s);
            }
        }));

        // Dispatch console command
        this.server.addEventListener("console-command", String.class, (socketIOClient, s, ackRequest) -> {
            if (this.acceptedClients.contains(socketIOClient.getSessionId())) {
                Bukkit.getScheduler().runTaskLater(McWebinterface.getInstance(), () -> {
                    McWebinterface.getInstance().getServer().dispatchCommand(McWebinterface.getInstance().getServer().getConsoleSender(), s);
                }, 1L);
            }
        });

        // World Actions
        this.server.addEventListener("world-action", WorldActionData.class, (socketIOClient, worldActionData, ackRequest1) -> {
            if (this.acceptedClients.contains(socketIOClient.getSessionId())) {
                switch (worldActionData.getAction()) {
                    case "delete":
                        Bukkit.getScheduler().runTaskLater(McWebinterface.getInstance(), () -> {
                            Bukkit.getWorld(worldActionData.getWorldName()).getPlayers().forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));
                            Bukkit.unloadWorld(worldActionData.getWorldName(), false);
                            File folder = new File(worldActionData.getWorldName());
                            folder.delete();
                            sendWorldList(socketIOClient);
                        }, 1L);
                        break;
                    case "rename":
                        Bukkit.getScheduler().runTaskLater(McWebinterface.getInstance(), () -> {
                            Bukkit.getWorld(worldActionData.getWorldName()).getPlayers().forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));
                            Bukkit.unloadWorld(worldActionData.getWorldName(), true);
                            File worldFolder = new File(worldActionData.getWorldName());
                            worldFolder.renameTo(new File(worldActionData.getTo()));
                            new WorldCreator(worldActionData.getTo()).createWorld();
                            sendWorldList(socketIOClient);
                        }, 1L);
                        break;
                    case "clone":
                        Bukkit.getScheduler().runTaskLater(McWebinterface.getInstance(), () -> {
                            new WorldCreator(worldActionData.getTo()).copy(Bukkit.getWorld(worldActionData.getWorldName())).createWorld();
                            sendWorldList(socketIOClient);
                        }, 1L);
                        break;
                    case "new":
                        Bukkit.getScheduler().runTaskLater(McWebinterface.getInstance(), () -> {
                            new WorldCreator(worldActionData.getWorldName()).seed(Long.valueOf(worldActionData.getSeed())).createWorld();
                            sendWorldList(socketIOClient);
                        }, 1L);
                        break;
                }
            }
        });

        // Plugin actions
        this.server.addEventListener("plugin-disable", String.class, (socketIOClient, s, ackRequest) -> {
            if (this.acceptedClients.contains(socketIOClient.getSessionId())) {
                Plugin target = Arrays.asList(Bukkit.getPluginManager().getPlugins()).stream().filter(p -> p.getName().equalsIgnoreCase(s)).findFirst().orElse(null);
                if (target != null) {
                    Bukkit.getPluginManager().disablePlugin(target);
                    sendPluginList(socketIOClient);
                }
            }
        });

        // Requests
        this.server.addEventListener("request", String.class, (socketIOClient, string, ackRequest) -> {
            if (this.acceptedClients.contains(socketIOClient.getSessionId())) {
                switch (string) {
                    case "player-list":
                        List<String> users = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
                        socketIOClient.sendEvent("player-list", users);
                        break;
                    case "world-list":
                        sendWorldList(socketIOClient);
                        break;
                    case "plugin-list":
                        sendPluginList(socketIOClient);
                        break;
                }
            }
        });

        this.server.addEventListener("item-request", String.class, (socketIOClient, path, ackRequest) -> {
            if (this.acceptedClients.contains(socketIOClient.getSessionId())) {
                System.out.println(path);
                if (!path.startsWith("../")) {
                    sendFileList(path, socketIOClient);
                }
            }
        });

        // File actions
        this.server.addEventListener("item-edit", String.class, (socketIOClient, path, ackRequest) -> {
            if (this.acceptedClients.contains(socketIOClient.getSessionId())) {
                socketIOClient.sendEvent("item-edit", new String(Files.readAllBytes(Paths.get(path))));
            }
        });

        this.server.addEventListener("item-save", FileSaveData.class, (socketIOClient, fileSaveData, ackRequest) -> {
            if (this.acceptedClients.contains(socketIOClient.getSessionId())) {
                Files.write(Paths.get(fileSaveData.getPath()), fileSaveData.getText().getBytes(),
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        });

        this.server.addEventListener("item-delete", String.class, (socketIOClient, s, ackRequest) -> {
            if (this.acceptedClients.contains(socketIOClient.getSessionId())) {
                Files.deleteIfExists(Paths.get(s));
                sendFileList(".", socketIOClient);
            }
        });

    }

    private void sendFileList(String path, SocketIOClient client) {
        List<FileItemData> files = new ArrayList<>();
        files.add(new FileItemData("/", "dir", 0, ".", false));
        try {
            Files.walk(Paths.get(path), 1).forEach(file -> {
                if (file.toString().equalsIgnoreCase("..")) return;
                if (file.toString().equalsIgnoreCase(path)) return;
                files.add(new FileItemData(file.toFile().getName(), Files.isRegularFile(file) ? "file" : "dir",
                        file.toFile().length(), file.toString(), FileCheck.isEditable(file)));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.sendEvent("item-request", files);
    }

    private void sendWorldList(SocketIOClient client) {
        List<WorldData> worldDataList = new ArrayList<>();
        Bukkit.getWorlds().forEach(world -> worldDataList.add(new WorldData(world.getName(), world.getDifficulty().name(), String.valueOf(world.getSeed()))));
        client.sendEvent("world-list", worldDataList);
    }

    private void sendPluginList(SocketIOClient client) {
        List<PluginData> pluginDataList = new ArrayList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            pluginDataList.add(new PluginData(plugin.getName(), plugin.getDescription().getVersion(), Joiner.on(", ").join(plugin.getDescription().getAuthors())));
        }
        client.sendEvent("plugin-list", pluginDataList);
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
