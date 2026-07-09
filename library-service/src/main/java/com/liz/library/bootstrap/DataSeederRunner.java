package com.liz.library.bootstrap;

import com.liz.library.bootstrap.config.SeedProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class DataSeederRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederRunner.class);

    private final List<AbstractDataSeeder> seeders;
    private final SeedProperties seedProperties;

    @Override
    public void run(ApplicationArguments args) {
        log.info("DataSeederRunner starting with seed types: {}", seedProperties.getTypes());

        seeders.stream()
                .sorted(Comparator.comparingInt(AbstractDataSeeder::order))
                .forEach(seeder -> {
                    if (seedProperties.shouldRun(seeder.type())) {
                        seeder.run();
                    } else {
                        log.info("Seeder {} skipped by configuration", seeder.type());
                    }
                });

        log.info("DataSeederRunner finished.");
    }
}
