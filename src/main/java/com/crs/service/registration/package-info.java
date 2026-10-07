/**
 * OWNER: Member 2 (Registration Engine)
 *
 * Write here: RegistrationServiceImpl. Runs the list of ValidationRules, then calls
 * RegistrationDAO.registerAtomic and publishes an event. cancel() must call WaitlistService.promoteNext().
 * Test with the InMemory DAOs, NoOpWaitlistService, and SimpleRegistrationSubject from com.crs.fake.
 */
package com.crs.service.registration;
