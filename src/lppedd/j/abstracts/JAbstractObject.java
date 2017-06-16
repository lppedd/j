package lppedd.j.abstracts;

import lppedd.j.JAPIOutput;
import lppedd.j.JConnection;
import lppedd.j.enums.JType;
import lppedd.j.interfaces.JObject;
import lppedd.j.misc.JUtil;

import static com.ibm.as400.access.QSYSObjectPathName.toPath;
import static java.lang.Long.parseLong;
import static lppedd.j.JAPI.QUSROBJD;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Represents an IBMi object.
 *
 * @author Edoardo Luppi
 */
public abstract class JAbstractObject extends JAbstractBase implements JObject
{
   // QUSROBJD - OBJD0400 format
   protected byte[] _OBJD0400;

   protected JAbstractObject(final String name, final String library, final JType type) {
      super(name, library, type);

      // If the object exists on the system, then load its informations
      if (exists()) {
         retriveObjectDescription();
      }
   }

   @Override
   public boolean exists() {
      return getConnection().exists(_library, _name, _type, "");
   }

   @Override
   public boolean copy(final String library, final String name, final boolean overwrite) {
      final JConnection connection = getConnection();

      if (!overwrite && connection.exists(library, name, _type, "")) {
         return false;
      }

      final StringBuilder builder = new StringBuilder(112);
      builder.append("CRTDUPOBJ OBJ(");
      builder.append(_name);
      builder.append(") FROMLIB(");
      builder.append(_library);
      builder.append(") NEWOBJ(");
      builder.append(name);
      builder.append(") TOLIB(");
      builder.append(library);
      builder.append(") OBJTYPE(*");
      builder.append(_type);
      builder.append(") CST(*NO) TRG(*NO) DATA(*NO)");

      return checkForMessage("CPI2101", connection.executeCommand(builder.toString()));
   }

   @Override
   public boolean move(final String library, final String name, final boolean overwrite) {
      if (!copy(library, name, overwrite)) {
         return false;
      }

      // Delete the object from the actual position
      if (!delete()) {
         return false;
      }

      _library = library;
      _name = name;
      return true;
   }

   @Override
   public String getPath() {
      return toPath(_library, _name, _type.getObjectType().substring(1));
   }

   @Override
   public String getQualifiedPath() {
      return JUtil.getQualifiedPath(_library, _name);
   }

   @Override
   public String getName() {
      return _name;
   }

   @Override
   protected boolean performSetName() {
      final StringBuilder builder = new StringBuilder(84);
      builder.append("RNMOBJ OBJ(");
      builder.append(_library);
      builder.append("/");
      builder.append(_originalName);
      builder.append(") NEWOBJ(");
      builder.append(_name);
      builder.append(") OBJTYPE(*");
      builder.append(_type);
      builder.append(")");

      return checkForMessage("CPC2192", getConnection().executeCommand(builder.toString()));
   }

   /**
    * Calls QUSROBJD API to retrive the object informations.
    */
   private final void retriveObjectDescription() {
      final JAPIOutput output = QUSROBJD("OBJD0400", this);

      if (output.getMessages().length != 0) {
         return;
      }

      _OBJD0400 = output.getValue();

      _text = CHAR50.toObject(_OBJD0400, 100).toString().trim();
      _creator = CHAR10.toObject(_OBJD0400, 219).toString().trim();

      if (_attribute == null || _attribute.isEmpty()) {
         _attribute = CHAR10.toObject(_OBJD0400, 90).toString().trim();
      }

      final StringBuilder builder = new StringBuilder(19);
      builder.append(CHAR13.toObject(_OBJD0400, 64));

      if (builder.charAt(0) == '0') {
         builder.insert(1, "19");
      } else {
         builder.insert(1, "20");
      }

      builder.delete(0, 1);
      builder.append("000");

      _creationDateTime = parseLong(builder.toString());
   }
}
