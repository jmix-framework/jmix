package io.jmix.flowui.exception;

public interface ExceptionDialogProvider {

    boolean supports(Throwable throwable);

    ExceptionDialog getExceptionDialogOpener(Throwable throwable);
}
