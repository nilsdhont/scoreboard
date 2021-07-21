package be.brigandze.sporteasy;

import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.spi.ConfigSource;

@Getter
@Setter
public class SportEasyConfig implements ConfigSource {

    private final static Logger LOGGER = Logger.getLogger(SportEasyConfig.class.getName());

    @Override
    public Map<String, String> getProperties() {
        try (InputStream in = getClass().getResourceAsStream("/sporteasy.env")) {
            Properties properties = new Properties();
            properties.load(in);

            return properties.stringPropertyNames()
                .stream()
                .collect(Collectors.toMap(Function.identity(), properties::getProperty));

        } catch (IOException e) {
            LOGGER.log(SEVERE, "Error getting properties from sporteasy.env file ", e);
        }
        return null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return new HashSet<>();
    }

    @Override
    public int getOrdinal() {
        return ConfigSource.super.getOrdinal();
    }

    @Override
    public String getValue(String propertyName) {
        return getProperties().get(propertyName);
    }

    @Override
    public String getName() {
        return "env file";
    }

    public String getUsername() {
        return getValue("username");
    }

    public String getPassword() {
        return getValue("password");
    }
}
