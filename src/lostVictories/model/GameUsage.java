package lostVictories.model;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GameUsage {

	private String gameName;
	private UUID userID;
	private Long startTime;
	private Long endTime;

	public GameUsage(String gameName, UUID userID, Long startTime, Long endTime) {
		this.gameName = gameName;
		this.userID = userID;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public GameUsage(Map<String, Object> sourceAsMap) {
		this.gameName = (String) sourceAsMap.get("gameName");
		this.userID = UUID.fromString((String) sourceAsMap.get("userID"));
		this.startTime = (Long) sourceAsMap.get("startTime");
		this.endTime = (Long) sourceAsMap.get("endTime");
	}

	@JsonIgnore
	public XContentBuilder getJSONRepresentation() throws IOException {
		return jsonBuilder()
				.startObject()
				.field("gameName", gameName)
				.field("userID", userID)
				.field("startTime", startTime)
				.field("endTime", endTime)
				.endObject();
	}

	public void setEndTime(long currentTimeMillis) {
		this.endTime = currentTimeMillis;
		
	}

}
