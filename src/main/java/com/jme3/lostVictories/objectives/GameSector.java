package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jme3.lostVictories.network.messages.*;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import jdk.nashorn.internal.runtime.ScriptObject;
import lostVictories.dao.HouseDAO;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static lostVictories.dao.CharacterDAO.MAPPER;

@JsonSerialize(converter = GameSectorConverter.class)
public class GameSector {
    @JsonIgnore
    Set<Rectangle> rects = new HashSet<>();

    UUID id;
    Set<UUID> houses = new HashSet<>();
    Set<UUID> defences = new HashSet<>();

    Set<SectorRectangle> sectorRectangles;

    GameSector(){}

    @JsonCreator
    GameSector(@JsonProperty("id") UUID id, @JsonProperty("sectorRectangles") Set<SectorRectangle> rectangles, @JsonProperty("houses") Set<UUID> houses, @JsonProperty("defences") Set<UUID> defences){
        this.id = id;
        this.houses = houses;
        this.defences = defences;
        this.rects = rectangles.stream().map(r->new Rectangle(r.x, r.y, r.width, r.height)).collect(Collectors.toSet());
    }

    public GameSector(Rectangle rect) {
        this.id = UUID.randomUUID();
        this.rects.add(rect);
    }

    public GameSector(Map<String, String> source) throws IOException {
        this.id = UUID.fromString(source.get("id"));

        JavaType type = MAPPER.getTypeFactory().constructCollectionType(Set.class, UUID.class);
        houses = MAPPER.readValue(source.get("houses"), type);
        defences = MAPPER.readValue(source.get("defences"), type);

        JavaType rectType = MAPPER.getTypeFactory().constructCollectionType(Set.class, SectorRectangle.class);
        sectorRectangles = MAPPER.readValue(source.get("sectorRectangles"), rectType);
        rects = sectorRectangles.stream().map(sr->new Rectangle(sr.x, sr.y, sr.width, sr.height)).collect(Collectors.toSet());
    }

    public Map<String, String> getMapRepresentation() throws JsonProcessingException {
        HashMap<String, String> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("houses", MAPPER.writeValueAsString(houses));
        map.put("defences", MAPPER.writeValueAsString(defences));
        sectorRectangles = rects.stream().map(r->new SectorRectangle(r.x, r.y, r.width, r.height)).collect(Collectors.toSet());
        map.put("sectorRectangles", MAPPER.writeValueAsString(sectorRectangles));
        return map;
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

    public UUID getId(){
        return id;
    }

    public void merge(GameSector neighbour) {
        houses.addAll(neighbour.houses);
        defences.addAll(neighbour.defences);
        rects.addAll(neighbour.rects);

    }

    public boolean contains(Structure house) {
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

    public void addHouse(Structure house) {
        houses.add(house.getId());
    }
    public void addBunker(Structure house) {
        defences.add(house.getId());
    }

    boolean isUnsecured(Country country, HouseDAO houseDAO) {
        return houses.stream()
                .map(houseDAO::getHouse)
                .filter(Objects::nonNull)
                .anyMatch(h->h.getOwner()!=country);

    }

    Vector3f location() {
        Rectangle rect = rects.iterator().next();
        return new Vector3f((float)rect.getCenterX(), 0, (float)rect.getCenterY());
    }

    Set<HouseMessage> getHouses(HouseDAO houseDAO){
        return houses.stream()
                .map(houseDAO::getHouse)
                .filter(Objects::nonNull)
                .filter(HouseMessage.class::isInstance).collect(Collectors.toSet());
    }

    Set<BunkerMessage> getBunkers(HouseDAO houseDAO){
        return defences.stream()
                .map(houseDAO::getBunker)
                .filter(Objects::nonNull)
                .filter(BunkerMessage.class::isInstance).collect(Collectors.toSet());
    }

    public int getHousesCount() {
        return houses.size();
    }

    @Override
    public boolean equals(Object obj) {
        return ((GameSector)obj).houses.equals(houses);
    }

    @Override
    public int hashCode() {
        return houses.hashCode();
    }

    @Override
    public String toString() {
        return "houses:"+getHousesCount();
    }

    public Set<UUID> allStructures() {
        Set<UUID> all = new HashSet<>();
        all.addAll(houses);
        all.addAll(defences);
        return all;
    }

    public Set<UUID> getHouses() {
        return houses;
    }

    public Set<UUID> getDefences() {
        return defences;
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
