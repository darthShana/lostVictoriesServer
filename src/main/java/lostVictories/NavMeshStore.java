package lostVictories;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


import com.jme3.ai.navmesh.DebugInfo;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.asset.AssetManager;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.system.JmeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavMeshStore {
	private static NavMeshStore instance;
	private final NavMeshPathfinder humanPathFinder;
	private final NavMeshPathfinder vehiclePathFinder;
	private static Logger log = LoggerFactory.getLogger(NavMeshStore.class);

	private Predicate<CharacterMessage> vehiclePredicate = character -> {
        CharacterType characterType = character.getCharacterType();
        return characterType==CharacterType.ANTI_TANK_GUN ||
                characterType==CharacterType.HALF_TRACK || characterType == CharacterType.ARMORED_CAR ||
                characterType==CharacterType.PANZER4 || characterType == CharacterType.M4SHERMAN;
    };

	public static synchronized NavMeshStore intstace(){
		if(instance == null){
			instance = new NavMeshStore();
		}
		return instance;
	}
	
	private NavMeshStore(){
    	log.debug("attempting to load NavMesh");
        URL assetCfgUrl = JmeSystem.getPlatformAssetConfigURL();
    	AssetManager assetManager  = JmeSystem.newAssetManager(assetCfgUrl);
        Geometry humanNavGeom = (Geometry) assetManager.loadModel("HumanNavMesh.j3o");
        NavMesh hnm = new NavMesh(humanNavGeom.getMesh());
        humanPathFinder = new NavMeshPathfinder(hnm);

        Geometry vehicleNavGeom = (Geometry) assetManager.loadModel("VehicleNavMesh.j3o");
        NavMesh vnm = new NavMesh(vehicleNavGeom.getMesh());
        vehiclePathFinder = new NavMeshPathfinder(vnm);

    	log.debug("NavMEsh loaded");

	}

	public List<Vector> findPath(CharacterMessage character, Vector location, Vector destination){
	    if(vehiclePredicate.test(character)){
	        return findPath(vehiclePathFinder, location, destination);
        }else{
	        return findPath(humanPathFinder, location, destination);
        }
    }

	private List<Vector> findPath(NavMeshPathfinder pathFinder, Vector location, Vector destination) {
		synchronized (instance) {
		    try {
                pathFinder.clearPath();
                pathFinder.setEntityRadius(.8f);
                pathFinder.setPosition(location.toVector());
                final DebugInfo debugInfo = new DebugInfo();

                Vector3f dest = destination.toVector();
                pathFinder.warpInside(dest);
                if (pathFinder.computePath(dest, debugInfo)) {
                    return pathFinder.getPath().getWaypoints().stream().map(w -> new Vector(w.getPosition())).collect(Collectors.toList());
                }
                return null;
            }catch(Exception e){
		        e.printStackTrace();
		        throw e;
            }
		}
	}

	public Vector3f warp(CharacterMessage character, Vector point){
		synchronized (instance){
            if(vehiclePredicate.test(character)) {
                return vehiclePathFinder.warp(point.toVector());
            }else{
                return humanPathFinder.warp(point.toVector());
            }
		}
	}

}
