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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.files.database.JPhysicalFile;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.JLibrary;
import lppedd.j.api.objects.JObject;

/**
 * @author Edoardo Luppi
 */
public final class DSPPGMREF implements JCommand<List<JObject>>
{
   public static final String PROGRAM_ALL = "*ALL";
   public static final String OBJTYPE_ALL = "*ALL";
   public static final String OBJTYPE_PGM = "*PGM";
   public static final String OBJTYPE_SQLPKG = "*SQLPKG";
   public static final String OBJTYPE_SRVPGM = "*SRVPGM";
   public static final String OBJTYPE_MODULE = "*MODULE";
   public static final String OBJTYPE_QRYDFN = "*QRYDFN";
   
   public static class Filter
   {
      public String program = "";
      public String library = "";
      public String referencedObject = "";
      public String referencedLibrary = "";
      
      private Filter() {}
      
      public String buildString() {
         final StringBuilder builder = new StringBuilder(100);
         
         if (!library.isEmpty()) {
            builder.append("WHPNAM = '");
            builder.append(program);
            builder.append("' AND ");
         }
         
         if (!library.isEmpty()) {
            builder.append("WHLIB = '");
            builder.append(library);
            builder.append("' AND ");
         }
         
         if (!referencedObject.isEmpty()) {
            builder.append("WHFNAM = '");
            builder.append(referencedObject);
            builder.append("' AND ");
         }
         
         if (!referencedLibrary.isEmpty()) {
            builder.append("WHLNAM = '");
            builder.append(referencedLibrary);
            builder.append("' AND ");
         }
         
         final int builderLength = builder.length();
         
         if (builderLength > 4) {
            builder.delete(builderLength - 5, builderLength);
         }
         
         return builder.toString();
      }
   }
   
   private String program = "";
   private String library = "";
   private String[] objectType = new String[0];
   private final Filter filter = new Filter();
   
   @Override
   public List<JObject> execute(final JConnection connection) {
      final String tempLibraryName = JUtil.getRandomString(10);
      final String tempFileName = JUtil.getRandomString(10);
      
      final JLibrary tempLibrary = new JLibrary(connection, tempLibraryName);
      tempLibrary.setText("Temporary library for DSPDBR");
      tempLibrary.create();
      
      if (JObjectFactory.get(connection, "DSPPGMREFS", "WRKEDOLUP", JType.FILE).copy(tempLibraryName, tempFileName, true)) {
         ((JPhysicalFile) JObjectFactory.get(connection, tempFileName, tempLibraryName, JType.FILE)).changeSize(200000);
      }
      
      final StringBuilder builder = new StringBuilder(120);
      builder.append("DSPPGMREF PGM(");
      builder.append(library);
      builder.append("/");
      builder.append(program);
      builder.append(") OUTPUT(*OUTFILE) OBJTYPE(");
      
      for (final String type : objectType) {
         builder.append(type);
         builder.append(" ");
      }
      
      builder.append(") OUTFILE(");
      builder.append(tempLibraryName);
      builder.append("/");
      builder.append(tempFileName);
      builder.append(")");
      
      if (JUtil.checkForMessage("CPF3030", connection.executeCommand(builder.toString()))) {
         builder.delete(0, builder.length());
         builder.append("SELECT DISTINCT WHPNAM, WHFNAM FROM ");
         builder.append(tempLibraryName);
         builder.append(".");
         builder.append(tempFileName);
         
         final String filters = filter.buildString();
         
         if (!filters.isEmpty()) {
            builder.append(" WHERE ");
            builder.append(filters);
         }
         
         PreparedStatement statement = null;
         ResultSet result = null;
         
         try {
            statement = connection.getConnection().prepareStatement(builder.toString());
            result = statement.executeQuery();
            
            final List<JObject> references = new ArrayList<>(128);
            
            while (result.next()) {
               references.add(JObjectFactory.get(connection, result.getString("WHPNAM").trim(), "*LIBL", JType.ALL));
            }
            
            return references;
         } catch (final SQLException e) {
            e.printStackTrace();
         } finally {
            tempLibrary.delete();
            
            try {
               if (result != null) {
                  result.close();
               }
               
               if (statement != null) {
                  statement.close();
               }
            } catch (final SQLException e) {
               e.printStackTrace();
            }
         }
      }
      
      return Collections.emptyList();
   }
   
   public String getProgram() {
      return program;
   }
   
   public String getLibrary() {
      return library;
   }
   
   public String[] getObjectType() {
      return objectType;
   }
   
   public Filter getFilter() {
      return filter;
   }
   
   public void setProgram(final String program) {
      this.program = program;
   }
   
   public void setLibrary(final String library) {
      this.library = library;
   }
   
   public void setObjectType(final String... objectType) {
      this.objectType = objectType;
   }
}
