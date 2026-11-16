package domain.match;

import domain.physics.Field;
import domain.player.Player;

public interface MatchFactory {
    Match create(Field field, Player p1, Player p2, double limitValue);
}
