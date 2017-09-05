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

/**
 * Represents an IBMi file field.
 *
 * @author Edoardo Luppi
 */
public class JField
{
   /**
    * A file field data type.
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

   private final JRecordFormat parent;
   private final String name;
   private final DataType dataType;
   private final int length;
   private final int digits;
   private final int decimalPositions;
   private final String description;

   JField(
         final JRecordFormat parent,
         final String name,
         final DataType dataType,
         final int length,
         final int digits,
         final int decimalPositions,
         final String description) {
      this.parent = parent;
      this.name = name;
      this.dataType = dataType;
      this.length = length;
      this.digits = digits;
      this.decimalPositions = decimalPositions;
      this.description = description;
   }

   public JRecordFormat getParent() {
      return parent;
   }

   public String getName() {
      return name;
   }

   public DataType getDataType() {
      return dataType;
   }

   public int getLength() {
      return length;
   }

   public int getDigits() {
      return digits;
   }

   public int getDecimalPositions() {
      return decimalPositions;
   }

   public String getDescription() {
      return description;
   }

   public static DataType getEnumDataType(final String apiDataType) {
      switch (apiDataType.toUpperCase()) {
         case "A":
            return DataType.ALPHANUMERIC;
         case "B":
            return DataType.BINARY;
         case "F":
            return DataType.FLOAT;
         case "G":
            return DataType.GRAPHIC;
         case "L":
            return DataType.DATE;
         case "T":
            return DataType.TIME;
         case "P":
            return DataType.PACKED;
         case "S":
            return DataType.ZONED;
         default:
            break;
      }

      return DataType.UNDEFINED;
   }

   @Override
   public String toString() {
      return name;
   }
}
