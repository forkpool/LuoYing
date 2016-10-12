/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.mess;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import name.huliqing.ly.Factory;
import name.huliqing.ly.layer.network.PlayNetwork;
import name.huliqing.ly.layer.service.PlayService;
import name.huliqing.ly.data.ConnData;
import name.huliqing.ly.network.GameServer;
import name.huliqing.ly.object.entity.Entity;

/**
 * 客户端向服务端发出自动攻击命令
 * @author huliqing
 */
@Serializable
public class MessAutoAttack extends MessBase {

    // 被攻击的角色id,-1表示没有攻击目标，但是让角色转入自动攻击状态
    private long targetId = -1;
    
    public MessAutoAttack() {}

    /**
     * 获取攻击目标
     * @return 
     */
    public long getTargetId() {
        return targetId;
    }

    /**
     * 设置被攻击的角色id,如果设置为-1，表示没有攻击目标，但是让角色转入自动攻击状态
     * @param targetId 
     */
    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    @Override
    public void applyOnServer(GameServer gameServer, HostedConnection source) {
        PlayService playService = Factory.get(PlayService.class);
        PlayNetwork playNetwork = Factory.get(PlayNetwork.class);
        
        // remove20160615
//        Long actorId = source.getAttribute(GameServer.ATTR_ACTOR_UNIQUE_ID);

        ConnData cd = source.getAttribute(ConnData.CONN_ATTRIBUTE_KEY);
        Long actorId = cd != null ? cd.getEntityId() : null;
        
        Entity actor = playService.getEntity(actorId);
        Entity target = playService.getEntity(targetId);
        playNetwork.attack(actor, target);
    }

}