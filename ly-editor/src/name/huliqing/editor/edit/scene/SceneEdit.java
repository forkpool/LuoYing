/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.edit.scene;

import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.editor.Editor;
import name.huliqing.editor.edit.SimpleJmeEdit;
import name.huliqing.editor.edit.select.EntitySelectObj;
import name.huliqing.editor.manager.SelectObjManager;
import name.huliqing.editor.toolbar.EditToolbar;
import name.huliqing.editor.undoredo.UndoRedo;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.constants.IdConstants;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.luoying.manager.PickManager;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.game.Game;
import name.huliqing.luoying.object.scene.Scene;
import name.huliqing.luoying.object.scene.SceneListener;

/**
 *
 * @author huliqing
 */
public class SceneEdit extends SimpleJmeEdit<EntitySelectObj> implements SceneListener {

    private static final Logger LOG = Logger.getLogger(SceneEdit.class.getName());
    private final PlayService playService = Factory.get(PlayService.class);

    private Game game;
    private Scene scene;
    private boolean sceneLoaded;
    
    private final Map<EntityData, EntitySelectObj> objMap = new LinkedHashMap<EntityData, EntitySelectObj>();
    
    public SceneEdit() {}
    
    @Override
    public void initialize(Editor editor) {
        super.initialize(editor);
        setToolbar(new EditToolbar());
    }
    
    @Override
    public void cleanup() {
        if (game != null && game.isInitialized()) {
            game.cleanup();
            game = null;
        }
        sceneLoaded = false;
        getEditRoot().detachAllChildren();
        super.cleanup(); 
    }
    
    public Scene getScene() {
        return scene;
    }
    
    public void setScene(Scene newScene) {
        if (game != null) {
            game.cleanup();
            game = null;
            objMap.clear();
        }
        sceneLoaded = false;
        scene = newScene;
        scene.addSceneListener(this);
        
        game = Loader.load(IdConstants.SYS_GAME);
        playService.registerGame(game); // game.initialize必须先调用
        
        game.setScene(scene);
        game.initialize(editor);
        getEditRoot().attachChild(game.getScene().getRoot());
    }

    @Override
    public EntitySelectObj doPick(Ray ray) {
        if (scene == null)
            return null;
        
        EntitySelectObj result = null;
        float minDistance = Float.MAX_VALUE;
        Float temp;
        for (EntitySelectObj seo : objMap.values()) {
            temp = seo.distanceOfPick(ray);
            if (temp != null && temp < minDistance) {
                minDistance = temp;
                result = seo;
            }
        }
        if (result != null) {
            LOG.log(Level.INFO, "doPick, selectObj={0}", result.getObject().getData().getId());
        }
        return result;
    }

    @Override
    public void onSceneLoaded(Scene scene) {
        // 先预生成SelectObjs
        List<Entity> entities = scene.getEntities();
        if (entities != null) {
            entities.stream().filter(t -> t != null).forEach(t -> {
                EntitySelectObj eso = SelectObjManager.createSelectObj(t.getData().getTagName());
                objMap.put(t.getData(), eso);
                eso.setObject(t);
                eso.initialize(this);
            });
        }
        sceneLoaded = true;
    }

    @Override
    public void onSceneEntityAdded(Scene scene, Entity entityAdded) {}
    
    @Override
    public void onSceneEntityRemoved(Scene scene, Entity entityRemoved) {}
    
    public void setSelected(EntityData entityData) {
        EntitySelectObj eso = objMap.get(entityData);
        if (eso != null) {
            setSelected(eso);
        }
    }
    
    /**
     * 重装载入指定的Entity
     * @param entityData 
     */
    public void reloadEntity(EntityData entityData) {
        if (!sceneLoaded)
            return;
        EntitySelectObj eso = objMap.get(entityData);
        if (eso != null) {
            eso.getObject().cleanup();
            eso.getObject().setData(entityData);
            eso.getObject().initialize();
            eso.getObject().onInitScene(scene);
        }
    }  
    
    public void addEntity(EntityData ed) {
        if (!sceneLoaded)
            return;
        if (objMap.containsKey(ed)) {
            return;
        }
        Entity entity = Loader.load(ed);
        scene.addEntity(entity);
        EntitySelectObj eso = SelectObjManager.createSelectObj(ed.getTagName());
        objMap.put(ed, eso);
        eso.setObject(entity);
        eso.initialize(this);
        addUndoRedo(new EntityAddedUndoRedo(eso));
    }
    
    public void addEntityOnCursor(EntityData ed, Vector2f cursorPos) {
        if (!sceneLoaded)
            return;
        if (objMap.containsKey(ed)) {
            return;
        }
        Vector3f loc = PickManager.pick(editor.getCamera(), cursorPos, editRoot);
        if (loc != null) {
            ed.setLocation(loc);
        }
        Entity entity = Loader.load(ed);
        scene.addEntity(entity);
        EntitySelectObj eso = SelectObjManager.createSelectObj(ed.getTagName());
        objMap.put(ed, eso);
        eso.setObject(entity);
        eso.initialize(this);
        addUndoRedo(new EntityAddedUndoRedo(eso));
    }
    
    public boolean removeEntity(EntityData ed) {
        if (!sceneLoaded) 
            return false;
        EntitySelectObj eso = objMap.get(ed);
        if (eso == null) {
            return false;
        }
        eso.getObject().updateDatas();
        if (eso.isInitialized()) {
            eso.cleanup();
        }
        scene.removeEntity(eso.getObject());
        objMap.remove(ed);
        addUndoRedo(new EntityRemovedUndoRedo(eso));
        return true;
    }
    
    private class EntityAddedUndoRedo implements UndoRedo {
        private final EntitySelectObj eso;
        public EntityAddedUndoRedo(EntitySelectObj eso) {
            this.eso = eso;
        }
        
        @Override
        public void undo() {
            eso.getObject().updateDatas();
            if (eso.isInitialized()) {
                eso.cleanup();
            }
            objMap.remove(eso.getObject().getData());
            scene.removeEntity(eso.getObject());
        }

        @Override
        public void redo() {
            eso.getObject().setData(eso.getObject().getData()); // 重新更新一次数据，优先
            scene.addEntity(eso.getObject());
            eso.initialize(SceneEdit.this);
            objMap.put(eso.getObject().getData(), eso);
        }
        
    }
    
    private class EntityRemovedUndoRedo implements UndoRedo {

        private final EntitySelectObj eso;
        
        public EntityRemovedUndoRedo(EntitySelectObj eso) {
            this.eso = eso;
        }
        
        @Override
        public void undo() {
            eso.getObject().setData(eso.getObject().getData()); // 重新更新一次数据，优先
            scene.addEntity(eso.getObject());
            eso.initialize(SceneEdit.this);
            objMap.put(eso.getObject().getData(), eso);
        }

        @Override
        public void redo() {
            eso.getObject().updateDatas();
            if (eso.isInitialized()) {
                eso.cleanup();
            }
            objMap.remove(eso.getObject().getData());
            scene.removeEntity(eso.getObject());
        }
        
    }
}