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
package lppedd.j.api.members;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.exceptions.JInvalidWorkItemException;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.misc.Util;
import smi.workitem.SmiAbstractWorkItem;
import smi.workitem.SmiSqlWorkItem;

/**
 * Represents an IBMi SQL source member.
 *
 * @author Edoardo Luppi
 */
public class JSqlMember extends JAbstractMember
{
   public JSqlMember(final JConnection connection, final String name, final String object, final String library) {
      super(connection, name, object, library);
   }

   @Override
   public boolean compile(final String library) {
      final long time = Long.parseLong(getConnection().getSystemValue("QDATETIME").toString().substring(0, 14) + "000");

      final StringBuilder builder = new StringBuilder(92);
      builder.append("ABCRTSQL SRCFILE(");
      builder.append(library);
      builder.append("/");
      builder.append(object);
      builder.append(") MEM(");
      builder.append(name);
      builder.append(") OBJ(");
      builder.append(library);
      builder.append("/");
      builder.append(name);
      builder.append(")");

      getConnection().executeCommand(builder.toString());
      return time <= JObjectFactory.get(getConnection(), name, library, JType.FILE).getCreationDateTime();
   }

   @Override
   protected void inspectForWorkItems() {
      // Se necessario pulisco la lista prima di popolarla
      if (workItemList.size() > 0) {
         workItemList.clear();
      }

      for (int i = 0; i < source.size(); i++) {
         final String line = source.get(i);

         // Sono alla fine dei work item in testatina? Se si, salvo la posizione
         if (!workItemList.isEmpty() && PATTERN_EOC.matcher(line).matches()) {
            workItemsEnd = i;
            break;
         }

         // Se la linea corrisponde ad un un work item, lo aggiungo alla lista
         // e mi salvo la posizioni in modo da poter, successivamente, andare ad inserire il suo testo
         if (PATTERN_MOD.matcher(line).matches() && Util.isInteger(line.substring(16, 24))) {
            try {
               workItemList.add(new SmiSqlWorkItem(this, line), i);
            } catch (final JInvalidWorkItemException e) {
               e.printStackTrace();
            }
         }
      }

      // Completo i work item con i rispettivi testi
      final int size = workItemList.size() - 1;

      for (int i = 0; i <= size; i++) {
         final SmiAbstractWorkItem workItem = workItemList.getWorkItem(i);

         for (int k = workItemList.getPosition(i) + 1; k < (i == size ? workItemsEnd : workItemList.getPosition(i + 1)); k++) {
            workItem.appendText(source.get(k).substring(9));
         }
      }
   }

   @Override
   protected String backToThePast(final String line, final String modNumber) {
      // TODO
      return line;
   }
}
