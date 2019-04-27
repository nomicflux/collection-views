package dev.marksman.collectionviews;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;

import java.util.List;

/**
 * A {@link Vector} that is guaranteed at compile-time to safe from mutation anywhere.  In other words,
 * it owns the sole reference to the underlying collection.
 * <p>
 * In addition to guarantees of {@link Vector}, provides the following benefits :
 * <ul>
 * <li>{@link ImmutableVector#fmap} always returns a {@code ImmutableVector}.</li>
 * <li>{@link ImmutableVector#tail} always returns a {@code ImmutableVector}.</li>
 * <li>{@link ImmutableVector#take} always returns a {@code ImmutableVector}.</li>
 * <li>{@link ImmutableVector#drop} always returns a {@code ImmutableVector}.</li>
 * <li>{@link ImmutableVector#slice} always returns a {@code ImmutableVector}.</li>
 * <li>{@link ImmutableVector#toImmutable} always returns itself.</li>
 * </ul>
 *
 * @param <A> the element type
 */
public interface ImmutableVector<A> extends Vector<A>, Immutable {

    @Override
    default ImmutableVector<A> toImmutable() {
        return this;
    }

    @Override
    default Maybe<? extends ImmutableNonEmptyVector<A>> toNonEmpty() {
        return null;
    }

    @Override
    default ImmutableNonEmptyVector<A> toNonEmptyOrThrow() {
        return null;
    }

    @Override
    default ImmutableVector<A> tail() {
        return drop(1);
    }

    @Override
    default <B> ImmutableVector<B> fmap(Fn1<? super A, ? extends B> f) {
        return Vectors.immutableMap(f, this);
    }

    @Override
    default ImmutableVector<A> take(int count) {
        return Vectors.immutableTake(count, this);
    }

    @Override
    default ImmutableVector<A> drop(int count) {
        return Vectors.immutableDrop(count, this);
    }

    @Override
    default ImmutableVector<A> slice(int startIndex, int endIndexExclusive) {
        return Vectors.immutableSlice(startIndex, endIndexExclusive, this);
    }

    static <A> ImmutableVector<A> wrapAndVouchFor(A[] underlying) {
        return Vectors.immutableWrap(underlying);
    }

    static <A> ImmutableVector<A> wrapAndVouchFor(List<A> underlying) {
        return Vectors.immutableWrap(underlying);
    }
}
