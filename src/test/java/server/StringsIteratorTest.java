package server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringsIteratorTest {

  @Test
  public void testAlphabet() {
    StringsIterator stringsIterator = new StringsIterator(0, 1);

    int i = -1;
    while (stringsIterator.hasNext()) {
      if (i == -1) {
        assertEquals("Wrong value while testing the " + i + "eme element", "",
            stringsIterator.next());
      } else {
        assertEquals("Wrong value while testing the " + i + "eme element", "" + (char) ('a' + i),
            stringsIterator.next());
      }
      i++;
    }
  }

  @Test
  public void testVisual() {
    StringsIterator stringsIterator = new StringsIterator(0, 3);

    while (stringsIterator.hasNext()) {
      System.out.println(stringsIterator.next());
    }
  }

  @Test
  public void testMinLen() {
    StringsIterator stringsIterator = new StringsIterator(1, 1);

    int i = 0;
    while (stringsIterator.hasNext()) {
      assertEquals("Wrong value while testing the " + i + "eme element", "" + (char) ('a' + i),
          stringsIterator.next());
      i++;
    }
  }

  @Test
  public void testMinLen2() {
    StringsIterator stringsIterator = new StringsIterator(6, 6);

    assertEquals("Min len 6", "aaaaaa", stringsIterator.next());
  }

}
