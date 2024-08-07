package io.jmix.flowui.asynctask;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Wraps a delegate {@link Runnable} with logic for setting up an {@link SecurityContext} before invoking the delegate
 * {@link Runnable} and then removing the {@link SecurityContext} after the delegate has completed.
 * <p>
 * If there is a {@link SecurityContext} that already exists, it will be restored after the {@link #run()} method is
 * invoked.
 */
public class DelegatingSecurityRunnable implements Runnable {

    private final Runnable delegate;
    private final SecurityContext securityContext;

    public DelegatingSecurityRunnable(Runnable delegate) {
        this(delegate, SecurityContextHolder.getContext());
    }

    public DelegatingSecurityRunnable(Runnable delegate, SecurityContext securityContext) {
        this.delegate = delegate;
        this.securityContext = securityContext;
    }

    @Override
    public void run() {
        SecurityContext originalSecurityContext = SecurityContextHolder.getContext();
        try {
            SecurityContextHolder.setContext(securityContext);
            delegate.run();
        } finally {
            SecurityContextHolder.setContext(originalSecurityContext);
        }
    }
}
