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
package lppedd.j.api;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400JDBCConnectionHandle;
import com.ibm.as400.access.AS400JDBCConnectionPoolDataSource;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.ObjectDescription;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ObjectList;
import com.ibm.as400.access.SystemValue;

import lppedd.j.api.exceptions.JConnectionException;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.JNullObject;
import lppedd.j.api.objects.JObject;

/**
 * @author Edoardo Luppi
 */
public class JConnection implements AutoCloseable
{
   private AS400 as400;
   private Connection connection;
   private CommandCall commandCall;

   public JConnection(final String ip, final String user, final String password) throws JConnectionException {
      final AS400JDBCConnectionPoolDataSource dataSource = new AS400JDBCConnectionPoolDataSource(ip, user, password);
      dataSource.setPrompt(false);

      try {
         connection = dataSource.getPooledConnection().getConnection();
         as400 = new AS400(ip, user, password);
         as400.setGuiAvailable(false);
         as400.connectService(AS400.FILE);
         commandCall = new CommandCall(as400);
      } catch (AS400SecurityException | IOException | PropertyVetoException | SQLException e) {
         disconnect();
         throw new JConnectionException(e);
      }
   }

   /**
    * Checks if the object exists in the system.
    *
    * @param library
    *        The object library
    * @param object
    *        The object name
    * @param type
    *        The object type
    * @param member
    *        The member name or {@code null} if none
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
      builder.append(member == null ? "*NONE" : member);
      builder.append(")");

      return executeCommand(builder.toString()).length == 0;
   }

   /**
    * Cancella tutti gli spool di un utente
    *
    * @param user
    *        L'utente di cui si vogliono cancellare gli spool
    */
   public boolean deleteSpools(final String user) {
      return JUtil.checkForMessage("CPC3305", executeCommand("DLTSPLF FILE(*SELECT) SELECT(" + user + ")"));
   }

   /**
    * Esegue un comando AS400
    *
    * @param command
    *        Comando da eseguire
    *
    * @return Lista identificativi degli eventuali messaggi di errore con il
    *         relativo testo
    */
   public AS400Message[] executeCommand(final String command) {
      try {
         commandCall.run(command);
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | PropertyVetoException e) {
         e.printStackTrace();
      }

      return commandCall.getMessageList();
   }

   /**
    * Ritorna una lista di oggetti per il tipo di oggetto passato
    *
    * @param objects
    *        Lista che verra' popolato con gli oggetti trovati
    * @param library
    *        Libreria dove cercare
    * @param type
    *        Tipo di oggetto da cercare
    */
   public void getListOf(final List<JObject> objects, final String library, final JType type) {
      final ObjectList objectList = new ObjectList(as400, library, ObjectList.ALL, type.getObjectType());
      objectList.addObjectAttributeToRetrieve(ObjectDescription.EXTENDED_ATTRIBUTE);

      try {
         objectList.load();

         for (final ObjectDescription description : objectList.getObjects(0, objectList.getLength())) {
            final JObject object = JObjectFactory.get(this, description.getValueAsString(ObjectDescription.NAME).trim(),
                  description.getValueAsString(ObjectDescription.LIBRARY).trim(), type);

            if (!(object instanceof JNullObject)) {
               objects.add(object);
            }
         }
      } catch (final AS400SecurityException | ObjectDoesNotExistException | ErrorCompletingRequestException | InterruptedException | IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Returns the connection job.
    */
   public JJob getJob() {
      Job job = null;
      
      try {
         job = commandCall.getServerJob();
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException e) {
         e.printStackTrace();
      }

      return new JJob(this, job.getName(), job.getUser(), job.getNumber());
   }

   /**
    * Ritorna il lavoro aperto per la connessione al database
    */
   public JJob getDatabaseJob() {
      try {
         final String jobId = ((AS400JDBCConnectionHandle) connection).getServerJobIdentifier();
         return new JJob(this, jobId.substring(0, 10), jobId.substring(10, 20), jobId.substring(20));
      } catch (final NullPointerException | SQLException e) {
         e.printStackTrace();
      }

      return null;
   }

   /**
    * Returns the {@link AS400} instance for this connection.
    */
   public AS400 getAs400() {
      return as400;
   }

   /**
    * Returns the database {@link Connection} instance for this connection.
    */
   public Connection getConnection() {
      return connection;
   }

   /**
    * Returns the {@link CommandCall} instance for this connection.
    */
   public CommandCall getCommandCall() {
      return commandCall;
   }

   /**
    * Returns a system value.
    *
    * @param value
    *        The system value name
    */
   // TODO: Create a new separate class representing the access to system values.
   public Optional<?> getSystemValue(final String value) {
      try {
         return Optional.ofNullable(new SystemValue(as400, value).getValue());
      } catch (final Exception e) {
         e.printStackTrace();
      }

      return Optional.empty();
   }

   /**
    * Checks if the connection to the system is active.
    */
   public boolean isConnected() {
      try {
         return as400 != null && connection != null && as400.isConnected() && !connection.isClosed();
      } catch (final SQLException e) {
         e.printStackTrace();
      }

      return false;
   }

   /**
    * Close the connection to the system.
    */
   public boolean disconnect() {
      try {
         if (as400 != null && as400.isConnected()) {
            as400.disconnectAllServices();
         }

         if (connection != null && !connection.isClosed()) {
            connection.close();
         }

         as400 = null;
         connection = null;
         commandCall = null;
         return true;
      } catch (final SQLException e) {
         e.printStackTrace();
      }

      return false;
   }

   @Override
   public void close() throws Exception {
      disconnect();
   }
}
