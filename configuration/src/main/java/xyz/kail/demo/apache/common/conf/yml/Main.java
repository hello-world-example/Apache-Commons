package xyz.kail.demo.apache.common.conf.yml;

import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.net.URL;

public class Main {

    public static void main(String[] args) throws ConfigurationException {
URL myYaml = ClassLoader.getSystemResource("my.yaml");

Configurations configurations = new Configurations();

YAMLConfiguration configuration = configurations.fileBased(YAMLConfiguration.class, myYaml);

String string = configuration.getString("spring.application.name");

        System.out.println(string);


    }

}
