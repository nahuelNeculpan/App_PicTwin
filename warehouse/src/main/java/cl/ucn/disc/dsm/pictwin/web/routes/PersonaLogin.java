/*
 * Copyright (c) 2024. Departamento de Ingenieria de Sistemas y Computacion
 */

package cl.ucn.disc.dsm.pictwin.web.routes;

import cl.ucn.disc.dsm.pictwin.services.Controller;
import cl.ucn.disc.dsm.pictwin.web.Route;

import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

/** The Persona Login route. */
@Slf4j
public class PersonaLogin extends Route {

    /** The Constructor. */
    public PersonaLogin(Controller controller) {
        super(Method.POST, "/api/personas");

        this.handler = buildHandler(controller);
    }

    private Handler buildHandler(Controller controller) {
        return ctx -> {
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");

            log.debug("Detected email={} and password={} for Persona.", email, password);

            // can't find password or email
            if (StringUtils.isEmpty(password) || StringUtils.isEmpty(email)) {
                ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
                return;
            }

            // trim the email and password
            email = email.trim();
            password = password.trim();

            ctx.json(controller.login(email, password));
        };
    }
}