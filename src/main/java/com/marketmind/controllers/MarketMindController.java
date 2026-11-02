package com.marketmind.controllers;

import com.marketmind.models.AnalysisResult;
import com.marketmind.models.MarketRequest;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/** GUI-to-JADE bridge. Queue is drained by the UiGatewayAgent. */
public class MarketMindController {
    public interface Listener {
        void onStatus(String requestId, String message);
        void onResult(AnalysisResult result);
    }
    private final ConcurrentLinkedQueue<MarketRequest> queue = new ConcurrentLinkedQueue<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Listener listener) { listeners.add(listener); }
    public void submit(MarketRequest request) {
        if (request == null) throw new IllegalArgumentException("Request is required");
        queue.add(request);
        status(request.getId(), "Request queued");
    }
    public MarketRequest nextRequest() { return queue.poll(); }
    public void status(String id, String message) {
        listeners.forEach(listener -> listener.onStatus(id, message));
    }
    public void result(AnalysisResult result) {
        listeners.forEach(listener -> listener.onResult(result));
    }
}
