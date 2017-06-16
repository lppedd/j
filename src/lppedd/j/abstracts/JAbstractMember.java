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
package lppedd.j.abstracts;

import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;
import lppedd.j.JAPIOutput;
import lppedd.j.JConnection;
import lppedd.j.JSQLMember;
import lppedd.j.interfaces.JMember;
import lppedd.j.misc.JUtil;
import smi.workitem.SMIPgmWorkItem;
import smi.workitem.SMISqlWorkItem;
import smi.workitem.SMIWorkItemList;
import smi.workitem.abstracts.SMIWorkItem;

import static com.ibm.as400.access.AS400File.COMMIT_LOCK_LEVEL_NONE;
import static com.ibm.as400.access.AS400File.READ_WRITE;
import static com.ibm.as400.access.QSYSObjectPathName.toPath;
import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static lppedd.j.JAPI.QUSRMBRD;
import static lppedd.j.enums.JType.MBR;
import static lppedd.j.misc.JUtil.checkForMessage;

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
   protected String _object = "";

   /**
    * Il codice del membro sorgente
    */
   protected final List<String> _source = new ArrayList<>(1024);

   /**
    * La lista dei work item del membro sorgente
    *
    * @see SMIWorkItem
    */
   protected final SMIWorkItemList _workItemList = new SMIWorkItemList();

   /**
    * La posizione di fine testatina del membro sorgente. E' aggiornata ogni
    * qualvolta si inserisce o rimuove un work item e serve per l'inserimento
    * in posizione corretta
    */
   protected int _workItemsEnd;

   protected JAbstractMember(final String name, final String object, final String library) {
      super(name, library, MBR);
      _object = object.trim().toUpperCase();

      // If the member exists on the system, then load its informations
      if (exists()) {
         retriveMemberDescription();
      }
   }

   /**
    * Check for the member existence.
    */
   @Override
   public boolean exists() {
      return getConnection().exists(_library, _object, _type, _name);
   }

   @Override
   public boolean loadSource() {
      if (!exists()) {
         return false;
      }

      // Leggo il sorgente
      final AS400File file = new SequentialFile(getConnection().getAS400(), getPath());

      try {
         file.setRecordFormat();

         for (final Record record : file.readAll()) {
            _source.add(record.getField(2).toString());
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
   public void releaseResources() {
      _source.clear();
      _workItemList.clear();
   }

   @Override
   public boolean commitChanges() {
      if (!super.commitChanges()) {
         return false;
      }

      if (_changedSource) {
         if (!performSetSource()) {
            return false;
         }

         _changedSource = false;
      }

      if (_changedAttribute) {
         if (!performSetAttribute()) {
            return false;
         }

         _changedAttribute = false;
      }

      return true;
   }

   @Override
   public boolean create(final boolean overwrite) {
      final boolean exists = exists();

      if (exists && !overwrite) {
         return false;
      }

      final AS400File file = new SequentialFile(getConnection().getAS400(), getPath());

      try {
         if (exists) {
            file.deleteMember();
         }

         file.addPhysicalFileMember(_name, _text);
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

      if (!overwrite && connection.exists(library, object, _type, name)) {
         return true;
      }

      final StringBuilder builder = new StringBuilder(152);
      builder.append("CPYSRCF FROMFILE(");
      builder.append(_library);
      builder.append("/");
      builder.append(_object);
      builder.append(") FROMMBR(");
      builder.append(_name);
      builder.append(") TOFILE(");
      builder.append(library);
      builder.append("/");
      builder.append(object);
      builder.append(") TOMBR(");
      builder.append(name);
      builder.append(") MBROPT(*REPLACE) SRCCHGDATE(*NEW)");

      return checkForMessage("CPF2889", connection.executeCommand(builder.toString()));
   }

   @Override
   public boolean move(final String library, final String object, final String name, final boolean overwrite) {
      if (!copy(library, object, name, overwrite)) {
         return false;
      }

      // Delete the member at the actual position
      delete();

      _library = library;
      _object = object;
      _name = name;
      return true;
   }

   @Override
   public boolean delete() {
      final StringBuilder builder = new StringBuilder(50);
      builder.append("RMVM FILE(");
      builder.append(_library);
      builder.append("/");
      builder.append(_object);
      builder.append(") MBR(");
      builder.append(_name);
      builder.append(")");

      return checkForMessage("CPC7309", getConnection().executeCommand(builder.toString()));
   }

   @Override
   public SMIWorkItem[] getWorkItems() {
      return _workItemList.getWorkItems();
   }

   @Override
   public SMIWorkItem getWorkItem(final int number) {
      final int i = _workItemList.contains(number);
      return i >= 0 ? _workItemList.getWorkItem(i) : null;
   }

   @Override
   public boolean putWorkItem(
           final int number, final String username, final long date, final String text, final boolean work) {
      final int listSize = _workItemList.size();
      final int newIndex = listSize > 0 ? _workItemList.getWorkItem(listSize - 1).getIndex() + 1 : STARTING_INDEX;
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

      if (_workItemList.contains(number) >= 0) {
         return false;
      }

      SMIWorkItem workItem = null;

      if (this instanceof JSQLMember) {
         workItem = new SMISqlWorkItem(this, index, number, username, date, work);
      } else {
         workItem = new SMIPgmWorkItem(this, index, number, username, date, work);
      }

      workItem.appendText(text);

      final List<String> workItemSource = workItem.getSource();
      _source.addAll(_workItemsEnd, workItemSource);
      _workItemList.add(workItem, _workItemsEnd);
      _workItemsEnd += workItemSource.size();
      _changedSource = true;
      return true;
   }

   @Override
   public SMIWorkItem removeWorkItem(final int number, final boolean cleanAll) {
      final int i = _workItemList.contains(number);

      if (i < 0) {
         return null;
      }

      // Pulisco la testatina
      for (int k = _workItemList.getPosition(i), j = k; k < (i == _workItemList.size() - 1 ? _workItemsEnd : _workItemList.getPosition(i + 1)); k++) {
         _source.remove(j);
      }

      if (cleanAll) {
         // Pulisco tutto il sorgente
         removeWorkItemSpecs(_workItemList.getWorkItem(i).getIndex());
      }

      _changedSource = true;
      return _workItemList.remove(i).first;
   }

   @Override
   public String getPath() {
      return toPath(_library, _object, _name, "MBR");
   }

   @Override
   public String getQualifiedPath() {
      return JUtil.getQualifiedPath(_library, _object);
   }

   @Override
   public String getName() {
      return _name;
   }

   /**
    * Ritorna il nome dell'oggetto che contiene il membro sorgente
    */
   @Override
   public String getObject() {
      return _object;
   }

   /**
    * Ritorna una lista di tutte le righe del membro sorgente
    */
   @Override
   public List<String> getSource() {
      return new ArrayList<>(_source);
   }

   @Override
   public void setSourceType(final String attribute) {
      if (!_attribute.equalsIgnoreCase(attribute)) {
         _attribute = attribute;
         _changedAttribute = true;
      }
   }

   @Override
   public void setSource(final List<String> source) {
      _source.clear();
      _source.addAll(source);
      _changedSource = true;
   }

   @Override
   protected boolean performSetName() {
      final StringBuilder builder = new StringBuilder(72);
      builder.append("RNMM FILE(");
      builder.append(_library);
      builder.append("/");
      builder.append(_object);
      builder.append(") MBR(");
      builder.append(_originalName);
      builder.append(") NEWMBR(");
      builder.append(_name);
      builder.append(")");

      return checkForMessage("CPC3202", getConnection().executeCommand(builder.toString()));
   }

   /**
    * Lancia il comando AS400 per rendere effettiva la modifica all'attributo
    * del membro sorgente
    */
   protected boolean performSetAttribute() {
      final StringBuilder builder = new StringBuilder(76);
      builder.append("CHGPFM FILE(");
      builder.append(_library);
      builder.append("/");
      builder.append(_object);
      builder.append(") MBR(");
      builder.append(_name);
      builder.append(") SRCTYPE(");
      builder.append(_attribute);
      builder.append(")");

      return checkForMessage("CPC3201", getConnection().executeCommand(builder.toString()));
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(180);
      builder.append("CHGPFM FILE(");
      builder.append(_library);
      builder.append("/");
      builder.append(_object);
      builder.append(") MBR(");
      builder.append(_name);
      builder.append(") TEXT('");
      builder.append(_text);
      builder.append("')");

      return checkForMessage("CPC3201", getConnection().executeCommand(builder.toString()));
   }

   protected boolean performSetSource() {
      final JConnection connection = getConnection();
      final AS400File file = new SequentialFile(getConnection().getAS400(), getPath());
      final String date = connection.getSystemValue("QDATE").toString().substring(2).replace("-", "");
      final int sourceSize = _source.size();

      try {
         file.setRecordFormat();
         final RecordFormat recordFormat = file.getRecordFormat();
         final Record[] records = new Record[sourceSize];

         for (int i = 0; i < sourceSize; i++) {
            records[i] = new Record(recordFormat);
            records[i].setRecordNumber(i + 1);
            records[i].setField("SRCSEQ", new BigDecimal(i + 1));
            records[i].setField("SRCDAT", new BigDecimal(date));
            records[i].setField("SRCDTA", _source.get(i));
         }

         file.open(READ_WRITE, 400, COMMIT_LOCK_LEVEL_NONE);

         // Cancello tutti i record dal membro.
         while (file.readNext() != null) {
            file.deleteCurrentRecord();
         }

         // Scrivo i nuovi record.
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

   /**
    * Reperisce tutte le informazioni sul membro dalle API di sistema
    */
   private final void retriveMemberDescription() {
      final JAPIOutput output = QUSRMBRD("MBRD0300", this);

      if (output.getMessages().length != 0) {
         return;
      }

      _MBRD0300 = output.getValue();

      _attribute = CHAR10.toObject(_MBRD0300, 48).toString().trim();
      _text = CHAR50.toObject(_MBRD0300, 84).toString().trim();

      final StringBuilder builder = new StringBuilder(19);
      builder.append(CHAR13.toObject(_MBRD0300, 58));

      if (builder.charAt(0) == '0') {
         builder.insert(1, "19");
      } else {
         builder.insert(1, "20");
      }

      builder.delete(0, 1);
      builder.append("000");

      _creationDateTime = parseLong(builder.toString());
   }

   /**
    * Riorganizza una linea di sorgente per rimuovere un work item
    *
    * @param modificationNumber Numero di modifica del work item da rimuovere
    */
   private void removeWorkItemSpecs(final int modificationNumber) {
      final String stringModificationNumber = valueOf(modificationNumber);

      for (final ListIterator<String> iterator = _source.listIterator(_workItemsEnd); iterator.hasNext();) {
         final String line = backToThePast(iterator.next().trim(), stringModificationNumber);

         if (line != null) {
            iterator.set(line);
         } else {
            iterator.remove();
         }
      }
   }

   /**
    * Ispeziona il codice del membro sorgente e popola la lista dei work item.
    * Va chiamato dal membro solo dopo aver letto tutto il sorgente
    */
   protected abstract void inspectForWorkItems();

   /**
    * Ricostruisce una linea di codice sorgente eliminando il numero di
    * modifica passato
    *
    * @param line               Linea da ricostruire
    * @param modificationNumber Numero di modifica da eliminare
    */
   protected abstract String backToThePast(final String line, final String modificationNumber);
}
