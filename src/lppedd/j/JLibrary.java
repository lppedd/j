package lppedd.j;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDescription;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ObjectList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lppedd.j.abstracts.JAbstractObject;
import lppedd.j.enums.JType;
import lppedd.j.interfaces.JObject;

import static com.ibm.as400.access.ObjectList.ALL;
import static java.util.Collections.EMPTY_LIST;
import static lppedd.j.JObjectFactory.get;
import static lppedd.j.enums.JType.LIB;
import static lppedd.j.enums.JType.valueOf;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Represents an IBMi library.
 *
 * @author Edoardo Luppi
 */
public class JLibrary extends JAbstractObject
{
   public JLibrary(final String name) {
      super(name, "QSYS", LIB);
   }

   /**
    * Returns the library content.
    *
    * @return A list of the objects contained in the library
    */
   public List<JObject> getObjects(final JType objectType) {
      final ObjectList objectList = new ObjectList(getConnection().getAS400(), _name, ALL, objectType.getObjectType());

      try {
         objectList.load();

         final int size = objectList.getLength();
         final List<JObject> objects = new ArrayList<>(size);
         final ObjectDescription[] objectDescriptions = objectList.getObjects(0, size);

         for (int i = 0; i < size; i++) {
            final ObjectDescription objectDescription = objectDescriptions[i];
            objects.add(get(objectDescription.getName(), objectDescription.getLibrary(), valueOf(objectDescription.getType())));
         }

         return objects;
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return EMPTY_LIST;
   }

   /**
    * Create the library.
    */
   public boolean create() {
      if (exists()) {
         return false;
      }

      final StringBuilder builder = new StringBuilder(95);
      builder.append("CRTLIB LIB(");
      builder.append(_name);
      builder.append(") TYPE(*PROD)");

      if (!_text.isEmpty()) {
         builder.append(" TEXT('");
         builder.append(_text);
         builder.append("')");
      }

      return checkForMessage("CPC2102", getConnection().executeCommand(builder.toString()));
   }

   @Override
   public boolean delete() {
      return checkForMessage("CPC2194", getConnection().executeCommand("DLTLIB (" + _name + ")"));
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(30);
      builder.append("RNMOBJ (");
      builder.append(_library);
      builder.append("/");
      builder.append(_originalName);
      builder.append(") OBJTYPE(");
      builder.append(_type.getObjectType());
      builder.append(") NEWOBJ(");
      builder.append(_name);
      builder.append(")");

      return checkForMessage("CPC2192", getConnection().executeCommand(builder.toString()));
   }
}
