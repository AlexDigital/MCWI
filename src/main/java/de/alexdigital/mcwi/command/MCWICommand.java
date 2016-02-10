package de.alexdigital.mcwi.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class MCWICommand implements CommandExecutor {

    private SetLoginCommand setLoginCommand;

    public MCWICommand() {
        this.setLoginCommand = new SetLoginCommand();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String s, String[] args) {
        switch (args.length) {
            case 3:
                if (args[0].equalsIgnoreCase("setlogin")) {
                    setLoginCommand.onCommand(cs, cmd, s, args);
                }
                break;
            default:
                sendUsage(cs);
                break;
        }
        return true;
    }

    List<String> usage = Arrays.asList(
            "§a------------------------ [§6MCWI§a] ---------------------",
            "§7- §aWebinterface reachable at " + Bukkit.getServer().getIp() + ":8080",
            "§7- §2/mcwi setlogin [Username] [Password] §6Set login data",
            "§a------------------------ [§6MCWI§a] ---------------------"
    );

    public void sendUsage(CommandSender cs) {
        usage.forEach(cs::sendMessage);
    }

}
