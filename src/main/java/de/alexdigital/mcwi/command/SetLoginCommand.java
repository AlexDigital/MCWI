package de.alexdigital.mcwi.command;

import de.alexdigital.mcwi.McWebinterface;
import de.alexdigital.mcwi.util.Constants;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLoginCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String s, String[] args) {
        if (cs.hasPermission("mcwi.setlogin")) {
            String username = cs instanceof Player ? cs.getName() : "admin";
            McWebinterface.getInstance().getLoginConfig().setLoginData(username, args[1]);
            cs.sendMessage(Constants.LOGINDATA_SUCCESS);
        } else {
            cs.sendMessage(Constants.NO_PERM);
        }
        return true;
    }

}
