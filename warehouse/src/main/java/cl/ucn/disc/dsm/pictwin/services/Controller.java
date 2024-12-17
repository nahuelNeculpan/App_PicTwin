/*
 *Copyright (c) 2024. Departamento de Ingenieria de Sistemas y Computacion
 */
package cl.ucn.disc.dsm.pictwin.services;

import cl.ucn.disc.dsm.pictwin.model.Persona;
import cl.ucn.disc.dsm.pictwin.model.Pic;
import cl.ucn.disc.dsm.pictwin.model.PicTwin;
import cl.ucn.disc.dsm.pictwin.model.query.QPersona;
import cl.ucn.disc.dsm.pictwin.model.query.QPicTwin;
import cl.ucn.disc.dsm.pictwin.Utils.FileUtils;

import com.password4j.Password;

import io.ebean.Database;
import io.ebean.annotation.Transactional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.Instant;
import java.util.List;

@Slf4j
public class Controller {
    /** The database. */
    private final Database database;

    public Controller(@NonNull final Database database) {
        this.database = database;
    }


    /**The seed of the database. */
    public Boolean seed() {
        // find the Persona size
        int personaSize = new QPersona().findCount();
        log.debug("Personas in database: " + personaSize);
        // if the Persona exists → don't seed!
        if (personaSize != 0) {
            return Boolean.FALSE;
        }
        log.debug("Can't find data, seeding the database..");
        //seed the Persona
        Persona persona = this.register("durrutia@ucn.c1", "durrutia123");
        log.debug("Persona registered: {}", persona);
        log.debug("Database seeded.");
        return Boolean.TRUE;
    }


    /** Register a new user. */
    @Transactional
    public Persona register(@NonNull final String email, @NonNull final String password) {
        // hash the password
        String hashedPassword = Password.hash(password).withBcrypt().getResult();
        log.debug("Hashed password: {}",hashedPassword);
        // build the Persona
        Persona persona =
                Persona.builder()
                        .email(email)
                        .password (hashedPassword)
                        .strikes (0)
                        .blocked(false)
                        .build();
        // save the Persona
        this.database.save(persona);
        log.debug("Persona saved: {}",persona);
        return persona;
    }

    /** Login a user. */
    public Persona login(@NonNull final String email, @NonNull final String password) {
        // find the Persona
        Persona persona = new QPersona().email.equalTo(email).findOne();
        if (persona == null) {
            throw new RuntimeException("User not found");
        }
        // check the password
        if (!Password.check (password,persona.getPassword()).withBcrypt()) {
            throw new RuntimeException("Wrong password");
        }
        return persona;
    }

    /**Add a new Pic. */
    @Transactional
    public PicTwin addPic(
            @NonNull String ulidPersona,
            @NonNull Double latitude,
            @NonNull Double longitude,
            @NonNull File picture) {
        // read the file
        byte[] data = FileUtils.readAllBytes(picture);
        // find the Persona
        Persona persona = new QPersona().ulid.equalTo(ulidPersona).findOne();
        log.debug("Persona found: {}", persona);
        // save the Pic
        Pic pic=
                Pic.builder()
                        .latitude (latitude)
                        .longitude (longitude)
                        .reports (0)
                        .date (Instant.now())
                        .photo(data)
                        .bloqued(false)
                        .views (0)
                        .persona (persona)
                        .build();
        log.debug("Pic to save: {}", pic);
        this.database.save(pic);
        // save the PicTwin
        PicTwin picTwin =
                PicTwin.builder()
                        .expiration (Instant.now().plusSeconds (7 * 24 * 60 *60))
                        .expired(false)
                        .reported(false) .persona (persona)
                        .pic(pic)
                        .twin (pic) // FIXME: retrieve a new pic from the database
                        .build();
        log.debug("PicTwin to save: {}", picTwin);
        this.database.save(picTwin);
        return picTwin;
    }
    /** Get the PicTwins. */
    public List<PicTwin> getPicTwins (@NonNull String ulidPersona) {
        return new QPicTwin().persona.ulid.equalTo(ulidPersona).findList();
    }

}
