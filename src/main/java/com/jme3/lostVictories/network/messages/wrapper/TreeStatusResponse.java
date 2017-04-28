package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.TreeGroupMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dharshanar on 1/04/17.
 */
public class TreeStatusResponse extends LostVictoryMessage {
    private Set<TreeGroupMessage> allTrees;

    public TreeStatusResponse(Collection<TreeGroupMessage> allTrees) {
        this.allTrees = new HashSet<>(allTrees);
    }
}
