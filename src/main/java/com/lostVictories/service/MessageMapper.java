package com.lostVictories.service;

import com.jme3.lostVictories.network.messages.SquadType;
import com.lostVictories.api.AchievementStatus;
import com.lostVictories.api.CaptureStatus;
import com.lostVictories.api.CharacterMessage;
import com.lostVictories.api.CharacterStatusResponse;
import com.lostVictories.api.CharacterType;
import com.lostVictories.api.Country;
import com.lostVictories.api.EquipmentStatusResponse;
import com.lostVictories.api.GameStatistics;
import com.lostVictories.api.GameStatsResponse;
import com.lostVictories.api.HouseMessage;
import com.lostVictories.api.HouseStatusResponse;
import com.lostVictories.api.LostVictoryMessage;
import com.lostVictories.api.RankMessage;
import com.lostVictories.api.RelatedCharacterStatusResponse;
import com.lostVictories.api.TreeGroupMessage;
import com.lostVictories.api.TreeStatusResponse;
import com.lostVictories.api.UnClaimedEquipmentMessage;
import com.lostVictories.api.Weapon;

import java.util.UUID;
import java.util.stream.Collectors;

import static com.lostVictories.service.LostVictoriesService.bytes;

/**
 * Created by dharshanar on 27/05/17.
 */
public class MessageMapper {

    public LostVictoryMessage toMessage(com.jme3.lostVictories.network.messages.TreeGroupMessage t) {
        return LostVictoryMessage.newBuilder()
                .setTreeStatusResponse(TreeStatusResponse.newBuilder()
                        .setTreeGroup(TreeGroupMessage.newBuilder()
                                .setId(bytes(t.getId()))
                                .setLocation(t.getLocation().toMessage())
                                .addAllTrees(t.getTrees().stream().map(tt->tt.toMessage()).collect(Collectors.toSet()))
                                .build())
                        .build())
                .build();
    }

    public LostVictoryMessage toMessage(com.jme3.lostVictories.network.messages.HouseMessage h) {
        HouseMessage.Builder builder = HouseMessage.newBuilder()
                .setId(bytes(h.getId()))
                .setType(h.getType())
                .setLocation(h.getLocation().toMessage())
                .setRotation(h.getRotation().toMessage());

        if(h.getOwner()!=null){
            builder.setOwner(Country.valueOf(h.getOwner().name()));
        }
        if(h.getContestingOwner()!=null){
            builder.setContestingOwner(Country.valueOf(h.getContestingOwner().name()));
        }
        if(h.getCaptureStatus()!=null){
            builder.setCaptureStatus(CaptureStatus.valueOf(h.getCaptureStatus().name()));
        }
        if(h.getStatusChangeTime()!=null){
            builder.setStatusChangeTime(h.getStatusChangeTime());
        }

        HouseMessage build = builder.build();
        return LostVictoryMessage.newBuilder()
                .setHouseStatusResponse(HouseStatusResponse.newBuilder()
                        .setHouse(build)
                        .build())
                .build();
    }

    public LostVictoryMessage toMessage(com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage e) {
        return LostVictoryMessage.newBuilder()
                .setEquipmentStatusResponse(EquipmentStatusResponse.newBuilder()
                        .setUnClaimedEquipment(UnClaimedEquipmentMessage.newBuilder()
                                .setId(bytes(e.getId()))
                                .setWeapon(Weapon.valueOf(e.getWeapon().name()))
                                .setLocation(e.getLocation().toMessage())
                                .setRotation(e.getRotation().toMessage())
                                .build())
                        .build())
                .build();

    }

    public LostVictoryMessage toMessage(com.jme3.lostVictories.network.messages.CharacterMessage characterMessage, int backoff) {
        CharacterMessage.Builder characterBuilder = CharacterMessage.newBuilder()
                .setId(bytes(characterMessage.getId()))
                .setLocation(characterMessage.getLocation().toMessage())
                .setCountry(Country.valueOf(characterMessage.getCountry().name()))
                .setWeapon(Weapon.valueOf(characterMessage.getWeapon().name()))
                .setRank(RankMessage.valueOf(characterMessage.getRank().name()))
                .addAllUnitsUnderCommand(characterMessage.getUnitsUnderCommand().stream().map(uuid -> bytes(uuid)).collect(Collectors.toSet()))
                .addAllPassengers(characterMessage.getPassengers().stream().map(uuid -> bytes(uuid)).collect(Collectors.toSet()))
                .setType(CharacterType.valueOf(characterMessage.getCharacterType().name()))
                .setOrientation(characterMessage.getOrientation().toMessage())
                .addAllActions(characterMessage.getActions().stream().map(action -> action.toMessage()).collect(Collectors.toSet()))
                .putAllObjectives(characterMessage.readObjectives())
                .addAllCompletedObjectives(characterMessage.getAllCompletedObjectives().stream().map(c -> bytes(UUID.fromString(c))).collect(Collectors.toSet()))
                .setDead(characterMessage.isDead())
                .setEngineDamaged(characterMessage.hasEngineDamage())
                .setVersion(characterMessage.getVersion())
                .setKillCount(characterMessage.getKillCount())
                .setSquadType(characterMessage.getSquadType(SquadType.RIFLE_TEAM).toMessage())
                .setCreationTime(characterMessage.getCreationTime())
                .setBusy(characterMessage.isBusy())
                .setAttacking(characterMessage.isAttacking())
                .setType(CharacterType.valueOf(characterMessage.getCharacterType().name()));

        if(characterMessage.getCommandingOfficer()!=null){
            characterBuilder.setCommandingOfficer(bytes(characterMessage.getCommandingOfficer()));
        }
        if (characterMessage.getBoardedVehicle()!=null){
            characterBuilder.setBoardedVehicle(bytes(characterMessage.getBoardedVehicle()));
        }
        if (characterMessage.getCheckoutClient()!=null){
            characterBuilder.setCheckoutClient(bytes(characterMessage.getCheckoutClient()));
        }
        if (characterMessage.getCheckoutTime()!=null){
            characterBuilder.setCheckoutTime(characterMessage.getCheckoutTime());
        }
        if(characterMessage.getTimeOfDeath()!=null){
            characterBuilder.setTimeOfDeath(characterMessage.getTimeOfDeath());
        }

        CharacterStatusResponse.Builder builder = CharacterStatusResponse.newBuilder();
        builder.setUnit(characterBuilder.build());
        builder.setBackoff(backoff);

        CharacterStatusResponse build = builder.build();
        return LostVictoryMessage.newBuilder()
                .setCharacterStatusResponse(build)
                .build();
    }

    public LostVictoryMessage toMessage(com.jme3.lostVictories.network.messages.CharacterMessage characterMessage, boolean b) {
        CharacterMessage.Builder builder = CharacterMessage.newBuilder()
                .setId(bytes(characterMessage.getId()))
                .setLocation(characterMessage.getLocation().toMessage())
                .setCountry(Country.valueOf(characterMessage.getCountry().name()))
                .setWeapon(Weapon.valueOf(characterMessage.getWeapon().name()))
                .setRank(RankMessage.valueOf(characterMessage.getRank().name()))
                .addAllUnitsUnderCommand(characterMessage.getUnitsUnderCommand().stream().map(uuid -> bytes(uuid)).collect(Collectors.toSet()))
                .addAllPassengers(characterMessage.getPassengers().stream().map(uuid -> bytes(uuid)).collect(Collectors.toSet()))
                .setType(CharacterType.valueOf(characterMessage.getCharacterType().name()))
                .setOrientation(characterMessage.getOrientation().toMessage())
                .setDead(characterMessage.isDead())
                .setEngineDamaged(characterMessage.hasEngineDamage())
                .setVersion(characterMessage.getVersion())
                .setKillCount(characterMessage.getKillCount())
                .setSquadType(characterMessage.getSquadType(SquadType.RIFLE_TEAM).toMessage())
                .setCreationTime(characterMessage.getCreationTime())
                .setBusy(characterMessage.isBusy())
                .setAttacking(characterMessage.isAttacking())
                .setType(CharacterType.valueOf(characterMessage.getCharacterType().name()));

        if(characterMessage.getCommandingOfficer()!=null){
            builder.setCommandingOfficer(bytes(characterMessage.getCommandingOfficer()));
        }
        if (characterMessage.getBoardedVehicle()!=null){
            builder.setBoardedVehicle(bytes(characterMessage.getBoardedVehicle()));
        }
        if (characterMessage.getCheckoutClient()!=null){
            builder.setCheckoutClient(bytes(characterMessage.getCheckoutClient()));
        }
        if (characterMessage.getCheckoutTime()!=null){
            builder.setCheckoutTime(characterMessage.getCheckoutTime());
        }
        if(characterMessage.getTimeOfDeath()!=null){
            builder.setTimeOfDeath(characterMessage.getTimeOfDeath());
        }

        return LostVictoryMessage.newBuilder()
                .setRelatedCharacterStatusResponse(RelatedCharacterStatusResponse.newBuilder()
                        .setUnit(builder
                                .build())
                        .build())
                .build();
    }

    public LostVictoryMessage toMessage(com.jme3.lostVictories.network.messages.wrapper.GameStatsResponse gameStatsResponse) {
        GameStatistics.Builder builder = GameStatistics.newBuilder()
                .setBlueVictoryPoints(gameStatsResponse.getGameStatistics().getBlueVictoryPoints())
                .setRedVictoryPoints(gameStatsResponse.getGameStatistics().getRedVictoryPoints());

        if(gameStatsResponse.getGameStatistics().getBlueHouses()!=null){
            builder.setBlueHouses(gameStatsResponse.getGameStatistics().getBlueHouses());
        }
        if(gameStatsResponse.getGameStatistics().getRedHouses()!=null){
            builder.setRedHouses(gameStatsResponse.getGameStatistics().getRedHouses());
        }
        if(gameStatsResponse.getGameStatistics().getAvatarRespawnEstimate()!=null){
            builder.setAvatarRespawnEstimate(gameStatsResponse.getGameStatistics().getAvatarRespawnEstimate());

        }

        return LostVictoryMessage.newBuilder()
                .setGameStatsResponse(GameStatsResponse.newBuilder()
                        .setAchivementStatus(AchievementStatus.newBuilder()
                                .setAchivementCurrent(gameStatsResponse.getAchivementStatus().getAchivementCurrent())
                                .setAchivementStatusText(gameStatsResponse.getAchivementStatus().getAchivementStatusText())
                                .setAchivementTotal(gameStatsResponse.getAchivementStatus().getAchivementTotal())
                                .setSentTime(gameStatsResponse.getAchivementStatus().getSentTime())
                                .build())
                        .setGameStatistics(builder
                                .build())
                        .addAllMessages(gameStatsResponse.getMessages())
                        .build())
                .build();
    }
}
