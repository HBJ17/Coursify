package com.crs.observer.impl;

import com.crs.observer.RegistrationEvent;
import com.crs.observer.RegistrationListener;
import com.crs.observer.RegistrationSubject;

/** STUB. Member 3 replaces the method bodies (keep both constructors). */
public class RegistrationSubjectImpl implements RegistrationSubject {
    private final boolean useEdt;

    /** Notifies listeners on the Swing Event Dispatch Thread (what the real app uses). */
    public RegistrationSubjectImpl() { this(true); }

    /** useEdt = false notifies on the calling thread (handy in unit tests). */
    public RegistrationSubjectImpl(boolean useEdt) { this.useEdt = useEdt; }

    @Override public void addListener(RegistrationListener listener) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public void removeListener(RegistrationListener listener) { throw new UnsupportedOperationException("TODO Member 3"); }
    @Override public void publish(RegistrationEvent event) { throw new UnsupportedOperationException("TODO Member 3"); }
}
