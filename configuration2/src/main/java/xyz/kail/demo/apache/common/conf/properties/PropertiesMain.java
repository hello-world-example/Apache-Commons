package xyz.kail.demo.apache.common.conf.properties;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class PropertiesMain {

    public static void main(String[] args) throws ConfigurationException, IOException {

//
//        URL pathsRes = ClassLoader.getSystemResource("comment.properties");
//
//        Configurations configs = new Configurations();
//
//        PropertiesConfiguration properties = configs.properties(pathsRes);
//
//        ReloadingStrategy strategy =new FileChangedReloadingStrategy();
//        strategy.setRefreshDelay(5000);
//        config.setReloadingStrategy(strategy);
//
//        System.in.read();

    }
}



