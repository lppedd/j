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
package lppedd.j.api.members;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.regex.Pattern;

import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;

import lppedd.j.api.JAbstractBase;
import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.ibm.JApi;
import lppedd.j.api.ibm.JApiResult;
import lppedd.j.api.misc.JUtil;
import smi.workitem.SmiAbstractWorkItem;
import smi.workitem.SmiPgmWorkItem;
import smi.workitem.SmiSqlWorkItem;
import smi.workitem.list.SmiWorkItemList;

/**
 * Represents an IBMi source member.
 *
 * @author Edoardo Luppi
 */
public abstract class JAbstractMember extends JAbstractBase implements JMember
{
   /**
    * Starting modification number for the source member work items.
    */
   protected static final int STARTING_INDEX = 9000;
   protected static final Pattern PATTERN_MOD = Pattern.compile("^((9|--9)[0-9]{3}[ §].*)");
   protected static final Pattern PATTERN_EOC = Pattern.compile("^(?!(^.{5}[*])|(^--.*)|(^$)|(^ +$))|(.*([*=§-]{4,}).*)");

   // Path
   protected String object = "";

   /**
    * The source code of the source member.
    */
   protected final List<String> source = new ArrayList<>(1024);

   /**
    * The work item list of the source member.
    *
    * @see SmiAbstractWorkItem
    */
   protected final SmiWorkItemList workItemList = new SmiWorkItemList();

   /**
    * La posizione di fine testatina del membro sorgente. E' aggiornata ogni
    * qualvolta si inserisce o rimuove un work item e serve per l'inserimento
    * in posizione corretta
    */
   protected int workItemsEnd;

   protected JAbstractMember(final JConnection connection, final String name, final String object, final String library) {
      super(connection, name, library, JType.MBR);
      this.object = object.trim().toUpperCase();

      if (exists()) {
         retriveMemberDescription();
      }
   }

   /**
    * Check for the member existence in the system.
    */
   @Override
   public boolean exists() {
      return getConnection().exists(library, object, type, name);
   }

   @Override
   public boolean loadSource() {
      if (!exists()) {
         return false;
      }

      final AS400File file = new SequentialFile(getConnection().getAs400(), getIfsPath());

      try {
         file.setRecordFormat();

         for (final Record record : file.readAll()) {
            source.add(record.getField(2).toString());
         }

         inspectForWorkItems();
         return true;
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | PropertyVetoException e) {
         e.printStackTrace();
      } finally {
         try {
            if (file != null) {
               file.close();
            }
         } catch (AS400Exception | AS400SecurityException | InterruptedException | IOException e) {
            e.printStackTrace();
         }
      }

      return false;
   }

   @Override
   public void dispose() {
      source.clear();
      workItemList.clear();
   }

   @Override
   public boolean persist() {
      if (!super.persist()) {
         return false;
      }

      if (isSourceChanged) {
         if (!performSetSource()) {
            return false;
         }

         isSourceChanged = false;
      }

      if (isAttributeChanged) {
         if (!performSetAttribute()) {
            return false;
         }

         isAttributeChanged = false;
      }

      return true;
   }

   @Override
   public boolean create(final boolean overwrite) {
      final boolean exists = exists();

      if (exists && !overwrite) {
         return false;
      }

      final AS400File file = new SequentialFile(getConnection().getAs400(), getIfsPath());

      try {
         if (exists) {
            file.deleteMember();
         }

         file.addPhysicalFileMember(name, text);
         return true;
      } catch (AS400Exception | AS400SecurityException | InterruptedException | IOException e) {
         e.printStackTrace();
      } finally {
         try {
            if (file != null) {
               file.close();
            }
         } catch (AS400Exception | AS400SecurityException | InterruptedException | IOException e) {
            e.printStackTrace();
         }
      }

      return false;
   }

   @Override
   public boolean copy(final String library, final String object, final String name, final boolean overwrite) {
      final JConnection connection = getConnection();

      if (!overwrite && connection.exists(library, object, type, name)) {
         return true;
      }

      final StringBuilder builder = new StringBuilder(152);
      builder.append("CPYSRCF FROMFILE(");
      builder.append(library);
      builder.append("/");
      builder.append(object);
      builder.append(") FROMMBR(");
      builder.append(name);
      builder.append(") TOFILE(");
      builder.append(library);
      builder.append("/");
      builder.append(object);
      builder.append(") TOMBR(");
      builder.append(name);
      builder.append(") MBROPT(*REPLACE) SRCCHGDATE(*NEW)");

      return JUtil.checkForMessage("CPF2889", connection.executeCommand(builder.toString()));
   }

   @Override
   public boolean move(final String library, final String object, final String name, final boolean overwrite) {
      if (!copy(library, object, name, overwrite)) {
         return false;
      }

      // Delete the member at the actual position
      delete();

      this.name = name;
      this.object = object;
      this.library = library;
      return true;
   }

   @Override
   public boolean delete() {
      final StringBuilder builder = new StringBuilder(50);
      builder.append("RMVM FILE(");
      builder.append(library);
      builder.append("/");
      builder.append(object);
      builder.append(") MBR(");
      builder.append(name);
      builder.append(")");

      return JUtil.checkForMessage("CPC7309", getConnection().executeCommand(builder.toString()));
   }

   @Override
   public SmiAbstractWorkItem[] getWorkItems() {
      return workItemList.getWorkItems();
   }

   @Override
   public Optional<SmiAbstractWorkItem> getWorkItem(final int number) {
      // FIXME
      return Optional.ofNullable(workItemList.getWorkItem(workItemList.contains(number)));
   }

   @Override
   public boolean putWorkItem(
         final int number,
         final String username,
         final long date,
         final String text,
         final boolean work) {
      final int listSize = workItemList.size();
      final int newIndex = listSize > 0 ? workItemList.getWorkItem(listSize - 1).getIndex() + 1 : STARTING_INDEX;
      return addWorkItem(newIndex, number, username, date, text, work);
   }

   @Override
   public boolean addWorkItem(
         final int index,
         final int number,
         final String username,
         final long date,
         final String text,
         final boolean work) {
      if (index < STARTING_INDEX) {
         return false;
      }

      if (workItemList.contains(number) >= 0) {
         return false;
      }

      SmiAbstractWorkItem workItem = null;

      if (this instanceof JSqlMember) {
         workItem = new SmiSqlWorkItem(this, index, number, username, date, work);
      } else {
         workItem = new SmiPgmWorkItem(this, index, number, username, date, work);
      }

      workItem.appendText(text);

      final List<String> workItemSource = workItem.getSource();
      source.addAll(workItemsEnd, workItemSource);
      workItemList.add(workItem, workItemsEnd);
      workItemsEnd += workItemSource.size();
      isSourceChanged = true;
      return true;
   }

   @Override
   public SmiAbstractWorkItem removeWorkItem(final int number, final boolean cleanAll) {
      final int i = workItemList.contains(number);

      if (i < 0) {
         return null;
      }

      // Remove the work item from the header
      for (int k = workItemList.getPosition(i), j = k; k < (i == workItemList.size() - 1 ? workItemsEnd : workItemList.getPosition(i + 1)); k++) {
         source.remove(j);
      }

      if (cleanAll) {
         // TODO Removes lines related to the work item
         removeWorkItemSpecs(workItemList.getWorkItem(i).getIndex());
      }

      isSourceChanged = true;
      return workItemList.remove(i).first;
   }

   @Override
   public String getIfsPath() {
      return QSYSObjectPathName.toPath(library, object, name, "MBR");
   }

   @Override
   public String getQualifiedPath() {
      return JUtil.getQualifiedPath(library, object);
   }

   @Override
   public String getName() {
      return name;
   }

   /**
    * Returns the name of the object containing the member.
    */
   @Override
   public String getObject() {
      return object;
   }

   /**
    * Returns the source code of the source member.
    */
   @Override
   public List<String> getSource() {
      return new ArrayList<>(source);
   }

   @Override
   public void setSourceType(String attribute) {
      attribute = attribute.toUpperCase();

      if (!this.attribute.equalsIgnoreCase(attribute)) {
         this.attribute = attribute;
         isAttributeChanged = true;
      }
   }

   @Override
   public void setSource(final List<String> source) {
      this.source.clear();
      this.source.addAll(source);
      isSourceChanged = true;
   }

   @Override
   protected boolean performSetName() {
      final StringBuilder builder = new StringBuilder(72);
      builder.append("RNMM FILE(");
      builder.append(library);
      builder.append("/");
      builder.append(object);
      builder.append(") MBR(");
      builder.append(originalName);
      builder.append(") NEWMBR(");
      builder.append(name);
      builder.append(")");

      return JUtil.checkForMessage("CPC3202", getConnection().executeCommand(builder.toString()));
   }

   /**
    * Executes the IBMi command the persists the changed attribute.
    */
   protected boolean performSetAttribute() {
      final StringBuilder builder = new StringBuilder(76);
      builder.append("CHGPFM FILE(");
      builder.append(library);
      builder.append("/");
      builder.append(object);
      builder.append(") MBR(");
      builder.append(name);
      builder.append(") SRCTYPE(");
      builder.append(attribute);
      builder.append(")");

      return JUtil.checkForMessage("CPC3201", getConnection().executeCommand(builder.toString()));
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(180);
      builder.append("CHGPFM FILE(");
      builder.append(library);
      builder.append("/");
      builder.append(object);
      builder.append(") MBR(");
      builder.append(name);
      builder.append(") TEXT('");
      builder.append(text);
      builder.append("')");

      return JUtil.checkForMessage("CPC3201", getConnection().executeCommand(builder.toString()));
   }

   /**
    * Perists the changed source code of the source member.
    */
   protected boolean performSetSource() {
      final JConnection connection = getConnection();
      final AS400File file = new SequentialFile(getConnection().getAs400(), getIfsPath());
      final String date = connection.getSystemValue("QDATE").toString().substring(2).replace("-", "");
      final int sourceSize = source.size();

      try {
         file.setRecordFormat();
         final RecordFormat recordFormat = file.getRecordFormat();
         final Record[] records = new Record[sourceSize];

         for (int i = 0; i < sourceSize; i++) {
            records[i] = new Record(recordFormat);
            records[i].setRecordNumber(i + 1);
            records[i].setField("SRCSEQ", new BigDecimal(i + 1));
            records[i].setField("SRCDAT", new BigDecimal(date));
            records[i].setField("SRCDTA", source.get(i));
         }

         file.open(AS400File.READ_WRITE, 400, AS400File.COMMIT_LOCK_LEVEL_NONE);
         
         while (file.readNext() != null) {
            file.deleteCurrentRecord();
         }

         file.write(records);
         return true;
      } catch (AS400Exception | AS400SecurityException | InterruptedException | IOException | PropertyVetoException e) {
         e.printStackTrace();
      } finally {
         try {
            if (file != null) {
               file.close();
            }
         } catch (AS400Exception | AS400SecurityException | InterruptedException | IOException e) {
            e.printStackTrace();
         }
      }

      return false;
   }
   
   private void retriveMemberDescription() {
      final JApiResult output = JApi.QUSRMBRD(getConnection(), "MBRD0300", this);

      if (output.getMessages().length != 0) {
         return;
      }

      MBRD0300 = output.getValue();

      attribute = CHAR10.toObject(MBRD0300, 48).toString().trim();
      text = CHAR50.toObject(MBRD0300, 84).toString().trim();

      final StringBuilder builder = new StringBuilder(19);
      builder.append(CHAR13.toObject(MBRD0300, 58));

      if (builder.charAt(0) == '0') {
         builder.insert(1, "19");
      } else {
         builder.insert(1, "20");
      }

      builder.delete(0, 1);
      builder.append("000");

      creationDateTime = Long.parseLong(builder.toString());
   }

   /**
    * Reworks a source line to remove a work item.
    *
    * @param modificationNumber
    *        The modofication number of the work item to remove
    */
   private void removeWorkItemSpecs(final int modificationNumber) {
      final String stringModificationNumber = String.valueOf(modificationNumber);

      for (final ListIterator<String> iterator = source.listIterator(workItemsEnd); iterator.hasNext();) {
         final String line = backToThePast(iterator.next().trim(), stringModificationNumber);

         if (line != null) {
            iterator.set(line);
         } else {
            iterator.remove();
         }
      }
   }

   /**
    * Inspects the source code of the source member for work items and inserts them in the work item list.
    */
   protected abstract void inspectForWorkItems();

   /**
    * Ricostruisce una linea di codice sorgente eliminando il numero di
    * modifica passato
    *
    * @param line
    *        Linea da ricostruire
    * @param modificationNumber
    *        Numero di modifica da eliminare
    */
   protected abstract String backToThePast(final String line, final String modificationNumber);
}
