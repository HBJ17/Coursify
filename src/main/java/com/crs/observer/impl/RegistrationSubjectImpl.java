package com.crs.observer.impl;

import com.crs.observer.RegistrationEvent;
import com.crs.observer.RegistrationListener;
import com.crs.observer.RegistrationSubject;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** STUB. Member 3 replaces the method bodies (keep both constructors). */
public class RegistrationSubjectImpl implements RegistrationSubject {
    private final boolean useEdt;
    private final List<RegistrationListener> listeners = new CopyOnWriteArrayList<>();

    /** Notifies listeners on the Swing Event Dispatch Thread (what the real app uses). */
    public RegistrationSubjectImpl() { this(true); }

    /** useEdt = false notifies on the calling thread (handy in unit tests). */
    public RegistrationSubjectImpl(boolean useEdt) { this.useEdt = useEdt; }

    @Override public void addListener(RegistrationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    @Override public void removeListener(RegistrationListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }
    
    @Override public void publish(RegistrationEvent event) {
        Runnable notifyTask = () -> {
            for (RegistrationListener listener : listeners) {
                try {
                    listener.onRegistrationEvent(event);
                } catch (Exception e) {
                    System.err.println("Listener exception: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        };

        if (useEdt) {
            SwingUtilities.invokeLater(notifyTask);
        } else {
            notifyTask.run();
        }
    }
}
