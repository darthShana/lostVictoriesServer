package com.lostVictories.service;

import com.lostVictories.api.LostVictoryMessage;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

/**
 * Created by dharshanar on 5/06/17.
 */
public class SafeStreamObserver {
    private StreamObserver<LostVictoryMessage> responseObserver;
    private UUID clientID;

    public SafeStreamObserver(StreamObserver<LostVictoryMessage> responseObserver) {
        this.responseObserver = responseObserver;
    }

    public void onNext(LostVictoryMessage lostVictoryMessage) {
        synchronized (responseObserver){
            responseObserver.onNext(lostVictoryMessage);
        }
    }

    public void setClientID(UUID clientID) {
        this.clientID = clientID;
    }

    @Override
    public boolean equals(Object obj) {
        return clientID.equals(obj);
    }

    @Override
    public int hashCode() {
        return clientID.hashCode();
    }

    public UUID getClientID() {
        return clientID;
    }
}
