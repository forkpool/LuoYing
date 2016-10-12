/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.loader;

import java.util.ArrayList;
import java.util.List;
import name.huliqing.ly.data.GameData;
import name.huliqing.ly.data.GameLogicData;
import name.huliqing.ly.xml.Proto;
import name.huliqing.ly.data.SceneData;
import name.huliqing.ly.xml.DataFactory;
import name.huliqing.ly.xml.DataLoader;

/**
 * @author huliqing
 * @param <T>
 */
public class GameDataLoader<T extends GameData> implements DataLoader<T>{

    @Override
    public void load(Proto proto, T store) {
        SceneData sceneData = DataFactory.createData(store.getAsString("scene"));
        store.setSceneData(sceneData);
        
        String[] gameLogicsArr = proto.getAsArray("gameLogics");
        if (gameLogicsArr != null && gameLogicsArr.length > 0) {
            List<GameLogicData> gameLogics = new ArrayList<GameLogicData>(gameLogicsArr.length);
            store.setGameLogicDatas(gameLogics);
            for (String gla : gameLogicsArr) {
                gameLogics.add((GameLogicData) DataFactory.createData(gla));
            }
        }
        
//        store.setAvailableActors(store.getAsStringList("availableActors"));
    }
    
}