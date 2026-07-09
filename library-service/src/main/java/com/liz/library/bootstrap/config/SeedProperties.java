package com.liz.library.bootstrap.config;

import com.liz.library.bootstrap.SeedType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.seed")
public class SeedProperties {

    private boolean enabled = false;

    private List<SeedType> types = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<SeedType> getTypes() {
        return types;
    }

    public void setTypes(List<SeedType> types) {
        this.types = types;
    }

    public boolean shouldRun(SeedType t) {
        // If no types specified, run only ROLE by default (important minimal seeding).
        if (types == null || types.isEmpty()) {
            return t == SeedType.ROLE;
        }
        // If ALL present, run all seeders.
        if (types.contains(SeedType.ALL)) {
            return true;
        }
        return types.contains(t);
    }
}
