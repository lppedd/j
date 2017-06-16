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
package lppedd.j.commands;

import lppedd.j.JPhysicalFile;
import lppedd.j.JTrigger;
import lppedd.j.interfaces.JCommand;

import static lppedd.j.JConnection.getInstance;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * @author Edoardo Luppi
 */
public class RMVPFTRG implements JCommand<Boolean>
{
   public static final String TRGTIME_ALL = "*ALL";
   public static final String TRGTIME_BEFORE = "*BEFORE";
   public static final String TRGTIME_AFTER = "*AFTER";

   public static final String TRGEVENT_ALL = "*ALL";
   public static final String TRGEVENT_INSERT = "*INSERT";
   public static final String TRGEVENT_DELETE = "*DELETE";
   public static final String TRGEVENT_UPDATE = "*UPDATE";
   public static final String TRGEVENT_READ = "*READ";

   private JPhysicalFile _file;
   private JTrigger _trigger;
   private String _triggerTime = TRGTIME_ALL;
   private String _triggetEvent = TRGEVENT_ALL;

   @Override
   public Boolean execute() {
      final StringBuilder builder = new StringBuilder(100);
      builder.append("RMVPFTRG FILE(");
      builder.append(_file.getLibrary());
      builder.append("/");
      builder.append(_file.getName());
      builder.append(") TRGTIME(");
      builder.append(_triggerTime);
      builder.append(") TRGEVENT(");
      builder.append(_triggetEvent);
      builder.append(") TRG(");
      builder.append(_trigger.getName());
      builder.append(") TRGLIB(");
      builder.append(_trigger.getLibrary());
      builder.append(")");

      return checkForMessage("CPC3203", getInstance().executeCommand(builder.toString()));
   }

   public JPhysicalFile getFile() {
      return _file;
   }

   public JTrigger getTrigger() {
      return _trigger;
   }

   public String getTriggerTime() {
      return _triggerTime;
   }

   public String getTriggerEvent() {
      return _triggetEvent;
   }

   public void setFile(final JPhysicalFile file) {
      _file = file;
   }

   public void setTrigger(final JTrigger trigger) {
      _trigger = trigger;
   }

   public void setTriggerTime(final String triggerTime) {
      _triggerTime = triggerTime;
   }

   public void setTriggerEvent(final String triggerEvent) {
      _triggetEvent = triggerEvent;
   }
}
