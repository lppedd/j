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

import static lppedd.j.api.misc.JUtil.checkForMessage;

import java.io.IOException;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.JobLog;
import com.ibm.as400.access.MessageQueue;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.QueuedMessage;

import lppedd.j.api.misc.EmptyArrays;
import lppedd.j.api.misc.JUtil;

/**
 * Represents an IBMi job.
 *
 * @author Edoardo Luppi
 */
public class JJob
{
   public static class ListPosition
   {
      private final String position;
      private final String library;

      private ListPosition(final String position, final String library) {
         this.position = position;
         this.library = library;
      }
      
      public static ListPosition first() {
         return new ListPosition("*FIRST", null);
      }

      public static ListPosition last() {
         return new ListPosition("*LAST", null);
      }
      
      public static ListPosition after(final String library) {
         return new ListPosition("*AFTER", library);
      }

      public static ListPosition before(final String library) {
         return new ListPosition("*BEFORE", library);
      }
      
      public static ListPosition replace() {
         return new ListPosition("*REPLACE", null);
      }

      private String getPosition() {
         return position;
      }

      private String getLibrary() {
         return library;
      }
   }

   public class JLibraryList
   {
      private JLibraryList() {}

      public String[] getUserPortion() {
         try {
            return job.getUserLibraryList();
         } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
            e.printStackTrace();
         }

         return EmptyArrays.EMPTY_STRING;
      }

      public String[] getSystemPortion() {
         try {
            return job.getSystemLibraryList();
         } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
            e.printStackTrace();
         }

         return EmptyArrays.EMPTY_STRING;
      }

      public boolean addUserLibrary(final String library, final ListPosition position) {
         final StringBuilder builder = new StringBuilder(30);
         builder.append("ADDLIBLE LIB(");
         builder.append(library);
         builder.append(") POSITION(");
         builder.append(position.getPosition());
         
         final String targetLibrary = position.getLibrary();

         if (targetLibrary != null) {
            builder.append(" ");
            builder.append(targetLibrary);
         }

         builder.append(")");

         return JUtil.checkForMessage("CPC2196", getConnection().executeCommand(builder.toString()));
      }

      public boolean removeUserLibrary(final String library) {
         return checkForMessage("CPC2197", getConnection().executeCommand("RMVLIBLE (" + library + ")"));
      }

      public boolean addSystemLibrary(final String library) {
         return JUtil.checkForMessage("CPC2196", getConnection().executeCommand("CHGSYSLIBL LIB(" + library + ") OPTION(*ADD)"));
      }

      public boolean removeSystemLibrary(final String library) {
         return JUtil.checkForMessage("CPC2197", getConnection().executeCommand("CHGSYSLIBL LIB(" + library + ") OPTION(*REMOVE)"));
      }
   }

   private final JConnection connection;

   /**
    * Il lavoro AS400
    */
   private final Job job;

   public JJob(final JConnection connection, final String jobName, final String userName, final String jobNumber) {
      this.connection = connection;
      job = new Job(this.connection.getAs400(), jobName, userName, jobNumber);
      job.loadInformation();
   }

   /**
    * Answers a pending job message.
    *
    * @param answer
    *        The answer code
    */
   public boolean answer(final String answer) {
      if (answer.length() > 1) {
         return false;
      }

      final MessageQueue msgQueue = new MessageQueue(job.getSystem(), "/QSYS.LIB/%LIBL%.LIB/QSYSOPR.MSGQ");
      msgQueue.setSelectMessagesNeedReply(true);
      msgQueue.setListDirection(false);

      try {
         msgQueue.load();

         for (final QueuedMessage message : msgQueue.getMessages(0, msgQueue.getLength())) {
            if (message.getFromJobNumber().trim().equals(getNumber())) {
               msgQueue.reply(message.getKey(), answer);
               return true;
            }
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return false;
   }

   /**
    * Answers a pending job message using the default code.
    */
   public boolean answer() {
      return answer(" ");
   }

   public JConnection getConnection() {
      return connection;
   }

   /**
    * Returns the job name.
    */
   public String getName() {
      return job.getName();
   }

   /**
    * Returns the job number.
    */
   public String getNumber() {
      return job.getNumber();
   }

   /**
    * Returns the job user.<br>
    * To get the specific user see {@link #getUserIdentity()}
    */
   public String getUser() {
      return job.getUser();
   }

   /**
    * Returns the specific owner of the job.
    */
   public String getUserIdentity() {
      try {
         return job.getStringValue(Job.JOB_USER_IDENTITY);
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return "";
   }

   /**
    * Returns the job state.
    */
   public String getStatus() {
      try {
         return job.getStringValue(Job.ACTIVE_JOB_STATUS);
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return "";
   }

   /**
    * Returns the job log.
    */
   public JobLog getJobLog() {
      return job.getJobLog();
   }

   /**
    * Returns all the job messages.
    */
   public QueuedMessage[] getMessages() {
      final JobLog jobLog = job.getJobLog();

      try {
         jobLog.load();
         return jobLog.getMessages(0, jobLog.getLength());
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return new QueuedMessage[0];
   }

   /**
    * Returns the job library list.
    */
   public JLibraryList getLibraryList() {
      return new JLibraryList();
   }

   /**
    * Set the job priority.
    *
    * @param priority
    *        Priorita' del lavoro
    */
   public boolean setPriority(final int priority) {
      if (priority < 1 || priority > 99) {
         return false;
      }

      try {
         job.setRunPriority(priority);
         return true;
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return false;
   }
}
