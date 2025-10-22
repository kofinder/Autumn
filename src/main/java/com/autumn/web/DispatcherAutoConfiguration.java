package com.autumn.web;

import com.autumn.AutumnApplicationContext;
import com.autumn.beans.Autowired;
import com.autumn.beans.Component;
import com.autumn.beans.ConditionalOnMissingBean;
import com.autumn.beans.PostConstruct;
import com.autumn.web.converter.JsonMessageConverter;

import java.io.IOException;

@Component
@ConditionalOnMissingBean(MiniDispatcher.class)
public class DispatcherAutoConfiguration {

    @Autowired
    private AutumnApplicationContext context;

    @PostConstruct
    public void startDispatcher() throws IOException {
        MiniDispatcher dispatcher = new MiniDispatcher(context);

        dispatcher.addConverter(new JsonMessageConverter());

        dispatcher.start(8080);
    }
}
