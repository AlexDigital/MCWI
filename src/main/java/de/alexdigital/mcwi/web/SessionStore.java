package de.alexdigital.mcwi.web;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomUtils;

import java.util.HashMap;

public class SessionStore extends HashMap<String, String> {

    public void setSession(String username, String sessionCode) {
        this.put(username, sessionCode);
    }

    public String getSession(String username) {
        if (this.containsKey(username)) {
            return this.get(username);
        } else {
            String session = new String(Hex.encodeHex(RandomUtils.nextBytes(8)));
            this.setSession(username, session);
            return session;
        }
    }

    public void removeSession(String username) {
        if (this.containsKey(username)) {
            this.remove(username);
        }
    }

}
