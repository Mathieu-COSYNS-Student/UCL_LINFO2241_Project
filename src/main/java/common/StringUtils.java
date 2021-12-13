package common;

import java.util.function.Function;

public class StringUtils {

  public static String loopAll(StringBuilder sb, int length, Function<String, String> callback) {
    if(sb.length() > length)
      return null;

    if(sb.length() == length)
      return callback.apply(sb.toString());

    for (char c = 'a'; c <= 'z'; c++) {
      sb.append(c);
      String result = loopAll(sb, length, callback);
      if(result != null)
        return result;
      sb.deleteCharAt(sb.length() - 1);
    }

    return null;
  }

}
