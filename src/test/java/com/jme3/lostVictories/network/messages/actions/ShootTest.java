package com.jme3.lostVictories.network.messages.actions;

import com.jme3.lostVictories.network.messages.Vector;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by dharshanar on 3/06/17.
 */
public class ShootTest {

    @Test
    public void toMessage() throws Exception {
        Shoot shoot = (Shoot) Action.shoot(12345l, new Vector[]{new Vector(1, 2, 3)});
        com.lostVictories.api.Action action = shoot.toMessage();

        assertEquals(com.lostVictories.api.Action.ActionType.SHOOT, action.getActionType());
        assertEquals(12345l, action.getShootTime());
        ArrayList<Object> expected = new ArrayList<>();
        expected.add(com.lostVictories.api.Vector.newBuilder().setX(1).setY(2).setZ(3).build());
        assertEquals(expected, action.getTargetsList());
    }

}