package Services;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import Enums.ObjectTypes;
import Enums.PlayerActions;
import Models.GameObject;
import Models.PlayerAction;
import Models.World;

public class GreedyCommand extends BotService {
  public static PlayerAction run(GameObject enemy, PlayerAction p, UUID objectTracker, GameObject bot,
      boolean switchDirection, int direction) {

    System.out.println("speed: " + bot.getSpeed() + "  size:  " + bot.getSize() + "  status: "
        + bot.getAfterburnerStatus() + " effect:  " + bot.getEffects() + "  distance: "
        + (BotService.getDistanceBetween(enemy, bot) - enemy.getSize() - bot.getSize()) + "   enemy size  :"
        + enemy.getSize() + "  objectType:  " + enemy.getGameObjectType());

    if (enemy.getGameObjectType() != ObjectTypes.TORPEDO_SALVO) {
      activateAfterburner(p, bot, enemy, false);
    }
    
    if (objectTracker == null || !(objectTracker == null ? UUID.randomUUID() : objectTracker).equals(enemy.getId())) {
      if (switchDirection) {
        p.heading = (enemy.currentHeading + direction) % 360;
      } else {
        p.heading = (enemy.currentHeading - direction) % 360;
      }
      System.out.println("bot heading :" + bot.getCurrentHeading() + "   danger heading: " + enemy.getCurrentHeading());
    }
    return p;
  }

  public static void activateAfterburner(PlayerAction p, GameObject bot, GameObject enemy, boolean attack) {
    if (attack) {
      if (bot.getAfterburnerStatus()) {
        if (bot.getSize() - enemy.getSize() < 20 || bot.getSize() < 30) {
          p.action = PlayerActions.STOPAFTERBURNER;
          p.heading = BotService.getHeadingBetween(bot, enemy);
        } else {
          p.action = PlayerActions.FORWARD;
          p.heading = BotService.getHeadingBetween(bot, enemy);
        }
      } else {
        if (bot.getSpeed() == 0) {
          p.action = PlayerActions.FORWARD;
          p.heading = BotService.getHeadingBetween(bot, enemy);
        } else if (bot.getSize() - enemy.getSize() > 20 && bot.getSize() > 40) {
          p.action = PlayerActions.STARTAFTERBURNER;
          p.heading = BotService.getHeadingBetween(bot, enemy);
        }
      }

    } else {
      // System.out.println(bot.getAfterburnerStatus());
      if (!bot.getAfterburnerStatus()) {
        if (bot.getSpeed() == 0) {
          // System.out.println("speed 0");
          p.action = PlayerActions.FORWARD;
        } else if (bot.getSize() > 30) {
          if (enemy.getGameObjectType() == ObjectTypes.PLAYER) {
            if (BotService.getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize() < 100
                && enemy.getSize() - bot.getSize() < 20) {
              p.action = PlayerActions.STARTAFTERBURNER;
            } else {
              p.action = PlayerActions.FORWARD;
            }
          } else {
            p.action = PlayerActions.STARTAFTERBURNER;
          }

        } else if (bot.getSize() < 20) {
          // System.out.println("kurang dari 20");
          p.action = PlayerActions.FORWARD;
        }
      } else {
        if (bot.getSize() < 20 && getDistanceBetween(bot, enemy) > 50) {
          p.action = PlayerActions.STOPAFTERBURNER;
        } else if (bot.getTorpedoCount() > 0 && bot.getSize() > 35) {
          System.out.println("fire torpedos while running");
          p.action = PlayerActions.FIRETORPEDOES;
          p.heading = getHeadingBetween(bot, enemy);
        } else {
          p.action = PlayerActions.FORWARD;
          p.heading = (getHeadingBetween(bot, enemy) + 120) % 360;
        }
      }
    }
  }

  public static UUID checkBorder(GameObject bot, World w, PlayerAction p, UUID objectTracker) {
    var condition = Math.sqrt(Math.pow(bot.getPosition().getX(), 2) + Math.pow(bot.getPosition().getY(), 2))
        + bot.getSize() + 40 > w.getRadius();
    if (condition) {
      System.out.println("Evade border   size: " + bot.getSize());
      p.heading = BotService.getHeadingBetween(bot, 0, 0);
      p.action = PlayerActions.FORWARD;
      return null;
    }
    return objectTracker;
  }

  public static UUID checkHarmfulObject(GameObject bot, List<GameObject> gameObj, PlayerAction p,
      ObjectTypes objectType, int treshold, String text, UUID objectTracker) {
    var ObjList = gameObj.stream()
        .filter(
            item -> item.getGameObjectType() == objectType)
        .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());
    if (ObjList.size() > 0) {
      var condition = BotService.getDistanceBetween(bot, ObjList.get(0)) - bot.getSize()
          - ObjList.get(0).getSize() < treshold;
      if (condition) {
        System.out.println(text + "  size: " + bot.getSize());
        p.heading = (BotService.getHeadingBetween(bot, ObjList.get(0)) + 180) % 360;
        p.action = PlayerActions.FORWARD;
        return null;
      }
    }
    return objectTracker;
  }

  public static UUID evadeObject(GameObject bot, List<GameObject> gameObj, World w, PlayerAction p,
      UUID objectTracker) {
    if (p.action == PlayerActions.FIRETORPEDOES || p.action == PlayerActions.FIRETELEPORT
        || p.action == PlayerActions.FIRESUPERNOVA) {
      return objectTracker;
    }
    // objectTracker = checkHarmfulObject(bot, gameObj, p,
    // ObjectTypes.ASTEROID_FIELD, 10, "evade asteroid field",
    // objectTracker);
    objectTracker = checkHarmfulObject(bot, gameObj, p, ObjectTypes.GAS_CLOUD, 20, "evade gas cloud", objectTracker);
    // objectTracker = checkHarmfulObject(bot, gameObj, p, ObjectTypes.WORMHOLE, 20,
    // "evade wormhole", objectTracker);
    objectTracker = checkBorder(bot, w, p, objectTracker);
    // switchDirection = !switchDirection;
    return objectTracker;
  }

  public static void catchFood(List<GameObject> allState, GameObject bot, PlayerAction p) {
    System.out.println("Cathing food     Size : " + bot.getSize());
    var foodList = allState.stream()
        .filter(
            item -> ((bot.getCurrentHeading() - getHeadingBetween(bot, item)) % 360 != 165 || (bot.getCurrentHeading() - getHeadingBetween(bot, item)) % 360 != 195) && item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPER_FOOD)
        .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());

    if (foodList.size() > 0) {
      System.out.print("food distance1 : " + BotService.getDistanceBetween(foodList.get(0), bot));
      if (foodList.size() > 1) {
        System.out.println("   distance2 : " + BotService.getDistanceBetween(foodList.get(1), bot));
      }
      p.heading = getHeadingBetween(bot, foodList.get(0));
      p.action = PlayerActions.FORWARD;
    } else {
      p.action = PlayerActions.FORWARD;
      p.heading = getHeadingBetween(bot, 0, 0);
    }

  }

  // public static boolean launchTorpedo(GameObject bot, GameObject enemy, int
  // distanceTreshold, PlayerAction p,
  // boolean attack) {
  // boolean condition1 = bot.getSize() > 35;
  // boolean condition2 = false;

  // if (!attack) {
  // condition2 = enemy != null ? getDistanceBetween(bot, enemy) - bot.getSize() -
  // enemy.getSize() < distanceTreshold
  // : false;
  // } else {
  // condition2 = getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize()
  // > distanceTreshold;
  // }

  // boolean condition3 = bot.getTorpedoCount() > 0;
  // boolean condition = condition1 && condition2 && condition3;

  // if (condition) {
  // System.out.println((attack ? "Attack with torpedo" : "Defend with torpedo") +
  // " bot size : " + bot.getSize()
  // + " enemy size: "
  // + enemy.getSize() + " distance : "
  // + (getDistanceBetween(bot, enemy) - bot.getSize() - enemy.getSize()) + "
  // heading: "
  // + getHeadingBetween(bot, enemy));
  // p.action = PlayerActions.FIRETORPEDOES;
  // p.heading = getHeadingBetween(bot, enemy);
  // }
  // return condition;
  // }

}
