package com.obviousengine.rxbus.dispatcher;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import rx.Scheduler;
import rx.functions.Action0;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

final class DefaultFlusher implements Flusher {

    private final Scheduler scheduler;
    private final long flushDelayNanos;
    private final Deque<Flushable> flushables = new ConcurrentLinkedDeque<>();
    private final FlushAction flushAction = new FlushAction();
    private final SerialSubscription subscription = new SerialSubscription();

    public static Flusher create(Scheduler scheduler, long flushDelay, TimeUnit flushDelayUnit) {
        return new DefaultFlusher(scheduler, flushDelay, flushDelayUnit);
    }

    private DefaultFlusher(Scheduler scheduler, long flushDelay, TimeUnit flushDelayUnit) {
        this.scheduler = scheduler;
        this.flushDelayNanos = flushDelayUnit.toNanos(flushDelay);
        resetSubscription();
    }

    @Override
    public void schedule(Flushable flushable) {
        flushables.add(flushable);
        scheduleFlush();
    }

    private void scheduleFlush() {
        if (subscription.get() == Subscriptions.unsubscribed()) {
            subscription.set(scheduler.createWorker().schedule(
                    flushAction, flushDelayNanos, TimeUnit.NANOSECONDS));
        }
    }

    private void resetSubscription() {
        subscription.set(Subscriptions.unsubscribed());
    }

    private class FlushAction implements Action0 {

        @Override
        public void call() {
            while (!flushables.isEmpty()) {
                Flushable flushable = flushables.remove();
                flushable.flush();
                resetSubscription();
            }
        }
    }
}
