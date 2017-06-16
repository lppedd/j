package lppedd.j.abstracts;

import java.util.ArrayList;
import java.util.List;
import lppedd.j.JAPIOutput;
import lppedd.j.JField;
import lppedd.j.JRecordFormat;
import lppedd.j.interfaces.JFile;

import static com.ibm.as400.access.BinaryConverter.byteArrayToInt;
import static java.util.Collections.EMPTY_LIST;
import static lppedd.j.JAPI.QUSLFLD;
import static lppedd.j.JAPI.QUSLRCD;
import static lppedd.j.JField.getEnumDataType;
import static lppedd.j.enums.JType.FILE;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * @author Edoardo Luppi
 */
public abstract class JAbstractFile extends JAbstractObject implements JFile
{
   protected JAbstractFile(final String name, final String library) {
      super(name, library, FILE);
   }

   @Override
   public boolean delete() {
      return checkForMessage("CPC2191", getConnection().executeCommand("DLTF FILE(" + _library + "/" + _name + ")"));
   }

   @Override
   public List<JRecordFormat> getRecordFormats() {
      JAPIOutput output = QUSLRCD("RCDL0200", this);
      final byte[] outputValue = output.getValue();

      if (outputValue.length < 140) {
         return EMPTY_LIST;
      }

      int recordFormatsOffset = byteArrayToInt(outputValue, 124);
      int recordFormatsCount = byteArrayToInt(outputValue, 132);
      final int recordFormatEntrySize = byteArrayToInt(outputValue, 136);
      final List<JRecordFormat> recordFormats = new ArrayList<>(recordFormatsCount);

      for (; recordFormatsCount > 0; --recordFormatsCount, recordFormatsOffset += recordFormatEntrySize) {
         final String recordFormatName = ((String) CHAR10.toObject(outputValue, recordFormatsOffset)).trim();
         final List<JField> recordFormatFields = new ArrayList<>(20);
         final JRecordFormat recordFormat = new JRecordFormat(
                 this,
                 recordFormatName,
                 ((String) CHAR50.toObject(outputValue, recordFormatsOffset + 32)).trim(),
                 recordFormatFields);

         recordFormats.add(recordFormat);

         // Per ogni formato record ricavo i campi.
         output = QUSLFLD("FLDL0100", recordFormatName, this);
         final byte[] outputValueFields = output.getValue();

         if (outputValueFields.length < 140) {
            continue;
         }

         int fieldsOffset = byteArrayToInt(outputValueFields, 124);
         int fieldsCount = byteArrayToInt(outputValueFields, 132);
         final int fieldEntrySize = byteArrayToInt(outputValueFields, 136);

         for (; fieldsCount > 0; --fieldsCount, fieldsOffset += fieldEntrySize) {
            final String fieldName = ((String) CHAR10.toObject(outputValueFields, fieldsOffset)).trim();

            // Non ha senso considerare gli indicatori come dei campi.
            // Questo caso si puo' presentare con i printer file.
            if (fieldName.startsWith("*IN")) {
               continue;
            }

            recordFormatFields.add(new JField(
                    recordFormat,
                    fieldName,
                    getEnumDataType((String) CHAR1.toObject(outputValueFields, fieldsOffset + 10)),
                    byteArrayToInt(outputValueFields, fieldsOffset + 20),
                    byteArrayToInt(outputValueFields, fieldsOffset + 24),
                    byteArrayToInt(outputValueFields, fieldsOffset + 28),
                    ((String) CHAR50.toObject(outputValueFields, fieldsOffset + 32)).trim()));
         }
      }

      return recordFormats;
   }
}
