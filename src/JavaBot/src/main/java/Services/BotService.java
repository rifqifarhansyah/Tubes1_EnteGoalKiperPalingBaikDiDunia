package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    public PlayerAction playerAction;
    private GameState gameState;
    // private GameState prevState = null;
    public Integer tick = null;
    public Integer subtick = null;
    public UUID objectTracker = null;
    public GameObject launchedTeleport = null;
    public boolean justLaunchedTeleportStatus = false;
    public boolean switchDirection = false;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void teleportSerang(PlayerAction playerAction, GameObject enemy) {
        // Punya return value yang menandakan heading teleporter dan bahwa telah
        // menembakkan teleporter
        playerAction.action = PlayerActions.FIRETELEPORT;
        // Kalau belum pakai radar
        playerAction.heading = getHeadingBetween(bot, enemy);
        System.out.println("Attack with launch teleport " + "  bot size : " + bot.getSize()
                + "    enemy size: " + enemy.getSize() + "  distance : "
                + (getDistanceBetween(bot, enemy) - enemy.getSize() - bot.getSize()));
        // return GameObject;
    }

    public GameObject checkTeleportSerang(PlayerAction p, GameObject bot, GameObject launchedTeleport,
            List<GameObject> enemyList, List<GameObject> teleportList) {

        boolean teleportStatus = false;
        if (launchedTeleport != null) {
            System.out.println("test 1");
            for (int i = 0; i < teleportList.size(); i++) {
                System.out.println(teleportList.get(i).getId() + "   " + launchedTeleport.getId());
                if (teleportList.get(i).getId().equals(launchedTeleport.getId())) {
                    teleportStatus = true;
                    launchedTeleport = teleportList.get(i);
                }
            }
            GameObject updateLaunceTeleport = launchedTeleport;
            System.out.println(updateLaunceTeleport);
            // System.out.println(updateLaunceTeleport);
            if (teleportStatus) {
                System.out.println("test 2");
                List<GameObject> closeEnemy = enemyList.stream()
                        .filter(item -> getDistanceBetween(item, updateLaunceTeleport) - bot.getSize()
                                - item.getSize() <= 5
                                && item.getId() != bot.getId() && item.getSize() < bot.getSize())
                        .sorted(Comparator.comparing(
                                item -> getDistanceBetween(item, updateLaunceTeleport) - bot.getSize()
                                        - item.getSize()))
                        .collect(Collectors.toList());

                if (closeEnemy.size() > 0) {
                    System.out.println("detonate attack teleport  size " + bot.getSize() + "   enemy size :"
                            + closeEnemy.get(0).getSize() + "    distance: "
                            + (getDistanceBetween(updateLaunceTeleport, closeEnemy.get(0)) - bot.getSize()
                                    - closeEnemy.get(0).getSize()));
                    p.action = PlayerActions.TELEPORT;
                    p.heading = getHeadingBetween(bot, closeEnemy.get(0));
                    return null;
                } else {
                    System.out.println("can't detonate, too far away" );
                    return updateLaunceTeleport;
                }
            }
        }
        return null;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        var objectState = gameState.getGameObjects();
        var playerState = gameState.getPlayerGameObjects().stream().filter(item -> !item.getShieldStatus())
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var teleportState = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());

        List<GameObject> preyList = playerState.stream()
                .filter(item -> item.getSize() < bot.getSize())
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        GameObject torpedoTarget = null;
        if (playerState.size() > 1) {
            torpedoTarget = playerState.get(1);
        }
        GameObject closestEnemy = null;
        if (preyList.size() > 0) {
            closestEnemy = preyList.get(0);
        }

        var objectRadar = new Radar();

        if (!objectState.isEmpty() && !playerState.isEmpty()) {
            if (gameState.getWorld().getCurrentTick() != tick) {
                System.out.println("tick: " + gameState.getWorld().getCurrentTick());
                System.out.println(launchedTeleport);
                var warning = objectRadar.checkRadar(gameState, bot);
                // System.out.println(warning);
                if (warning != null) {
                    if (warning.gameObjectType == ObjectTypes.SUPERNOVA_BOMB || objectRadar.supernovaDefend) {
                        // Jika muncul bahaya supernova bomb
                        playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot, switchDirection,
                                -90);
                        objectTracker = warning.getId();

                    } else if (warning.gameObjectType == ObjectTypes.TELEPORTER || objectRadar.teleportDefend) {
                        // Jika muncul bahaya teleporter
                        playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot, switchDirection,
                                -90);
                        objectTracker = warning.getId();

                    } else if (warning.gameObjectType == ObjectTypes.TORPEDO_SALVO || objectRadar.torpedoDefend) {
                        // Jika muncul bahaya torpedo
                        if (!bot.getShieldStatus()) {
                            if (bot.getShieldCount() > 0 && bot.getSize() > 40
                                    && getDistanceBetween(bot, warning) - bot.getSize() < 130) {
                                playerAction.action = PlayerActions.ACTIVATESHIELD;
                            } else {
                                playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot,
                                        switchDirection, -90);
                                objectTracker = warning.getId();
                            }
                        }

                    } else if (warning.gameObjectType == ObjectTypes.PLAYER) {
                        if (launchTorpedo(bot, warning, playerAction, false, objectState)) {
                            objectTracker = null;
                        } else {
                            playerAction = GreedyCommand.run(warning, playerAction, objectTracker, bot, switchDirection,
                                    -60);
                            objectTracker = warning.getId();
                        }
                        // System.out.println(
                        // "1. " + warning.getGameObjectType() + " heading: " +
                        // warning.getCurrentHeading());
                        // Jika muncul bahaya player
                        // System.out.println(bot.getEffects());

                        // System.out.println(warning.getCurrentHeading() + " "
                        // +bot.getCurrentHeading());

                    } else if (closestEnemy != null
                            ? getDistanceBetween(closestEnemy, bot) - bot.getSize() - closestEnemy.getSize() < 20
                            : false) {
                        GreedyCommand.activateAfterburner(playerAction, bot, closestEnemy, true);
                    }

                } else if (!justLaunchedTeleportStatus && bot.getTeleCount() > 0 && bot.getSize() > 45
                        && (closestEnemy != null
                                ? bot.getSize() - closestEnemy.getSize() > 30
                                : false)
                        && launchedTeleport == null
                        && getDistanceBetween(bot, closestEnemy) - bot.getSize() - closestEnemy.getSize() > 50) {
                    teleportSerang(playerAction, closestEnemy);
                    justLaunchedTeleportStatus = true;

                } else if (launchTorpedo(bot, torpedoTarget, playerAction, true, objectState)) {

                }

                // playerAction.action = PlayerActions.STOP;
                else {
                    System.out.println("aman");

                    objectTracker = null;
                    if (bot.getAfterburnerStatus()) {
                        playerAction.action = PlayerActions.STOPAFTERBURNER;
                    } else {
                        GreedyCommand.catchFood(gameState.getGameObjects(), bot, playerAction);
                    }
                }

                if (objectTracker != GreedyCommand.evadeObject(bot, objectState, gameState.getWorld(), playerAction,
                        objectTracker)) {
                    objectTracker = GreedyCommand.evadeObject(bot, objectState, gameState.getWorld(), playerAction,
                            objectTracker);
                    switchDirection = !switchDirection;
                }

                if (justLaunchedTeleportStatus && teleportState.size() > 0
                        && playerAction.action != PlayerActions.FIRETELEPORT) {
                    launchedTeleport = teleportState.get(0);
                    justLaunchedTeleportStatus = false;
                }
                launchedTeleport = checkTeleportSerang(playerAction, bot, launchedTeleport, preyList, teleportState);
                System.out.println("launched teleport:  " + launchedTeleport + "     just launched teleport: "
                        + justLaunchedTeleportStatus);
                // if (headingTele != -1) {
                // headingTele = checkTeleportSerang(playerAction, headingTele, preyTracker,
                // playerState);
                // }

            }

            if (gameState.getWorld().getCurrentTick() != tick) {
                // prevState = gameState;
                tick = gameState.getWorld().getCurrentTick();
                System.out.println();
            }
            this.playerAction = playerAction;
        }
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream()
                .filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    public static double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public static int getHeadingBetween(GameObject bot, GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    public static int getHeadingBetween(GameObject bot, int x, int y) {
        var direction = toDegrees(Math.atan2(y - bot.getPosition().y, x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    public static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    public boolean launchTorpedo(GameObject bot, GameObject enemy, PlayerAction p,
            boolean attack, List<GameObject> ObjList) {
        if (enemy == null) {
            return false;
        }
        boolean condition1 = bot.getSize() > (attack ? 20 : 15);
        boolean condition2 = false;
        if (attack) {
            if (enemy.getSize() > 50) {
                condition2 = getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize() < 450;
            } else if (enemy.getSize() > 80) {
                condition2 = true;
            } else {
                condition2 = getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize() < 300
                        && enemy.getSize() > 20;
            }

        } else {
            condition2 = getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize() > 150;
        }

        boolean condition3 = bot.getTorpedoCount() > 0;

        int heading = getHeadingBetween(bot, enemy);
        List<GameObject> obstructObjList = ObjList.stream().filter(item -> getHeadingBetween(bot, item)
                + toDegrees(Math.atan2(item.getSize(), getDistanceBetween(bot, item))) > heading
                && getHeadingBetween(bot, item)
                        - toDegrees(Math.atan2(item.getSize(), getDistanceBetween(bot, item))) < heading
                && getDistanceBetween(bot, item) < getDistanceBetween(bot, enemy) &&
                (item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD
                        || item.getGameObjectType() == ObjectTypes.GAS_CLOUD
                                && item.getGameObjectType() == ObjectTypes.WORMHOLE))
                .collect(Collectors.toList());
        boolean condition4 = obstructObjList.size() < 2;

        boolean condition = condition1 && condition2 && condition3 && condition4;

        System.out.println("heading clear  (condition4): " + condition4 + "  obstruction count: " + obstructObjList.size());

        if (condition) {
            System.out
                    .println((attack ? "Attack with torpedo" : "Defend with torpedo") + "   bot size : " + bot.getSize()
                            + "    enemy size: "
                            + enemy.getSize() + "  distance : "
                            + (getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize()) + "   heading:  "
                            + getHeadingBetween(bot, enemy));
            p.action = PlayerActions.FIRETORPEDOES;
            p.heading = getHeadingBetween(bot, enemy);
        }

        // if (attack && condition) {
        // p.action = PlayerActions.FORWARD;
        // p.heading = getHeadingBetween(bot, enemy);
        // }

        return condition;
    }
}
