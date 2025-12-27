package com.apress.crm.management.observability;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RegionObservationHandler implements ObservationHandler<Observation.Context> {

    private static final Logger log = LoggerFactory.getLogger(RegionObservationHandler.class);

    @Override
    public void onStart(Observation.Context context) {
        context.addLowCardinalityKeyValue(KeyValue.of("region", "US-East"));
        log.debug("Observation started with region tag: {}", context.getName());
    }

    @Override
    public void onStop(Observation.Context context) {
        log.debug("Observation stopped: {}", context.getName());
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }
}
