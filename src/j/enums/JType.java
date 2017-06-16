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
package j.enums;

/**
 * Represet an object type in the IBMi environment.
 *
 * @author Edoardo Luppi
 */
public enum JType
{
   // Specific values
   TABLE("SQL", "PF", "*FILE", "TABLE"),
   VIEW("SQL", "LF", "*FILE", "VIEW"),
   PF("PF", "PF", "*FILE", "TABLE"),
   LF("LF", "LF", "*FILE", "VIEW"),
   DS("DS", "PF", "*FILE", ""),
   PRTF("PRTF", "", "*FILE", ""),
   SQLRPGLE("SQLRPGLE", "RPGLE", "*PGM", ""),
   RPGLE("RPGLE", "RPGLE", "*PGM", ""),
   CLLE("CLLE", "CLLE", "*PGM", ""),
   // Generic values
   USRSPC("", "", "*USRSPC", ""),
   FILE("", "", "*FILE", ""),
   MBR("", "MBR", "*FILE", ""),
   PGM("", "", "*PGM", ""),
   SRVPGM("", "", "*SRVPGM", ""),
   MODULE("", "", "*MODULE", ""),
   JRN("", "", "*JRN", ""),
   LIB("", "", "*LIB", ""),
   // Special values
   ALL("", "", "*ALL", ""),
   NONE("", "", "", "");

   private final String _sourceType;
   private final String _attribute;
   private final String _objectType;
   private final String _sqlType;

   private JType(final String sourceType, final String attribute, final String objecType, final String sqlType) {
      _sourceType = sourceType;
      _attribute = attribute;
      _objectType = objecType;
      _sqlType = sqlType;
   }

   public String getSourceType() {
      return _sourceType;
   }

   public String getObjectType() {
      return _objectType;
   }

   public String getAttribute() {
      return _attribute;
   }

   public String getSqlType() {
      return _sqlType;
   }
}
