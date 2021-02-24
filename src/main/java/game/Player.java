package game;

import lombok.Getter;
import lombok.Setter;
import net.Client;

public class Player {
    @Getter @Setter private Room room;
    @Getter @Setter private Client client;

    public Player(Room room) {
        this.room = room;
    }
}