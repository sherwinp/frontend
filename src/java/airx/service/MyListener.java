/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package airx.service;

import java.util.*;
import airx.storage.FileController;
import java.io.*;

/**
 *
 * @author sherwinp
 */
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MyListener implements ServletContextListener {
    schedulerservice service = null;
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // start the thread
       service = new schedulerservice(
                (File) sce.getServletContext().getAttribute("javax.servlet.context.tempdir"));
       service.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // stop the thread
       if( service != null )
           service.shutdown();
    }

    class schedulerservice extends Thread {

        File tempDir;
        Queue<Job> queue;
        private boolean shouldbeRunning = false;

        schedulerservice(File tempDir) {
            this.tempDir = tempDir;
            queue = new java.util.concurrent.ArrayBlockingQueue<Job>(5);
        }

        @Override
        public void run() {
            shouldbeRunning = true;
            while (shouldbeRunning) {
                try {
                    service_jobs();
                    service_queue();
                    
                    // wait seconds
                    sleep(20000);
                } catch (InterruptedException e) {
                }
            }
            // shutting down
            service_jobs();
            queue.clear();
        }

        void service_queue() {

            /* build service state from filesystem
             * Access Configuration for directory path
             * 
             * File(Dir).for each uploaded file
             * build job queue
             */
            final HashMap fileonqueue = new HashMap();
            for (Job _job : queue) {
                fileonqueue.put( _job.getShipmentFileName(), _job.getShipmentFileName() );
            }
            String[] children = null;
            FilenameFilter filter = new FilenameFilter() {

                int count = 0;

                @Override
                public boolean accept(File dir, String name) {
                    boolean result = name.endsWith("upload.txt") && count < 5;
                    result = result && !fileonqueue.containsKey(name);
                    count++;

                    return result;
                }
            };
            children = tempDir.list(filter);

            /** 
             * enqueue(Job)
             */
            for (String fileName : children) {
                Job job = new Job(new File(tempDir, fileName));
                queue.add(job);                     
            }
        }
        void service_jobs() {
            System.out.println(String.format("Listener -- Queue size: %d", queue.size()));
            for (Job job : queue) {
                if (job.getState() == Thread.State.TERMINATED) {
                    if (queue.remove(job)) {
                        // job re-write shipment record
                        System.out.println("Listener -- Job Terminated");
                    }
                }
                if(job.getState() == Thread.State.NEW){
                    job.start();
                }
            }
        }

        @Override
        public synchronized void start() {
            this.setDaemon(true);
            super.start();
        }

        public void shutdown() {
            shouldbeRunning = false;
        }
    }
}