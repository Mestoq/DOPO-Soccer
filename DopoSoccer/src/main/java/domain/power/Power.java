package domain.power;

import domain.DopoSoccerException;
import domain.character.Character_;
import domain.match.Match;
import domain.physics.Ball;
import domain.player.Player;
import domain.power.effect.Effect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Power {

    private static final Map<String, Supplier<Power>> POWER_FACTORIES = new HashMap<>();

    static {
        POWER_FACTORIES.put("AURA AZUL", BlueAuraPower::new);
        POWER_FACTORIES.put("RAYO",      LightningPower::new);
        POWER_FACTORIES.put("TORNADO",   TornadoPower::new);
        POWER_FACTORIES.put("ILUSION",   IllusionPower::new);
    }

    protected final String powerName;
    protected final double baseDamage;
    protected boolean activeShotInAir;


    protected Power(String powerName, double baseDamage) {
        this.powerName = powerName;
        this.baseDamage = baseDamage;
        this.activeShotInAir = false;
    }

    /**
     * con el metodo Factory crea una instancia del tiro de poder por su nombre sin usar if/else.
     */
    public static Power create(String powerName) throws DopoSoccerException {
        if (powerName == null) {
            throw new DopoSoccerException("El nombre del poder no puede ser nulo.");
        }
        Supplier<Power> factory = POWER_FACTORIES.get(powerName.trim().toUpperCase());
        if (factory == null) {
            throw new DopoSoccerException("Tiro de poder no reconocido: " + powerName);
        }
        return factory.get();
    }

    /**
     * Se ejecuta cuando el jugador con aura toca el balón.
     */
    public abstract void launchShot(Ball ball, Character_ shooter, Match match);

    /**
     * Crea el efecto negativo que se aplicará si este tiro golpea al rival.
     */
    public abstract Effect createImpactEffect();

    /**
     * Se ejecuta cuando el tiro de poder impacta al rival antes de tocar el suelo/arco.
     */
    public void onHitRival(Player shooterPlayer, Player rivalPlayer) {
        if (!this.activeShotInAir) {
            return;
        }
        double totalDamage = shooterPlayer.getPowerShotDamage(this.baseDamage);
        rivalPlayer.takeDamage(totalDamage);

        //  Aqui reemplazamos cualquier efecto previo que tuviera el rival
        if (rivalPlayer.getCharacter() != null) {
            rivalPlayer.getCharacter().applyEffect(createImpactEffect());
        }
        this.activeShotInAir = false;
    }

    public void deactivateShot() {
        this.activeShotInAir = false;
    }

    public boolean isActiveShotInAir() { return activeShotInAir; }
    public String getPowerName() { return powerName; }
    public double getBaseDamage() { return baseDamage; }
    public double getGravityScale() { return 1.0; }
    public boolean endsOnGroundContact() { return true; }
}