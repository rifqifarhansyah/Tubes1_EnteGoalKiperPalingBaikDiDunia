package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class GreedyCommand extends BotService {
  public static PlayerActions activateAfterburner(List<GameObject> playerList, double distance, double distanceToAnotherEnemy, boolean attack) {
    var ourShip = playerList.get(0);
    var enemyShip = playerList.get(1);

    if (attack) {
      var condition1 = ourShip.getSize() > 50;
      var condition2 = ourShip.getSize() - (distance / 2*2*(200/ourShip.getSize())) > enemyShip.getSize();
      var condition3 = ourShip.getSize() - (distance / 2*2*(200/ourShip.getSize())) > 35;
      var condition4 = distanceToAnotherEnemy + enemyShip.getSize() >= 100;
  
      if (condition1 && condition2 && condition3 && condition4) {
        return PlayerActions.STARTAFTERBURNER;
      } else {
        return PlayerActions.STOP;
      }
    } else {
      if (ourShip.getSize() > 30) {
        return PlayerActions.STARTAFTERBURNER;
      } else {
        return PlayerActions.STOP;
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
