package lppedd.j;

import java.util.List;
import lppedd.j.enums.JType;
import lppedd.j.interfaces.JDatabaseFile;
import lppedd.j.interfaces.JDeviceFile;
import lppedd.j.interfaces.JMember;
import lppedd.j.interfaces.JObject;

import static java.util.Collections.EMPTY_LIST;
import static lppedd.j.enums.JType.NONE;
import static lppedd.misc.EmptyArrays.EMPTY_STRING;

/**
 * @author Edoardo Luppi
 */
public class JNullObject implements JDeviceFile, JDatabaseFile
{
   private static final JConnection _CONNECTION = JConnection.getInstance();
   private static JNullObject _instance = null;

   /**
    * Returns the only one ever instance of the class.
    */
   public static synchronized JNullObject getInstance() {
      if (_instance == null) {
         _instance = new JNullObject();
      }

      return _instance;
   }

   private JNullObject() {
      //
   }

   @Override
   public boolean exists() {
      return false;
   }

   @Override
   public boolean commitChanges() {
      return false;
   }

   @Override
   public boolean copy(final String library, final String name, final boolean overwrite) {
      return false;
   }

   @Override
   public boolean move(final String library, final String name, final boolean overwrite) {
      return false;
   }

   @Override
   public boolean delete() {
      return false;
   }

   @Override
   public String getPath() {
      return "";
   }

   @Override
   public String getQualifiedPath() {
      return "";
   }

   @Override
   public String getLibrary() {
      return "";
   }

   @Override
   public String getName() {
      return "";
   }

   @Override
   public JType getType() {
      return NONE;
   }

   @Override
   public String getAttribute() {
      return "";
   }

   @Override
   public String getText() {
      return "";
   }

   @Override
   public String getCreator() {
      return "";
   }

   @Override
   public long getCreationDateTime() {
      return 0;
   }

   @Override
   public void setName(final String name) {
      //
   }

   @Override
   public void setText(final String text) {
      //
   }

   @Override
   public List<JRecordFormat> getRecordFormats() {
      return EMPTY_LIST;
   }

   @Override
   public String[] getMembers() {
      return EMPTY_STRING;
   }

   @Override
   public List<JObject> getDatabaseRelations() {
      return EMPTY_LIST;
   }

   @Override
   public JJournal getJournal() {
      throw new UnsupportedOperationException();
   }

   @Override
   public JMember toSql(final String library, final String object, final String member, final String type) {
      throw new UnsupportedOperationException();
   }

   @Override
   public JConnection getConnection() {
      return _CONNECTION;
   }
}
