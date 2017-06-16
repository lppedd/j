package lppedd.j;

import static lppedd.j.JField.DataType.ALPHANUMERIC;
import static lppedd.j.JField.DataType.BINARY;
import static lppedd.j.JField.DataType.DATE;
import static lppedd.j.JField.DataType.FLOAT;
import static lppedd.j.JField.DataType.GRAPHIC;
import static lppedd.j.JField.DataType.PACKED;
import static lppedd.j.JField.DataType.TIME;
import static lppedd.j.JField.DataType.UNDEFINED;
import static lppedd.j.JField.DataType.ZONED;

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
