package com.lessmarkup.interfaces.structure;

public class Tuple<T1, T2> {
    
    private final T1 t1;
    private final T2 t2;
    
    public Tuple(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }
    
    public T1 getValue1() {
        return this.t1;
    }
    
    public T2 getValue2() {
        return this.t2;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        Tuple<T1, T2> other2 = (Tuple<T1, T2>) other;
        return other2.t1 == this.t1 && other2.t2 == this.t2;
    }
    
    @Override
    public int hashCode() {
        return this.t1.hashCode() + this.t2.hashCode();
    }
}
