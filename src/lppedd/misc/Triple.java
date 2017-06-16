package lppedd.misc;

import java.util.Objects;

/**
 * @author Edoardo Luppi
 */
public final class Triple<F, S, T>
{
    public final F first;
    public final S second;
    public final T third;

    public Triple(final F first, final S second, final T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Triple)) {
            return false;
        }

        final Triple<?, ?, ?> p = (Triple<?, ?, ?>) object;
        return Objects.equals(p.first, first) && Objects.equals(p.second, second) && Objects.equals(p.third, third);
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode()) ^ (third == null ? 0 : third.hashCode());
    }

    public static <F, S, T> Triple<F, S, T> of(F f, S s, T t) {
        return new Triple<>(f, s, t);
    }
}
