/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.navmesh;

import com.jme3.math.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author dharshanar
 */
public class NavigationProvider extends NavMeshPathfinder {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NavigationProvider(NavMesh navMesh) {
        super(navMesh);
    }

    private Optional<List<Vector3f>> computePath(float entityRadius, Vector3f start, Vector3f destination) {
        clearPath();
        setEntityRadius(entityRadius);
        setPosition(start);
        Vector3f dest = new Vector3f(destination);
        warpInside(dest);
        final DebugInfo debugInfo = new DebugInfo();

        if(computePath(dest, debugInfo)){
//            if(getPath().getEnd().getPosition().distance(destination)>10){
//                return Optional.empty();
//            }

            List<Vector3f> path = new ArrayList<>();
            for(Path.Waypoint w: getPath().getWaypoints()){
//                Float terrainHeight = worldMap.getTerrainHeight(new Vector3f(w.getPosition().x, 200, w.getPosition().z));
//                path.add(new Vector3f(w.getPosition().x, terrainHeight!=null?terrainHeight:w.getPosition().y, w.getPosition().z));
                path.add(new Vector3f(w.getPosition().x, w.getPosition().y, w.getPosition().z));
            }
            return Optional.of(path);
        }
        return Optional.empty();

    }



    public Future<Optional<List<Vector3f>>> computePathFuture(float entityRadius, Vector3f start, Vector3f destination) {
        return executor.submit(() -> computePath(entityRadius, start, destination));
    }
}
