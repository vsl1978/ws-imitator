package xyz.vsl.wsimitator.util;

/**
 * Created by vsl on 15.05.2016.
 */
public class Pair<A,B> {
    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static <K,V> Pair<K,V> of(K k, V v) {
        return new Pair<K,V>(k, v);
    }
}
