package Services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import Enums.ObjectTypes;
import Models.GameObject;
import Models.GameState;

public class Radar {
  public boolean supernovaDefend;
  public boolean teleportDefend;
  public boolean torpedoDefend;
  public boolean playerDefend;

  public static final int ALL_CLEAR = 0;
  public static final int TELEPORTDEFENCE = 1;
  public static final int SUPERNOVADEFENCE = 2;
  public static final int TORPEDODEFENCE = 3;

  public Radar() {
    supernovaDefend = false;
    teleportDefend = false;
    torpedoDefend = false;
    playerDefend = false;
  }

  public List<GameObject> getCloseGameObjects(GameState gameState, GameObject bot, ObjectTypes objectType,
      int distance) {
    // Mendapatkan semua gameObject dalam radius distance
    if (objectType == ObjectTypes.PLAYER) {
      var allPlayer = gameState.getPlayerGameObjects()
          .stream()
          .filter(item -> item.getGameObjectType() == objectType
              && (BotService.getDistanceBetween(bot, item) - bot.getSize() - item.getSize() < distance))
          .sorted(Comparator.comparing(item -> BotService.getDistanceBetween(bot, item)))
          .collect(Collectors.toList());
      if (allPlayer.size() > 1) {
        return allPlayer.subList(1, allPlayer.size());
      } else {
        return null;
      }

    } else {
      return gameState.getGameObjects()
          .stream()
          .filter(item -> item.getGameObjectType() == objectType
              && (BotService.getDistanceBetween(bot, item) < (distance + bot.getSize() + item.getSize())))
          .collect(Collectors.toList());
    }

  }

  // public static int checkObjects(List<GameObject> prevState, List<GameObject>
  // currentState, GameObject bot, int returnValue) {
  // for (int i = 0; i < prevState.size(); i++) {
  // for (int j = 0; j < currentState.size(); j++) {
  // if (prevState.get(i).getId() == currentState.get(j).getId()) {
  // if ((prevState.get(i).getPosition().x - bot.getPosition().x) != 0) {
  // var prevGradient =
  // BotService.toDegrees(Math.atan2(prevState.get(i).getPosition().y -
  // bot.getPosition().y,
  // prevState.get(i).getPosition().x - bot.getPosition().x));
  // var curGradient =
  // BotService.toDegrees(Math.atan2(currentState.get(j).getPosition().y -
  // bot.getPosition().y,
  // currentState.get(j).getPosition().x - bot.getPosition().x));
  // if ((prevGradient - curGradient) < 0.05 || (prevGradient - curGradient) >
  // -0.05) {
  // return returnValue;
  // }
  // } else if ((prevState.get(i).getPosition().y - bot.getPosition().y) != 0) {
  // var prevGradient =
  // BotService.toDegrees(Math.atan2(prevState.get(i).getPosition().x -
  // bot.getPosition().x,
  // prevState.get(i).getPosition().y - bot.getPosition().y));
  // var curGradient =
  // BotService.toDegrees(Math.atan2(currentState.get(j).getPosition().x -
  // bot.getPosition().x,
  // currentState.get(j).getPosition().y - bot.getPosition().y));
  // if ((prevGradient - curGradient) < 0.05 || (prevGradient - curGradient) >
  // -0.05) {
  // return returnValue;
  // }
  // } else {
  // return 0;
  // }
  // }
  // }
  // }
  // return 0;
  // }

  public GameObject checkObjectsNextVersion(List<GameObject> closeObjects, GameObject bot, int vs) {
    List<GameObject> objectList = new ArrayList<>();
    if (closeObjects != null) {
      for (int i = 0; i < closeObjects.size(); i++) {
        double objDistance = BotService.getDistanceBetween(closeObjects.get(i), bot);
        double warningTreshold = BotService.toDegrees(Math.atan2(bot.getSize() + 10, objDistance));
        double objDegree = BotService.getHeadingBetween(closeObjects.get(i), bot);
        boolean warningStatus = closeObjects.get(i).getCurrentHeading() > (objDegree - warningTreshold) &&
            closeObjects.get(i).getCurrentHeading() < (objDegree + warningTreshold);

        if (closeObjects.get(i).getGameObjectType() == ObjectTypes.PLAYER) {
          if (closeObjects.get(i).getSize() > bot.getSize() && warningStatus) {
            objectList.add(closeObjects.get(i));
          }
        } else {
          if (closeObjects.get(i).gameObjectType == ObjectTypes.SUPERNOVA_BOMB) {
            warningTreshold = Math.atan2(bot.getSize() + 480, objDistance);
            warningStatus = closeObjects.get(i).getCurrentHeading() > (objDegree - warningTreshold)
                && closeObjects.get(i).getCurrentHeading() < (objDegree + warningTreshold);
          }
          if (warningStatus) {
            // if (closeObjects.get(i).getGameObjectType() == ObjectTypes.TORPEDO_SALVO) {
            //   System.out.println("heading: ");
            // }
            // System.out.println("vm: " + vm + " > vs: " + vs*Math.cos(radian));
            objectList.add(closeObjects.get(i));
          }

        }
      }
    } else {
      return null;
    }

    // System.out.println("test");

    if (objectList.size() > 0) {
      var newObjList = objectList.stream()
          .sorted(Comparator.comparing(item -> BotService.getDistanceBetween(bot, item)))
          .collect(Collectors.toList());
      return newObjList.get(0);
    } else {
      return null;
    }
  }

  public GameObject checkRadar(GameState gameState, GameObject bot) {
    if (gameState != null) {
      var currSupernova = getCloseGameObjects(gameState, bot, ObjectTypes.SUPERNOVA_BOMB, 1000);
      var currTele = getCloseGameObjects(gameState, bot, ObjectTypes.TELEPORTER, 250);
      var currTorpedo = getCloseGameObjects(gameState, bot, ObjectTypes.TORPEDO_SALVO, 500);
      var currPlayer = getCloseGameObjects(gameState, bot, ObjectTypes.PLAYER, 230);

      var danger = checkObjectsNextVersion(currSupernova, bot, 40);
      if (danger != null) {
        supernovaDefend = true;
        return danger;
      }
      danger = checkObjectsNextVersion(currTele, bot, 20);
      if (danger != null) {
        teleportDefend = true;
        return danger;
      }
      danger = checkObjectsNextVersion(currTorpedo, bot, 20);
      if (danger != null) {
        torpedoDefend = true;
        return danger;
      }
      danger = checkObjectsNextVersion(currPlayer, bot, 20);
      if (danger != null) {
        playerDefend = true;
        return danger;
      }
    }
    return null;
  }
  // if (prevState != null) {
  // var prevSupernova = prevState.getGameObjects()
  // .stream()
  // .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB
  // && (BotService.getDistanceBetween(bot, item) < 200))
  // .collect(Collectors.toList());
  // var currSupernova = gameState.getGameObjects()
  // .stream()
  // .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB
  // && (BotService.getDistanceBetween(bot, item) < 200))
  // .collect(Collectors.toList());
  // for (int i = 0; i < prevSupernova.size(); i++) {
  // for (int j = 0; j < currSupernova.size(); j++) {
  // if (prevSupernova.get(i).getId() == currSupernova.get(j).getId()) {
  // if ((prevSupernova.get(i).getPosition().x - bot.getPosition().x) != 0) {
  // var prevGradien = BotService
  // .toDegrees(Math.atan2(prevSupernova.get(i).getPosition().y -
  // bot.getPosition().y,
  // prevSupernova.get(i).getPosition().x - bot.getPosition().x));
  // var curGradien = BotService
  // .toDegrees(Math.atan2(currSupernova.get(j).getPosition().y -
  // bot.getPosition().y,
  // currSupernova.get(j).getPosition().x - bot.getPosition().x));
  // if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05)
  // {
  // return 2;
  // }
  // } else if ((prevSupernova.get(i).getPosition().y - bot.getPosition().y) != 0)
  // {
  // var prevGradien = BotService
  // .toDegrees(Math.atan2(prevSupernova.get(i).getPosition().x -
  // bot.getPosition().x,
  // prevSupernova.get(i).getPosition().y - bot.getPosition().y));
  // var curGradien = BotService
  // .toDegrees(Math.atan2(currSupernova.get(j).getPosition().x -
  // bot.getPosition().x,
  // currSupernova.get(j).getPosition().y - bot.getPosition().y));
  // if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05)
  // {
  // return 2;
  // }
  // } else {
  // return 0;
  // }
  // }
  // }
  // }

  // var prevTele = prevState.getGameObjects()
  // .stream()
  // .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER
  // && (BotService.getDistanceBetween(bot, item) < 120))
  // .collect(Collectors.toList());
  // var currentTele = gameState.getGameObjects()
  // .stream()
  // .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER
  // && (BotService.getDistanceBetween(bot, item) < 120))
  // .collect(Collectors.toList());
  // for (int i = 0; i < prevTele.size(); i++) {
  // for (int j = 0; j < currentTele.size(); j++) {
  // if (prevTele.get(i).getId() == currentTele.get(j).getId()) {
  // if ((prevTele.get(i).getPosition().x - bot.getPosition().x) != 0) {
  // var prevGradien =
  // BotService.toDegrees(Math.atan2(prevTele.get(i).getPosition().y -
  // bot.getPosition().y,
  // prevTele.get(i).getPosition().x - bot.getPosition().x));
  // var curGradien =
  // BotService.toDegrees(Math.atan2(currentTele.get(j).getPosition().y -
  // bot.getPosition().y,
  // currentTele.get(j).getPosition().x - bot.getPosition().x));
  // if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05)
  // {
  // return 1;
  // }
  // } else if ((prevTele.get(i).getPosition().y - bot.getPosition().y) != 0) {
  // var prevGradien =
  // BotService.toDegrees(Math.atan2(prevTele.get(i).getPosition().x -
  // bot.getPosition().x,
  // prevTele.get(i).getPosition().y - bot.getPosition().y));
  // var curGradien =
  // BotService.toDegrees(Math.atan2(currentTele.get(j).getPosition().x -
  // bot.getPosition().x,
  // currentTele.get(j).getPosition().y - bot.getPosition().y));
  // if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05)
  // {
  // return 1;
  // }
  // } else {
  // return 0;
  // }
  // } else {
  // return 0;
  // }
  // }
  // }

  // for(int i=0; i<prevTele.size(); i++){
  // for(int j=0; j<currentTele.size(); j++){
  // if(prev)
  // }
  // }

  // var prevTorpedo = prevState.getGameObjects()
  // .stream()
  // .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
  // && (BotService.getDistanceBetween(bot, item) < 120))
  // .collect(Collectors.toList());
  // var currTorpedo = gameState.getGameObjects()
  // .stream()
  // .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
  // && (BotService.getDistanceBetween(bot, item) < 120))
  // .collect(Collectors.toList());
  // for (int i = 0; i < prevTorpedo.size(); i++) {
  // for (int j = 0; j < currTorpedo.size(); j++) {
  // if (prevTorpedo.get(i).getId() == currTorpedo.get(j).getId()) {
  // if ((prevTorpedo.get(i).getPosition().x - bot.getPosition().x) != 0) {
  // var prevGradien = BotService
  // .toDegrees(Math.atan2(prevTorpedo.get(i).getPosition().y -
  // bot.getPosition().y,
  // prevTorpedo.get(i).getPosition().x - bot.getPosition().x));
  // var curGradien =
  // BotService.toDegrees(Math.atan2(currTorpedo.get(j).getPosition().y -
  // bot.getPosition().y,
  // currTorpedo.get(j).getPosition().x - bot.getPosition().x));
  // if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05)
  // {
  // return 3;
  // }
  // } else if ((prevTorpedo.get(i).getPosition().y - bot.getPosition().y) != 0) {
  // var prevGradien = BotService
  // .toDegrees(Math.atan2(prevTorpedo.get(i).getPosition().x -
  // bot.getPosition().x,
  // prevTorpedo.get(i).getPosition().y - bot.getPosition().y));
  // var curGradien =
  // BotService.toDegrees(Math.atan2(currTorpedo.get(j).getPosition().x -
  // bot.getPosition().x,
  // currTorpedo.get(j).getPosition().y - bot.getPosition().y));
  // if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05)
  // {
  // return 3;
  // }
  // } else {
  // return 0;
  // }
  // }
  // }
  // }
  // }

}
