package com.crs.fake;

import com.crs.observer.RegistrationEvent;
import com.crs.observer.RegistrationListener;
import com.crs.observer.RegistrationSubject;
import java.util.ArrayList;
import java.util.List;

/** Bare-minimum Observer so others can test. Member 3 writes the real one. */
public class SimpleRegistrationSubject implements RegistrationSubject {
    private final List<RegistrationListener> listeners = new ArrayList<>();

    @Override public void addListener(RegistrationListener l) { listeners.add(l); }
    @Override public void removeListener(RegistrationListener l) { listeners.remove(l); }
    @Override public void publish(RegistrationEvent e) { for (RegistrationListener l : listeners) l.onRegistrationChanged(e); }
}
