package com.timetablegenerator.delta;

public interface Diffable<T> extends Comparable<T>{

    /**
     * Returns a structural mapping from the implementing instance to the argument instance.
     *
     * @param d The instance being mapped to.
     * @return A structural mapping from this instance to the argument instance.
     */
    StructureDelta findDifferences(T d);

    /**
     * Returns the ID used to identify this particular resource instance in a diff set.
     *
     * @return The delta ID.
     */
    String getDeltaId();
}