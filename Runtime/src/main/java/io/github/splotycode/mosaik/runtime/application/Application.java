package io.github.splotycode.mosaik.runtime.application;

import lombok.Getter;
import io.github.splotycode.mosaik.runtime.startup.BootContext;
import io.github.splotycode.mosaik.runtime.startup.environment.ConfiguriseEnvironmentChanger;
import io.github.splotycode.mosaik.util.datafactory.DataFactory;
import io.github.splotycode.mosaik.util.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class Application implements IApplication {

    @Getter private ShutdownManager localShutdownManager = new ShutdownManager();
    @Getter private static ShutdownManager globalShutdownManager = new ShutdownManager();

    @Getter private ApplicationState state = ApplicationState.NOT_FOUND;

    @Getter private DataFactory dataFactory = new DataFactory();
    @Getter private DataFactory config = new DataFactory();
    @Getter private List<Class<ApplicationType>> applicationTypes = new ArrayList<>();

    @Getter private Logger logger = Logger.getInstance("Application: " + getName());

    public abstract void start(BootContext context) throws Exception;
    public void configurise(ConfiguriseEnvironmentChanger environmentChanger, DataFactory config) throws Exception {}

    @Override
    public synchronized void setState(ApplicationState state) {
        logger.trace("Switched from State " + this.state + " to " + state);
        this.state = state;
        notify();
    }

    public synchronized void awaitState(ApplicationState state) throws InterruptedException {
        while (this.state != state) {
            wait();
        }
    }

    @Override
    public Application getApplication() {
        return this;
    }

}
