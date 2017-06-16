package lppedd.j.interfaces;

import java.util.List;
import lppedd.j.JRecordFormat;

public interface JFile extends JObject
{
   public List<JRecordFormat> getRecordFormats();
}
