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
package j.abstracts;

import java.util.ArrayList;
import java.util.List;
import j.JAPIOutput;
import j.JField;
import j.JRecordFormat;
import j.interfaces.JFile;

import static com.ibm.as400.access.BinaryConverter.byteArrayToInt;
import static java.util.Collections.EMPTY_LIST;
import static j.JAPI.QUSLFLD;
import static j.JAPI.QUSLRCD;
import static j.JField.getEnumDataType;
import static j.enums.JType.FILE;
import static j.misc.JUtil.checkForMessage;

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
