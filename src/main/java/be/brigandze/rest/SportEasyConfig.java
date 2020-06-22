package be.brigandze.rest;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class SportEasyConfig  implements ConfigSource {

    @ConfigProperty(name = "x-csrftoken")
    private String xCsrfToken;
    @ConfigProperty(name = "cookie")
    private String cookie;


    @Override
    public Map<String, String> getProperties() {
        try (InputStream in = getClass().getResourceAsStream("/sporteasy.properties")) {
            Properties properties = new Properties();
            properties.load(in);

            return properties.stringPropertyNames()
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), properties::getProperty));

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public String getValue(String propertyName) {
        return getProperties().get(propertyName);
    }

    @Override
    public String getName() {
        return "env file";
    }
}
