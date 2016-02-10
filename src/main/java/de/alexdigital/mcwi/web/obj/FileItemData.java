package de.alexdigital.mcwi.web.obj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class FileItemData {

    @Getter
    private String name;

    @Getter
    private String type;

    @Getter
    private long size;

    @Getter
    private String path;

    @Getter
    private boolean editable;

}
