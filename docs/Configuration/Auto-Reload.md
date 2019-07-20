# 自动加载



```java
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
```

