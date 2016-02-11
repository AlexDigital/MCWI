package de.alexdigital.mcwi.util;

public class LoginConfig extends YamlConfig {

    public LoginConfig() {
        super("login");
    }

    public void setLoginData(String username, String password) {
        this.getConfig().set(username, SHA256.getInstance().hash(password));
    }

    public boolean check(String username, String password) {
        if (this.getConfig().contains(username)) {
            return SHA256.getInstance().hash(password).equals(this.getConfig().getString(username));
        } else {
            return false;
        }
    }

}
