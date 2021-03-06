package zookeeperStudy.client;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper统一的client
 *
 * @author chenwu on 2021.3.25
 */
public class ZkClient implements Watcher {

    //允许一个或者多个线程等待的同步辅助
    //CountDownLatch的一个非常典型的应用场景是：有一个任务想要往下执行，但必须要等到其他的任务执行完毕后才可以继续往下执行。
    private static CountDownLatch connectedSemphore = new CountDownLatch(1);
    private static Stat stat = new Stat();
    private ZooKeeper zookeeper;

    public ZkClient(String connectedString, int sessionTimeout) throws IOException {
        zookeeper = new ZooKeeper(connectedString, sessionTimeout, this);
        System.out.println("zookeeper state:" + zookeeper.getState());
        try {
            //如果当前计数器为0,则立即返回
            //如果当前计数器>0  则等待计数器为0(外部调用countDown)或者线程被打断
            connectedSemphore.await();
        } catch (Exception e) {

        }
        System.out.println("zookeeper connected is established");
    }

    public ZkClient(String connectedString, int sessionTimeout, long sessionId, byte[] sessionPassword) throws IOException {
        zookeeper = new ZooKeeper(connectedString, sessionTimeout, this, sessionId, sessionPassword);
        try {
            Thread.sleep(100 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getSessionId() {
        return zookeeper.getSessionId();
    }

    public byte[] getSessionPassword() {
        return zookeeper.getSessionPasswd();
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("received event:" + event);
        if (Event.KeeperState.SyncConnected == event.getState()) {
            if(Event.EventType.None == event.getType() && null == event.getPath()){
                connectedSemphore.countDown();//将当前计数器-1  如果计数达到0,则释放所有等待的线程
            }else if(event.getType() == Event.EventType.NodeChildrenChanged){
                try{
                    System.out.println("reget "+zookeeper.getChildren(event.getPath(),true));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else if(event.getType() == Event.EventType.NodeDataChanged){
                try{
                    System.out.println("data:"+new String(zookeeper.getData(event.getPath(),true,stat)));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        } else if (Event.KeeperState.Disconnected == event.getState()) {
            System.out.println("disconnected at " + LocalTime.now());
        }
    }

    /**
     * 利用同步的方法创建节点
     *
     * @param path
     * @param data
     * @param acl
     * @param createMode
     * @throws KeeperException
     * @throws InterruptedException
     * @author chenwu on 2021.3.26
     */
    public void createDataSync(String path, byte[] data, List<ACL> acl,
                               CreateMode createMode) throws KeeperException, InterruptedException {
        zookeeper.create(path, data, acl, createMode);
    }

    /**
     * 异步创建节点
     *
     * @param path
     * @param data
     * @param acl
     * @param createMode
     * @param callback
     * @param ctx
     * @author chenwu on 2021.4.1
     */
    public void createDataAsync(String path, byte[] data, List<ACL> acl,
                                CreateMode createMode, AsyncCallback.StringCallback callback, Object ctx) {
        zookeeper.create(path, data, acl, createMode, callback, ctx);
    }

    /**
     * 删除节点内容
     *
     * @param path
     * @param version
     * @author chenwu on 2021.4.6
     */
    public void deleteData(String path, int version) throws KeeperException, InterruptedException {
        zookeeper.delete(path, version);
    }

    public List<String> getChildrenWithOrNotWatch(String path, boolean watch) throws KeeperException,
            InterruptedException {
        return zookeeper.getChildren(path, watch);
    }

    public List<String> getChildrenWithWatcher(String path, Watcher watcher) throws KeeperException,
            InterruptedException {
        return zookeeper.getChildren(path, watcher);
    }

    public void setData(String path,byte[] data,int version) throws KeeperException, InterruptedException {
        zookeeper.setData(path,data,version);
    }

    public String getData(String path, Stat stat) throws KeeperException, InterruptedException {
        return new String (zookeeper.getData(path,true,stat));
    }

    public Stat existData(String path,Watcher watcher) throws KeeperException, InterruptedException {
        return zookeeper.exists(path,watcher);
    }

    /**
     * 添加权限控制信息
     * world：默认方式，相当于全世界都能访问
     * auth：代表已经认证通过的用户(cli中可以通过addauth digest user:pwd 来添加当前上下文中的授权用户)
     * digest：即用户名:密码这种方式认证，这也是业务系统中最常用的
     * ip：使用Ip地址认证
     *
     * @param schema
     * @param authInfo
     */
    public void addAuth(String schema,byte[] authInfo){
        zookeeper.addAuthInfo(schema,authInfo);
    }
}
