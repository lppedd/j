package lppedd.j.enums;

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
