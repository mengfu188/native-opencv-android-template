package com.example.nativeopencvandroidtemplate;

/**
 * Created by cmf on 20-5-8.
 */

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Util {

}
class Timer{
    private static final String TAG = "Timer";
    Stack<Long> stack = new Stack<Long>();
    public void tic(){
        long time = System.currentTimeMillis();
        stack.push(time);
    }
    public void toc(String info){
        long time = stack.pop();
        long pad = (System.currentTimeMillis() - time);
        Log.d(TAG, "toc: <<< " + info + " >>> :" + pad + " ms");
    }
}
