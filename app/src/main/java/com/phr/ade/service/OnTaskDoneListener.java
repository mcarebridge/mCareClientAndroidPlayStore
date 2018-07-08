package com.phr.ade.service;

/**
 * Created by dheerajs on 4/9/2017.
 */

import java.util.HashMap;

public interface OnTaskDoneListener
{
    void onTaskDone(HashMap mCareKeyValue);

    void onError();
}
