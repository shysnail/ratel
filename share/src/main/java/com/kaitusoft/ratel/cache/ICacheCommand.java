package com.kaitusoft.ratel.cache;


import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2018/1/11
 *          <p>
 *          write description here
 */
public interface ICacheCommand<String, V> {


    /**
     * 向缓存中添加对象，默认出错抛异常
     *
     * @param key
     * @param value
     * @return
     */
    boolean put(String key, V value);

    /**
     * 向缓存中添加对象，指定有效期
     *
     * @param key
     * @param value
     * @param expires
     * @return
     */
    boolean put(String key, V value, int expires);


    /**
     * 获取某个key的值，遇到任何异常均抛出
     *
     * @param key
     * @return
     */
    V get(String key);

    boolean remove(String key);

    V getAndRemove(String key);

    //...
    boolean putList(String key, List<V> data);

    boolean setListElem(String key, int index, V data);

    List getListAll(String key);

    List getList(String key, int startIndex, int endIndex);


    Long getLong(String key);

    Long addAndGetLong(String key);

    Long addAndGetLong(String key, Long delta);

    Long getAndAddLong(String key);

    Long getAndAddLong(String key, Long delta);


}
