package io.jmix.flowui.asynctask;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.function.Supplier;

/**
 * Wraps a delegate {@link Supplier} with logic for setting up an {@link SecurityContext} before invoking the delegate
 * {@link Supplier} and then removing the {@link SecurityContext} after the delegate has completed.
 * <p>
 * If there is a {@link SecurityContext} that already exists, it will be restored after the {@link #get()} method is
 * invoked.
 */
public class DelegatingSecuritySupplier<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    private final SecurityContext securityContext;

    public DelegatingSecuritySupplier(Supplier<T> delegate) {
        this(delegate, copyOfCurrentContext());
    }

    public DelegatingSecuritySupplier(Supplier<T> delegate, SecurityContext securityContext) {
        this.delegate = delegate;
        this.securityContext = securityContext;
    }

    @Override
    public T get() {
        SecurityContext originalSecurityContext = SecurityContextHolder.getContext();
        try {
            SecurityContextHolder.setContext(securityContext);
            return delegate.get();
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
