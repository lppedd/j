package lppedd.j.interfaces;

import com.ibm.as400.access.AS400Bin4;
import com.ibm.as400.access.AS400Text;

/**
 * @author Edoardo Luppi
 */
public interface AS400DataTypes
{
   public AS400Text CHAR1 = new AS400Text(1);
   public AS400Text CHAR3 = new AS400Text(3);
   public AS400Text CHAR8 = new AS400Text(8);
   public AS400Text CHAR10 = new AS400Text(10);
   public AS400Text CHAR13 = new AS400Text(13);
   public AS400Text CHAR20 = new AS400Text(20);
   public AS400Text CHAR36 = new AS400Text(36);
   public AS400Text CHAR50 = new AS400Text(50);
   public AS400Text CHAR258 = new AS400Text(258);
   public AS400Bin4 BIN4 = new AS400Bin4();
}
