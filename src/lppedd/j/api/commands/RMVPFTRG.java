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
package lppedd.j.api.commands;

import lppedd.j.api.JConnection;
import lppedd.j.api.files.database.JPhysicalFile;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.virtual.JTrigger;

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

   private JPhysicalFile file;
   private JTrigger trigger;
   private String triggerTime = TRGTIME_ALL;
   private String triggetEvent = TRGEVENT_ALL;

   @Override
   public Boolean execute(final JConnection connection) {
      final StringBuilder builder = new StringBuilder(100);
      builder.append("RMVPFTRG FILE(");
      builder.append(file.getLibrary());
      builder.append("/");
      builder.append(file.getName());
      builder.append(") TRGTIME(");
      builder.append(triggerTime);
      builder.append(") TRGEVENT(");
      builder.append(triggetEvent);
      builder.append(") TRG(");
      builder.append(trigger.getName());
      builder.append(") TRGLIB(");
      builder.append(trigger.getLibrary());
      builder.append(")");

      return JUtil.checkForMessage("CPC3203", connection.executeCommand(builder.toString()));
   }

   public JPhysicalFile getFile() {
      return file;
   }

   public JTrigger getTrigger() {
      return trigger;
   }

   public String getTriggerTime() {
      return triggerTime;
   }

   public String getTriggerEvent() {
      return triggetEvent;
   }

   public void setFile(final JPhysicalFile file) {
      this.file = file;
   }

   public void setTrigger(final JTrigger trigger) {
      this.trigger = trigger;
   }

   public void setTriggerTime(final String triggerTime) {
      this.triggerTime = triggerTime;
   }

   public void setTriggerEvent(final String triggerEvent) {
      triggetEvent = triggerEvent;
   }
}
