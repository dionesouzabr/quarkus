package io.quarkus.micrometer.runtime.binder.vertx;

import java.util.function.Consumer;

import io.quarkus.arc.Arc;
import io.quarkus.micrometer.runtime.binder.HttpBinderConfiguration;
import io.quarkus.micrometer.runtime.export.exemplars.OpenTelemetryContextUnwrapper;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.runtime.annotations.RuntimeInit;
import io.quarkus.runtime.annotations.StaticInit;
import io.vertx.core.VertxOptions;

@Recorder
public class VertxMeterBinderRecorder {

    static VertxMeterBinderAdapter binderAdapter = new VertxMeterBinderAdapter();
    static volatile HttpBinderConfiguration devModeConfig;

    @StaticInit
    public Consumer<VertxOptions> setVertxMetricsOptions() {
        return new Consumer<>() {
            @Override
            public void accept(VertxOptions vertxOptions) {
                vertxOptions.setMetricsOptions(binderAdapter);
            }
        };
    }

    @RuntimeInit
    public void configureBinderAdapter() {
        HttpBinderConfiguration httpConfig = Arc.container().instance(HttpBinderConfiguration.class).get();
        OpenTelemetryContextUnwrapper openTelemetryContextUnwrapper = Arc.container()
                .instance(OpenTelemetryContextUnwrapper.class).get();
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT) {
            if (devModeConfig == null) {
                // Create an object whose attributes we can update
                devModeConfig = httpConfig.unwrap();
                binderAdapter.initBinder(devModeConfig, openTelemetryContextUnwrapper);
            } else {
                // update config attributes
                devModeConfig.update(httpConfig);
            }
        } else {
            // unwrap the CDI bean (use POJO)
            binderAdapter.initBinder(httpConfig.unwrap(), openTelemetryContextUnwrapper);
        }
    }
}
