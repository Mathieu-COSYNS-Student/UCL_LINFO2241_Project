package server;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
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
  public void testEdges() {
    StringsIterator stringsIterator = new StringsIterator(0, 3);
    HashMap<Integer, String> positionValues = new HashMap<>();
    positionValues.put(0, "");
    positionValues.put(1, "a");
    positionValues.put(26, "z");
    positionValues.put(1 + 26, "aa");
    positionValues.put(1 + 26 + 26 * 26 - 1, "zz");
    positionValues.put(1 + 26 + 26 * 26, "aaa");
    positionValues.put(1 + 26 + 26 * 26 + 26 * 26 * 26 - 1, "zzz");

    int i = 0;
    while (stringsIterator.hasNext()) {
      String item = stringsIterator.next();
      String expected = positionValues.get(i);
      if (expected != null) {
        assertEquals("Iterator item n°" + i + " has not the expected value", expected, item);
      }
      i++;
    }

    assertEquals("Iterator has not iterated over all supposed item",
        1 + 26 + 26 * 26 + 26 * 26 * 26,
        i);
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
