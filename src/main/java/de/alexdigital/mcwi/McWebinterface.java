package de.alexdigital.mcwi;

import de.alexdigital.mcwi.command.MCWICommand;
import de.alexdigital.mcwi.listener.PlayerJoinLeaveListener;
import de.alexdigital.mcwi.log.ConsoleLogAppender;
import de.alexdigital.mcwi.util.LoginConfig;
import de.alexdigital.mcwi.web.WebServer;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class McWebinterface extends JavaPlugin {

    @Getter
    private WebServer webServer;

    @Getter
    private LoginConfig loginConfig;

    @Getter
    private static McWebinterface instance;

    @Override
    public void onEnable() {
        instance = this;

        this.loginConfig = new LoginConfig();

        // Register commands
        this.getCommand("mcwi").setExecutor(new MCWICommand());

        // Register listeners
        this.getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(), this);

        // Starting webserver
        this.webServer = new WebServer(8080);
        this.webServer.setLogger(this.getLogger());
        this.webServer.start();

        // Add appender
        Logger log = (Logger) LogManager.getRootLogger();
        log.addAppender(new ConsoleLogAppender());
    }

    @Override
    public void onDisable() {
        this.webServer.stop();
        this.loginConfig.save();
    }

}
