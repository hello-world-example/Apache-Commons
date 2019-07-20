package xyz.kail.demo.apache.common.conf.properties;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class PropertiesMain {

    public static void main(String[] args) throws ConfigurationException {

Configurations configs = new Configurations();

URL databaseRes = ClassLoader.getSystemResource("database.properties");

System.out.println(databaseRes);

FileBasedConfigurationBuilder<PropertiesConfiguration> configurationBuilder = configs.propertiesBuilder(databaseRes);

PropertiesConfiguration config = configurationBuilder.getConfiguration();

String dbHost = config.getString("database.host");

int dbPort = config.getInt("database.port");

List<String> addresses = config.getList(String.class, "zk.addresses");

Iterator<String> database = config.getKeys("database");

config.setProperty("database.password", 1723);

config.addProperty("zk.addresses", "new-zk2");

configurationBuilder.save();

    }

}
