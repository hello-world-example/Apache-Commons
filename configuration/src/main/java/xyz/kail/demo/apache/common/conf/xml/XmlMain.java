package xyz.kail.demo.apache.common.conf.xml;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class XmlMain {

    public static void main(String[] args) throws ConfigurationException {
Configurations configs = new Configurations();

URL pathsRes = ClassLoader.getSystemResource("paths.xml");

XMLConfiguration xmlConfiguration = configs.xml(pathsRes);

String stage = xmlConfiguration.getString("processing[@stage]");

List<String> paths = xmlConfiguration.getList(String.class, "processing.paths.path");

String secondPath = xmlConfiguration.getString("processing.paths.path(1)");
    }

}
