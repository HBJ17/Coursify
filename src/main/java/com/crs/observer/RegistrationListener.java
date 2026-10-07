package com.crs.observer;

/** Anything that wants to refresh when registrations change (e.g. the "My Courses" table). */
@FunctionalInterface
public interface RegistrationListener {
    void onRegistrationChanged(RegistrationEvent event);
}
