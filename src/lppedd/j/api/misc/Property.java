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
package lppedd.j.api.misc;

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
    * @param name       Nome della proprieta'
    * @param parameters Parametri ordinati, in sostituzione dei placeholder, se
    *                   necessario
    */
   public final String get(final String name, final Object... parameters) {
      if (parameters.length > 0) {
         return format(properties.getProperty(name), parameters);
      }

      return properties.getProperty(name);
   }
}
