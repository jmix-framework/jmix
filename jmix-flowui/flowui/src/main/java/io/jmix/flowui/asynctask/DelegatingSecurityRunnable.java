package io.jmix.flowui.asynctask;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

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
        this(delegate, copyOfCurrentContext());
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
            if (SecurityContextHolder.createEmptyContext().equals(originalSecurityContext)) {
                SecurityContextHolder.clearContext();
            } else {
                SecurityContextHolder.setContext(originalSecurityContext);
            }
        }
    }

    /**
     * The current context instance may be shared with the HTTP session and other threads, so the delegate gets a
     * copy holding the same {@link org.springframework.security.core.Authentication}.
     */
    private static SecurityContext copyOfCurrentContext() {
        return new SecurityContextImpl(SecurityContextHolder.getContext().getAuthentication());
    }
}
