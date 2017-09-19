package lostVictories;

import bathe.BatheBooter;
import com.lostVictories.server.LostVictoriesServerGRPC;

import java.io.File;
import java.io.IOException;

public class LostVictoriesServerRunner {

    public static void main(String[] args) throws IOException {
        if(!new File("src/test/resources").exists()){
            throw new RuntimeException("please ensure this test is run from in the home directory of the project...");
        }

        new BatheBooter().runWithLoader(LostVictoriesServerGRPC.class.getClassLoader(), null, LostVictoriesServerGRPC.class.getName(),
                new String[]{"-Psrc/test/resources/server.properties"});

    }
}
