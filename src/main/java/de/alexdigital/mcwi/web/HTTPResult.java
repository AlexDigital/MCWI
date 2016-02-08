package de.alexdigital.mcwi.web;

import lombok.Getter;
import lombok.Setter;

public class HTTPResult {

    @Getter
    @Setter
    private String method;

    @Getter
    @Setter
    private String path;

    @Getter
    @Setter
    private String httpVersion;

    @Getter
    @Setter
    private String host;

    @Getter
    @Setter
    private String userAgent;

}
