package com.crs.observer;

/**
 * CONTRACT for the Observer pattern. Real version: Member 3 (observer.impl.RegistrationSubjectImpl).
 *  - Member 2 and Member 3 call publish(...) when something changes.
 *  - Member 4's panels call addListener(...) so they refresh automatically.
 */
public interface RegistrationSubject {
    void addListener(RegistrationListener listener);
    void removeListener(RegistrationListener listener);
    void publish(RegistrationEvent event);
}
