package com.crs.observer.impl;

import com.crs.observer.RegistrationEvent;
import com.crs.observer.RegistrationListener;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationSubjectImplTest {

    @Test
    void testPublishNotifiesListeners() {
        RegistrationSubjectImpl subject = new RegistrationSubjectImpl(false);
        AtomicInteger count = new AtomicInteger(0);

        RegistrationListener listener = event -> count.incrementAndGet();
        subject.addListener(listener);

        subject.publish(new RegistrationEvent(RegistrationEvent.Type.REGISTERED, "S001", "CS101"));

        assertEquals(1, count.get());

        subject.removeListener(listener);
        subject.publish(new RegistrationEvent(RegistrationEvent.Type.REGISTERED, "S001", "CS102"));
        
        assertEquals(1, count.get());
    }

    @Test
    void testExceptionInListenerDoesNotStopOthers() {
        RegistrationSubjectImpl subject = new RegistrationSubjectImpl(false);
        AtomicInteger count = new AtomicInteger(0);

        subject.addListener(event -> { throw new RuntimeException("Test exception"); });
        subject.addListener(event -> count.incrementAndGet());

        subject.publish(new RegistrationEvent(RegistrationEvent.Type.REGISTERED, "S001", "CS101"));

        assertEquals(1, count.get());
    }
}
