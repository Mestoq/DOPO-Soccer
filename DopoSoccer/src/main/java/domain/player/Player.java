package domain.player;

import domain.DopoSoccerException;
import domain.character.Character_;
import domain.match.Match;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntSupplier;

import static domain.DopoSoccerException.NULL_CHARECTER_UPGRADE;

public abstract class Player {
    public static final int MAX_STAT_LEVEL = 10;
    public static final int BASE_UPGRADE_COST = 500;
    public static final double STUN_DURATION_SECONDS = 3.0;

    // Identificación
    protected final String name;
    protected final boolean isHuman;
    protected Character_ character;

    // Progreso persistente (aplica a todos los personajes del jugador)
    protected int points;
    protected int speed;
    protected int jump;
    protected int kick;
    protected int survival;

    // Estado dentro del partido
    protected int goals;
    protected double currentHealth;
    protected double powerBar;
    protected boolean powerAuraActive;
    protected double stunTimer;

    // Mapas para las mejoras

    private final Map<String, IntSupplier> statGetters = new HashMap<>();
    private final Map<String, Runnable> statUpgraders = new HashMap<>();

    public Player(String name, boolean isHuman) {
        this.name = name;
        this.isHuman = isHuman;

        // Todas las estadísticas inician en 1 y los puntos en 0
        this.points = 0;
        this.speed = 1;
        this.jump = 1;
        this.kick = 1;
        this.survival = 1;

        initUpgradeRegistry();
        resetMatchState();
    }

    private void initUpgradeRegistry() {
        statGetters.put("SPEED", () -> this.speed);
        statGetters.put("JUMP", () -> this.jump);
        statGetters.put("KICK", () -> this.kick);
        statGetters.put("SURVIVAL", () -> this.survival);

        statUpgraders.put("SPEED", () -> this.speed++);
        statUpgraders.put("JUMP", () -> this.jump++);
        statUpgraders.put("KICK", () -> this.kick++);
        statUpgraders.put("SURVIVAL", () -> {
            this.survival++;
            this.currentHealth = getMaxHealth();
        });
    }

    /**
     * Reinicia los valores de partida antes de iniciar un nuevo encuentro (0-0, barras vacías, vida máxima).
     */
    public void resetMatchState() {
        this.goals = 0;
        this.currentHealth = getMaxHealth();
        this.powerBar = 0.0;
        this.powerAuraActive = false;
        this.stunTimer = 0.0;
    }

    //metodos para usar las formulas de requiisito

    public double getMaxHealth(){
        return 100.0 * (1.0 + 0.10 * (survival - 1));
    }
    public double getKickMultiplier(){
        return 1.0 + 0.10 * (kick - 1);
    }

    public double getNormalKickDamage(){
        return 10.0 * getKickMultiplier();
    }

    public double getPowerShotDamage(double baseShotDamage){
        return baseShotDamage * getKickMultiplier();
    }

    //Ahora las mejoras

    public int getUpgradeCost(int currentLevel) {
        return BASE_UPGRADE_COST * (int) Math.pow(2, currentLevel - 1);
    }

    public void upgrade(String type) throws DopoSoccerException {
        if (type == null) {
            throw new DopoSoccerException(NULL_CHARECTER_UPGRADE);
        }

        String key = type.trim().toUpperCase();
        IntSupplier getter = statGetters.get(key);
        Runnable upgrader = statUpgraders.get(key);

        if (getter == null || upgrader == null) {
            throw new DopoSoccerException("Estadística no válida: " + type);
        }

        int currentLevel = getter.getAsInt();
        if (currentLevel >= MAX_STAT_LEVEL) {
            throw new DopoSoccerException("La estadística " + type + " ya alcanzó su nivel máximo (10).");
        }

        int cost = getUpgradeCost(currentLevel);
        if (this.points < cost) {
            throw new DopoSoccerException("Puntos insuficientes. Necesitas " + cost + " puntos y tienes " + this.points + ".");
        }

        this.points -= cost;
        upgrader.run();
    }

    public void addPoints(int earnedPoints) {
        if (earnedPoints > 0) {
            this.points += earnedPoints;
        }
    }

    //Update de los estados de los powers


    public void update(double dt, Match match) {
            // Controlamos el aturdimiento teniendo 3 segundos sin moverse y al terminar recupera vida máxima
        if (this.stunTimer > 0.0) {
            this.stunTimer -= dt;
            if (this.stunTimer <= 0.0) {
                this.stunTimer = 0.0;
                this.currentHealth = getMaxHealth();
                // [FIX 6] Al terminar el aturdimiento el personaje vuelve a poder actuar.
                if (this.character != null) {
                    this.character.setStunned(false);
                }
            }
        }

        // La barra de poder se llena en 10 segundos (10% por segundo) incluso mientras el aura está activa
        if (this.powerBar < 100.0) {
            this.powerBar = Math.min(100.0, this.powerBar + (10.0 * dt));
        }

        // Se ejecuta la lógica de control de la subclase (Human o Cpu) solo si no está aturdido
        if (!isStunned()) {
            decideAction(dt, match);
        }
    }

    /**
     * Activa el aura de poder si la barra está al 100% y no tiene ya un aura activa.
     */
    public void activatePower() {
        if (this.powerBar >= 100.0 && !this.powerAuraActive && !isStunned()) {
            this.powerBar = 0.0;
            this.powerAuraActive = true;
        }
    }

    /**
     * Se ejecuta cuando el personaje con aura toca el balón para lanzar su tiro especial.
     */
    public void consumePowerAura() {
        this.powerAuraActive = false;
    }
    /**
     * Al recibir un gol, la barra de poder aumenta inmediatamente un cuarto (25%).
     */
    public void onGoalConceded() {
        this.powerBar = Math.min(100.0, this.powerBar + 25.0);
    }

    public void addGoal() {
        this.goals++;
    }

    /**
     * Aplica daño por patada o tiro de poder. Si la vida llega a 0, el jugador queda aturdido 3 segundos.
     */
    /**
     * Aplica daño por patada o tiro de poder. Si la vida llega a 0, el jugador queda aturdido 3 segundos.
     */
    public void takeDamage(double damage) {
        if (damage <= 0.0 || isStunned()) {
            return;
        }

        this.currentHealth = Math.max(0.0, this.currentHealth - damage);
        if (this.currentHealth == 0.0) {
            this.stunTimer = STUN_DURATION_SECONDS;
            if (this.character != null) {
                this.character.setStunned(true);
            }
        }


        this.currentHealth = Math.max(0.0, this.currentHealth - damage);
        if (this.currentHealth == 0.0) {
            this.stunTimer = STUN_DURATION_SECONDS;
            if (this.character != null) {
                this.character.setStunned(true);
            }
        }
        this.currentHealth = Math.max(0.0, this.currentHealth - damage);
        if (this.currentHealth == 0.0) {
            this.stunTimer = STUN_DURATION_SECONDS;
        }
    }
    public abstract void decideAction(double dt, Match match);

    public void restoreMatchState(int goals, double health, double powerBar, boolean aura, double stunTimer) {
        this.goals = Math.max(0, goals);
        this.currentHealth = Math.max(0.0, Math.min(getMaxHealth(), health));
        this.powerBar = Math.max(0.0, Math.min(100.0, powerBar));
        this.powerAuraActive = aura;
        this.stunTimer = Math.max(0.0, stunTimer);
        if (this.character != null) {
            this.character.setStunned(this.stunTimer > 0.0);
        }
    }



    public boolean isStunned() { return stunTimer > 0.0; }
    public boolean isHuman() { return isHuman; }
    public String getName() { return name; }

    public Character_ getCharacter() { return character; }
    public void setCharacter(Character_ character) { this.character = character; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = Math.max(0, points); }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = Math.max(1, Math.min(MAX_STAT_LEVEL, speed)); }

    public int getJump() { return jump; }
    public void setJump(int jump) { this.jump = Math.max(1, Math.min(MAX_STAT_LEVEL, jump)); }

    public int getKick() { return kick; }
    public void setKick(int kick) { this.kick = Math.max(1, Math.min(MAX_STAT_LEVEL, kick)); }

    public int getSurvival() { return survival; }
    public void setSurvival(int survival) {
        this.survival = Math.max(1, Math.min(MAX_STAT_LEVEL, survival));
        this.currentHealth = getMaxHealth();
    }


    public String getAiProfile() { return null; }
    public int getGoals() { return goals; }
    public double getCurrentHealth() { return currentHealth; }
    public double getPowerBar() { return powerBar; }
    public boolean hasPowerAura() { return powerAuraActive; }
    public double getStunTimer() { return stunTimer; }
    public boolean isPowerReady() { return this.powerAuraActive; }
    public void consumePower() { consumePowerAura(); }
    public int getEffectiveKick() { return this.kick; }
}