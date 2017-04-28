package com.jme3.lostVictories.network.messages;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by dharshanar on 26/04/17.
 */
public class CustomCodecTest {

    private CustomCodec codec;

    @Before
    public void setUp(){
        codec = new CustomCodec();
    }

    @Test
    public void testEncode(){
        String uncpressed = "{" +
                "\"class\":\".CharacterStatusResponse\",\"unit\":{" +
                "\"id\":\"36a0ae50-59d3-4588-92d2-169c4d54f0f5\"," +
                "\"location\":{\"x\":231.82343,\"y\":96.30658,\"z\":6.0718513}," +
                "\"country\":\"GERMAN\"," +
                "\"weapon\":\"RIFLE\"," +
                "\"rank\":\"CADET_CORPORAL\"," +
                "\"commandingOfficer\":\"7589e998-df20-4d3c-8d3d-16034a598241\"," +
                "\"unitsUnderCommand\":[\"eb4fce21-d3df-45b9-ae99-37a21fb340b2\",\"a279c654-0ea3-4162-bf18-d1dd229923e7\",\"0a8749c1-8286-429a-a5f8-e479fb025fc2\",\"8841cac6-4cff-4dd5-80eb-8fd72e2d4eb2\"]," +
                "\"passengers\":[]," +
                "\"checkoutClient\":\"2fbe421f-f701-49c9-a0d4-abb0fa904204\"," +
                "\"checkoutTime\":1493146813916," +
                "\"type\":\"SOLDIER\"," +
                "\"orientation\":{\"x\":0.22427,\"y\":0.0,\"z\":-0.97452706}," +
                "\"actions\":[{\"class\":\"com.jme3.lostVictories.network.messages.actions.Idle\",\"type\":\"idle\"}]," +
                "\"objectives\":{" +
                    "\"33d34b79-ae11-43b2-ac38-d6e9faa70a21\":\"{\"class\":\"com.jme3.lostVictories.objectives.SurvivalObjective\"}\"," +
                    "\"3e1921d4-c2fd-4d69-abb6-5da53939ffa4\":\"{\"class\":\"com.jme3.lostVictories.objectives.TransportSquad\",\"isComplete\":false,\"destination\":{\"x\":333.28955,\"y\":96.04469,\"z\":115.02185},\"issuedOrders\":{}}\"," +
                    "\"4a349d65-c479-4ea9-bd25-5ad1fec24ad9\":\"{\"class\":\"com.jme3.lostVictories.objectives.CollectUnusedEquipment\"}\"," +
                    "\"5c7914e1-aa19-47d7-9b9b-9e072733ad00\":\"{\"class\":\"com.jme3.lostVictories.objectives.RemanAbandonedVehicles\"}\"}," +
                "\"dead\":false," +
                "\"engineDamaged\":false," +
                "\"version\":18," +
                "\"squadType\":\"MG42_TEAM\"," +
                "\"busy\":true," +
                "\"attacking\":false}}";

        byte[] out = codec.encode(uncpressed).getBytes();
        System.out.println("encoded length:"+out.length);
        System.out.println("encoded string:"+codec.encode(uncpressed));
        assertTrue(out.length<1024);
    }

}