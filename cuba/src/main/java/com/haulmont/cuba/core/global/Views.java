package com.haulmont.cuba.core.global;

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;

public class Views extends FetchPlans {

    @Override
    public View copy(FetchPlan fetchPlan) {
        return View.copy((View) fetchPlan);
    }
}
