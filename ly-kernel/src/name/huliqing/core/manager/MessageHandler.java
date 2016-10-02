/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.manager;

/**
 * 消息钩子，主要用来处理从系统发出的消息。通过实现一个MessageHandler，然后把它注册到MessageManager，
 * 就可以实现对系统发出的消息的监听,并实现自定义处理消息。示例：
 * <code>
 * <pre>
 * MessageHandler mh = new MessageHandlerImpl();
 * MessageFactory.registerHandler(mh);
 * </pre>
 * </code>
 * @author huliqing
 */
public interface MessageHandler {
    
        /**
         * 处理消息
         * @param code 消息代码
         * @param message 消息内容
         * @param params 参数
         */
        void onPostMessage(int code, String message, Object... params);
}
