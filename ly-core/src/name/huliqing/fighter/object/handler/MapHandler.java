/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.object.handler;

import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.data.HandlerData;
import name.huliqing.fighter.data.ProtoData;
import name.huliqing.fighter.game.service.PlayService;
import name.huliqing.fighter.object.actor.Actor;
import name.huliqing.fighter.ui.FrameLayout;
import name.huliqing.fighter.ui.Icon;
import name.huliqing.fighter.ui.UI;
import name.huliqing.fighter.utils.ConvertUtils;

/**
 * @author huliqing
 */
public class MapHandler extends AbstractHandler {
    private final PlayService playService = Factory.get(PlayService.class);
    
    private String image;
    private List<Location> locations;
    
    //----inner
    private MapView mapView;
    
    @Override
    public void initData(HandlerData data) {
        super.initData(data); 
        this.image = data.getAttribute("image");
        String[] tempLocations = data.getAsArray("locations");
        if (tempLocations != null) {
            locations = new ArrayList<Location>(tempLocations.length);
            for (String s : tempLocations) {
                String[] tmp = s.split("\\|");
                Location loc = new Location();
                loc.id = tmp[0];
                loc.x = ConvertUtils.toFloat(tmp[1], 0);
                loc.y = ConvertUtils.toFloat(tmp[2], 0);
                loc.gameId = tmp.length > 3 ?  tmp[3] : null;
                loc.icon = tmp.length > 4 ? tmp[4] : null;
                locations.add(loc);
            }
        }
    }
    
    @Override
    protected void useObject(Actor actor, ProtoData data) {
        // test
//        locations.get(0).x = FastMath.nextRandomFloat();
//        locations.get(0).y = FastMath.nextRandomFloat();

        // 创建一个map view用于显示地图
        if (mapView == null) {
            mapView = new MapView(image, locations);
            mapView.setToCorner(UI.Corner.CC);
        }
        
        // 显示当前玩家位置及方向
        Flag playerFlag = createFlag(playService.getPlayer(), "place1", true, true);
        mapView.mapContainer.addFlag(playerFlag);
        playService.addObjectGui(mapView);
    }
    
    private Flag createFlag(Actor actor, String locationId, boolean showDirection, boolean focus) {
        Flag flag = new Flag(actor.getData().getUniqueId(), "Interface/map/flag.png");
        
        // 计算位置
        float mw = mapView.mapContainer.getWidth();
        float mh = mapView.mapContainer.getHeight();
        float x = 0;
        float y = 0;
        // 如果提供了locationId，则直接定位到locationId在地图上的位置.
        // 否则将角色的当前世界世界位置转化到地图上的位置
        if (locationId != null) {
            if (mapView.mapContainer.locations != null) {
                for (Location loc : mapView.mapContainer.locations) {
                    if (locationId.equals(loc.id)) {
                        x = mw * loc.x;
                        y = mh * loc.y;
                        break;
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("需要根据地图的实际大小来计算比例,然后计算实际位置");
            // 需要根据地图的实际大小来计算比例,可用WorldBound来计算
//            Vector3f worldPos = actor.getLocation();
//            x = mw * 0.5f + worldPos.x;
//            y = mh * 0.5f + worldPos.z;
        }
        flag.setLocalTranslation(x, y, 0);
        
        // 计算方向，将角色的视角方向（ViewDirection)转化为地图上的方向，注意：用于标示方向的
        // 图标(icon)的初始方向必须是向下的, 即在GUI上的初始指向是: (0,-1,0)。
        if (showDirection) {
           
            // 把角色的世界视角方向转化到GUI上的方向.
            // 1.将角色视角在世界上的x,z方向转化为GUI上的x,y
            TempVars tv = TempVars.get();
            Vector3f dir = tv.vect1;
            dir.set(actor.getViewDirection()).setY(0).normalizeLocal();
            dir.y = -dir.z;
            dir.z = 0;
            
            // 2.根据与GUI上的flag icon的初始方向(0,-1,0)进行比较，计算出夹角，再根据夹角计算出旋转角度
            Vector3f flagDir = tv.vect2.set(0, -1, 0);
            float angle = dir.angleBetween(flagDir);
            Quaternion rot = flag.getLocalRotation();
            if (dir.x > 0) {
                rot.fromAngleAxis(angle, Vector3f.UNIT_Z);
            } else {
                rot.fromAngleAxis(-angle, Vector3f.UNIT_Z);
            }
            flag.setLocalRotation(rot);
            
            tv.release();
        }
        
        if (focus) {
            float sw = playService.getScreenWidth();
            float sh = playService.getScreenHeight();
            float offsetX = sw * 0.5f - x ;
            float offsetY = sh * 0.5f - y;
            Vector3f loc = mapView.mapContainer.getLocalTranslation();
            loc.addLocal(offsetX, offsetY, 0);
            // 把当前点移动到中央，然后再fixPosition，避免地图出边界
            mapView.mapContainer.setLocalTranslation(loc);
            mapView.mapContainer.fixPosition();
        }
        
        return flag;
    }
    
    private class Location {
        public String id;
        public float x;
        public float y;
        public String gameId;
        // 可以为地图上的点定义一个图标
        public String icon;
    }
    
    /**
     * MapView封装整个地图，并且始终和整个屏幕的大小完全匹配.
     * 其中mapContainer封装基本地图及各个地图坐标点信息
     */
    private class MapView extends FrameLayout {
        
        // 存放基本地图及各个地图坐标点,
        private MapContainer mapContainer;
        // 关闭按钮
        private Icon close;
        
        public MapView(String image, List<Location> locations) {
            super();
            this.width = playService.getScreenWidth();
            this.height = playService.getScreenHeight();
            
            mapContainer = new MapContainer(image, locations);
            mapContainer.setDragEnabled(true);
            
            // close button
            close = new Icon();
            close.setImage("Interface/map/close.png");
            close.setWidth(32);
            close.setHeight(32);
            close.addClickListener(new Listener() {
                @Override
                public void onClick(UI view, boolean isPressed) {
                    if (isPressed) return;
                    MapView.this.removeFromParent();
                }
            });
            
            addView(mapContainer);
            addView(close);
        }

        @Override
        protected void updateViewLayout() {
            super.updateViewLayout(); 
            close.setToCorner(Corner.RT);
        }
        
    }
    
    // MapContainer包含基本地图及坐标点
    private class MapContainer extends FrameLayout {
        
        private List<Location> locations;
        private Map<Long, Flag> flags;

        public MapContainer(String baseMapImage, List<Location> locations) {
            this.locations = locations;
            // 基本地图,地图的宽度、高度和图片一致
            Icon map = new Icon();
            map.setImage(image);
            setWidth(map.getWidth());
            setHeight(map.getHeight());
            addView(map);
            
            // 添加各种地图坐标点
            Location loc;
            float iconw = 16;
            float iconh = 16;
            float btnw = 32;
            float btnh = 32;
            for (int i = 0; i < locations.size(); i++) {
                loc = locations.get(i);
                float posX = width * loc.x;
                float posY = height * loc.y;
                
                // 添加特殊的地图点图标。
                if (loc.icon != null) {
                    Icon icon = new Icon();
                    icon.setImage(loc.icon);
                    icon.setWidth(iconw);
                    icon.setHeight(iconh);
                    icon.setPosition(posX - iconw * 0.5f, posY - iconh * 0.5f);
                }
                
                // 添加一个按钮，点击后可显示对于当前的各种命令，如："传送"之类
                // 这个按钮正常情况下是隐藏的，只是作为触发事件用，不用于显示。
                // 显示交由loc.icon配置
                Icon locBtn = new Icon();
                locBtn.setImage("Interface/map/location.jpg");
                locBtn.setWidth(btnw);
                locBtn.setHeight(btnh);
                locBtn.setPosition(posX - btnw * 0.5f,  posY - btnh * 0.5f);
                // 让按钮颜色变透明以隐藏,不能使用cullHint，否则无法触发事件
                locBtn.getMaterial().getParam("Color").setValue(new ColorRGBA(1,1,1,0.0f));
                locBtn.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
                addView(locBtn);
            }
        }
        
        public void addFlag(Flag flag) {
            if (flags == null) {
                flags = new HashMap<Long, Flag>();
            }
            // 移除旧的
            if (flags.containsKey(flag.id)) {
                removeView(flags.get(flag.id));
            }
            flags.put(flag.id, flag);
            addView(flag);
        }
        
        public Flag getFlag(long id) {
            if (flags == null)
                return null;
            return flags.get(id);
        }
        
        @Override
        protected void onDragMove(float xMoveAmount, float yMoveAmount) {
            super.onDragMove(xMoveAmount, yMoveAmount);
            fixPosition();
        }
        
        private void fixPosition() {
            // 修正位置，不要让地图拖动的时候出边界
            float sw = playService.getScreenWidth();
            float sh = playService.getScreenHeight();
            
            Vector3f loc = getLocalTranslation();
            float l = loc.x;
            float r = loc.x + width;
            float b = loc.y;
            float t = loc.y + height;
            
            if (width < sw) {
                loc.x = (sw - width) * 0.5f;
            } else {
                if (l > 0) {
                    loc.x = 0;
                }
                if (r < sw) {
                    loc.x = sw - width;
                }
            }
            
            if (height < sh) {
                loc.y = (sh - height) * 0.5f;
            } else {
                if (b > 0) {
                    loc.y = 0;
                }
                if (t <  sh) {
                    loc.y = sh - height;
                }                
            }
            setPosition(loc.x, loc.y);
        }
        
    }
    
    // 用于标记地图上的特殊位置点,如玩家位置
    private class Flag extends FrameLayout {
        private long id;
        public Flag(long id, String image) {
            this.id = id;
            Icon icon = new Icon();
            icon.setImage(image);
            icon.setWidth(32);
            icon.setHeight(32);
            icon.setLocalTranslation(-0.5f * icon.getWidth(), -0.5f * icon.getHeight(), 0);
            addView(icon);
        }
    }
}