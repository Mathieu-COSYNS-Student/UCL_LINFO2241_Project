package server;

import java.util.Iterator;

public class StringsIterator implements Iterator<String> {

  private final StringBuilder sb;
  private final int maxStringLength;

  public StringsIterator(int stringLength) {
    this(stringLength, stringLength);
  }

  public StringsIterator(int minStringLength, int maxStringLength) {
    this.maxStringLength = maxStringLength;
    this.sb = new StringBuilder();
    sb.append("a".repeat(Math.max(0, minStringLength)));
  }

  @Override
  public boolean hasNext() {
    return sb.length() <= maxStringLength;
  }

  @Override
  public String next() {
    String value = sb.toString();
    int length = sb.length();
    while (length > 0 && sb.charAt(length - 1) == 'z') {
      length--;
      sb.setCharAt(length, 'a');
    }
    if (length == 0) {
      sb.append('a');
    } else {
      sb.setCharAt(length - 1, (char) (sb.charAt(length - 1) + 1));
    }
    return value;
  }

}
