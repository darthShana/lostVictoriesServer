package lostVictories.dao

import com.jme3.lostVictories.network.messages.Vector
import org.elasticsearch.client.Client
import spock.lang.Specification

import java.awt.geom.Rectangle2D

class HouseDAOSpec extends Specification {

    def "get bunkers in rectangle"(){
        given:
        def houseDAO = new HouseDAO(Mock(Client), "some index")

        when:
        def bunkers = houseDAO.getBunkers([houseDAO.allBunkers[1].id, houseDAO.allBunkers[2].id] as Set)

        then:
        new Vector(-342.9169f, 96.32557f, -144.11838f) == bunkers[0].location
        new Vector(57.210407f, 100.232506f, -270.88477f) == bunkers[1].location
        2 == bunkers.size()
    }
}
