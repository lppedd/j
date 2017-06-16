package smi.workitem;

import java.util.ArrayList;
import lppedd.misc.Pair;
import smi.workitem.abstracts.SMIWorkItem;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static lppedd.misc.Pair.of;

/**
 * {@code Work item : posizione nel sorgente}<br>
 * Il funzionamento e' identico a quello di un {@link ArrayList}
 *
 * @author Edoardo Luppi
 */
public final class SMIWorkItemList
{
   private static final int INITIAL_SIZE = 50;

   private SMIWorkItem[] _workItems;
   private int[] _workItemPositions;
   private int _size;

   public SMIWorkItemList() {
      _workItems = new SMIWorkItem[INITIAL_SIZE];
      _workItemPositions = new int[INITIAL_SIZE];
   }

   public SMIWorkItemList(final int initialSize) {
      if (initialSize < 0) {
         throw new IllegalArgumentException();
      }

      _workItems = new SMIWorkItem[initialSize];
      _workItemPositions = new int[initialSize];
   }

   public void add(final SMIWorkItem workItem, final int position) {
      ensureCapacity(_size + 1);
      _workItems[_size] = workItem;
      _workItemPositions[_size] = position;
      _size++;
   }

   public SMIWorkItem getWorkItem(final int index) {
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

   public Pair<SMIWorkItem, Integer> remove(final int index) {
      if (index < 0 || index >= _size) {
         throw new IndexOutOfBoundsException();
      }

      final Pair<SMIWorkItem, Integer> oldValue = of(_workItems[index], _workItemPositions[index]);
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

   public SMIWorkItem[] getWorkItems() {
      final SMIWorkItem[] copy = new SMIWorkItem[_size];
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
