package de.cedricrupb.react.controller;

import com.google.common.eventbus.Subscribe;
import de.cedricrupb.ApplicationContext;
import de.cedricrupb.event.config.ConfigLoadingEvent;
import de.cedricrupb.event.config.MappingConfigEvent;
import de.cedricrupb.event.config.SourceInfoEvent;
import de.cedricrupb.event.config.TargetInfoEvent;

/**
 *
 * Class that loads configurations for source, target and mapping.
 *
 * @author Cedric Richter
 */


public class ConfigLoadingController {

    private ApplicationContext ctx;

    public ConfigLoadingController(ApplicationContext ctx) {
        this.ctx = ctx;
        this.ctx.getBus().register(this);
    }

    @Subscribe
    public void onConfig(ConfigLoadingEvent event){
        this.ctx.getBus().post(new SourceInfoEvent(event.getConfig(), event.getConfig().getSrcConfig()));
        this.ctx.getBus().post(new TargetInfoEvent(event.getConfig(),event.getConfig().getTargetConfig()));
        this.ctx.getBus().post(new MappingConfigEvent(event.getConfig(), event.getConfig().getMapping()));
    }

}
