package de.alexdigital.mcwi.web.obj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PluginData {

    @Getter
    private String name;

    @Getter
    private String version;

    @Getter
    private String author;

}
