package de.alexdigital.mcwi.web.obj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class LoginSuccessResponse {

    @Getter
    private String username;

    @Getter
    private String session;

}
