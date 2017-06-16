package lppedd.j.misc;

import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.QSYSObjectPathName;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lppedd.misc.Util;

import static lppedd.misc.Util.indexOfAnyBut;

/**
 * Utility methods for the IBMi environment.
 *
 * @author Edoardo Luppi
 */
public final class JUtil
{
   protected static final char[] SPECIFICATIONS = {
      'C',
      'D',
      'H',
      'F',
      'O',
      'A'
   };

   private static Constructor<AS400Message> as400Message;

   static {
      try {
         as400Message = (Constructor<AS400Message>) AS400Message.class.getDeclaredConstructors()[2];
         as400Message.setAccessible(true);
      } catch (final SecurityException e) {
         e.printStackTrace();
      }
   }

   /**
    * Ritorna un nuovo {@link AS400Message} personalizzato tramite reflection
    *
    * @param id   ID messaggio
    * @param text Testo messaggio
    */
   public static AS400Message newMessage(final String id, final String text) {
      if (as400Message != null) {
         try {
            return as400Message.newInstance(id, text);
         } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
         }
      }

      return null;
   }

   public static boolean checkForMessage(final String id, final AS400Message[] messages) {
      for (final AS400Message message : messages) {
         if (id.equals(message.getID())) {
            return true;
         }
      }

      return false;
   }

   /**
    * Ritorna una stringa alfanumerica casuale adatta ad AS400 (lunga 10 e
    * iniziante per lettera)
    *
    * @param length TODO
    */
   public static final String getRandomString(final int length) {
      String string = null;

      while ((string = Util.getRandomString(length)).substring(0, 1).matches("[0-9]")) {
         ;
      }

      return string.toUpperCase();
   }

   /**
    * Ritorna la path formattata del membro/oggetto
    */
   public static String getFormattedPath(final QSYSObjectPathName path) {
      return path.getLibraryName() + "/" + path.getObjectName() + (!path.getMemberName().isEmpty() ? "/" + path.getMemberName() : "");
   }

   /**
    * Ritorna la path formattata di 20 caratteri: 10 oggetto e 10 libreria.<br>
    * Versione migliorata di {@link QSYSObjectPathName#toQualifiedObjectName()}
    *
    * @param library Libreria
    * @param object  Oggetto
    */
   public static String getQualifiedPath(final String library, final String object) {
      final int objectLength = object.length();
      final int libraryLength = library.length();
      final StringBuilder builder = new StringBuilder("                    ");

      if (objectLength > 10) {
         builder.replace(0, 10, object.substring(0, 10));
      } else {
         builder.replace(0, objectLength, object);
      }

      if (libraryLength > 10) {
         builder.replace(10, 20, library.substring(0, 10));
      } else {
         builder.replace(10, libraryLength + 10, library);
      }

      return builder.toString();
   }

   /**
    * Commenta una riga di sorgente.
    *
    * @param line La linea da commentare.
    */
   public static String doComment(final String line) {
      final int length = line.length();
      final StringBuilder builder = new StringBuilder(length + 3);

      if (isSpecification(line.charAt(5))) {
         // RPG posizionale.
         builder.append(line.substring(0, 6));
         builder.append("*");

         if (length > 6) {
            builder.append(line.substring(6));
         }
      } else {
         // RPG formato libero.
         // Trovo da dove inizia l'istruzione, senza considerare le prime 8 posizione, che non sono valide.
         final int i = indexOfAnyBut(line.substring(7), " ") + 7;
         builder.append(line.substring(0, i));
         builder.append("// ");
         builder.append(line.substring(i));
      }

      return builder.toString();
   }

   /**
    * Verifica se una riga di sorgente e' un commento
    */
   public static boolean isComment(final String line) {
      // SQL - RPG posizionale - RPG free - CL
      return line.startsWith("--") || line.charAt(6) == '*' || line.trim().startsWith("//") || line.trim().startsWith("/*")
              || !line.contains("/*") && line.trim().endsWith("*/");
   }

   /**
    * Verifica se si tratta di una specifica di calcolo
    */
   public static boolean isSpecification(final char spec) {
      for (final char s : SPECIFICATIONS) {
         if (s == spec) {
            return true;
         }
      }

      return false;
   }
}
