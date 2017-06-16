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
package lppedd.j;

import com.ibm.as400.access.AS400Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lppedd.misc.Util;

/**
 * Represents an IBMi message.
 *
 * @author Edoardo Luppi
 */
public class JMessage
{
   private final String _id;
   private final String _text;
   private final String _sendingProgramName;

   public JMessage(final String id, final String text) {
      this(id, text, "");
   }

   public JMessage(final String id, final String text, final String sendingProgramName) {
      _id = id == null ? "" : id;
      _text = text == null ? "" : text;
      _sendingProgramName = sendingProgramName == null ? "" : sendingProgramName;
   }

   public String getId() {
      return _id;
   }

   public String getText() {
      return _text;
   }

   public String getSendingProgramName() {
      return _sendingProgramName;
   }

   @Override
   public String toString() {
      return _id + ": " + _text;
   }

   @Override
   public boolean equals(final Object object) {
      if (!(object instanceof JMessage)) {
         return false;
      }

      final JMessage message = (JMessage) object;
      return _id.equals(message._id) && _text.equals(message._text) && _sendingProgramName.equals(message._sendingProgramName);
   }

   @Override
   public int hashCode() {
      int result = 7;
      result = result * 31 + Util.hashCode(_id);
      result = result * 31 + Util.hashCode(_text);
      result = result * 31 + Util.hashCode(_sendingProgramName);
      return result;
   }

   /**
    * Check if an array of messages contains a message ID.
    *
    * @param messages The array of messages
    * @param ids      The messages to look for
    */
   public static boolean contains(final List<JMessage> messages, final String... ids) {
      for (int i = 0; i < messages.size(); i++) {
         for (final String id : ids) {
            if (messages.get(i)._id.equals(id)) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Converts an {@link AS400Message} to a {@link JMessage}.
    *
    * @param ASMessage The {@link AS400Message} to be converted
    */
   public static JMessage convertMessage(final AS400Message ASMessage) {
      return new JMessage(ASMessage.getID(), ASMessage.getText(), ASMessage.getSendingProgramName());
   }

   /**
    * Converts an array of {@link AS400Message} to an array of
    * {@link JMessage}.
    *
    * @param ASMessages The array of {@link AS400Message} to be converted
    */
   public static List<JMessage> convertMessages(final AS400Message[] ASMessages) {
      final int size = ASMessages.length;
      final List<JMessage> messages = size < 1 ? Collections.<JMessage>emptyList() : new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
         final AS400Message ASMessage = ASMessages[i];
         messages.add(new JMessage(ASMessage.getID(), ASMessage.getText(), ASMessage.getSendingProgramName()));
      }

      return messages;
   }
}
