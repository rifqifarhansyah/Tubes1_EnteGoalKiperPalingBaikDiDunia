package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class GreedyCommand extends BotService {
  public static UUID run(GameObject enemy, PlayerAction p, UUID objectTracker, GameObject bot) {
    
    activateAfterburner(p, bot, enemy, false);

    if (objectTracker == null || !(objectTracker == null? UUID.randomUUID() : objectTracker).equals(enemy.getId()) ) {
        p.heading = (enemy.currentHeading + 90) % 360;    
        System.out.println(enemy.currentHeading + "   " +p.heading);
        // System.out.println(commandTracker + "   " +  );
    }

    return enemy.getId();
    // return p;
  }


  public static void activateAfterburner(PlayerAction p, GameObject bot, GameObject enemy, boolean attack ) {
    if (attack) {

    } else {
      if (bot.getAfterburnerStatus()) {
        p.action = PlayerActions.FORWARD;
      } else {
        if (bot.getSize() > 30 && bot.getSpeed() != 0) {
          p.action = PlayerActions.STARTAFTERBURNER;
        } else if (bot.getSize() < 20) {
          p.action = PlayerActions.STOPAFTERBURNER;
        }
      }

    }
  }

  // public static PlayerActions deactivateAfterburner(List<GameObject> playerList0,List<GameObject> playerList1, double distance, double distanceToAnotherEnemy, boolean attack) {
  //   var ourShip = playerList0.get(0);
  //   var enemyShip = playerList0.get(1);    

  //   if (attack) {
  //     if (ourShip.getSize() < 40) return PlayerActions.STOPAFTERBURNER;
  //     if (ourShip.getSize() < enemyShip.getSize()) return PlayerActions.STOPAFTERBURNER;
  //   }
  //   else {
  //     if (ourShip.getSize() < 20) return PlayerActions.STOPAFTERBURNER;
  //   }

  //     // jika UID musuh dari playerList1 tidak ada di playerList0 (musuh mati) -> matiin
  //     // jika UID musuh dari playerList0 makin jauh di playerList1 -> matiin 
  //     // else return PlayerActions.STOP;
    
  // }

  
  public static int catchFoodHeading(List<GameObject> allState, GameObject bot) {
    var foodList = allState.stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
    return getHeadingBetween(bot, foodList.get(0));
  }

  public static PlayerActions catchFoodAction() {
    return PlayerActions.FORWARD;
  }

}
