/* 
 * The MIT License
 *
 * Copyright (c) 2017 Edoardo Luppi <lp.edoardo@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package j.misc;

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
    * @param locale  Il locale del formattatore
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
