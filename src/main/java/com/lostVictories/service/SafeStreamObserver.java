package com.lostVictories.service;

import com.lostVictories.api.LostVictoryMessage;
import io.grpc.stub.StreamObserver;

/**
 * Created by dharshanar on 5/06/17.
 */
public class SafeStreamObserver {
    private StreamObserver<LostVictoryMessage> responseObserver;

    public SafeStreamObserver(StreamObserver<LostVictoryMessage> responseObserver) {
        this.responseObserver = responseObserver;
    }

    public void onNext(LostVictoryMessage lostVictoryMessage) {
        synchronized (responseObserver){
            responseObserver.onNext(lostVictoryMessage);
        }
    }
}
