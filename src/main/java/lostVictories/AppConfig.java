package lostVictories;

public enum AppConfig {
    GAME_MANAGER_URL, GAME_ID;

    public String get(){
        return System.getProperty(this.name());
    }
}
