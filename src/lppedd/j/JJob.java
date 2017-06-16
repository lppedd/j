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

import com.ibm.as400.access.*;
import java.io.IOException;

import static com.ibm.as400.access.Job.ACTIVE_JOB_STATUS;
import static com.ibm.as400.access.Job.JOB_USER_IDENTITY;
import static lppedd.j.JConnection.getInstance;

/**
 * Permette di manipolare un lavoro AS400
 *
 * @author Edoardo Luppi
 */
public class JJob
{
   /**
    * Il lavoro AS400
    */
   private final Job _job;

   public JJob(final Job job) {
      _job = job;
      _job.loadInformation();
   }

   public JJob(final String jobName, final String userName, final String jobNumber) {
      _job = new Job(getInstance().getAS400(), jobName, userName, jobNumber);
      _job.loadInformation();
   }

   /**
    * Risponde ad un messaggio del lavoro che sta aspettando una risposta
    *
    * @param answer La risposta
    */
   public boolean answer(final String answer) {
      if (answer.length() > 1) {
         return false;
      }

      final MessageQueue msgQueue = new MessageQueue(getInstance().getAS400(), "/QSYS.LIB/%LIBL%.LIB/QSYSOPR.MSGQ");
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
    * Risponde ad un messaggio del lavoro che sta aspettando una risposta.<br>
    * Utilizza il messaggio predefinito
    */
   public boolean answer() {
      return answer(" ");
   }

   /**
    * Ritorna il nome del lavoro
    */
   public String getName() {
      return _job.getName();
   }

   /**
    * Ritorna il numero del lavoro
    */
   public String getNumber() {
      return _job.getNumber();
   }

   /**
    * Ritorna l'utente del lavoro.<br>
    * Per l'utente specifico vedere {@link #getUserIdentity()}
    */
   public String getUser() {
      return _job.getUser();
   }

   /**
    * Ritorna l'utente specifico proprietario del lavoro
    */
   public String getUserIdentity() {
      try {
         return _job.getStringValue(JOB_USER_IDENTITY);
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return "";
   }

   /**
    * Ritorna lo stato del lavoro
    */
   public String getStatus() {
      try {
         return _job.getStringValue(ACTIVE_JOB_STATUS);
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return "";
   }

   /**
    * Ritorna il joblog del lavoro
    */
   public JobLog getJobLog() {
      return _job.getJobLog();
   }

   /**
    * Ritorna tutti i messaggi legati al lavoro
    */
   public QueuedMessage[] getMessages() {
      final JobLog jobLog = _job.getJobLog();

      try {
         jobLog.load();
         return jobLog.getMessages(0, jobLog.getLength());
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return new QueuedMessage[0];
   }

   /**
    * Imposta la priorita' del lavoro
    *
    * @param priority Priorita' del lavoro
    */
   public boolean setPriority(final int priority) {
      if (priority < 1 || priority > 99) {
         return false;
      }

      try {
         _job.setRunPriority(priority);
         return true;
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return false;
   }
}
