package cl.ucn.disc.dsm.pictwin.web.routes;

import cl.ucn.disc.dsm.pictwin.services.Controller;
import cl.ucn.disc.dsm.pictwin.web.Route;
import io.javalin.http.Handler;
import lombok.extern.slf4j.Slf4j;
import cl.ucn.disc.dsm.pictwin.Utils.FileUtils;
import java.io.File;

@Slf4j
public class PersonaPic extends Route {

    public PersonaPic(Controller controller) {

        super(Method.POST, "/api/personas/{ulid}/pic");
        this.handler = buildHandler(controller);
    }

    private static Handler buildHandler(Controller controller) {
        return ctx-> {
            String ulid = ctx.pathParam("ulid");
            Double latitude = Double.parseDouble(ctx.pathParam("latitude"));
            Double longitude = Double.parseDouble(ctx.pathParam("longitude"));
            String picture_name = ctx.pathParam("picture");
            File picture = FileUtils.getResourceFile(picture_name);
            log.debug("Detected ulid={} for Persona.", ulid);
            ctx.json(controller.addPic(ulid,latitude,longitude,picture));
        };
    }
}
