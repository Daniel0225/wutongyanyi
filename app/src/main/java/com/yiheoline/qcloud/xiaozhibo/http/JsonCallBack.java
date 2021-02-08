package com.yiheoline.qcloud.xiaozhibo.http;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.callback.AbsCallback;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2018/5/28.
 */

public abstract class JsonCallBack<T> extends AbsCallback<T> {
    private Type type;
    private Class<T> clazz;
    @Override
    public T convertResponse(Response response) throws Throwable {
        ResponseBody body = response.body();
        if (body == null) return null;
        Type genType = getClass().getGenericSuperclass();
        Type type = ((ParameterizedType) genType).getActualTypeArguments()[0];
        T data = JSON.parseObject(body.string(), type);
//        if(((BaseResponse)data).getRes() == -1002){
//            Intent intent = new Intent(TCApplication.Companion.getApplication(), LoginActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            TCApplication.Companion.getApplication().startActivity(intent);
//        }
        response.close();
        return data;
    }

    @Override
    public void onError(com.lzy.okgo.model.Response<T> response) {
        super.onError(response);

    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }


}
