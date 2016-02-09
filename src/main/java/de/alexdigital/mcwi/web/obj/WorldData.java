package de.alexdigital.mcwi.web.obj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class WorldData {

    @Getter
    public String name;

    @Getter
    public String difficulty;

    @Getter
    public String seed;

}
