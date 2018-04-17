package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.stream.Collectors;

class GameSectorConverter extends StdConverter<GameSector, GameSector> {

    @Override
    public GameSector convert(GameSector value) {
        value.sectorRectangles = value.rects.stream().map(r->new GameSector.SectorRectangle(r.x, r.y, r.width, r.height)).collect(Collectors.toSet());
        return value;
    }
}
