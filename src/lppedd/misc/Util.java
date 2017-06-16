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
package lppedd.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.lang.Character.digit;
import static java.lang.Character.isWhitespace;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.String.format;

/**
 * @author Edoardo Luppi
 */
public final class Util
{
   public static int hashCode(final Object object) {
      return object == null ? 0 : object.hashCode();
   }

   public static <T> List<List<T>> splitList(final List<T> list, final int parts) {
      final List<List<T>> subLists = new ArrayList<>(parts);
      final int listSize = list.size();
      final int subListSize = listSize / parts;
      int leftOver = listSize % parts;
      int take = subListSize;

      for (int i = 0, iT = listSize; i < iT; i += take) {
         if (leftOver > 0) {
            leftOver--;
            take = subListSize + 1;
         } else {
            take = subListSize;
         }

         subLists.add(new ArrayList<>(list.subList(i, min(iT, i + take))));
      }

      return subLists;
   }

   public static <T> List<Set<T>> split(final Set<T> original, final int count) {
      final List<Set<T>> l = new ArrayList<>(count);
      final Iterator<T> it = original.iterator();

      // Calculate required number for each sub-set
      final int each = original.size() / count;

      // Loop for each new set
      for (int i = 0; i < count; i++) {
         // Create the set.
         final Set<T> s = new HashSet<>(original.size() / count + 1);
         l.add(s);

         for (int j = 0; j < each && it.hasNext(); j++) {
            // Add the element to the set.
            s.add(it.next());
         }
      }

      return l;
   }

   /**
    * Ritorna una stringa alfanumerica casuale (senza caratteri speciali)
    *
    * @param lenght Lunghezza della stringa
    *
    * @return Stringa casuale
    */
   public static String getRandomString(final int lenght) {
      final char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
      final StringBuilder builder = new StringBuilder(lenght);
      final Random random = new Random();

      for (int i = 0; i < lenght; i++) {
         final char c = chars[random.nextInt(chars.length)];
         builder.append(c);
      }

      return builder.toString();
   }

   /**
    * Concatena tutti gli elementi di un array aggiungendo un separatore
    *
    * @param array     Array di stringhe da concatenare
    * @param separator Separatore
    */
   public static String joinStringArray(final String[] array, final String separator) {
      final StringBuilder builder = new StringBuilder(array.length * 16 * separator.length());

      for (final String string : array) {
         builder.append(string).append(separator);
      }

      builder.delete(builder.length() - separator.length(), builder.length());
      return builder.toString();
   }

   /**
    * Ritorna la posizione del primo carattere trovato, dato dalla lista
    * caratteri, nella stringa
    *
    * @param string Stringa da scansionare
    * @param chars  Set di caratteri da controllare
    */
   public static int indexOfAny(final String string, final String chars) {
      if (string.isEmpty()) {
         return -1;
      }

      for (int i = 0; i < string.length(); i++) {
         final char c = string.charAt(i);

         for (final char searchChar : chars.toCharArray()) {
            if (searchChar == c) {
               return i;
            }
         }
      }

      return -1;
   }

   /**
    * Ritorna la posizione del primo carattere, non in lista caratteri, trovato
    * nella stringa
    *
    * @param string Stringa da scansionare
    * @param chars  Set di caratteri da non considerare
    */
   public static int indexOfAnyBut(final String string, final String chars) {
      if (string.isEmpty()) {
         return -1;
      }

      for (int i = 0; i < string.length(); i++) {
         final char c = string.charAt(i);

         if (chars.indexOf(c) < 0) {
            return i;
         }
      }

      return -1;
   }

   /**
    * Splitta una stringa ogni tot caratteri
    *
    * @param string   Stringa da dividere
    * @param interval Intervallo
    */
   public static String[] splitString(final String string, final int interval) {
      if (string.isEmpty()) {
         return new String[0];
      }

      final int arrayLength = (int) ceil(string.length() / (double) interval);
      final String[] result = new String[arrayLength];

      final int lastIndex = arrayLength - 1;
      int j = 0;

      for (int i = 0; i < lastIndex; i++, j += interval) {
         result[i] = string.substring(j, j + interval).trim();
      }

      result[lastIndex] = string.substring(j).trim();
      return result;
   }

   /**
    * Trimma una stringa a sinistra
    *
    * @param string Stringa da trimmare
    */
   public static String ltrim(final String string) {
      int i = 0;

      while (i < string.length() && isWhitespace(string.charAt(i))) {
         i++;
      }

      return string.substring(i);
   }

   /**
    * Trimma una stringa a destra
    *
    * @param string Stringa da trimmare
    */
   public static String rtrim(final String string) {
      int i = string.length() - 1;

      while (i >= 0 && isWhitespace(string.charAt(i))) {
         i--;
      }

      return string.substring(0, i + 1);
   }

   /**
    * Costruisce una stringa.
    *
    * @param length  La dimensione totale della stringa
    * @param objects La lista degli oggetti da concatenare
    */
   public static String buildString(final int length, final Object... objects) {
      final StringBuilder builder = new StringBuilder(length > 0 ? length : objects.length * 6);

      for (final Object object : objects) {
         if (object instanceof String) {
            builder.append((String) object);
         } else {
            builder.append(object);
         }
      }

      return builder.toString();
   }

   public static String leftPad(final String string, final int length) {
      return format("%1$" + length + "s", string);
   }

   public static String rightPad(final String string, final int length) {
      return format("%1$-" + length + "s", string);
   }

   public static boolean isInteger(final String string) {
      return isInteger(string, 10);
   }

   public static boolean isInteger(final String string, final int radix) {
      if (string.isEmpty()) {
         return false;
      }

      for (int i = 0; i < string.length(); i++) {
         if (i == 0 && string.charAt(i) == '-') {
            if (string.length() == 1) {
               return false;
            }

            continue;
         }

         if (digit(string.charAt(i), radix) < 0) {
            return false;
         }
      }

      return true;
   }
}
