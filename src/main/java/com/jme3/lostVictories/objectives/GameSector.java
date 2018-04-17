package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import lostVictories.dao.HouseDAO;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

@JsonSerialize(converter = GameSectorConverter.class)
public class GameSector {
    @JsonIgnore
    Set<Rectangle> rects = new HashSet<>();
    Set<UUID> structures = new HashSet<>();

    Set<SectorRectangle> sectorRectangles;

    GameSector(){}

    @JsonCreator
    GameSector(@JsonProperty("sectorRectangles") Set<SectorRectangle> rectangles, @JsonProperty("structures") Set<UUID> structures){
        this.structures = structures;
        this.rects = rectangles.stream().map(r->new Rectangle(r.x, r.y, r.width, r.height)).collect(Collectors.toSet());
    }

    public GameSector(Rectangle rect) {
        this.rects.add(rect);
    }

    public boolean isJoinedTo(GameSector s) {
        for(Rectangle r1: rects){
            for(Rectangle r2:s.rects){
                if(new Rectangle(r1.x-1, r1.y-1, r1.width+2, r1.height+2).intersects(r2)){
                    return true;
                }
            }

        }
        return false;
    }

    public void merge(GameSector neighbour) {
        structures.addAll(neighbour.structures);
        rects.addAll(neighbour.rects);

    }

    boolean containsHouse(Structure house) {
        return rects.stream().filter(r->r.contains(house.getLocation().x, house.getLocation().z)).findAny().isPresent();
    }

    public boolean containsPoint(Vector centre) {
        if(rects.isEmpty()){
            return false;
        }
        Rectangle union = null;
        for(Iterator<Rectangle> it = rects.iterator(); it.hasNext();){
            if(union == null){
                union = it.next();
            }else{
                union.add(it.next());
            }
        }
        return union.contains(centre.x, centre.z);
    }

    void add(Structure house) {
        structures.add(house.getId());
    }

    boolean isUnsecured(Country country, HouseDAO houseDAO) {
        return structures.stream()
                .map(houseDAO::getHouse)
                .filter(Objects::nonNull)
                .anyMatch(h->h.getOwner()!=country);

    }

    Vector3f location() {
        Rectangle rect = rects.iterator().next();
        return new Vector3f((float)rect.getCenterX(), 0, (float)rect.getCenterY());
    }

    Set<HouseMessage> getHouses(HouseDAO houseDAO){
        return structures.stream()
                .map(houseDAO::getHouse)
                .filter(Objects::nonNull)
                .filter(HouseMessage.class::isInstance).collect(Collectors.toSet());
    }

    Set<BunkerMessage> getBunkers(HouseDAO houseDAO){
        return structures.stream()
                .map(houseDAO::getBunker)
                .filter(Objects::nonNull)
                .filter(BunkerMessage.class::isInstance).collect(Collectors.toSet());
    }

    public int getHousesCount() {
        return structures.size();
    }

    public static class SectorRectangle{
        int x;
        int y;
        int width;
        int height;

        private SectorRectangle(){}

        public SectorRectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }



}
