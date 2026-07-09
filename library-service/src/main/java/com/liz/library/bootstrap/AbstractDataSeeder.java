package com.liz.library.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataSeeder {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public abstract SeedType type();

    public int order() {
        return type().getOrder();
    }

    public abstract void seed() throws Exception;

    public void run() {
        log.info("Starting seeder: {}", type());
        try {
            seed();
            log.info("Finished seeder: {}", type());
        } catch (Exception ex) {
            log.error("Seeder {} failed: {}", type(), ex.getMessage(), ex);
        }
    }
}
