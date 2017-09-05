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
package lppedd.j.api.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.as400.access.BinaryConverter;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.ibm.JApi;
import lppedd.j.api.ibm.JApiResult;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.JAbstractObject;

/**
 * @author Edoardo Luppi
 */
public abstract class JAbstractFile extends JAbstractObject implements JFile
{
   protected JAbstractFile(final JConnection connection, final String name, final String library) {
      super(connection, name, library, JType.FILE);
   }

   @Override
   public boolean delete() {
      return JUtil.checkForMessage("CPC2191", getConnection().executeCommand("DLTF FILE(" + library + "/" + name + ")"));
   }

   @Override
   public List<JRecordFormat> getRecordFormats() {
      final JConnection connection = getConnection();

      JApiResult output = JApi.QUSLRCD(connection, "RCDL0200", this);
      final byte[] outputValue = output.getValue();

      if (outputValue.length < 140) {
         return Collections.emptyList();
      }

      int recordFormatsOffset = BinaryConverter.byteArrayToInt(outputValue, 124);
      int recordFormatsCount = BinaryConverter.byteArrayToInt(outputValue, 132);
      final int recordFormatEntrySize = BinaryConverter.byteArrayToInt(outputValue, 136);
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
         output = JApi.QUSLFLD(connection, "FLDL0100", recordFormatName, this);
         final byte[] outputValueFields = output.getValue();

         if (outputValueFields.length < 140) {
            continue;
         }

         int fieldsOffset = BinaryConverter.byteArrayToInt(outputValueFields, 124);
         int fieldsCount = BinaryConverter.byteArrayToInt(outputValueFields, 132);
         final int fieldEntrySize = BinaryConverter.byteArrayToInt(outputValueFields, 136);

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
                  JField.getEnumDataType((String) CHAR1.toObject(outputValueFields, fieldsOffset + 10)),
                  BinaryConverter.byteArrayToInt(outputValueFields, fieldsOffset + 20),
                  BinaryConverter.byteArrayToInt(outputValueFields, fieldsOffset + 24),
                  BinaryConverter.byteArrayToInt(outputValueFields, fieldsOffset + 28),
                  ((String) CHAR50.toObject(outputValueFields, fieldsOffset + 32)).trim()));
         }
      }

      return recordFormats;
   }
}
