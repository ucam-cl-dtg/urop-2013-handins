package uk.ac.cam.sup.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayHelper {
    public static <T> List<T> interleave(final List<T> list1, final List<T> list2) {
        return interleave(list1, list2, 20);
    }
    public static <T> List<T> interleave(final List<T> list1, final List<T> list2, int size) {
        List<T> result = new ArrayList<T>(list2.size()
                + list1.size());

        int count = 0;

        Iterator<T> it1 = list1.iterator();
        Iterator<T> it2 = list2.iterator();
        while (it1.hasNext() || it2.hasNext()) {
            if (it1.hasNext()) {
                result.add(it1.next());
                count ++;
            }
            if (it2.hasNext()) {
                result.add(it2.next());
                count ++;
            }
            if (count >= size)
                break;
        }
        return result;
    }
}
