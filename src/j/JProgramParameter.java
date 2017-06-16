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

import com.ibm.as400.access.AS400Bin1;
import com.ibm.as400.access.AS400Bin2;
import com.ibm.as400.access.AS400Bin4;
import com.ibm.as400.access.AS400Bin8;
import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400PackedDecimal;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.AS400UnsignedBin1;
import com.ibm.as400.access.AS400UnsignedBin2;
import com.ibm.as400.access.AS400UnsignedBin4;
import com.ibm.as400.access.AS400UnsignedBin8;
import com.ibm.as400.access.AS400ZonedDecimal;
import j.interfaces.AS400DataTypes;

import static j.JProgramParameter.PassingStyle.REFERENCE;
import static j.misc.EmptyArrays.EMPTY_BYTE;

public class JProgramParameter implements AS400DataTypes
{
   public enum VariableType
   {
      CHAR,
      INT,
      UNS,
      PACKED,
      ZONED
   }

   public enum PassingStyle
   {
      VALUE,
      REFERENCE,
      REFERENCE_CONST
   }

   private final byte[] _value;
   private final int _length;
   private final int _decimals;
   private final VariableType _type;
   private final PassingStyle _style;
   private AS400DataType _converter;
   private byte[] _output;

   public JProgramParameter(final VariableType type, final int length, final int decimals, final Object value, final PassingStyle style) {
      switch (type) {
         case CHAR:
            _converter = new AS400Text(length);
            break;
         case INT:
            switch (length) {
               case 3:
                  _converter = new AS400Bin1();
                  break;
               case 5:
                  _converter = new AS400Bin2();
                  break;
               case 7:
                  _converter = new AS400Bin4();
                  break;
               case 10:
                  _converter = new AS400Bin8();
                  break;
               default:
                  throw new IllegalArgumentException();
            }

            break;
         case UNS:
            switch (length) {
               case 3:
                  _converter = new AS400UnsignedBin1();
                  break;
               case 5:
                  _converter = new AS400UnsignedBin2();
                  break;
               case 7:
                  _converter = new AS400UnsignedBin4();
                  break;
               case 10:
                  _converter = new AS400UnsignedBin8();
                  break;
               default:
                  throw new IllegalArgumentException();
            }

            break;
         case PACKED:
            _converter = new AS400PackedDecimal(length, decimals);
            break;
         case ZONED:
            _converter = new AS400ZonedDecimal(length, decimals);
            break;
         default:
            break;
      }

      if (_converter == null) {
         throw new UnsupportedOperationException();
      }

      _value = _converter.toBytes(value);
      _length = length;
      _decimals = decimals;
      _type = type;
      _style = style;
      _output = EMPTY_BYTE;
   }

   public JProgramParameter(final VariableType type, final int length, final Object value, final PassingStyle style) {
      this(type, length, 0, value, style);
   }

   public JProgramParameter(final VariableType type, final int length, final int decimals) {
      this(type, length, decimals, null, REFERENCE);
   }

   public JProgramParameter(final VariableType type, final int length) {
      this(type, length, 0, null, REFERENCE);
   }

   public byte[] getAS400Bytes() {
      return _value;
   }

   public Object getJavaObject() {
      return _converter.toObject(_value);
   }

   public byte[] getOutput() {
      if (_style != REFERENCE) {
         throw new UnsupportedOperationException();
      }

      return _output;
   }

   public void setOutput(final byte[] output) {
      if (_style != REFERENCE) {
         throw new UnsupportedOperationException();
      }

      _output = output;
   }
}
