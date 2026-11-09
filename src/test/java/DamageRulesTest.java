package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


class DamageRulesTest {


    private static class TestPlayer extends Player {
        TestPlayer(int speed, int jump, int kick, int survival) { super(0, speed, jump, kick, survival); }
        @Override public boolean canChoose(String id) { return true; }
        @Override public void act(Match match) { }
    }

    @Test
    void statsFormulasMatchTheStatement() {
        Player p = new TestPlayer(1, 1, 5, 3);
        assertEquals(1.4, p.getKickMultiplier(), 1e-9);   // 1 + 0.10 * (5 - 1)
        assertEquals(120.0, p.getMaxLife(), 1e-9);        // 100 * (1 + 0.10 * (3 - 1))
    }

    @Test
    void kickAndPowerDamageFollowTheExpectedResult() {
        Player attacker = new TestPlayer(1, 1, 5, 1);     // Kick = 5
        Player victim = new TestPlayer(1, 1, 1, 3);       // Survival = 3
        Character cameroon = new CameroonCharacter(attacker);
        Character target = new CameroonCharacter(victim);

        assertEquals(14.0, cameroon.getKickDamage(), 1e-9);          // 10 * 1.4
        target.receiveDamage(cameroon.getKickDamage());
        assertEquals(106.0, target.getLife(), 1e-9);                 // 120 - 14

        assertEquals(84.0, cameroon.getPower().getBaseDamage() * cameroon.getKickMultiplier(), 1e-9); // 60 * 1.4
    }

    @Test
    void lifeNeverDropsBelowZeroAndZeroStunsTheCharacter() {
        Character c = new USACharacter(new TestPlayer(1, 1, 1, 1));
        c.receiveDamage(1000);
        assertEquals(0.0, c.getLife(), 1e-9);
        assertTrue(c.isStunned());
        assertFalse(c.canMove());
    }
}
