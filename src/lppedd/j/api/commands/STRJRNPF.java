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
import lppedd.j.api.objects.JJournal;

/**
 * @author Edoardo Luppi
 */
public class STRJRNPF implements JCommand<Boolean>
{
   public static final String IMAGES_AFTER = "*AFTER";
   public static final String IMAGES_BOTH = "*BOTH";
   public static final String OMTJRNE_NONE = "*NONE";
   public static final String OMTJRNE_OPNCLO = "*OPNCLO";
   public static final String LOGLVL_ALL = "*ALL";
   public static final String LOGLVL_ERRORS = "*ERRORS";

   private JPhysicalFile file;
   private JJournal journal;
   private String images = IMAGES_AFTER;
   private String omittedEntires = OMTJRNE_NONE;
   private String logLevel = LOGLVL_ALL;

   @Override
   public Boolean execute(final JConnection connection) {
      final StringBuilder builder = new StringBuilder(85);
      builder.append("STRJRNPF FILE(");
      builder.append(file.getLibrary());
      builder.append("/");
      builder.append(file.getName());
      builder.append(") JRN(");
      builder.append(journal.getLibrary());
      builder.append("/");
      builder.append(journal.getName());
      builder.append(") IMAGES(");
      builder.append(images);
      builder.append(") OMTJRNE(");
      builder.append(omittedEntires);
      builder.append(") LOGLVL(");
      builder.append(logLevel);
      builder.append(")");

      return JUtil.checkForMessage("CPC7031", connection.executeCommand(builder.toString()));
   }

   public JPhysicalFile getFile() {
      return file;
   }

   public JJournal getJournal() {
      return journal;
   }

   public String getImages() {
      return images;
   }

   public String getOmittedEntries() {
      return omittedEntires;
   }

   public String getLogLevel() {
      return logLevel;
   }

   public void setFile(final JPhysicalFile file) {
      this.file = file;
   }

   public void setJournal(final JJournal jrn) {
      journal = jrn;
   }

   public void setImages(final String images) {
      this.images = images;
   }

   public void setOmittedEntries(final String omtjrne) {
      omittedEntires = omtjrne;
   }

   public void setLogLevel(final String loglvl) {
      logLevel = loglvl;
   }
}
