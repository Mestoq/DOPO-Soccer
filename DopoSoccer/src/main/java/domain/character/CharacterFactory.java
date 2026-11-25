package domain.character;

import domain.DopoSoccerException;
import domain.character.Character_;
import domain.physics.Field;
import domain.player.Player;
import domain.power.Power;

public interface CharacterFactory {
    Character_ create(String country, Power specialPower)throws DopoSoccerException;;

}
