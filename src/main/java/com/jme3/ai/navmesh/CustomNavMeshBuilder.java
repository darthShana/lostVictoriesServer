/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.navmesh;


import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;


/**
 *
 * @author dharshanar
 */
public class CustomNavMeshBuilder {

    public static NavMesh getMesh(AssetManager assetManager) {

        Geometry navGeom = (Geometry) assetManager.loadModel("NavMesh.j3o");
        System.out.println("NavMEsh loaded");
        NavMesh nm = new NavMesh(navGeom.getMesh());
        return nm;

    }


    
}
