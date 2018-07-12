package de.cedricrupb;

import com.google.common.eventbus.EventBus;
import de.cedricrupb.react.controller.*;
import de.cedricrupb.utils.ObjectRecorder;
import de.cedricrupb.utils.ObjectRecorderManager;


/**
 *
 * Class determines Application Context, records objects and gives context based on Controllers
 *
 * @author Cedric Richter
 */


public class ApplicationContext {

    public static ApplicationContext createDefault(EventBus bus){
        ApplicationContext ctx = new ApplicationContext(bus);
        bus.register(new ConfigLoadingController(ctx));
        bus.register(new ClassLearningController(ctx));
        bus.register(new PropertyLearningController(ctx));
        bus.register(new ExampleFindingController(ctx));
        bus.register(new QualityController(ctx));
        bus.register(new TerminationController(ctx));
        //bus.register(new ExampleExpansionController(ctx));
        return ctx;
    }

    private EventBus bus;

    private ObjectRecorderManager objectRecorder = new ObjectRecorderManager();

    public ApplicationContext(EventBus bus) {
        this.bus = bus;
        ObjectRecorder recorder = new ObjectRecorder((o) -> objectRecorder.registerObjectRecorder(o));
        objectRecorder.registerObjectRecorder(recorder);
    }

    public EventBus getBus() {
        return bus;
    }

    public ObjectRecorderManager getObjectRecorder() {
        return objectRecorder;
    }



}
