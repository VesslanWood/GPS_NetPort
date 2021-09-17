package android_serialport_api.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>文件描述：<p>
 * <p>作者：jambestwick<p>
 * <p>创建时间：2021/8/27<p>
 * <p>更新时间：2021/8/27<p>
 * <p>版本号：<p>
 * <p>邮箱：jambestwick@126.com<p>
 */

public class StringUtil {
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\\t|\\r|\\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String addLineHeadByParams(String origin, char... strs) {
        String replaceEnter = replaceBlank(origin).trim();
        String bcd = replaceEnter.replaceAll("\\$", "\r\n\\$");
        String res = bcd.replaceAll("\\#", "\r\n\\#");
        return res;
    }

    /**
     * 求Map<K,V>中Value(值)的最大值
     *
     * @param map
     * @return
     */
    public static Object getMaxValue(Map<Integer, Integer> map) {
        if (map == null)
            return null;
        int length = map.size();
        Collection<Integer> c = map.values();
        Object[] obj = c.toArray();
        Arrays.sort(obj);
        return obj[length - 1];
    }

    public static <K, V> K getMaxKey(Map<K, V> map) {
        if (map == null)
            return null;
        int length = map.size();
        Collection<V> c = map.values();
        Object[] obj = c.toArray();
        Arrays.sort(obj);
        return getKeyByLoop(map, (V) obj[length - 1]);
    }

    private static <K, V> K getKeyByLoop(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
