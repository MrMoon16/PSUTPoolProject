package com.psut.pool.Models;

import java.util.HashMap;
import java.util.Map;

public class Route {

    //Global Variables and Objects:
    private String pickUpLocation, dropOffLocation, distance, status;

    public Route(String pickUpLocation, String dropOffLocation, String distance, String status) {
        this.pickUpLocation = pickUpLocation;
        this.dropOffLocation = dropOffLocation;
        this.distance = distance;
        this.status = status;
    }

    public Map<String, Object> toRouteMap() {
        HashMap<String, Object> routes = new HashMap<>();
        routes.put("Pick Up Location", pickUpLocation);
        routes.put("Drop Off Location", dropOffLocation);
        routes.put("Distance", distance);
        routes.put("Status", status);
        return routes;
    }
}