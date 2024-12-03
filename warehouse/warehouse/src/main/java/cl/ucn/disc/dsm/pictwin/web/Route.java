/*
 *Copyright (c) 2024. Departamento de Ingenieria de Sistemas y Computacion
 */
package cl.ucn.disc.dsm.pictwin.web;

import io.javalin.http.Handler;

import lombok.Getter;
import lombok.NonNull;

/**The Route. */
@Getter
public abstract class Route {

    /**The Method.*/
    protected Method method;

    /**The path.*/
    protected String path;

    /**The handler.*/
    protected Handler handler;

    /**The Constructor.*/
    protected Route(@NonNull final Method method, @NonNull final String path) {
        this.method =method;
        this.path = path;
    }

    /**Methods.*/
    public enum Method {
        GET,
        POST,
        PUT,
    }
}
