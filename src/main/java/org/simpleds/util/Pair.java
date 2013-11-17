package org.simpleds.util;

import com.google.common.collect.ComparisonChain;

import java.util.Objects;

/**
 * For methods that should return two values
 */
public class Pair<A extends Comparable, B extends Comparable> implements Comparable<Pair<A, B>> {

    private A value1;

    private B value2;

    public Pair() {
    }

    public Pair(A value1, B value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public A getValue1() {
        return value1;
    }

    public B getValue2() {
        return value2;
    }

    @Override
    public String toString() {
        return "Pair { value1=" + value1 + ", value2=" + value2 + "}";
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(value1, value2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Pair)) {
            return false;
        }
        Pair<A, B> other = (Pair<A, B>) obj;
        return Objects.equals(value1, other.getValue1()) && Objects.equals(value2, other.getValue2());
    }

    @Override
    public int compareTo(Pair<A, B> o) {
        return (o == null) ? -1 : ComparisonChain.start()
                .compare(this.value1, o.getValue1())
                .compare(this.value2, o.getValue2())
                .result();
    }

}
