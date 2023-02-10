package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class Radar {

  public static final int ALL_CLEAR = 0;
  public static final int TELEPORTDEFENCE = 1;
  public static final int SUPERNOVADEFENCE = 2;
  public static final int TORPEDODEFENCE = 3;

  public static int radar(GameState gameState, GameState prevState, GameObject bot) {
    if (prevState != null) {
      var prevSupernova = prevState.getGameObjects()
          .stream()
          .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB
              && (BotService.getDistanceBetween(bot, item) < 200))
          .collect(Collectors.toList());
      var currSupernova = gameState.getGameObjects()
          .stream()
          .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB
              && (BotService.getDistanceBetween(bot, item) < 200))
          .collect(Collectors.toList());
      for (int i = 0; i < prevSupernova.size(); i++) {
        for (int j = 0; j < currSupernova.size(); j++) {
          if (prevSupernova.get(i).getId() == currSupernova.get(j).getId()) {
            if ((prevSupernova.get(i).getPosition().x - bot.getPosition().x) != 0) {
              var prevGradien = BotService
                  .toDegrees(Math.atan2(prevSupernova.get(i).getPosition().y - bot.getPosition().y,
                      prevSupernova.get(i).getPosition().x - bot.getPosition().x));
              var curGradien = BotService
                  .toDegrees(Math.atan2(currSupernova.get(j).getPosition().y - bot.getPosition().y,
                      currSupernova.get(j).getPosition().x - bot.getPosition().x));
              if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
                return 2;
              }
            } else if ((prevSupernova.get(i).getPosition().y - bot.getPosition().y) != 0) {
              var prevGradien = BotService
                  .toDegrees(Math.atan2(prevSupernova.get(i).getPosition().x - bot.getPosition().x,
                      prevSupernova.get(i).getPosition().y - bot.getPosition().y));
              var curGradien = BotService
                  .toDegrees(Math.atan2(currSupernova.get(j).getPosition().x - bot.getPosition().x,
                      currSupernova.get(j).getPosition().y - bot.getPosition().y));
              if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
                return 2;
              }
            } else {
              return 0;
            }
          }
        }
      }

      var prevTele = prevState.getGameObjects()
          .stream()
          .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER
              && (BotService.getDistanceBetween(bot, item) < 120))
          .collect(Collectors.toList());
      var currentTele = gameState.getGameObjects()
          .stream()
          .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER
              && (BotService.getDistanceBetween(bot, item) < 120))
          .collect(Collectors.toList());
      for (int i = 0; i < prevTele.size(); i++) {
        for (int j = 0; j < currentTele.size(); j++) {
          if (prevTele.get(i).getId() == currentTele.get(j).getId()) {
            if ((prevTele.get(i).getPosition().x - bot.getPosition().x) != 0) {
              var prevGradien = BotService.toDegrees(Math.atan2(prevTele.get(i).getPosition().y - bot.getPosition().y,
                  prevTele.get(i).getPosition().x - bot.getPosition().x));
              var curGradien = BotService.toDegrees(Math.atan2(currentTele.get(j).getPosition().y - bot.getPosition().y,
                  currentTele.get(j).getPosition().x - bot.getPosition().x));
              if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
                return 1;
              }
            } else if ((prevTele.get(i).getPosition().y - bot.getPosition().y) != 0) {
              var prevGradien = BotService.toDegrees(Math.atan2(prevTele.get(i).getPosition().x - bot.getPosition().x,
                  prevTele.get(i).getPosition().y - bot.getPosition().y));
              var curGradien = BotService.toDegrees(Math.atan2(currentTele.get(j).getPosition().x - bot.getPosition().x,
                  currentTele.get(j).getPosition().y - bot.getPosition().y));
              if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
                return 1;
              }
            } else {
              return 0;
            }
          } else {
            return 0;
          }
        }
      }

      var prevTorpedo = prevState.getGameObjects()
          .stream()
          .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
              && (BotService.getDistanceBetween(bot, item) < 120))
          .collect(Collectors.toList());
      var currTorpedo = gameState.getGameObjects()
          .stream()
          .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
              && (BotService.getDistanceBetween(bot, item) < 120))
          .collect(Collectors.toList());
      for (int i = 0; i < prevTorpedo.size(); i++) {
        for (int j = 0; j < currTorpedo.size(); j++) {
          if (prevTorpedo.get(i).getId() == currTorpedo.get(j).getId()) {
            if ((prevTorpedo.get(i).getPosition().x - bot.getPosition().x) != 0) {
              var prevGradien = BotService
                  .toDegrees(Math.atan2(prevTorpedo.get(i).getPosition().y - bot.getPosition().y,
                      prevTorpedo.get(i).getPosition().x - bot.getPosition().x));
              var curGradien = BotService.toDegrees(Math.atan2(currTorpedo.get(j).getPosition().y - bot.getPosition().y,
                  currTorpedo.get(j).getPosition().x - bot.getPosition().x));
              if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
                return 3;
              }
            } else if ((prevTorpedo.get(i).getPosition().y - bot.getPosition().y) != 0) {
              var prevGradien = BotService
                  .toDegrees(Math.atan2(prevTorpedo.get(i).getPosition().x - bot.getPosition().x,
                      prevTorpedo.get(i).getPosition().y - bot.getPosition().y));
              var curGradien = BotService.toDegrees(Math.atan2(currTorpedo.get(j).getPosition().x - bot.getPosition().x,
                  currTorpedo.get(j).getPosition().y - bot.getPosition().y));
              if ((prevGradien - curGradien) < 0.05 || (prevGradien - curGradien) > -0.05) {
                return 3;
              }
            } else {
              return 0;
            }
          }
        }
      }
    } 
    return 0;
  }
}
