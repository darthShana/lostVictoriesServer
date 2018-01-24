package com.jme3.lostVictories.objectives

import com.jme3.lostVictories.network.messages.CharacterMessage
import com.jme3.lostVictories.network.messages.BunkerMessage
import com.jme3.lostVictories.network.messages.CharacterType
import com.jme3.lostVictories.network.messages.Vector
import lostVictories.dao.CharacterDAO
import lostVictories.dao.HouseDAO

import java.awt.geom.Rectangle2D

class SecureSectorSpec extends spock.lang.Specification {

    def "test commanders in the stating sector will occupy bunkers"(){
        given:
        def secureSector = new SecureSector(boundary: new Rectangle2D.Float(100, 100, 20, 20))
        def coId = UUID.randomUUID()
        def unit1_real = new CharacterMessage(id: UUID.randomUUID(), location: new Vector(105, 100, 105), commandingOfficer: coId, type: CharacterType.SOLDIER)
        def unit2_real = new CharacterMessage(id: UUID.randomUUID(), location: new Vector(105, 100, 105), commandingOfficer: coId, type: CharacterType.SOLDIER)
        def unit1 = Spy(unit1_real)
        def unit2 = Spy(unit2_real)

        and:
        def characterMessage = new CharacterMessage(id: coId, location: new Vector(105, 100, 105), unitsUnderCommand: [unit1.getId(), unit2.getId()])
        def houseDAO = Mock(HouseDAO.class)
        def characterDAO = Mock(CharacterDAO.class)
        houseDAO.getBunkers(new Rectangle2D.Float(100, 100, 20, 20)) >> [new BunkerMessage(new Vector(120, 100, 120)), new BunkerMessage(new Vector(100, 100, 100))]
        characterDAO.getAllCharacters(_) >> [(unit1.id):unit1, (unit2.id):unit2]

        when:
        secureSector.state = SecureSectorState.WAIT_FOR_REENFORCEMENTS
        secureSector.runObjective(characterMessage, characterMessage.getId().toString(), characterDAO, houseDAO, [:], [:])

        then:
        SecureSectorState.DEFEND_SECTOR == secureSector.state

        when:
        def toSave = [:]
        secureSector.runObjective(characterMessage, characterMessage.getId().toString(), characterDAO, houseDAO, toSave, [:])

        then:
        secureSector.issuedOrders[unit1.id] instanceof TransportSquad
        secureSector.issuedOrders[unit2.id] instanceof TransportSquad
        toSave[unit1.id]
        1 * unit1.addObjective(*_) >> {args ->
            assert args[1] == secureSector.issuedOrders[unit1.id]
            assert args[1].destination == new Vector(120, 100, 120)
        }

        1 * unit2.addObjective(*_) >> {args ->
            assert args[1] == secureSector.issuedOrders[unit2.id]
            assert args[1].destination == new Vector(100, 100, 100)
        }

        when:
        unit1 = Spy(unit1_real)
        unit2 = Spy(unit2_real)
        secureSector.runObjective(characterMessage, characterMessage.getId().toString(), characterDAO, houseDAO, [:], [:])

        then:
        0 * unit1.addObjective(*_)
        0 * unit2.addObjective(*_)


    }

}
