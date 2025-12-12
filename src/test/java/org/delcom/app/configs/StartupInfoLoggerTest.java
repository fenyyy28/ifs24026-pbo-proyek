package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StartupInfoLoggerTest {

    @Test
    void testOnApplicationEvent() {
        StartupInfoLogger logger = new StartupInfoLogger();

        // Mock Event & Context & Environment
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);

        when(event.getApplicationContext()).thenReturn(context);
        when(context.getEnvironment()).thenReturn(env);

        // Case 1: Default properties
        when(env.getProperty("server.port", "8080")).thenReturn("9090");
        when(env.getProperty("server.servlet.context-path", "/")).thenReturn("/");
        when(env.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(true);
        when(env.getProperty("spring.devtools.livereload.port", "35729")).thenReturn("35729");
        when(env.getProperty("server.address", "localhost")).thenReturn("127.0.0.1");

        logger.onApplicationEvent(event);

        // Case 2: Context path exists
        when(env.getProperty("server.servlet.context-path", "/")).thenReturn("/app");
        when(env.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(false);
        
        logger.onApplicationEvent(event);
        
        // Case 3: Context path null
        when(env.getProperty("server.servlet.context-path", "/")).thenReturn(null);
        logger.onApplicationEvent(event);
    }
}