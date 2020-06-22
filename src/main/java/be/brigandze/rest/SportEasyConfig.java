package be.brigandze.rest;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class SportEasyConfig implements ConfigSource {

    @Override
    public Map<String, String> getProperties() {
        try (InputStream in = getClass().getResourceAsStream("/sporteasy.env")) {
            Properties properties = new Properties();
            properties.load(in);

            Map<String, String> collect = properties.stringPropertyNames()
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), properties::getProperty));
            return collect;

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
