package de.dhbw.heidenheim.wi2012.securechat;

import de.dhbw.heidenheim.wi2012.securechat.gui.ChatDetailFragment;
import android.os.Handler;
import android.os.Looper;
/**
 * A class used to perform periodical updates,
 * specified inside a runnable object. An update interval
 * may be specified (otherwise, the class will perform the 
 * update every 12 seconds).
 */
public class MessageUpdater {
        // Create a Handler that uses the Main Looper to run in
        private Handler mHandler = new Handler(Looper.getMainLooper());

        private Runnable mStatusChecker;
        private int UPDATE_INTERVAL = 12000;

    	private static ChatDetailFragment chatDetailFragment;

        /**
         * Creates an MessageUpdater object, that can be used to
         * perform MessageUpdates on a specified time interval.
         * 
         * @param messageUpdater A runnable containing the update routine.
         */
        public MessageUpdater(final Runnable messageUpdater) {
            mStatusChecker = new Runnable() {
                @Override
                public void run() {
                    // Run the passed runnable
                    messageUpdater.run();
                    // Re-run it after the update interval
                    mHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            };
        }

        /**
         * Starts the periodical update routine (mStatusChecker 
         * adds the callback to the handler).
         */
        public synchronized void startUpdates(){
            mStatusChecker.run();
        }

        /**
         * Stops the periodical update routine from running,
         * by removing the callback.
         */
        public synchronized void stopUpdates(){
            mHandler.removeCallbacks(mStatusChecker);
        }

        public static void setChatDetailFragment(ChatDetailFragment cdf) {
        	chatDetailFragment = cdf;
        }
        public static ChatDetailFragment getChatDetailFragment() {
        	return chatDetailFragment;
        }
}