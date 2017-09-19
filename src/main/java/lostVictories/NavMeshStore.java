package lostVictories;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import com.jme3.ai.navmesh.CustomNavMeshPathfinder;
import com.jme3.ai.navmesh.DebugInfo;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavMeshStore {
	private static NavMeshStore instance;
	private final NavMeshPathfinder pathFinder;
	private static Logger log = LoggerFactory.getLogger(NavMeshStore.class);

	public static synchronized NavMeshStore intstace(){
		if(instance == null){
			instance = new NavMeshStore();
		}
		return instance;
	}
	
	private NavMeshStore(){
    	AssetManager assetManager = new DesktopAssetManager();
    	assetManager.registerLoader(BinaryImporter.class, "j3o");
    	assetManager.registerLocator("/", ClasspathLocator.class);
    	log.debug("attempting to load NavMesh");
    	Geometry loadedNode = (Geometry) assetManager.loadModel("NavMesh.j3o");
    	log.debug("NavMEsh loaded");
        NavMesh nm = new NavMesh(loadedNode.getMesh());
        pathFinder = new CustomNavMeshPathfinder(nm);
		
	}

	public List<Vector> findPath(Vector location, Vector destination) {
		synchronized (instance) {
			try{
				pathFinder.clearPath();
				pathFinder.setEntityRadius(.8f);
				pathFinder.setPosition(location.toVector());
				final DebugInfo debugInfo = new DebugInfo();

				Vector3f dest = destination.toVector();
				pathFinder.warpInside(dest);
				if(this.pathFinder.computePath(dest, debugInfo)){
					return pathFinder.getPath().getWaypoints().stream().map(w->new Vector(w.getPosition())).collect(Collectors.toList());
				}else{
					List<Vector> ret = new ArrayList<Vector>();
					ret.add(destination);
					return ret;
				}
			}catch(Throwable e){
				e.printStackTrace();
				return null;

			}
		}
	}

	public Vector3f warp(Vector point){
		synchronized (instance){
			return pathFinder.warp(point.toVector());
		}
	}

}
