package com.crs.service;

import com.crs.model.CourseDemand;
import java.util.List;

/** CONTRACT. Real version: Member 3 (service.analytics.AnalyticsServiceImpl, uses a max-heap). */
public interface AnalyticsService {
    /** The k most in-demand courses, highest first. */
    List<CourseDemand> topInDemand(int k);
}
