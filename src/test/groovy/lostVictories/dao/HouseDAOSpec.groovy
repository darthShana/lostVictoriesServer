package lostVictories.dao

import com.jme3.lostVictories.network.messages.Vector
import redis.clients.jedis.Jedis
import spock.lang.Specification

import java.awt.geom.Rectangle2D

class HouseDAOSpec extends Specification {

    def "get bunkers in rectangle"(){
        given:
        def houseDAO = new HouseDAO(Mock(Jedis.class), "some index")

        when:
        def bunkers = houseDAO.getBunkers([houseDAO.allBunkers[1].id, houseDAO.allBunkers[2].id] as Set)

        then:
        new Vector(99.83392f,96.43404f,95.658516f) == bunkers[0].location
        new Vector(336.1358f,95.88243f,66.05069f) == bunkers[1].location
        2 == bunkers.size()
    }
}
