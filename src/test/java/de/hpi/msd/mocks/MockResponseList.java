package de.hpi.msd.mocks;

import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class MockResponseList implements ResponseList<Status> {
    private final List<Status> statuses;

    public static ResponseList<Status> asResponse(List<Status> statuses) {
        return new MockResponseList(statuses);
    }

    private MockResponseList(List<Status> statuses) {
        this.statuses = statuses;
    }

    @Override
    public RateLimitStatus getRateLimitStatus() {
        return null;
    }

    @Override
    public int size() {
        return statuses.size();
    }

    @Override
    public boolean isEmpty() {
        return statuses.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return statuses.contains(o);
    }

    @Override
    public Iterator<Status> iterator() {
        return statuses.iterator();
    }

    @Override
    public Object[] toArray() {
        return statuses.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return statuses.toArray(a);
    }

    @Override
    public boolean add(Status status) {
        return statuses.add(status);
    }

    @Override
    public boolean remove(Object o) {
        return statuses.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return statuses.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Status> c) {
        return statuses.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Status> c) {
        return statuses.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return statuses.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return statuses.retainAll(c);
    }

    @Override
    public void clear() {
        statuses.clear();
    }

    @Override
    public Status get(int index) {
        return statuses.get(index);
    }

    @Override
    public Status set(int index, Status element) {
        return statuses.set(index, element);
    }

    @Override
    public void add(int index, Status element) {
        statuses.add(index, element);
    }

    @Override
    public Status remove(int index) {
        return statuses.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return statuses.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return statuses.indexOf(o);
    }

    @Override
    public ListIterator<Status> listIterator() {
        return statuses.listIterator();
    }

    @Override
    public ListIterator<Status> listIterator(int index) {
        return null;
    }

    @Override
    public List<Status> subList(int fromIndex, int toIndex) {
        return statuses.subList(fromIndex, toIndex);
    }

    @Override
    public int getAccessLevel() {
        return 0;
    }

    @Override
    public void replaceAll(UnaryOperator<Status> operator) {
        statuses.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super Status> c) {
        statuses.sort(c);
    }

    @Override
    public Spliterator<Status> spliterator() {
        return statuses.spliterator();
    }

    @Override
    public void forEach(Consumer<? super Status> action) {
        statuses.forEach(action);
    }
}
