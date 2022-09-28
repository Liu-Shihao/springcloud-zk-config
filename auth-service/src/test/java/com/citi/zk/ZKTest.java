package com.citi.zk;

import org.apache.zookeeper.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/5 13:51
 * @Desc:
 */
@SpringBootTest
public class ZKTest {

    String host = "192.168.153.128:2181,192.168.153.129:2181,192.168.153.130:2181";

    ZooKeeper zooKeeper;

    CountDownLatch countDownLatch = new CountDownLatch(1);

    Random random = new Random();


    @Before
    public void before(){
        try {
            //提高jute.maxbuffer大小,默认是4M，会报错Packet len5133637 is out of range!
            System.setProperty("jute.maxbuffer", 4096 * 1024 * 10 + "");
            zooKeeper = new ZooKeeper(host, 30000, new MyWatch());
            countDownLatch.await();
            System.out.println("Zookeeper Connected Successful!");
        }catch ( Exception e){
            e.printStackTrace();
        }

    }

    @Test
    public void test() throws Exception{
//        int nextInt = random.nextInt(10000000);
        List<String> children1 = zooKeeper.getChildren("/", true);
        System.out.println("========开始根目录下节点数： "+children1.size()+"========");

        long start = System.currentTimeMillis();

        int testtime = 50000;

        for (int i = 1; i <= testtime; i++) {
            UUID uuid = UUID.randomUUID();
            zooKeeper.create("/"+uuid, uuid.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//            Stat stat = new Stat();
//            byte[] data = zooKeeper.getData("/" + i, true,stat );
//            log.info("======/{} :{}",i,new String(data));
//            zooKeeper.setData("/"+i, uuid.toString().getBytes(),stat.getVersion());
//            zooKeeper.delete("/" + i,-1);
        }
        long end = System.currentTimeMillis();
        List<String> children2 = zooKeeper.getChildren("/", true);
        System.out.println("========开始根目录下节点数： "+children1.size()+"========");
        System.out.println("========结束根目录下节点数： "+children2.size()+"========");
        System.out.println("========创建 {} 节点耗时： "+(end-start)+" ms========");
    }
    @Test
    public void testOne() throws Exception{
        List<String> children1 = zooKeeper.getChildren("/", true);
        System.out.println("========开始根目录下节点数： "+children1.size()+"========");
        UUID uuid = UUID.randomUUID();
        long start = System.currentTimeMillis();
//        zooKeeper.create("/ttttttest5", uuid.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        zooKeeper.getData("/ttttttest5", true, new Stat());
        zooKeeper.setData("/"+uuid, uuid.toString().getBytes(), -1);
        long end = System.currentTimeMillis();
        List<String> children2 = zooKeeper.getChildren("/", true);
        System.out.println("========开始根目录下节点数： "+children1.size()+"========");
        System.out.println("========结束根目录下节点数： "+children2.size()+"========");
        System.out.println("========创建 {} 节点耗时： "+(end-start)+"========");
    }
    @Test
    public void testMulti() throws Exception{
        ExecutorService pool = Executors.newFixedThreadPool(10);
        int time = 100;


        for (int i = 0; i < 10; i++) {
            pool.execute(()->{
                for (int j = 0; j < time; j++) {
                    try {
                        UUID uuid = UUID.randomUUID();
                        List<String> children1 = zooKeeper.getChildren("/", true);
                        long start = System.currentTimeMillis();
                        zooKeeper.create("/"+uuid, uuid.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                        long end = System.currentTimeMillis();
                        List<String> children2 = zooKeeper.getChildren("/", true);
                        System.out.println(Thread.currentThread().getName()+"-------开始根目录下节点数： "+children1.size()+"；结束根目录下节点数： "+children2.size()+" ；耗时： "+(end-start)+" ms-------");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }

        System.in.read();
    }



    @Test
    public void total() throws Exception{
        List<String> children1 = zooKeeper.getChildren("/", true);
        System.out.println("========开始根目录下节点数： "+children1.size()+"========");
    }



    class MyWatch implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            switch (event.getState()) {
                case Unknown:
                    break;
                case Disconnected:
                    break;
                case NoSyncConnected:
                    break;
                case SyncConnected:
                    countDownLatch.countDown();
                    break;
                case AuthFailed:
                    break;
                case ConnectedReadOnly:
                    break;
                case SaslAuthenticated:
                    break;
                case Expired:
                    break;
            }
        }
    }


//    public static void read() throws IOException, InterruptedException {
//        File file = new File("download/Ecr Report.csv");
//        String filePath = file.getAbsolutePath();
//        String content = "permissionservice";
//
//        CsvReader csvReader = new CsvReader(filePath);
//        while (csvReader.readRecord()){
//            try {
//                //check ECR report content
//                System.out.println(csvReader.readRecord());
//                Assert.assertTrue(csvReader.getRawRecord().contains(content), "The ECR report content is not correct.");
//
//            }catch (AssertionError e){
//            }
//        }
//        csvReader.close();
//        //delete the downloaded ECR report
//        file.delete();
//    }



}
