package lppedd.misc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.String.format;

/**
 * @author Edoardo Luppi
 */
public class Property
{
    private final Properties properties;

    {
        properties = new Properties();
    }

    public Property(final InputStream input) {
        try {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restituisce una proprieta'
     *
     * @param name Nome della proprieta'
     * @param parameters Parametri ordinati, in sostituzione dei placeholder, se
     * necessario
     */
    public final String get(final String name, final Object... parameters) {
        if (parameters.length > 0) {
            return format(properties.getProperty(name), parameters);
        }

        return properties.getProperty(name);
    }
}
