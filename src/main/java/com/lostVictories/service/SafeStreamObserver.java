package com.lostVictories.service;

import com.google.protobuf.GeneratedMessageV3;
import com.lostVictories.api.LostVictoryMessage;
import com.lostVictories.api.LostVictoryStatusMessage;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

/**
 * Created by dharshanar on 5/06/17.
 */
public class SafeStreamObserver<T extends GeneratedMessageV3> {
    private StreamObserver<T> responseObserver;
    private UUID clientID;
    public int backOff;

    public SafeStreamObserver(StreamObserver<T> responseObserver) {
        this.responseObserver = responseObserver;
    }

    public void onNext(T lostVictoryMessage) {
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
