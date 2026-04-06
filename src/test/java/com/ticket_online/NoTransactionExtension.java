package com.ticket_online;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

public class NoTransactionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        var applicationContext = SpringExtension.getApplicationContext(extensionContext);

        if (!validateDataJpaTestAnnotationExists(extensionContext)) {
            validateTransactionalAnnotationExists(extensionContext);
        }
        cleanDatabase(applicationContext);
    }

    private static boolean validateDataJpaTestAnnotationExists(ExtensionContext extensionContext) {
        return TestContextAnnotationUtils.hasAnnotation(
                extensionContext.getRequiredTestClass(), DataJpaTest.class);
    }

    private static void validateTransactionalAnnotationExists(ExtensionContext extensionContext) {
        if (TestContextAnnotationUtils.hasAnnotation(
                        extensionContext.getRequiredTestClass(), Transactional.class)
                || TestContextAnnotationUtils.hasAnnotation(
                        extensionContext.getRequiredTestClass(),
                        jakarta.transaction.Transactional.class)) {
            Assertions.fail(
                    "The test class contains the @Transactional or @jakarta.transaction.Transactional annotation.");
        }

        if (AnnotatedElementUtils.hasAnnotation(
                        extensionContext.getRequiredTestMethod(), Transactional.class)
                || AnnotatedElementUtils.hasAnnotation(
                        extensionContext.getRequiredTestMethod(),
                        jakarta.transaction.Transactional.class)) {
            Assertions.fail(
                    "The test method contains the @Transactional or @jakarta.transaction.Transactional annotation.");
        }
    }

    private static void cleanDatabase(ApplicationContext applicationContext) {
        try {
            DatabaseCleaner.clear(applicationContext);
        } catch (NoSuchBeanDefinitionException e) {
        }
    }
}
