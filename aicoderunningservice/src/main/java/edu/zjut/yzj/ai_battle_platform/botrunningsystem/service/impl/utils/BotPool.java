package edu.zjut.yzj.ai_battle_platform.botrunningsystem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Queue<Bot> bots = new LinkedList<>();

    /**
     * 真正往AI代码运行队列中添加AI代码运行任务的方法
     * @param userId userId
     * @param botCode 需要执行的AI代码
     * @param input 当前对战的局面信息
     */
    public void addBot(Integer userId, String botCode, String input) {
        //todo 这里后面可以换成用BlockingQueue实现,从而可以省掉互斥锁和条件变量
        lock.lock();
        try {
            bots.add(new Bot(userId, botCode, input));
            condition.signalAll();  //条件满足，通知消费者线程取走任务并执行
        } finally {
            lock.unlock();
        }
    }

    /**
     * 动态编译运行java代码
     * @param bot 一个AI代码运行任务
     */
    private void consume(Bot bot) {
        //todo 这里可以用线程池去实现
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot); //启动新线程去执行，2s没执行完直接放弃
    }

    //todo 启动一个docker容器编译并运行用户的AI代码并
    private void excuteAICodeByDocker(Bot bot) {

        String AICode = bot.getBotCode();
        Integer userId = bot.getUserId();
        String gameInfo = bot.getInput();


    }

    @Override
    public void run() {
        while (true) {
            lock.lock();
            if (bots.isEmpty()) {
                try {
                    //任务队列是空的，只能等
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            } else {
                Bot bot = bots.remove();
                lock.unlock();

                consume(bot);  // 比较耗时，可能会执行几秒钟
            }
        }
    }
}
