package com.lsh.interceptor;


import com.hazelcast.map.MapInterceptor;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/22 23:18
 * @Desc:
 */
public class IMapInterceptor implements MapInterceptor {

    private static final long serialVersionUID = 3556808830046436753L;

    @Override
    public Object interceptGet(Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void afterGet(Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object interceptPut(Object oldValue, Object newValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void afterPut(Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object interceptRemove(Object removedValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void afterRemove(Object oldValue) {
        // TODO Auto-generated method stub

    }
}
