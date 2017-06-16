package lppedd.misc;

import java.util.Objects;

/**
 * @author Edoardo Luppi
 */
public final class Pair<F, S>
{
    public final F first;
    public final S second;

    public Pair(final F first, final S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Pair)) {
            return false;
        }

        final Pair<?, ?> p = (Pair<?, ?>) object;
        return Objects.equals(p.first, first) && Objects.equals(p.second, second);
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    public static <F, S> Pair<F, S> of(F f, S s) {
        return new Pair<>(f, s);
    }
}
