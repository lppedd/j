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
package lppedd.j;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400JDBCConnectionHandle;
import com.ibm.as400.access.AS400JDBCConnectionPoolDataSource;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDescription;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ObjectList;
import com.ibm.as400.access.SystemValue;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import lppedd.j.enums.JType;
import lppedd.j.interfaces.JObject;

import static com.ibm.as400.access.AS400.FILE;
import static com.ibm.as400.access.ObjectDescription.EXTENDED_ATTRIBUTE;
import static com.ibm.as400.access.ObjectDescription.LIBRARY;
import static com.ibm.as400.access.ObjectDescription.NAME;
import static com.ibm.as400.access.ObjectList.ALL;
import static lppedd.j.JLibraryList.ListPosition.AFTER;
import static lppedd.j.JObjectFactory.get;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * @author Edoardo Luppi
 */
public class JConnection
{
   /**
    * Una sola istanza possibile, il che significa una sola connessione attiva
    */
   private static JConnection _instance = null;

   /**
    * Ritorna l'unica connessione globale
    */
   public static synchronized JConnection getInstance() {
      if (_instance == null) {
         _instance = new JConnection();
      }

      return _instance;
   }

   private AS400 _AS400;
   private Connection _connection;
   private CommandCall _commandCall;
   private JLibraryList _libraryList;

   private JConnection() {
      //
   }

   public boolean connect(final String ip, final String user, final String password) {
      if (isConnected()) {
         return false;
      }

      final AS400JDBCConnectionPoolDataSource dataSource = new AS400JDBCConnectionPoolDataSource(ip, user, password);
      dataSource.setPrompt(false);

      try {
         _connection = dataSource.getPooledConnection().getConnection();
         _AS400 = new AS400(ip, user, password);
         _AS400.setGuiAvailable(false);
         _AS400.connectService(FILE);
      } catch (AS400SecurityException | IOException | PropertyVetoException | SQLException e) {
         e.printStackTrace();
         disconnect();
         return false;
      }

      _commandCall = new CommandCall(_AS400);

      try {
         _libraryList = new JLibraryList(_commandCall.getServerJob());
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException e) {
         e.printStackTrace();
      }

      _libraryList.addUserLibrary("S90SRC", AFTER, "S90PTFOBJ");
      _libraryList.addSystemLibrary("SMI90PGMR");
      return true;
   }

   /**
    * Controlla se l'oggetto/membro esiste
    *
    * @param library Libreria oggetto
    * @param object  Nome oggetto
    * @param type    Tipo oggetto (se MBR, diventa FILE)
    * @param member  Nome membro (opzionale)
    */
   public boolean exists(final String library, final String object, final JType type, final String member) {
      final StringBuilder builder = new StringBuilder(92);
      builder.append("CHKOBJ OBJ(");
      builder.append(library);
      builder.append("/");
      builder.append(object);
      builder.append(") OBJTYPE(");
      builder.append(type.getObjectType());
      builder.append(") MBR(");
      builder.append(member.isEmpty() ? "*NONE" : member);
      builder.append(")");

      return executeCommand(builder.toString()).length == 0;
   }

   /**
    * Cancella tutti gli spool di un utente
    *
    * @param user L'utente di cui si vogliono cancellare gli spool
    */
   public boolean deleteSpools(final String user) {
      return checkForMessage("CPC3305", executeCommand("DLTSPLF FILE(*SELECT) SELECT(" + user + ")"));
   }

   /**
    * Esegue un comando AS400
    *
    * @param command Comando da eseguire
    *
    * @return Lista identificativi degli eventuali messaggi di errore con il
    *         relativo testo
    */
   public AS400Message[] executeCommand(final String command) {
      try {
         _commandCall.run(command);
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException
              | PropertyVetoException e) {
         e.printStackTrace();
      }

      return _commandCall.getMessageList();
   }

   /**
    * Ritorna una lista di oggetti per il tipo di oggetto passato
    *
    * @param objects Lista che verra' popolato con gli oggetti trovati
    * @param library Libreria dove cercare
    * @param type    Tipo di oggetto da cercare
    */
   public void getListOf(final List<JObject> objects, final String library, final JType type) {
      final ObjectList objectList = new ObjectList(_AS400, library, ALL, type.getObjectType());
      objectList.addObjectAttributeToRetrieve(EXTENDED_ATTRIBUTE);

      try {
         objectList.load();

         for (final ObjectDescription description : objectList.getObjects(0, objectList.getLength())) {
            final JObject object = get(description.getValueAsString(NAME).trim(),
                    description.getValueAsString(LIBRARY).trim(), type);

            if (!(object instanceof JNullObject)) {
               objects.add(object);
            }
         }
      } catch (final AS400SecurityException | ObjectDoesNotExistException | ErrorCompletingRequestException
              | InterruptedException | IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Returns the user library list.
    */
   public JLibraryList getLibraryList() {
      return _libraryList;
   }

   /**
    * Ritorna il lavoro aperto per la connessione al database
    */
   public JJob getDatabaseJob() {
      try {
         final String jobId = ((AS400JDBCConnectionHandle) _connection).getServerJobIdentifier();
         return new JJob(jobId.substring(0, 10), jobId.substring(10, 20), jobId.substring(20));
      } catch (final NullPointerException | SQLException e) {
         e.printStackTrace();
      }

      return null;
   }

   /**
    * Ritorna la connessione aperta con l' AS400
    */
   public AS400 getAS400() {
      return _AS400;
   }

   /**
    * Ritorna la connessione aperta con il database
    */
   public Connection getConnection() {
      return _connection;
   }

   /**
    * Ritorna il CommandCall attivo per la sessione
    */
   public CommandCall getCommandCall() {
      return _commandCall;
   }

   /**
    * Ritorna un valore di sistema in formato stringa
    *
    * @param value Valore da reperire
    */
   public Object getSystemValue(final String value) {
      try {
         return new SystemValue(_AS400, value).getValue();
      } catch (final Exception e) {
         e.printStackTrace();
      }

      return "";
   }

   /**
    * Ritorna se le connessioni al database e all' AS400 sono attive
    */
   public boolean isConnected() {
      try {
         return _AS400 != null && _connection != null && _AS400.isConnected() && !_connection.isClosed();
      } catch (final SQLException e) {
         e.printStackTrace();
      }

      return false;
   }

   /**
    * Chiude la connessione con l' AS400
    */
   public boolean disconnect() {
      try {
         if (_AS400 != null && _AS400.isConnected()) {
            _AS400.disconnectAllServices();
         }

         if (_connection != null && !_connection.isClosed()) {
            _connection.close();
         }

         return true;
      } catch (final SQLException e) {
         e.printStackTrace();
      }

      return false;
   }
}
