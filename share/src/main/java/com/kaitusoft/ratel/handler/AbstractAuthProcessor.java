package com.kaitusoft.ratel.handler;


import com.kaitusoft.ratel.Result;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/7
 *          <p>
 *          write description here
 */
public abstract class AbstractAuthProcessor extends ExtendableProcessor {

    public void setResult(Result result) {
        this.failResult = result;
    }

}
