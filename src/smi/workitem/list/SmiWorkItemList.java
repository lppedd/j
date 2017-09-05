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
package smi.workitem.list;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static lppedd.j.api.misc.Pair.of;

import java.util.ArrayList;

import lppedd.j.api.misc.Pair;
import smi.workitem.SmiAbstractWorkItem;

/**
 * {@code Work item : posizione nel sorgente}<br>
 * Il funzionamento e' identico a quello di un {@link ArrayList}
 *
 * @author Edoardo Luppi
 */
public final class SmiWorkItemList
{
   private static final int INITIAL_SIZE = 50;
   
   private SmiAbstractWorkItem[] _workItems;
   private int[] _workItemPositions;
   private int _size;
   
   public SmiWorkItemList() {
      _workItems = new SmiAbstractWorkItem[INITIAL_SIZE];
      _workItemPositions = new int[INITIAL_SIZE];
   }
   
   public SmiWorkItemList(final int initialSize) {
      if (initialSize < 0) {
         throw new IllegalArgumentException();
      }
      
      _workItems = new SmiAbstractWorkItem[initialSize];
      _workItemPositions = new int[initialSize];
   }
   
   public void add(final SmiAbstractWorkItem workItem, final int position) {
      ensureCapacity(_size + 1);
      _workItems[_size] = workItem;
      _workItemPositions[_size] = position;
      _size++;
   }
   
   public SmiAbstractWorkItem getWorkItem(final int index) {
      if (index < 0 || index > _size) {
         throw new IndexOutOfBoundsException();
      }
      
      return _workItems[index];
   }
   
   public int getPosition(final int index) {
      if (index < 0 || index > _size) {
         throw new IndexOutOfBoundsException();
      }
      
      return _workItemPositions[index];
   }
   
   public Pair<SmiAbstractWorkItem, Integer> remove(final int index) {
      if (index < 0 || index >= _size) {
         throw new IndexOutOfBoundsException();
      }
      
      final Pair<SmiAbstractWorkItem, Integer> oldValue = of(_workItems[index], _workItemPositions[index]);
      final int numMoved = _size - index - 1;
      
      if (numMoved > 0) {
         arraycopy(_workItems, index + 1, _workItems, index, numMoved);
         arraycopy(_workItemPositions, index + 1, _workItemPositions, index, numMoved);
      }
      
      _workItems[--_size] = null;
      return oldValue;
   }
   
   public int contains(final int workItemNumber) {
      for (int i = 0; i < _size; i++) {
         if (_workItems[i].getNumber() == workItemNumber) {
            return i;
         }
      }
      
      return -1;
   }
   
   public void clear() {
      for (int i = 0; i < _size; i++) {
         _workItems[i] = null;
      }
      
      _size = 0;
   }
   
   public int size() {
      return _size;
   }
   
   public boolean isEmpty() {
      return _size == 0;
   }
   
   public SmiAbstractWorkItem[] getWorkItems() {
      final SmiAbstractWorkItem[] copy = new SmiAbstractWorkItem[_size];
      arraycopy(_workItems, 0, copy, 0, _size);
      return copy;
   }
   
   private void ensureCapacity(final int size) {
      if (size > _workItems.length) {
         final int newSize = _workItems.length + 30;
         _workItems = copyOf(_workItems, newSize);
         _workItemPositions = copyOf(_workItemPositions, newSize);
      }
   }
}
