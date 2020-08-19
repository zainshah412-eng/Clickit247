package com.gogrocerdb.tcc.WebSocket;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIOManager {

    private static SocketIOManager sSoleInstance;
    private Socket socket;
    public static SocketIOManager getInstance(){
        if (sSoleInstance == null){
            sSoleInstance = new SocketIOManager();
        }

        return sSoleInstance;
    }
    private SocketIOManager(){
        try {
            socket = IO.socket("https://hixol.website/websocket/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    //socket connection
    public void establishConnection(){
        socket.connect();
    }

    public void closeConnection(){
        socket.disconnect();
    }

    public void listenToConnectionChanges(Emitter.Listener onConnectHandler){
        socket.on(Socket.EVENT_CONNECT, onConnectHandler);
    }
    public void listenToDisConnectionChanges(Emitter.Listener onDisconnectHandler){
        socket.on(Socket.EVENT_DISCONNECT, onDisconnectHandler);
    }

    //Location share messages
    //Let server know thar a user started sharing location
    public void userStartedLocationSharing(String nickname){
        socket.emit("connectTrackedUser",nickname);
    }

    //Let server know thar a user stopped sharing location
    public void userStoppedLocationSharing(){
        socket.emit("disconnectTrackedUser");
    }

    //Send to server the coordinates
    public void sendCoordinates(String latitude,String longitude){
        socket.emit("trackedUserCoordinates",latitude,longitude);
    }

    //Location tracking messages
    public void userStartedTracking(String trackedUserSocketId){
        socket.emit("connectTrackedUserTracker",trackedUserSocketId);
        socket.on("trackedUserCoordinatesUpdate", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });
        socket.on("trackedUserHasStoppedUpdate", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });
    }


    //Let server know that a user stopped tracking a sharing location user
    public void userStoppedTracking(String trackedUserSocketId){
        socket.emit("disconnectTrackedUserTracker",trackedUserSocketId);
    }

    //Tracked users list monitoring
    public void checkForUpdatedTrackedUsersList(){
        socket.emit("requestUpdatedTrackedUsersList");
    }

    public void listenToTrackedUsersListUpdate(){
        socket.on("trackedUsersListUpdate", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });
    }
}
