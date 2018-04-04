package com.jme3.lostVictories.objectives

import com.jme3.lostVictories.network.messages.CharacterMessage
import com.jme3.lostVictories.network.messages.Country
import com.jme3.lostVictories.network.messages.HouseMessage
import com.jme3.lostVictories.network.messages.RankMessage
import com.jme3.lostVictories.network.messages.Structure
import com.jme3.lostVictories.network.messages.Vector
import lostVictories.dao.CharacterDAO
import lostVictories.dao.HouseDAO
import spock.lang.Specification

import java.awt.Rectangle

class CaptureTownSpec extends Specification {

    def createCharacterMock = { u, s ->
        u.getId() >> UUID.randomUUID()
        u.getLocation() >> new Vector(0, 0, 0)
        u.getCountry() >> Country.GERMAN
        u.getCurrentStrength(_) >> s
        u.isBusy() >> false
        u.getRank() >> RankMessage.LIEUTENANT

    }

    def "sends the larger platoon to best regions" (){
        given:
        def structure1 = Mock(HouseMessage.class)
        structure1.getLocation() >> new Vector(100, 0, 100)
        def structure2 = Mock(HouseMessage.class)
        structure2.getLocation() >> new Vector(200, 0, 100)
        def structure3 = Mock(HouseMessage.class)
        structure3.getLocation() >> new Vector(300, 0, 100)
        def structure4 = Mock(HouseMessage.class)
        structure4.getLocation() >> new Vector(400, 0, 100)

        def sector1 = new CaptureTown.GameSector(rects:[new Rectangle(100, 100, 10, 10)], structures:[structure1])
        def sector2 = new CaptureTown.GameSector(rects:[new Rectangle(200, 100, 10, 10)], structures:[structure2])
        def sector3 = new CaptureTown.GameSector(rects:[new Rectangle(300, 100, 10, 10)], structures:[structure3])
        def sector4 = new CaptureTown.GameSector(rects:[new Rectangle(400, 100, 10, 10)], structures:[structure4])
        def captureTown = new CaptureTown(gameSectors: [sector1, sector2, sector3, sector4])
        def unit1 = Mock(CharacterMessage.class)
        def unit2 = Mock(CharacterMessage.class)
        def unit3 = Mock(CharacterMessage.class)
        def unit4 = Mock(CharacterMessage.class)
        createCharacterMock(unit1, 4)
        createCharacterMock(unit2, 6)
        createCharacterMock(unit3, 14)
        createCharacterMock(unit4, 1)

        def c1 = new CharacterMessage(unitsUnderCommand: [unit1.id, unit2.id, unit3.id, unit4.id])
        def map = new HashMap<>()

        def characterDAO = Mock(CharacterDAO.class)
        characterDAO.getCharacter(unit1.id) >> unit1
        characterDAO.getCharacter(unit2.id) >> unit2
        characterDAO.getCharacter(unit3.id) >> unit3
        characterDAO.getCharacter(unit4.id) >> unit4

        when:
        captureTown.runObjective(c1, UUID.randomUUID().toString(), characterDAO, Mock(HouseDAO.class), map, new HashMap<>())

        then:
        1 * unit3.addObjective(*_) >> { args ->
            assert args[1].centre == new Vector(100, 0, 100)
        }


    }

}
