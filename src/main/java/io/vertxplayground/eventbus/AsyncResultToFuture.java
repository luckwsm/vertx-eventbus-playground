package io.vertxplayground.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class AsyncResultToFuture extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new AsyncResultToFuture());
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.eventBus().consumer("hello", msg -> {
            msg.reply(msg.body());
        });
        CompositeFuture.join(fut("hello"), fut2("hello")).setHandler((ar) -> {
            if(ar.succeeded()) {
                System.out.println(ar.result().list());
            }
        });
    }

    public Future fut(String address) {
        Future<JsonObject> future = Future.future();
        vertx.eventBus().<JsonObject>send(address, new JsonObject().put("Key", 456), (ar) -> {
            JsonObject json = ar.result().body();
            vertx.executeBlocking(future1 -> {
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                future1.complete();
            }, asyncResult -> {
                System.out.println(1);
                future.complete(json);
            });
        });
        return future;
    }

    public Future fut2(String address) {
        Future<Message<Object>> future = Future.future();
        System.out.println(2);
        vertx.eventBus().send(address, "hi", future::handle);
        return future;
    }
}
