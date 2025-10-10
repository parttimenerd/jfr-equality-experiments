package me.bechberger.equality;

import jdk.jfr.consumer.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<RecordedEvent> events = new ArrayList<>();
        AtomicBoolean running = new AtomicBoolean(true);
        // We obtain one hundred execution samples that have all the same stack trace
        final long currentThreadId = Thread.currentThread().threadId();
        try (RecordingStream rs = new RecordingStream()) {
            rs.enable("jdk.ExecutionSample").with("period", "1ms");
            rs.onEvent("jdk.ExecutionSample", event -> {
                if (event.getThread("sampledThread").getJavaThreadId() != currentThreadId) {
                    return;
                }
                events.add(event);
                if (events.size() >= 100) {
                    running.set(false);
                }
            });
            rs.startAsync();
            int i = 0;
            while (running.get()) {
                for (int j = 0; j < 100000; j++) {
                    i += j;
                }
            }
            rs.stop();
            System.out.println("Total events collected: " + events.size());
            // Now we check the equality and hashCode behavior
            check(events);
        }
    }

    static void check(List<RecordedEvent> events) {
        // First we check the methods
        List<RecordedMethod> uniqueMethods = getMethodsOnTop(events);
        Set<RecordedMethod> uniqueSet = new HashSet<>(uniqueMethods);
        System.out.println("Total methods on top: " + uniqueMethods.size() + ", unique: " + uniqueSet.size());

        // Now the frames
        List<RecordedFrame> framesOnTop = getFramesOnTop(events);
        Set<RecordedFrame> uniqueFrames = new HashSet<>(framesOnTop);
        System.out.println("Total frames on top: " + framesOnTop.size() + ", unique: " + uniqueFrames.size());

        RecordedFrame firstFrame = framesOnTop.get(0);
        for (RecordedFrame frame : framesOnTop.stream().limit(10).toList()) {
            System.out.println("Method: " + frame.getMethod().getType().getName() + "." + frame.getMethod().getName() +
                               " at " + frame.getLineNumber() + ", hashCode: " + frame.hashCode() + ", equal to first frame " + frame.equals(firstFrame));
        }

        // Now with the custom built wrapper
        Set<Wrapper> wrappedFrames = framesOnTop.stream().map(Wrapper::new).collect(Collectors.toSet());
        System.out.println("Total wrapped frames unique: " + wrappedFrames.size());
    }

    public static List<RecordedMethod> getMethodsOnTop(List<RecordedEvent> event) {
        return getFramesOnTop(event).stream()
                .map(RecordedFrame::getMethod)
                .toList();
    }

    public static List<RecordedFrame> getFramesOnTop(List<RecordedEvent> event) {
        return event.stream()
                .map(RecordedEvent::getStackTrace)
                .filter(stackTrace -> stackTrace != null && !stackTrace.getFrames().isEmpty())
                .map(stackTrace -> stackTrace.getFrames().get(0))
                .toList();
    }

}