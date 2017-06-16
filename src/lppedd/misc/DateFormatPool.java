package lppedd.misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Edoardo Luppi
 */
public final class DateFormatPool
{
    private static final Map<Long, DateFormat> FORMATTERS = new HashMap<>(16);

    /**
     * Ritorna un formattatore per il pattern specificato
     *
     * @param pattern Il pattern del formattatore
     */
    public static DateFormat get(final String pattern) {
        final long key = pattern.hashCode();
        DateFormat formatter = FORMATTERS.get(key);

        if (formatter == null) {
            formatter = new SimpleDateFormat(pattern);
            FORMATTERS.put(key, formatter);
        }

        return formatter;
    }

    /**
     * Ritorna un formattatore per il pattern specificato
     *
     * @param pattern Il pattern del formattatore
     * @param locale Il locale del formattatore
     */
    public static DateFormat get(final String pattern, final Locale locale) {
        final long key = pattern.hashCode() + locale.hashCode();
        DateFormat formatter = FORMATTERS.get(key);

        if (formatter == null) {
            formatter = new SimpleDateFormat(pattern, locale);
            FORMATTERS.put(key, formatter);
        }

        return formatter;
    }
}
