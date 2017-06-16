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
package j;

import static j.JField.DataType.ALPHANUMERIC;
import static j.JField.DataType.BINARY;
import static j.JField.DataType.DATE;
import static j.JField.DataType.FLOAT;
import static j.JField.DataType.GRAPHIC;
import static j.JField.DataType.PACKED;
import static j.JField.DataType.TIME;
import static j.JField.DataType.UNDEFINED;
import static j.JField.DataType.ZONED;

/**
 * Represents an IBM i file field.
 *
 * @author Edoardo Luppi
 */
public class JField
{
   /**
    * The field data type.
    *
    * @author Edoardo Luppi
    */
   public enum DataType
   {
      ALPHANUMERIC,
      BINARY,
      FLOAT,
      GRAPHIC,
      DATE,
      TIME,
      PACKED,
      ZONED,
      UNDEFINED
   }

   private final JRecordFormat _parent;
   private final String _name;
   private final DataType _dataType;
   private final int _length;
   private final int _digits;
   private final int _decimalPositions;
   private final String _description;

   public JField(
           final JRecordFormat parent,
           final String name,
           final DataType dataType,
           final int length,
           final int digits,
           final int decimalPositions,
           final String description) {
      _parent = parent;
      _name = name;
      _dataType = dataType;
      _length = length;
      _digits = digits;
      _decimalPositions = decimalPositions;
      _description = description;
   }

   public JRecordFormat getParent() {
      return _parent;
   }

   public String getName() {
      return _name;
   }

   public DataType getDataType() {
      return _dataType;
   }

   public int getLength() {
      return _length;
   }

   public int getDigits() {
      return _digits;
   }

   public int getDecimalPositions() {
      return _decimalPositions;
   }

   public String getDescription() {
      return _description;
   }

   public static final DataType getEnumDataType(final String APIdataType) {
      switch (APIdataType.toUpperCase()) {
         case "A":
            return ALPHANUMERIC;
         case "B":
            return BINARY;
         case "F":
            return FLOAT;
         case "G":
            return GRAPHIC;
         case "L":
            return DATE;
         case "T":
            return TIME;
         case "P":
            return PACKED;
         case "S":
            return ZONED;
         default:
            break;
      }

      return UNDEFINED;
   }

   @Override
   public String toString() {
      return _name;
   }
}
