package hu.dlaszlo.flickr.service;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MultiThreadRunner<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiThreadRunner.class);

    private Thread[] workers;
    private BlockingQueue<T> queue;
    private Consumer<T> consumer;
    private volatile boolean shutdown = false;
    private volatile boolean error = false;
    private int numberOfThreads;

    public MultiThreadRunner(String threadName, int numberOfThreads, Consumer<T> consumer) {
        Validate.isTrue(numberOfThreads > 0);
        this.numberOfThreads = numberOfThreads;
        this.consumer = consumer;
        queue = new LinkedBlockingQueue<>(numberOfThreads + 1);
        workers = new Thread[numberOfThreads];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(new Worker(), threadName + "-" + (i + 1));
            workers[i].setUncaughtExceptionHandler(new ExceptionHandler());
            workers[i].start();
        }
        LOGGER.info("Number of threads: {}", numberOfThreads);
    }

    public void submit(T entry) {
        if (numberOfThreads > 1) {
            try {
                boolean submit = false;
                while (!submit) {
                    checkShutdown();
                    checkError();
                    submit = queue.offer(entry, 1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            checkShutdown();
            consumer.accept(entry);
        }
    }

    public void shutdown() {
        checkShutdown();
        shutdown = true;
        if (numberOfThreads > 1) {
            for (Thread thread : workers) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            checkError();
        }
    }

    private void checkError() {
        if (error) {
            throw new IllegalStateException("Error occured in another thread.");
        }
    }

    private void checkShutdown() {
        if (shutdown) {
            throw new IllegalStateException("Shutdown is in progress.");
        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            try {
                while (!(shutdown && queue.isEmpty())) {
                    if (error) {
                        throw new IllegalStateException("Error occured in another thread.");
                    }
                    T item = queue.poll(1000L, TimeUnit.MILLISECONDS);
                    if (item != null) {
                        consumer.accept(item);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class ExceptionHandler implements UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOGGER.error("Error occured in {} thread", t.getName(), e);
            error = true;
        }
    }

}

