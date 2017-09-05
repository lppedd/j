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
package lppedd.j.api;

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
   
   private final String sourceType;
   private final String attribute;
   private final String objectType;
   private final String sqlType;
   
   private JType(final String sourceType, final String attribute, final String objectType, final String sqlType) {
      this.sourceType = sourceType;
      this.attribute = attribute;
      this.objectType = objectType;
      this.sqlType = sqlType;
   }
   
   public String getSourceType() {
      return sourceType;
   }
   
   public String getObjectType() {
      return objectType;
   }
   
   public String getAttribute() {
      return attribute;
   }
   
   public String getSqlType() {
      return sqlType;
   }
}
