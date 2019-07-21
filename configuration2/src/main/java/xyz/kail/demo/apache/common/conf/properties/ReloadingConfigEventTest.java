package xyz.kail.demo.apache.common.conf.properties;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.EventListenerParameters;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ReloadingConfigEventTest {

    public static void main(String[] args) throws Exception {

URL pathsRes = ClassLoader.getSystemResource("comment.properties");

EventListenerParameters listenerParams = new EventListenerParameters();

listenerParams.addEventListener(
        ConfigurationBuilderEvent.RESET,
        event -> System.out.println("Event:" + event.getEventType().getName())
);

FileBasedBuilderParameters parameters = new Parameters().fileBased().setURL(pathsRes);

ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
        new ReloadingFileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(parameters, listenerParams);

PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(
        builder.getReloadingController(),
        null,
        1,
        TimeUnit.SECONDS);

trigger.start();

    }
} 