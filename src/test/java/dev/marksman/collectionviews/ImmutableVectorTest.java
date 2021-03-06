package dev.marksman.collectionviews;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.builtin.fn2.Take;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Cycle.cycle;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Id.id;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Repeat.repeat;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Cons.cons;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Replicate.replicate;
import static com.jnape.palatable.lambda.functions.builtin.fn3.FoldLeft.foldLeft;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.*;

class ImmutableVectorTest {

    @Nested
    @DisplayName("empty")
    class EmptyVectorTests {

        @Test
        void alwaysYieldsSameReference() {
            ImmutableVector<Integer> v1 = Vector.empty();
            ImmutableVector<String> v2 = Vector.empty();
            assertSame(v1, v2);
        }

        @Test
        void isEmpty() {
            assertTrue(Vector.empty().isEmpty());
        }

        @Test
        void sizeIsZero() {
            assertEquals(0, Vector.empty().size());
        }

        @Test
        void getReturnsNothing() {
            ImmutableVector<Object> subject = Vector.empty();
            assertEquals(nothing(), subject.get(0));
            assertEquals(nothing(), subject.get(1));
            assertEquals(nothing(), subject.get(-1));
        }

        @Test
        void unsafeGetThrows() {
            ImmutableVector<Object> subject = Vector.empty();
            assertThrows(IndexOutOfBoundsException.class, () -> subject.unsafeGet(0));
            assertThrows(IndexOutOfBoundsException.class, () -> subject.unsafeGet(1));
            assertThrows(IndexOutOfBoundsException.class, () -> subject.unsafeGet(-1));
        }

        @Test
        void iteratesCorrectly() {
            assertThat(Vector.empty(), emptyIterable());
        }

        @Test
        void tailIsEmpty() {
            assertThat(Vector.empty().tail(), emptyIterable());
        }
    }

    @Nested
    @DisplayName("copyFrom")
    class CopyFromTests {

        @Nested
        @DisplayName("copyFrom array")
        class CopyFromArrayTests {

            @Test
            void throwsOnNullArgument() {
                Integer[] arr = null;
                assertThrows(NullPointerException.class, () -> Vector.copyFrom(arr));
            }

            @Test
            void makesCopy() {
                Integer[] arr = new Integer[]{1, 2, 3};
                ImmutableVector<Integer> subject = Vector.copyFrom(arr);
                assertThat(subject, contains(1, 2, 3));
                arr[0] = 4;
                assertThat(subject, contains(1, 2, 3));
            }

            @Test
            void getWillNeverReturnNull() {
                ImmutableVector<String> subject = Vector.copyFrom(new String[]{"foo", null, "baz"});
                assertEquals(just("foo"), subject.get(0));
                assertEquals(nothing(), subject.get(1));
                assertEquals(just("baz"), subject.get(2));
            }

            @Test
            void iteratorNextReturnsCorrectElements() {
                ImmutableVector<String> subject = Vector.copyFrom(new String[]{"foo", "bar", "baz"});
                Iterator<String> iterator = subject.iterator();
                assertEquals("foo", iterator.next());
                assertEquals("bar", iterator.next());
                assertEquals("baz", iterator.next());
            }

            @SuppressWarnings("ConstantConditions")
            @Test
            void iteratorHasNextCanBeCalledMultipleTimes() {
                ImmutableVector<String> subject = Vector.copyFrom(new String[]{"foo", "bar", "baz"});
                Iterator<String> iterator = subject.iterator();
                assertTrue(iterator.hasNext());
                assertTrue(iterator.hasNext());
                assertTrue(iterator.hasNext());
                assertEquals("foo", iterator.next());
            }

            @Test
            void iteratorHasNextReturnsFalseIfNothingRemains() {
                ImmutableVector<String> subject = Vector.copyFrom(new String[]{"foo"});
                Iterator<String> iterator = subject.iterator();
                iterator.next();
                assertFalse(iterator.hasNext());
            }

            @Test
            void iteratorNextThrowsIfNothingRemains() {
                ImmutableVector<String> subject = Vector.copyFrom(new String[]{"foo"});
                Iterator<String> iterator = subject.iterator();
                iterator.next();
                assertThrows(NoSuchElementException.class, iterator::next);
            }

            @Test
            void iteratorThrowsIfRemoveIsCalled() {
                ImmutableVector<String> subject = Vector.copyFrom(new String[]{"foo"});
                Iterator<String> iterator = subject.iterator();
                assertThrows(UnsupportedOperationException.class, iterator::remove);
            }

            @Nested
            @DisplayName("copyFrom size 3 array")
            class CopyFromArray3Tests {
                private ImmutableVector<String> subject;
                private String[] underlying;

                @BeforeEach
                void beforeEach() {
                    underlying = new String[]{"foo", "bar", "baz"};
                    subject = Vector.copyFrom(underlying);
                }

                @Test
                void notEmpty() {
                    assertFalse(subject.isEmpty());
                }

                @Test
                void sizeIs3() {
                    assertEquals(3, subject.size());
                }

                @Test
                void getForValidIndices() {
                    assertEquals(just("foo"), subject.get(0));
                    assertEquals(just("bar"), subject.get(1));
                    assertEquals(just("baz"), subject.get(2));
                }

                @Test
                void getForInvalidIndices() {
                    assertEquals(nothing(), subject.get(3));
                    assertEquals(nothing(), subject.get(-1));
                }

                @Test
                void unsafeGetForValidIndices() {
                    assertEquals("foo", subject.unsafeGet(0));
                    assertEquals("bar", subject.unsafeGet(1));
                    assertEquals("baz", subject.unsafeGet(2));
                }

                @Test
                void unsafeGetThrowsForInvalidIndices() {
                    assertThrows(IndexOutOfBoundsException.class, () -> subject.unsafeGet(3));
                    assertThrows(IndexOutOfBoundsException.class, () -> subject.unsafeGet(-1));
                }

                @Test
                void iteratesCorrectly() {
                    assertThat(subject, contains("foo", "bar", "baz"));
                }

                @Test
                void tailIteratesCorrectly() {
                    assertThat(subject.tail(), contains("bar", "baz"));
                }

                @Test
                void toNonEmptySucceeds() {
                    assertEquals(just(Vector.of("foo", "bar", "baz")),
                            subject.toNonEmpty());
                }

                @Test
                void toNonEmptyOrThrowSucceeds() {
                    assertEquals(Vector.of("foo", "bar", "baz"),
                            subject.toNonEmptyOrThrow());
                }

                @Test
                void toImmutableReturnsItself() {
                    assertSame(subject, subject.toImmutable());
                }

                @Test
                void notAffectedByMutation() {
                    underlying[0] = "qwerty";
                    assertThat(subject, contains("foo", "bar", "baz"));
                }

            }

            @Nested
            @DisplayName("copyFrom size 1 array")
            class CopyFromSingletonArrayTests {
                private ImmutableVector<String> subject;

                @BeforeEach
                void setUp() {
                    subject = Vector.copyFrom(new String[]{"foo"});
                }

                @Test
                void iteratesCorrectly() {
                    assertThat(subject, contains("foo"));
                }

                @Test
                void tailIsEmpty() {
                    assertTrue(subject.tail().isEmpty());
                    assertThat(subject.tail(), emptyIterable());
                }

            }

            @Nested
            @DisplayName("copyFrom empty array")
            class WrapEmptyArrayTests {
                private ImmutableVector<Integer> subject;

                @BeforeEach
                void setUp() {
                    subject = Vector.copyFrom(new Integer[]{});
                }

                @Test
                void isEmpty() {
                    assertTrue(subject.isEmpty());
                }

                @Test
                void sizeIsZero() {
                    assertEquals(0, subject.size());
                }

                @Test
                void iteratesCorrectly() {
                    assertThat(subject, emptyIterable());
                }

                @Test
                void tailIsEmpty() {
                    assertTrue(subject.tail().isEmpty());
                    assertThat(subject.tail(), emptyIterable());
                }
            }
        }

        @Nested
        @DisplayName("copyFrom List")
        class CopyFromListTests {

            @Test
            void throwsOnNullArgument() {
                List<Integer> list = null;
                assertThrows(NullPointerException.class, () -> Vector.copyFrom(list));
            }

            @Test
            void makesCopy() {
                List<Integer> list = asList(1, 2, 3);
                Vector<Integer> subject = Vector.copyFrom(list);
                assertThat(subject, contains(1, 2, 3));
                list.set(0, 4);
                assertThat(subject, contains(1, 2, 3));
            }

            @Test
            void getWillNeverReturnNull() {
                Vector<String> subject = Vector.copyFrom(asList("foo", null, "baz"));
                assertEquals(just("foo"), subject.get(0));
                assertEquals(nothing(), subject.get(1));
                assertEquals(just("baz"), subject.get(2));
            }

            @Test
            void iteratorNextReturnsCorrectElements() {
                Vector<String> subject = Vector.copyFrom(asList("foo", "bar", "baz"));
                Iterator<String> iterator = subject.iterator();
                assertEquals("foo", iterator.next());
                assertEquals("bar", iterator.next());
                assertEquals("baz", iterator.next());
            }

            @SuppressWarnings("ConstantConditions")
            @Test
            void iteratorHasNextCanBeCalledMultipleTimes() {
                Vector<String> subject = Vector.copyFrom(asList("foo", "bar", "baz"));
                Iterator<String> iterator = subject.iterator();
                assertTrue(iterator.hasNext());
                assertTrue(iterator.hasNext());
                assertTrue(iterator.hasNext());
                assertEquals("foo", iterator.next());
            }

            @Test
            void iteratorHasNextReturnsFalseIfNothingRemains() {
                Vector<String> subject = Vector.copyFrom(singletonList("foo"));
                Iterator<String> iterator = subject.iterator();
                iterator.next();
                assertFalse(iterator.hasNext());
            }

            @Test
            void iteratorNextThrowsIfNothingRemains() {
                Vector<String> subject = Vector.copyFrom(singletonList("foo"));
                Iterator<String> iterator = subject.iterator();
                iterator.next();
                assertThrows(NoSuchElementException.class, iterator::next);
            }

            @Test
            void iteratorThrowsIfRemoveIsCalled() {
                Vector<String> subject = Vector.copyFrom(singletonList("foo"));
                Iterator<String> iterator = subject.iterator();
                assertThrows(UnsupportedOperationException.class, iterator::remove);
            }

            @Nested
            @DisplayName("copyFrom size 3 List")
            class CopyFromList3Tests {
                private Vector<String> subject;
                private List<String> underlying;

                @BeforeEach
                void setUp() {
                    underlying = asList("foo", "bar", "baz");
                    subject = Vector.copyFrom(underlying);
                }

                @Test
                void notEmpty() {
                    assertFalse(subject.isEmpty());
                }

                @Test
                void sizeIs3() {
                    assertEquals(3, subject.size());
                }

                @Test
                void getForValidIndices() {
                    assertEquals(just("foo"), subject.get(0));
                    assertEquals(just("bar"), subject.get(1));
                    assertEquals(just("baz"), subject.get(2));
                }

                @Test
                void getForInvalidIndices() {
                    assertEquals(nothing(), subject.get(3));
                    assertEquals(nothing(), subject.get(-1));
                }

                @Test
                void unsafeGetForValidIndices() {
                    assertEquals("foo", subject.unsafeGet(0));
                    assertEquals("bar", subject.unsafeGet(1));
                    assertEquals("baz", subject.unsafeGet(2));
                }

                @Test
                void unsafeGetThrowsForInvalidIndices() {
                    assertThrows(IndexOutOfBoundsException.class, () -> subject.unsafeGet(3));
                    assertThrows(IndexOutOfBoundsException.class, () -> subject.unsafeGet(-1));
                }

                @Test
                void iteratesCorrectly() {
                    assertThat(subject, contains("foo", "bar", "baz"));
                }

                @Test
                void tailIteratesCorrectly() {
                    assertThat(subject.tail(), contains("bar", "baz"));
                }

                @Test
                void toNonEmptySucceeds() {
                    assertEquals(just(Vector.of("foo", "bar", "baz")),
                            subject.toNonEmpty());
                }

                @Test
                void toNonEmptyOrThrowSucceeds() {
                    assertEquals(Vector.of("foo", "bar", "baz"),
                            subject.toNonEmptyOrThrow());
                }

                @Test
                void toImmutableReturnsItself() {
                    assertSame(subject, subject.toImmutable());
                }

                @Test
                void notAffectedByMutation() {
                    underlying.set(0, "qwerty");
                    assertThat(subject, contains("foo", "bar", "baz"));
                }

            }

            @Nested
            @DisplayName("copyFrom size 1 List")
            class CopyFromSize1ListTests {

                private ImmutableVector<String> subject;

                @BeforeEach
                void setUp() {
                    subject = Vector.copyFrom(singletonList("foo"));
                }

                @Test
                void iteratesCorrectly() {
                    assertThat(subject, contains("foo"));
                }

                @Test
                void tailIsEmpty() {
                    assertTrue(subject.tail().isEmpty());
                    assertThat(subject.tail(), emptyIterable());
                }
            }

            @Nested
            @DisplayName("wrap empty List")
            class CopyFromEmptyListTests {
                private ImmutableVector<Integer> subject;

                @BeforeEach
                void setUp() {
                    subject = Vector.copyFrom(emptyList());
                }

                @Test
                void isEmpty() {
                    assertTrue(subject.isEmpty());
                }

                @Test
                void sizeIsZero() {
                    assertEquals(0, subject.size());
                }

                @Test
                void iteratesCorrectly() {
                    assertThat(subject, emptyIterable());
                }

                @Test
                void tailIsEmpty() {
                    assertTrue(subject.tail().isEmpty());
                    assertThat(subject.tail(), emptyIterable());
                }

                @Test
                void toNonEmptyFails() {
                    assertEquals(nothing(), subject.toNonEmpty());
                }


                @Test
                void toNonEmptyOrThrowThrows() {
                    assertThrows(IllegalArgumentException.class, () -> subject.toNonEmptyOrThrow());
                }

            }
        }

        @Nested
        @DisplayName("copyFrom Iterable")
        class CopyFromIterableTests {

            @Test
            void throwsOnNullArgument() {
                Iterable<String> source = null;
                assertThrows(NullPointerException.class, () -> Vector.copyFrom(source));
            }

            @Test
            void iteratesCorrectly() {
                Iterable<Integer> source = cons(1, cons(2, cons(3, emptyList())));
                Vector<Integer> subject = Vector.copyFrom(source);
                assertThat(subject, contains(1, 2, 3));
            }

            @Test
            void getWillNeverReturnNull() {
                Vector<String> subject = Vector.wrap(asList("foo", null, "baz"));
                assertEquals(just("foo"), subject.get(0));
                assertEquals(nothing(), subject.get(1));
                assertEquals(just("baz"), subject.get(2));
            }

            @Test
            void iteratorNextReturnsCorrectElements() {
                Vector<String> subject = Vector.wrap(asList("foo", "bar", "baz"));
                Iterator<String> iterator = subject.iterator();
                assertEquals("foo", iterator.next());
                assertEquals("bar", iterator.next());
                assertEquals("baz", iterator.next());
            }

            @SuppressWarnings("ConstantConditions")
            @Test
            void iteratorHasNextCanBeCalledMultipleTimes() {
                Vector<String> subject = Vector.wrap(asList("foo", "bar", "baz"));
                Iterator<String> iterator = subject.iterator();
                assertTrue(iterator.hasNext());
                assertTrue(iterator.hasNext());
                assertTrue(iterator.hasNext());
                assertEquals("foo", iterator.next());
            }

            @Test
            void iteratorHasNextReturnsFalseIfNothingRemains() {
                Vector<String> subject = Vector.wrap(singletonList("foo"));
                Iterator<String> iterator = subject.iterator();
                iterator.next();
                assertFalse(iterator.hasNext());
            }

            @Test
            void iteratorNextThrowsIfNothingRemains() {
                Vector<String> subject = Vector.wrap(singletonList("foo"));
                Iterator<String> iterator = subject.iterator();
                iterator.next();
                assertThrows(NoSuchElementException.class, iterator::next);
            }

            @Test
            void iteratorThrowsIfRemoveIsCalled() {
                Vector<String> subject = Vector.wrap(singletonList("foo"));
                Iterator<String> iterator = subject.iterator();
                assertThrows(UnsupportedOperationException.class, iterator::remove);
            }

            @Nested
            @DisplayName("copyFrom size 3 Iterable")
            class CopyFromSize3IterableTests {
                private ImmutableVector<String> subject;
                private Iterable<String> underlying;

                @BeforeEach
                void setUp() {
                    underlying = cons("foo", cons("bar", cons("baz", emptyList())));
                    subject = Vector.copyFrom(underlying);
                }

                @Test
                void notEmpty() {
                    assertFalse(subject.isEmpty());
                }

                @Test
                void sizeIs3() {
                    assertEquals(3, subject.size());
                }

                @Test
                void getForValidIndices() {
                    assertEquals(just("foo"), subject.get(0));
                    assertEquals(just("bar"), subject.get(1));
                    assertEquals(just("baz"), subject.get(2));
                }

                @Test
                void getForInvalidIndices() {
                    assertEquals(nothing(), subject.get(3));
                    assertEquals(nothing(), subject.get(-1));
                }

                @Test
                void unsafeGetForValidIndices() {
                    assertEquals("foo", subject.unsafeGet(0));
                    assertEquals("bar", subject.unsafeGet(1));
                    assertEquals("baz", subject.unsafeGet(2));
                }

                @Test
                void unsafeGetThrowsForInvalidIndices() {
                    assertThrows(IndexOutOfBoundsException.class, () -> subject.unsafeGet(3));
                    assertThrows(IndexOutOfBoundsException.class, () -> subject.unsafeGet(-1));
                }

                @Test
                void iteratesCorrectly() {
                    assertThat(subject, contains("foo", "bar", "baz"));
                }

                @Test
                void tailIteratesCorrectly() {
                    assertThat(subject.tail(), contains("bar", "baz"));
                }

                @Test
                void toNonEmptySucceeds() {
                    assertEquals(just(Vector.of("foo", "bar", "baz")),
                            subject.toNonEmpty());
                }

                @Test
                void toNonEmptyOrThrowSucceeds() {
                    assertEquals(Vector.of("foo", "bar", "baz"),
                            subject.toNonEmptyOrThrow());
                }

            }

            @Nested
            @DisplayName("copyFrom size 1 Iterable")
            class CopyFromSize1IterableTests {

                private ImmutableVector<String> subject;

                @BeforeEach
                void setUp() {
                    subject = Vector.copyFrom(cons("foo", emptyList()));
                }

                @Test
                void iteratesCorrectly() {
                    assertThat(subject, contains("foo"));
                }

                @Test
                void tailIsEmpty() {
                    assertTrue(subject.tail().isEmpty());
                    assertThat(subject.tail(), emptyIterable());
                }
            }

            @Nested
            @DisplayName("copyFrom empty Iterable")
            class CopyFromEmptyIterableTests {
                private Vector<Integer> subject;

                @BeforeEach
                void setUp() {
                    subject = Vector.copyFrom(Collections::emptyIterator);
                }

                @Test
                void isEmpty() {
                    assertTrue(subject.isEmpty());
                }

                @Test
                void sizeIsZero() {
                    assertEquals(0, subject.size());
                }

                @Test
                void iteratesCorrectly() {
                    assertThat(subject, emptyIterable());
                }

                @Test
                void tailIsEmpty() {
                    assertTrue(subject.tail().isEmpty());
                    assertThat(subject.tail(), emptyIterable());
                }

                @Test
                void toNonEmptyFails() {
                    assertEquals(nothing(), subject.toNonEmpty());
                }


                @Test
                void toNonEmptyOrThrowThrows() {
                    assertThrows(IllegalArgumentException.class, () -> subject.toNonEmptyOrThrow());
                }

            }
        }

        @Nested
        @DisplayName("with maxCount")
        class CopyFromMaxCountTests {

            private Iterable<Integer> source;

            @BeforeEach
            void setUp() {
                source = cons(1, cons(2, cons(3, emptyList())));
            }

            @Test
            void takesAsMuchAsItCan() {
                assertThat(Vector.copyFrom(1_000_000, source),
                        contains(1, 2, 3));
            }

            @Test
            void onlyTakesWhatWasAskedFor() {
                assertThat(Vector.copyFrom(3, source),
                        contains(1, 2, 3));
                assertThat(Vector.copyFrom(2, source),
                        contains(1, 2));
                assertThat(Vector.copyFrom(1, source),
                        contains(1));
                assertThat(Vector.copyFrom(0, source),
                        emptyIterable());
            }

            @Test
            void willNotEvaluateIterableUnlessNecessary() {
                Iterable<String> poison = () -> {
                    throw new AssertionError("Iterable was evaluated");
                };
                assertThat(Vector.copyFrom(0, poison), emptyIterable());
                assertThat(Vector.copyFrom(1, cons("foo", poison)), contains("foo"));
            }

            @Test
            void safeToUseOnInfiniteIterables() {
                assertThat(Vector.copyFrom(3, repeat("foo")),
                        contains("foo", "foo", "foo"));
            }

        }

        @Nested
        @DisplayName("with maxCount")
        class CopySliceFromMaxTests {

            private Iterable<Integer> finite;
            private Iterable<Integer> infinite;

            @BeforeEach
            void setUp() {
                infinite = cycle(asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
                finite = Take.take(10, infinite);
            }

            @Test
            void takesAsMuchAsItCan() {
                assertThat(Vector.copySliceFrom(5, 15, finite),
                        contains(5, 6, 7, 8, 9));
                assertThat(Vector.copySliceFrom(5, 15, infinite),
                        contains(5, 6, 7, 8, 9, 0, 1, 2, 3, 4));
            }

            @Test
            void onlyTakesWhatWasAskedFor() {
                assertThat(Vector.copySliceFrom(1, 4, infinite),
                        contains(1, 2, 3));
                assertThat(Vector.copySliceFrom(1, 3, infinite),
                        contains(1, 2));
                assertThat(Vector.copySliceFrom(1, 2, infinite),
                        contains(1));
                assertThat(Vector.copySliceFrom(1, 1, infinite),
                        emptyIterable());
            }

            @Test
            void willNotEvaluateIterableUnlessNecessary() {
                Iterable<String> poison = () -> {
                    throw new AssertionError("Iterable was evaluated");
                };
                assertThat(Vector.copySliceFrom(1000, 1000, poison), emptyIterable());
                assertThat(Vector.copySliceFrom(1, 2, cons("foo", cons("bar", poison))),
                        contains("bar"));
            }

            @Test
            void safeToUseOnInfiniteIterables() {
                assertThat(Vector.copySliceFrom(100, 103, infinite),
                        contains(0, 1, 2));
            }

        }
    }

    @Nested
    @DisplayName("fmap")
    class FmapTests {

        private ImmutableVector<Integer> subject;
        private Integer[] underlying;

        @BeforeEach
        void beforeEach() {
            underlying = new Integer[]{1, 2, 3};
            subject = Vector.copyFrom(underlying);
        }

        @Test
        void throwsOnNullFunction() {
            assertThrows(NullPointerException.class, () -> subject.fmap(null));
        }

        @Test
        void fmap() {
            assertThat(subject.fmap(Object::toString), contains("1", "2", "3"));
        }

        @Test
        void functorIdentity() {
            assertEquals(subject, subject.fmap(id()));
        }

        @Test
        void functorComposition() {
            Fn1<Integer, Integer> f = n -> n * 2;
            Fn1<Integer, String> g = Object::toString;
            assertEquals(subject.fmap(f).fmap(g), subject.fmap(f.andThen(g)));
        }

        @Test
        void notAffectedByMutation() {
            underlying[0] = 10;
            assertThat(subject.fmap(n -> n * 2), contains(2, 4, 6));
        }

        @Test
        void stackSafe() {
            ImmutableVector<Integer> mapped = foldLeft((acc, __) -> acc.fmap(n -> n + 1),
                    subject, replicate(10_000, UNIT));
            assertThat(mapped, contains(10_001, 10_002, 10_003));
        }
    }

    @Nested
    @DisplayName("take")
    class TakeTests {

        @Test
        void throwsOnNegativeCount() {
            assertThrows(IllegalArgumentException.class, () -> Vector.copyFrom(singletonList(1)).take(-1));
        }

        @Test
        void takesAsMuchAsItCan() {
            assertThat(Vector.copyFrom(asList(1, 2, 3)).take(1_000_000),
                    contains(1, 2, 3));
        }

        @Test
        void onlyTakesWhatWasAskedFor() {
            assertThat(Vector.copyFrom(asList(1, 2, 3)).take(3),
                    contains(1, 2, 3));
            assertThat(Vector.copyFrom(asList(1, 2, 3)).take(2),
                    contains(1, 2));
            assertThat(Vector.copyFrom(asList(1, 2, 3)).take(1),
                    contains(1));
            assertThat(Vector.copyFrom(asList(1, 2, 3)).take(0),
                    emptyIterable());
        }

        @Test
        void notAffectedByMutation() {
            List<String> originalUnderlying = asList("foo", "bar", "baz");
            ImmutableVector<String> original = Vector.copyFrom(originalUnderlying);
            Vector<String> sliced = original.take(2);
            assertThat(sliced, contains("foo", "bar"));
            originalUnderlying.set(0, "qwerty");
            assertThat(sliced, contains("foo", "bar"));
        }

        @Test
        void returnsOriginalVectorReferenceIfPossible() {
            ImmutableVector<String> original = Vector.copyFrom(asList("foo", "bar", "baz"));
            ImmutableVector<String> slice1 = original.take(100);
            ImmutableVector<String> slice2 = original.take(3);
            assertSame(original, slice1);
            assertSame(original, slice2);
        }

    }

    @Nested
    @DisplayName("drop")
    class DropTests {

        @Test
        void throwsOnNegativeCount() {
            assertThrows(IllegalArgumentException.class, () -> Vector.copyFrom(singletonList(1)).drop(-1));
        }

        @Test
        void countZeroReturnsSameReference() {
            ImmutableVector<Integer> source = Vector.copyFrom(asList(1, 2, 3));
            ImmutableVector<Integer> sliced = source.drop(0);
            assertSame(source, sliced);
        }

        @Test
        void countEqualToSizeReturnsEmptyVector() {
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).drop(3));
        }

        @Test
        void countExceedingSizeReturnsEmptyVector() {
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).drop(4));
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).drop(1_000_000));
        }

        @Test
        void oneElement() {
            ImmutableVector<Integer> source = Vector.copyFrom(asList(1, 2, 3));
            assertThat(source.drop(1), contains(2, 3));
        }

        @Test
        void twoElements() {
            ImmutableVector<Integer> source = Vector.copyFrom(asList(1, 2, 3));
            assertThat(source.drop(2), contains(3));
        }

        @Test
        void notAffectedByMutation() {
            List<String> originalUnderlying = asList("foo", "bar", "baz");
            ImmutableVector<String> original = Vector.copyFrom(originalUnderlying);
            Vector<String> sliced = original.drop(1);
            assertThat(sliced, contains("bar", "baz"));
            originalUnderlying.set(1, "qwerty");
            assertThat(sliced, contains("bar", "baz"));
        }

    }

    @Nested
    @DisplayName("slice")
    class SliceTests {

        @Test
        void throwsOnNegativeStartIndex() {
            assertThrows(IllegalArgumentException.class, () -> Vector.copyFrom(asList(1, 2, 3)).slice(-1, 1));
        }

        @Test
        void throwsOnNegativeEndIndex() {
            assertThrows(IllegalArgumentException.class, () -> Vector.copyFrom(asList(1, 2, 3)).slice(0, -1));
        }

        @Test
        void returnsEmptyVectorIfWidthIsZero() {
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).slice(0, 0));
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).slice(1_000_000, 1_000_000));
        }

        @Test
        void returnsEmptyVectorIfWidthLessThanZero() {
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).slice(10, 9));
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).slice(1_000_000, 0));
        }

        @Test
        void takesAsMuchAsItCan() {
            assertThat(Vector.copyFrom(asList(1, 2, 3)).slice(1, 1_000_000),
                    contains(2, 3));
        }

        @Test
        void onlyTakesWhatWasAskedFor() {
            assertThat(Vector.copyFrom(asList(1, 2, 3)).slice(0, 3),
                    contains(1, 2, 3));
            assertThat(Vector.copyFrom(asList(1, 2, 3)).slice(1, 3),
                    contains(2, 3));
            assertThat(Vector.copyFrom(asList(1, 2, 3)).slice(1, 2),
                    contains(2));
            assertThat(Vector.copyFrom(asList(1, 2, 3)).slice(0, 0),
                    emptyIterable());
        }

        @Test
        void startIndexEqualToSizeReturnsEmptyVector() {
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).slice(3, 6));
        }

        @Test
        void startIndexExceedingSizeReturnsEmptyVector() {
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).slice(4, 3));
            assertEquals(Vector.empty(), Vector.copyFrom(asList(1, 2, 3)).slice(1_000_000, 3));
        }

        @Test
        void notAffectedByMutation() {
            List<String> underlying = asList("foo", "bar", "baz");
            ImmutableVector<String> original = Vector.copyFrom(underlying);
            ImmutableVector<String> slice2 = original.slice(1, 3);
            ImmutableVector<String> slice3 = original.slice(2, 100);
            underlying.set(0, "qwerty");
            underlying.set(2, "quux");
            assertThat(original, contains("foo", "bar", "baz"));
            assertThat(slice2, contains("bar", "baz"));
            assertThat(slice3, contains("baz"));
        }

    }

}
