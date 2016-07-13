package name.huliqing.fighter;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.imageio.ImageIO;
import name.huliqing.fighter.game.service.ConfigService;
import name.huliqing.fighter.loader.data.ObjectLoader;
import name.huliqing.fighter.game.state.FpsState;
import name.huliqing.fighter.game.state.PlayState;
import name.huliqing.fighter.game.state.lan.LanState;
import name.huliqing.fighter.game.state.start.StartState;
import name.huliqing.fighter.object.DataFactory;
import name.huliqing.fighter.ui.UIConfig;
import name.huliqing.fighter.ui.UIFactory;
import name.huliqing.fighter.ui.UIConfigImpl;
import name.huliqing.fighter.utils.AdUtils;
import name.huliqing.fighter.utils.AdUtils.AdType;
import name.huliqing.fighter.utils.ThreadHelper;
import name.huliqing.fighter.ui.state.UIState;
import name.huliqing.fighter.utils.FileUtils;

/**
 * @author huliqing
 */
public class Fighter extends SimpleApplication {

    public static void main(String[] args) {
        final String SETTINGS_KEY = "luoying-settings";
        AppSettings settings = new AppSettings(true);
        
//        settings.setResolution(960, 540);     // HUAWEI G6-U00
//        settings.setResolution(1280, 800);    // GT N8010
//        settings.setResolution(1280, 720);    // 高清
//        settings.setResolution(1920, 1080);   // 超清
      
        settings.setTitle("落樱之剑");
        // 使用frameRate限制会多出一个线程
        settings.setSamples(4);
        settings.setFrameRate(60);
        settings.setIcons(createIcons());
        settings.setSettingsDialogImage("/data/SDImage.jpg");
        
//        // 载入上次的设置来覆盖
//        try {
//            settings.load(SETTINGS_KEY);
//        } catch (BackingStoreException ex) {
//            Logger.getLogger(Fighter.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        Fighter app = new Fighter();
        app.setSettings(settings);
        app.setShowSettings(true);
        app.setPauseOnLostFocus(false);
        app.start();
        
        // 必须在start后才把settings保存下来。
        try {
            settings.save(SETTINGS_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger(Fighter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static Object[] createIcons() {
        try {
            BufferedImage[] imgs = new BufferedImage[3];
            imgs[0] = ImageIO.read(FileUtils.readFile("/data/ly16.png"));
            imgs[1] = ImageIO.read(FileUtils.readFile("/data/ly32.png"));
            imgs[2] = ImageIO.read(FileUtils.readFile("/data/ly128.png"));
            return imgs;
        } catch (IOException e) {
            Logger.getLogger(Fighter.class.getName()).log(Level.WARNING
                    , "Could not load logo images! error={0}", e.getMessage());
        }
        return null;
    }
    
    // Debug state
    private final StartState startState = new StartState(this);
    // Debug view
    private StatsAppState stateView;
    
    // 当前的state
    private AppState currentState;
    
    @Override
    public void start() {
        super.start(); 
        // ---------- 
        // 这里必须优先载入,因为在Android下，需要在start之后和simpleInitApp之前做
        // 一些特殊设置,所以ConfigService必须在这里优先载入。
        // ----------
        
        // 1.初始化数据
        ObjectLoader.initData();
        
        // 注册SerializerObject
        MessRegister.register();
        // 注册内置的数据装载器和处理器（含注册SerializerObject）。
        DataFactory.initRegister();
        
        // 2.载入语言环境及系统配置
        Factory.get(ConfigService.class).loadGlobalConfig();
        Factory.get(ConfigService.class).loadLocale();
    }
    
    @Override
    public void simpleInitApp() {
//        speed = 0.5f;
        
        // 1.公共引用,必须优先载入
        Common.setCommon(this, settings);
        
        // 2.UI配置
        UIConfig uiconfig = new UIConfigImpl(getAssetManager());
        UIFactory.registerUIConfig(uiconfig);
        
        // ======== start init
        // 4.UI事件,
        UIState viewState = UIState.getInstance();
        viewState.register(this); // 注册APP及GUI Root
        stateManager.attach(viewState);
        
        // 5.FPS信息
        stateManager.attach(new FpsState());
        
        // 6.Debug 信息
        stateView = stateManager.getState(StatsAppState.class);
        setDebugView(Config.debug);
        
        // 7.基本设定
        getInputManager().setCursorVisible(true);
        getFlyByCamera().setEnabled(false);
        
       
        
        if (Config.debug) {
            TestFactory.preTest();
        }
        
        // start state
        changeStartState();
    }

    @Override
    public void simpleRender(RenderManager rm) {}
    
    /**
     * 进入开始界面
     */
    public void changeStartState() {
        changeState(startState);
    }
    
    /**
     * 进入“局域网”模式
     */
    public void changeLanState() {
        changeState(new LanState());
    }
    
    public void changeState(AppState appState) {
        if (appState == null || appState == currentState) {
            return;
        }
        
        // check to display ad
        checkToDisplayAd(currentState, appState);
        
        // 移除当前的state
        if (currentState != null) {
            preDetach();
            this.stateManager.detach(currentState);
        }
        
        // 切换到新的state.
        preStartState();
        stateManager.attach(appState);
        currentState = appState;
    }
    
    // state是将要执行的state
    private void checkToDisplayAd(AppState current, AppState next) {
        // 首次进入
        if (current == null) {
            AdUtils.showAd(AdType.banner); 
            return;
        }
        
        if ((current instanceof PlayState) && (next instanceof StartState)) {
            // 退回到startState时只显示insert
            AdUtils.hideAd(AdType.banner);
            AdUtils.showAd(AdType.insert);
        } else {
            AdUtils.hideAd(AdType.banner, AdType.insert);
        }
    }
    
    /**
     * 在state剥离之前先清理manager.
     */
    private void preDetach() {
        // ignore
    }
    
    /**
     * 在重新开始一个state时执行一些清理和重置。
     */
    private void preStartState() {
        // ==== 清理和GC        

        // 重要：清除所有按键侦听，部分state在detach时可能会忘记一些按键绑定的清理。
        // 这些没有或忘记释放的绑定会严重影响性能。特别是如：ChaseCamera这些内定的
        // 按键绑定了多个listener,如果不知道或忘记清理，则每次创建ChaseCamera时
        // inputManager中的mappings会越来越多的listener,很多detach后的listener是没有用的，
        // 但是仍会被触发执行导致性能下降并使画面越来越卡
        // == 这里进行统一清理，以避免各个state在人为清理绑定时忘记（有可能部分是内键应用创建的绑定，连自己也不知道的。）
        getInputManager().clearMappings();
        
        // ==== 初始化
        
        // 由于进行了clearMappings,所以viewState必须重新绑定事件
        UIState.getInstance().registerDefaultListener();
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
    }
    
    @Override
    public void destroy() {
        super.destroy();
        // 多线程工具清理
        ThreadHelper.cleanup();
    }
    
    public void setDebugView(boolean enabled) {
        if (enabled) {
            stateManager.attach(stateView);
        } else {
            stateManager.detach(stateView);
        }
    }
}
